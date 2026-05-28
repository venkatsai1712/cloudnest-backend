package venkatsai.cloudnest.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class MinioFileStorage implements FileStorage {
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioFileStorage(MinioClient minioClient, @Value("${minio.bucket:uploads}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public String bucketName() {
        return bucketName;
    }

    @Override
    public void store(String objectKey, MultipartFile file, String contentType) throws IOException, MinioException {
        ensureBucketExists();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .stream(file.getInputStream(), file.getSize(), -1L)
                        .bucket(bucketName)
                        .object(objectKey)
                        .contentType(contentType)
                        .build()
        );
    }

    @Override
    public InputStream load(String bucketName, String objectKey) throws IOException, MinioException {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build()
        );
    }

    @Override
    public void delete(String bucketName, String objectKey) throws IOException, MinioException {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build()
        );
    }

    private void ensureBucketExists() throws IOException, MinioException {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}
