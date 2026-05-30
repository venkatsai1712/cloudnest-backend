package venkatsai.cloudnest.storage;

import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface FileStorage {
    String bucketName();

    void store(String objectKey, MultipartFile file, String contentType) throws IOException, MinioException;

    InputStream load(String bucketName, String objectKey) throws IOException, MinioException;

    void delete(String bucketName, String objectKey) throws IOException, MinioException;

    String getUploadPresignedURL(String bucketName, String objectKey) throws IOException, MinioException;

    String getDownloadPresignedURL(String bucketName, String objectKey, String fileName, String contentType) throws IOException, MinioException;

    String getUploadChunkPresignedURL(String bucketName, String objectKey) throws MinioException;

    String chunksUploadComplete(String uploadId, List<Integer> chunks) throws MinioException;
}
