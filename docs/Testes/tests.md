# Todos os testes MinIO
mvn test -Dtest="MinioStorageServiceTest,DeliveryServiceTest,MinioConfigTest,MinioStorageServiceIntegrationTest"

# Apenas unit tests (sem Docker)
mvn test -Dtest="MinioStorageServiceTest,DeliveryServiceTest,MinioConfigTest"

# Apenas integration test (requer Docker)
mvn test -Dtest="MinioStorageServiceIntegrationTest"