package com.LunaLink.application.infrastructure.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.swing.plaf.synth.Region;

@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.presigned-endpoint:${minio.endpoint}}")
    private String presignedEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    @Qualifier("minioClient")
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Qualifier("minioPresignedClient")
    @Bean
    public MinioClient minioPresignedClient() {
        return MinioClient.builder()
                .endpoint(presignedEndpoint)
                .region("us-east-1")
                .credentials(accessKey, secretKey)
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createBucketIfNotExists() {
        try {
            MinioClient client = minioClient();
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Bucket '{}' criado com sucesso no MinIO", bucket);
            } else {
                log.info("Bucket '{}' já existe no MinIO", bucket);
            }
        } catch (Exception e) {
            log.error("Erro ao verificar/criar bucket no MinIO: {}", e.getMessage(), e);
        }
    }
}
