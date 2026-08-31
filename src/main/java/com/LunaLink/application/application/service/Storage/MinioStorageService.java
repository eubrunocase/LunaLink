package com.LunaLink.application.application.service.Storage;

import com.LunaLink.application.application.ports.input.StorageService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final MinioClient presignedClient;
    private final String bucket;

    public MinioStorageService(@Qualifier("minioClient") MinioClient minioClient,
                               @Qualifier("minioPresignedClient") MinioClient minioPresignedClient,
                               @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.presignedClient = minioPresignedClient;
        this.bucket = bucket;
    }

    @Override
    public String generateUploadUrl(String key, Duration expiration) {
        try {
            return presignedClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(key)
                            .expiry((int) expiration.toSeconds(), TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar presigned URL de upload", e);
        }
    }

    @Override
    public String generateDownloadUrl(String key, Duration expiration) {
        try {
            return presignedClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(key)
                            .expiry((int) expiration.toSeconds(), TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar presigned URL de download", e);
        }
    }

    @Override
    public void deleteObject(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar objeto do MinIO", e);
        }
    }
}
