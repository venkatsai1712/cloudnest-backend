package venkatsai.cloudnest.service;

import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import venkatsai.cloudnest.dto.FileResponse;
import venkatsai.cloudnest.entity.FileEntity;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.exception.DuplicateFileException;
import venkatsai.cloudnest.exception.FileStorageValidationException;
import venkatsai.cloudnest.mapper.FileMapper;
import venkatsai.cloudnest.repository.FileRepository;
import venkatsai.cloudnest.repository.UserRepository;
import venkatsai.cloudnest.storage.FileStorage;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileServiceTest {
    private  FileService fileService;
    private FileStorage fileStorage;
    private FileRepository fileRepository;
    private UserRepository userRepository;
    private FileMapper fileMapper;
    private UserEntity user;

    @BeforeEach
    public void setUp(){
        fileRepository = mock(FileRepository.class);
        userRepository = mock(UserRepository.class);
        fileStorage = mock(FileStorage.class);
        fileMapper = new FileMapper();
        fileService = new FileService(fileRepository, userRepository, fileStorage, fileMapper);
        user = UserEntity.builder()
                .id("user-1")
                .name("Test User")
                .email("test@example.com")
                .password("secret")
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(fileStorage.bucketName()).thenReturn("uploads");
    }

    @Test
    public void checkFileSize(){
        assertThrows(FileStorageValidationException.class, ()->{
            MockMultipartFile file = new MockMultipartFile("file","file.txt","text/plain",new byte[(10 * 1024 * 1024) + 1]);
            fileService.uploadFile(file, "test@example.com");
        });
    }

    @Test
    public void checkDuplicateFile(){
        when(fileRepository.existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCase("file123.txt", "test@example.com")).thenReturn(true);
        assertThrows(DuplicateFileException.class, ()->{
            MockMultipartFile file = new MockMultipartFile("file123","file123.txt","text/plain",new byte[10240]);
            fileService.uploadFile(file, "test@example.com");
        });
    }

    @Test
    public void checkUploadSuccess() throws MinioException, IOException {
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile file = new MockMultipartFile("file123","file123.txt","text/plain",new byte[10240]);
        FileResponse response = fileService.uploadFile(file, "test@example.com");
        assertEquals("file123.txt", response.getName());
        verify(fileRepository).save(any(FileEntity.class));
        verify(fileStorage).store(anyString(), eq(file), eq("text/plain"));
    }

    @Test
    public void checkDeleting() throws MinioException, IOException {
        when(fileRepository.findByIdAndUser_EmailIgnoreCase("file123", "test@example.com"))
                .thenReturn(Optional.of(FileEntity.builder().id("file123").name("file123").storagePath("uploads").build()));
        assertEquals("file123",fileService.deleteFile("file123", "test@example.com").getName());
        verify(fileStorage).delete("uploads", "file123");
        verify(fileRepository).delete(any(FileEntity.class));
    }
}
