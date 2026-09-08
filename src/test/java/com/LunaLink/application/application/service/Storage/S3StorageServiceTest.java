package com.LunaLink.application.application.service.Storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3StorageService storageService;

    private static final String BUCKET = "test-bucket";

    @BeforeEach
    void setUp() {
        storageService = new S3StorageService(s3Client, s3Presigner, BUCKET);
    }

    @Test
    @DisplayName("Deve gerar presigned URL de upload")
    void generateUploadUrl_ShouldReturnUrl_WhenValidKeyAndExpiration() throws MalformedURLException {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(15);
        String expectedUrl = "https://s3.us-east-1.amazonaws.com/test-bucket/" + key + "?X-Amz-Signature=abc123";

        PresignedPutObjectRequest presignedResponse = mock(PresignedPutObjectRequest.class);
        when(presignedResponse.url()).thenReturn(new URL(expectedUrl));

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presignedResponse);

        String result = storageService.generateUploadUrl(key, expiration);

        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("Deve gerar presigned URL de download")
    void generateDownloadUrl_ShouldReturnUrl_WhenValidKeyAndExpiration() throws MalformedURLException {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(15);
        String expectedUrl = "https://s3.us-east-1.amazonaws.com/test-bucket/" + key + "?X-Amz-Signature=abc123";

        PresignedGetObjectRequest presignedResponse = mock(PresignedGetObjectRequest.class);
        when(presignedResponse.url()).thenReturn(new URL(expectedUrl));

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedResponse);

        String result = storageService.generateDownloadUrl(key, expiration);

        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    @DisplayName("Deve deletar objeto no S3")
    void deleteObject_ShouldCallDeleteObject_WhenValidKey() {
        String key = "encomendas/user-uuid/file.jpg";

        assertDoesNotThrow(() -> storageService.deleteObject(key));

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao falhar geração de upload URL")
    void generateUploadUrl_ShouldThrowException_WhenPresignerFails() {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(15);

        lenient().when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("S3 connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storageService.generateUploadUrl(key, expiration));

        assertEquals("Erro ao gerar presigned URL de upload no S3", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao falhar geração de download URL")
    void generateDownloadUrl_ShouldThrowException_WhenPresignerFails() {
        String key = "encomendas/user-uuid/file.jpg";
        Duration expiration = Duration.ofMinutes(15);

        lenient().when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(new RuntimeException("S3 connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storageService.generateDownloadUrl(key, expiration));

        assertEquals("Erro ao gerar presigned URL de download no S3", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao falhar delete do objeto")
    void deleteObject_ShouldThrowException_WhenClientFails() {
        String key = "encomendas/user-uuid/file.jpg";

        doThrow(new RuntimeException("S3 connection failed"))
                .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storageService.deleteObject(key));

        assertEquals("Erro ao deletar objeto do S3", exception.getMessage());
        assertNotNull(exception.getCause());
    }
}
