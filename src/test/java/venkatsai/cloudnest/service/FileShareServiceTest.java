package venkatsai.cloudnest.service;

import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import venkatsai.cloudnest.dto.request.share.FileShareRequest;
import venkatsai.cloudnest.dto.request.share.FileShareResponse;
import venkatsai.cloudnest.dto.response.DownloadedFile;
import venkatsai.cloudnest.entity.*;
import venkatsai.cloudnest.exception.FileAccessDeniedException;
import venkatsai.cloudnest.exception.ResourceNotFoundException;
import venkatsai.cloudnest.repository.FileRepository;
import venkatsai.cloudnest.repository.FileShareRepository;
import venkatsai.cloudnest.repository.UserRepository;
import venkatsai.cloudnest.storage.FileStorage;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileShareServiceTest {
    private FileShareService fileShareService;
    private FileRepository fileRepository;
    private UserRepository userRepository;
    private FileShareRepository fileShareRepository;
    private FileStorage fileStorage;

    private UserEntity owner;
    private UserEntity sharedUser;
    private FileEntity file;

    @BeforeEach
    public void setUp() {
        fileRepository = mock(FileRepository.class);
        userRepository = mock(UserRepository.class);
        fileShareRepository = mock(FileShareRepository.class);
        fileStorage = mock(FileStorage.class);
        fileShareService = new FileShareService(fileRepository, userRepository, fileShareRepository, fileStorage);

        owner = UserEntity.builder().email("owner@example.com").build();
        sharedUser = UserEntity.builder().email("shared@example.com").build();
        file = FileEntity.builder()
                .id("file-1")
                .name("test.txt")
                .contentType("text/plain")
                .storagePath("bucket")
                .user(owner)
                .build();
    }

    @Test
    public void testShareFileSuccess() throws FileNotFoundException {
        FileShareRequest req = new FileShareRequest("file-1", "shared@example.com", FilePermission.READ, 3600);
        when(fileRepository.findByIdAndUser_EmailIgnoreCase("file-1", "owner@example.com")).thenReturn(Optional.of(file));
        when(userRepository.findByEmail("shared@example.com")).thenReturn(Optional.of(sharedUser));

        fileShareService.shareFile(req, "owner@example.com");

        verify(fileShareRepository).save(any(SharedFileEntity.class));
    }

    @Test
    public void testGetSharedFiles() {
        SharedFileEntity sharedFile = SharedFileEntity.builder()
                .id("share-1")
                .file(file)
                .user(sharedUser)
                .permission(FilePermission.READ)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(fileShareRepository.findByUser_EmailIgnoreCase("shared@example.com")).thenReturn(Collections.singletonList(sharedFile));

        List<FileShareResponse> responses = fileShareService.getSharedFiles("shared@example.com");

        assertEquals(1, responses.size());
        assertEquals("share-1", responses.get(0).getId());
    }

    @Test
    public void testDownloadSharedFileSuccess() throws IOException, MinioException {
        SharedFileEntity sharedFile = SharedFileEntity.builder()
                .id("share-1")
                .file(file)
                .user(sharedUser)
                .permission(FilePermission.DOWNLOAD)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(fileShareRepository.findByIdAndUser_EmailIgnoreCase("share-1", "shared@example.com")).thenReturn(Optional.of(sharedFile));
        when(fileStorage.load("bucket", "file-1")).thenReturn(new ByteArrayInputStream("content".getBytes()));

        DownloadedFile downloadedFile = fileShareService.downloadSharedFile("share-1", "shared@example.com");

        assertNotNull(downloadedFile);
        assertEquals("test.txt", downloadedFile.name());
    }

    @Test
    public void testDownloadSharedFileNoPermission() {
        SharedFileEntity sharedFile = SharedFileEntity.builder()
                .id("share-1")
                .file(file)
                .user(sharedUser)
                .permission(FilePermission.READ)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(fileShareRepository.findByIdAndUser_EmailIgnoreCase("share-1", "shared@example.com")).thenReturn(Optional.of(sharedFile));

        assertThrows(FileAccessDeniedException.class, () -> fileShareService.downloadSharedFile("share-1", "shared@example.com"));
    }

    @Test
    public void testDeleteSharedFileSuccess() throws IOException, MinioException {
        SharedFileEntity sharedFile = SharedFileEntity.builder()
                .id("share-1")
                .file(file)
                .user(sharedUser)
                .permission(FilePermission.ALL)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(fileShareRepository.findByIdAndUser_EmailIgnoreCase("share-1", "shared@example.com")).thenReturn(Optional.of(sharedFile));

        fileShareService.deleteSharedFile("share-1", "shared@example.com");

        verify(fileStorage).delete("bucket", "file-1");
        verify(fileRepository).delete(file);
    }
}
