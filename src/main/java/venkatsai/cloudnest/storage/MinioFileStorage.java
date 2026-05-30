package venkatsai.cloudnest.storage;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
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

    @Override
    public String getDownloadPresignedURL(String bucketName, String objectKey, String fileName, String contentType) throws MinioException {
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put(
                "response-content-disposition",
                "attachment; filename=\"" + fileName + "\""
        );
        reqParams.put(
                "response-content-type",
                contentType
        );

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Http.Method.GET)
                        .expiry(3600)
                        .bucket(bucketName)
                        .object(objectKey)
                        .extraQueryParams(reqParams)
                        .build()
        );
    }

    @Override
    public String getUploadPresignedURL(String bucketName, String objectKey) throws MinioException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .method(Http.Method.PUT)
                        .expiry(3600)
                        .build()
        );
    }

    public String getUploadChunkPresignedURL(String bucketName, String objectKey) throws MinioException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .method(Http.Method.PUT)
                        .expiry(3600)
                        .build()
        );
    }

    public String chunksUploadComplete(String uploadId, List<Integer> chunks) throws MinioException {
        List<SourceObject> sources = new ArrayList<>();
        for(int chunk : chunks){
            sources.add(
                    SourceObject.builder()
                            .bucket(bucketName())
                            .object(uploadId+chunk)
                            .build()
            );
        }

        for (Integer chunk : chunks) {
            String chunkObject = uploadId + chunk;

            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName())
                            .object(chunkObject)
                            .build()
            );

            log.info("Found chunk {}", chunkObject);
        }

        String objectKey = uploadId.substring(uploadId.lastIndexOf("/") + 1);
        minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket(bucketName())
                        .object(objectKey)
                        .sources(sources)
                        .build()
        );
        for(int chunk : chunks){
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName())
                                .object(uploadId+chunk)
                                .build()
                );
            } catch (Exception e) {
                log.warn("Failed to delete chunk {}", uploadId + chunk);
            }
        }

        return objectKey;
    }

    private void ensureBucketExists() throws IOException, MinioException {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }


}
