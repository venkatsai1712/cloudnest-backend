package venkatsai.cloudnest.storage;

import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorage {
    String bucketName();

    void store(String objectKey, MultipartFile file, String contentType) throws IOException, MinioException;

    InputStream load(String bucketName, String objectKey) throws IOException, MinioException;

    void delete(String bucketName, String objectKey) throws IOException, MinioException;
}
