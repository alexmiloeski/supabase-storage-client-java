package dev.alexmiloeski.supabasestorageclient;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageClientTest {

    final String testBucketId = "test-bucket-4";

    StorageClient storageClient;

    @BeforeEach
    void setUp() {
        Dotenv dotenv = Dotenv.load();
        String projectId = dotenv.get("SUPABASE_STORAGE_PROJECT_ID");
        String apiKey = dotenv.get("SUPABASE_STORAGE_API_KEY");
        storageClient = new StorageClient(projectId, apiKey);
    }

    @Test
    @Order(10)
    @Disabled
    void createBucket() {
        String testBucketIdRes = storageClient.createBucket(testBucketId, testBucketId, false, null, null);
        assertEquals(testBucketId, testBucketIdRes);
    }

    @Test
    @Order(50)
    @Disabled
    void deleteBucket() throws InterruptedException {
        Thread.sleep(100);
        String message = storageClient.deleteBucket(testBucketId);
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }
}
