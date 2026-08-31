package com.LunaLink.application.application.service.Storage;

import io.minio.*;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioClient minioPresignedClient;

    private MinioStorageService storageService;

    private static final String BUCKET = "lunalink";

    @BeforeEach
    void setUp() {
        storageService = new MinioStorageService(minioClient, minioPresignedClient, BUCKET);
    }

    @Test
    @DisplayName("Deve gerar presigned URL de upload com method PUT")
    void generateUploadUrl_ShouldReturnUrl_WhenValidKeyAndExpiration() throws Exception {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(15);
        String expectedUrl = "http://localhost:9000/lunalink/" + key + "?X-Amz-Algorithm=...";

        when(minioPresignedClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        String result = storageService.generateUploadUrl(key, expiration);

        assertNotNull(result);
        assertEquals(expectedUrl, result);

        verify(minioPresignedClient).getPresignedObjectUrl(argThat(args ->
                args.method() == Method.PUT &&
                args.bucket().equals(BUCKET) &&
                args.object().equals(key)
        ));
    }

    @Test
    @DisplayName("Deve gerar presigned URL de download com method GET")
    void generateDownloadUrl_ShouldReturnUrl_WhenValidKeyAndExpiration() throws Exception {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(15);
        String expectedUrl = "http://localhost:9000/lunalink/" + key + "?X-Amz-Algorithm=...";

        when(minioPresignedClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        String result = storageService.generateDownloadUrl(key, expiration);

        assertNotNull(result);
        assertEquals(expectedUrl, result);

        verify(minioPresignedClient).getPresignedObjectUrl(argThat(args ->
                args.method() == Method.GET &&
                args.bucket().equals(BUCKET) &&
                args.object().equals(key)
        ));
    }

    @Test
    @DisplayName("Deve deletar objeto no MinIO")
    void deleteObject_ShouldCallRemoveObject_WhenValidKey() throws Exception {
        String key = "encomendas/user-uuid/file.jpg";

        assertDoesNotThrow(() -> storageService.deleteObject(key));

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao falhar geração de upload URL")
    void generateUploadUrl_ShouldThrowException_WhenMinioClientFails() throws Exception {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(15);

        lenient().when(minioPresignedClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storageService.generateUploadUrl(key, expiration));

        assertEquals("Erro ao gerar presigned URL de upload", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao falhar geração de download URL")
    void generateDownloadUrl_ShouldThrowException_WhenMinioClientFails() throws Exception {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(15);

        lenient().when(minioPresignedClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storageService.generateDownloadUrl(key, expiration));

        assertEquals("Erro ao gerar presigned URL de download", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao falhar delete do objeto")
    void deleteObject_ShouldThrowException_WhenMinioClientFails() throws Exception {
        String key = "encomendas/user-uuid/file.jpg";

        doThrow(new RuntimeException("MinIO connection failed"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storageService.deleteObject(key));

        assertEquals("Erro ao deletar objeto do MinIO", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve usar expiração correta em segundos para upload URL")
    void generateUploadUrl_ShouldUseCorrectExpiration() throws Exception {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(10);

        when(minioPresignedClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://url");

        storageService.generateUploadUrl(key, expiration);

        verify(minioPresignedClient).getPresignedObjectUrl(argThat(args ->
                args.expiry() == (int) Duration.ofMinutes(10).toSeconds()
        ));
    }
}
