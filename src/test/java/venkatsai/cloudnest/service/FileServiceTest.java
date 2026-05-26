package venkatsai.cloudnest.service;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.errors.MinioException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import venkatsai.cloudnest.dto.APIResponseDTO;
import venkatsai.cloudnest.entity.FileEntity;
import venkatsai.cloudnest.repository.FileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileServiceTest {
    private  FileService fileService;
    private MinioClient minioClient;
    private FileRepository fileRepository;

    @BeforeEach
    public void setUp(){
        fileRepository = mock(FileRepository.class);
        minioClient = mock(MinioClient.class);
        fileService = new FileService(fileRepository, minioClient);
    }

    @Test
    public void checkFileSize(){
        assertThrows(FileUploadException.class, ()->{
            MockMultipartFile file = new MockMultipartFile("file","file.txt","text/plain",new byte[10241]);
            fileService.uploadFile(file);
        });
    }

    @Test
    public void checkDuplicateFile(){
        when(fileRepository.existsByNameEqualsIgnoreCase("file123.txt")).thenReturn(true);
        assertThrows(FileAlreadyExistsException.class, ()->{
            MockMultipartFile file = new MockMultipartFile("file123","file123.txt","text/plain",new byte[10240]);
            fileService.uploadFile(file);
        });
    }

    @Test
    public void checkUploadSuccess() throws MinioException, IOException {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);
        when(fileRepository.save(any(FileEntity.class))).thenReturn(FileEntity.builder().name("file123.txt").build());
        MockMultipartFile file = new MockMultipartFile("file123","file123.txt","text/plain",new byte[10240]);
        assertEquals("file123.txt",fileService.uploadFile(file).getName());
        verify(fileRepository).save(any(FileEntity.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    public void checkDeleting() throws MinioException, IOException {
        when(fileRepository.findById("file123")).thenReturn(FileEntity.builder().name("file123").storagePath("uploads").build());
        when(minioClient.removeObjects(any(RemoveObjectsArgs.class))).thenReturn(null);
        assertEquals("file123",fileService.deleteFile("file123").getName());
    }
}
