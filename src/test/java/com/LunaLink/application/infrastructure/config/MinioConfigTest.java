package com.LunaLink.application.infrastructure.config;

import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioConfigTest {

    @InjectMocks
    private MinioConfig minioConfig;

    private MinioClient mockClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioConfig, "endpoint", "http://localhost:9000");
        ReflectionTestUtils.setField(minioConfig, "accessKey", "minioadmin");
        ReflectionTestUtils.setField(minioConfig, "secretKey", "minioadmin");
        ReflectionTestUtils.setField(minioConfig, "bucket", "lunalink");

        mockClient = mock(MinioClient.class);
    }

    @Test
    @DisplayName("Deve criar bucket quando ele não existe")
    void createBucketIfNotExists_ShouldCreateBucket_WhenNotExists() throws Exception {
        MinioConfig spyConfig = spy(minioConfig);
        doReturn(mockClient).when(spyConfig).minioClient();

        when(mockClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        spyConfig.createBucketIfNotExists();

        verify(mockClient).bucketExists(any(BucketExistsArgs.class));
        verify(mockClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("Deve ignorar criação quando bucket já existe")
    void createBucketIfNotExists_ShouldNotCreateBucket_WhenAlreadyExists() throws Exception {
        MinioConfig spyConfig = spy(minioConfig);
        doReturn(mockClient).when(spyConfig).minioClient();

        when(mockClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        spyConfig.createBucketIfNotExists();

        verify(mockClient).bucketExists(any(BucketExistsArgs.class));
        verify(mockClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("Deve logar erro e não propagar exceção quando MinIO falha")
    void createBucketIfNotExists_ShouldNotThrow_WhenMinioFails() throws Exception {
        MinioConfig spyConfig = spy(minioConfig);
        doReturn(mockClient).when(spyConfig).minioClient();

        when(mockClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertDoesNotThrow(() -> spyConfig.createBucketIfNotExists());
    }

    @Test
    @DisplayName("Deve criar MinioClient com credenciais corretas")
    void minioClient_ShouldBuildWithCorrectCredentials() {
        MinioClient client = minioConfig.minioClient();

        assertNotNull(client);
    }
}
