package com.LunaLink.application.application.ports.input;

import java.time.Duration;

public interface StorageService {
    String generateUploadUrl(String key, Duration expiration);
    String generateDownloadUrl(String key, Duration expiration);
    void deleteObject(String key);
}
