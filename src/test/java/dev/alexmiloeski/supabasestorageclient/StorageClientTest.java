package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;

import java.util.List;

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
    @Order(20)
    @Disabled
    void listBuckets() throws InterruptedException {
        Thread.sleep(100);
        List<Bucket> buckets = storageClient.listBuckets();
        assertNotNull(buckets);
        assertTrue(buckets.size() > 1);
        assertTrue(buckets.stream().anyMatch(b -> testBucketId.equals(b.name())));
    }

    @Test
    @Order(25)
    @Disabled
    void getBucket() throws InterruptedException {
        Thread.sleep(100);
        Bucket bucket = storageClient.getBucket(testBucketId);
        assertNotNull(bucket);
        assertEquals(testBucketId, bucket.id());
    }

    @Test
    @Order(30)
    @Disabled
    void emptyBucket() throws InterruptedException {
        Thread.sleep(100);
        String message = storageClient.emptyBucket(testBucketId);
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }

    @Test
    @Order(40)
    @Disabled
    void updateBucket() throws InterruptedException {
        Thread.sleep(100);
        String message = storageClient.updateBucket(testBucketId, false, 0, null);
        assertNotNull(message);
        assertFalse(message.isEmpty());
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
