package dev.alexmiloeski.supabasestorageclient;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeEach;

class StorageClientTest {

    StorageClient storageClient;

    @BeforeEach
    void setUp() {
        Dotenv dotenv = Dotenv.load();
        String projectId = dotenv.get("SUPABASE_STORAGE_PROJECT_ID");
        String apiKey = dotenv.get("SUPABASE_STORAGE_API_KEY");
        storageClient = new StorageClient(projectId, apiKey);
    }
}
