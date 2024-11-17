package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageClientTest {

    final String testBucketId = "test-bucket-" + System.currentTimeMillis();

    StorageClient storageClient;

    @BeforeAll
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
    @Order(15)
    @Disabled
    void createBucketWithDuplicateNameShouldReturnNull() {
        String testBucketIdRes = storageClient.createBucket(testBucketId, testBucketId, false, null, null);
        assertNull(testBucketIdRes);
    }

    @Test
    @Order(10)
    @Disabled
    void createBucketWithWrapperWithValidNameReturnsBodyWithName() {
        ResponseWrapper responseWrapper = storageClient.createBucketWithWrapper(testBucketId, testBucketId, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        assertEquals(testBucketId, responseWrapper.body());
    }

    @Test
    @Order(15)
    @Disabled
    void createBucketWithDuplicateNameReturnsErrorResponse() {
        String statusCode = "409";
        String error = "Duplicate";
        String message = "The resource already exists";

        ResponseWrapper responseWrapper = storageClient
                .createBucketWithWrapper(testBucketId, testBucketId, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(statusCode, errorResponse.statusCode());
        assertEquals(error, errorResponse.error());
        assertEquals(message, errorResponse.message());
    }

    @Test
    @Order(10)
    @Disabled
    void createBucketWithWrapperWithInvalidNameReturnsErrorResponse() {
        ResponseWrapper responseWrapper = storageClient.createBucketWithWrapper(null, null, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals("400", errorResponse.statusCode());
        assertNotNull(errorResponse.error());
        assertFalse(errorResponse.error().isEmpty());
        assertNotNull(errorResponse.message());
        assertFalse(errorResponse.message().isEmpty());
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
    @Order(20)
    @Disabled
    void listBucketsWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper responseWrapper = storageClient.listBucketsWithWrapper();

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        List<Bucket> buckets = (List<Bucket>) responseWrapper.body();
        assertNotNull(buckets);
        assertInstanceOf(List.class, buckets);
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

    @Test
    @Order(60)
    @Disabled
    void healthCheck() {
        boolean isHealthy = storageClient.isHealthy();
        assertTrue(isHealthy);
    }
}
