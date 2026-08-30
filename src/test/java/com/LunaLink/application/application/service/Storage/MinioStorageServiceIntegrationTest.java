package com.LunaLink.application.application.service.Storage;

import io.minio.*;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DisplayName("Integração MinIO - Presigned URLs e Bucket")
class MinioStorageServiceIntegrationTest {

    @Container
    private static final GenericContainer<?> MINIO_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
                    .withExposedPorts(9000, 9001)
                    .withCommand("server", "/data", "--console-address", ":9001")
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");

    private static MinioClient minioClient;
    private static MinioStorageService storageService;
    private static final String BUCKET = "lunalink-test";

    @BeforeAll
    static void setUp() {
        String endpoint = "http://" + MINIO_CONTAINER.getHost() + ":" +
                MINIO_CONTAINER.getMappedPort(9000);

        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials("minioadmin", "minioadmin")
                .build();

        storageService = new MinioStorageService(minioClient, BUCKET);

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(BUCKET).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(BUCKET).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao criar bucket de teste", e);
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            minioClient.removeBucket(
                    RemoveBucketArgs.builder().bucket(BUCKET).build());
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("Deve gerar presigned URL de upload válida")
    void generateUploadUrl_ShouldReturnValidUrl() {
        String key = "encomendas/" + UUID.randomUUID() + "/test-file.jpg";
        Duration expiration = Duration.ofMinutes(15);

        String url = storageService.generateUploadUrl(key, expiration);

        assertNotNull(url);
        assertTrue(url.startsWith("http://"));
        assertTrue(url.contains(BUCKET));
        assertTrue(url.contains(key));
        assertTrue(url.contains("X-Amz-Algorithm"));
    }

    @Test
    @DisplayName("Deve gerar presigned URL de download válida")
    void generateDownloadUrl_ShouldReturnValidUrl() {
        String key = "encomendas/" + UUID.randomUUID() + "/test-file.jpg";
        Duration expiration = Duration.ofMinutes(15);

        String url = storageService.generateDownloadUrl(key, expiration);

        assertNotNull(url);
        assertTrue(url.startsWith("http://"));
        assertTrue(url.contains(BUCKET));
        assertTrue(url.contains(key));
        assertTrue(url.contains("X-Amz-Algorithm"));
    }

    @Test
    @DisplayName("Deve fazer upload e download de arquivo via presigned URLs")
    void uploadAndDownload_ShouldWorkEndToEnd() throws Exception {
        String userId = UUID.randomUUID().toString();
        String key = "encomendas/" + userId + "/" + UUID.randomUUID() + "-comprovante.jpg";
        byte[] fileContent = "conteudo-fake-de-imagem".getBytes(StandardCharsets.UTF_8);

        String uploadUrl = storageService.generateUploadUrl(key, Duration.ofMinutes(15));

        try (var httpClient = java.net.http.HttpClient.newHttpClient()) {
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(uploadUrl))
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(fileContent))
                    .header("Content-Type", "image/jpeg")
                    .build();

            var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
        }

        try (var httpClient = java.net.http.HttpClient.newHttpClient()) {
            String downloadUrl = storageService.generateDownloadUrl(key, Duration.ofMinutes(15));

            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(downloadUrl))
                    .GET()
                    .build();

            var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, response.statusCode());
            assertArrayEquals(fileContent, response.body());
        }
    }

    @Test
    @DisplayName("Deve deletar objeto do bucket")
    void deleteObject_ShouldRemoveObject() throws Exception {
        String key = "encomendas/" + UUID.randomUUID() + "/to-delete.txt";
        byte[] content = "para-deletar".getBytes(StandardCharsets.UTF_8);

        String uploadUrl = storageService.generateUploadUrl(key, Duration.ofMinutes(15));
        try (var httpClient = java.net.http.HttpClient.newHttpClient()) {
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(uploadUrl))
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(content))
                    .header("Content-Type", "text/plain")
                    .build();
            httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        }

        minioClient.statObject(
                StatObjectArgs.builder().bucket(BUCKET).object(key).build());

        storageService.deleteObject(key);

        assertThrows(Exception.class, () ->
                minioClient.statObject(
                        StatObjectArgs.builder().bucket(BUCKET).object(key).build()));
    }

    @Test
    @DisplayName("Deve gerar keys com estrutura encomendas/{userId}/{uuid}-{fileName}")
    void generateUploadData_ShouldFollowKeyStructure() {
        UUID userId = UUID.randomUUID();
        String fileName = "nota-fiscal.pdf";

        String key = "encomendas/" + userId + "/" + UUID.randomUUID() + "-" + fileName;

        assertTrue(key.startsWith("encomendas/" + userId + "/"));
        assertTrue(key.endsWith("-" + fileName));
        assertTrue(key.contains(userId.toString()));
    }

    @Test
    @DisplayName("Deve suportar diferentes tipos MIME no upload")
    void upload_ShouldSupportDifferentMimeTypes() throws Exception {
        String[] mimeTypes = {"image/jpeg", "image/png", "image/webp", "application/pdf"};

        for (String mimeType : mimeTypes) {
            String ext = mimeType.split("/")[1];
            String key = "encomendas/" + UUID.randomUUID() + "/" +
                    UUID.randomUUID() + "-test." + ext;
            byte[] content = ("conteudo-" + mimeType).getBytes(StandardCharsets.UTF_8);

            String uploadUrl = storageService.generateUploadUrl(key, Duration.ofMinutes(15));

            try (var httpClient = java.net.http.HttpClient.newHttpClient()) {
                var request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(uploadUrl))
                        .PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(content))
                        .header("Content-Type", mimeType)
                        .build();
                var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode(), "Falha no upload com MIME type: " + mimeType);
            }

            storageService.deleteObject(key);
        }
    }

    @Test
    @DisplayName("Deve fazer upload com Content-Type application/octet-stream")
    void upload_ShouldHandleOctetStreamContentType() throws Exception {
        String key = "encomendas/" + UUID.randomUUID() + "/test.bin";
        byte[] content = "conteudo-binario".getBytes(StandardCharsets.UTF_8);

        String uploadUrl = storageService.generateUploadUrl(key, Duration.ofMinutes(15));

        try (var httpClient = java.net.http.HttpClient.newHttpClient()) {
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(uploadUrl))
                    .PUT(java.net.http.HttpRequest.BodyPublishers.ofByteArray(content))
                    .header("Content-Type", "application/octet-stream")
                    .build();
            var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
        }

        storageService.deleteObject(key);
    }
}
