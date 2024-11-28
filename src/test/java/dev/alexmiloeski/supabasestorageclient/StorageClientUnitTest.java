package dev.alexmiloeski.supabasestorageclient;

import org.junit.jupiter.api.BeforeEach;

class StorageClientUnitTest {
    StorageClient storageClient;

    @BeforeEach
    void setUp() {
        storageClient = new StorageClient("", "");
    }
}
