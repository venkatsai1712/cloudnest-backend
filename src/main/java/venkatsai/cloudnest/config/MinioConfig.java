package venkatsai.cloudnest.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.username}")
    private String username;

    @Value("${minio.password}")
    private String password;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint("localhost",9000,false)
                .credentials(username, password)
                .build();
    }
}