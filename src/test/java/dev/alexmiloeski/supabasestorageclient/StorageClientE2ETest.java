package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.options.FileMoveOptions;
import dev.alexmiloeski.supabasestorageclient.model.options.ListFilesOptions;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import dev.alexmiloeski.supabasestorageclient.model.responses.FileObjectIdentity;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;

import java.util.List;

import static dev.alexmiloeski.supabasestorageclient.Arrange.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageClientE2ETest {

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
        String testBucketIdRes = storageClient.createBucket(TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);
        assertEquals(TEST_BUCKET_ID, testBucketIdRes);
    }

    @Test
    @Order(15)
    @Disabled
    void createBucketWithDuplicateNameShouldReturnNull() {
        String testBucketIdRes = storageClient.createBucket(TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);
        assertNull(testBucketIdRes);
    }

    @Test
    @Order(10)
    @Disabled
    void createBucketWithWrapperWithValidNameReturnsBodyWithName() {
        ResponseWrapper<String> responseWrapper = storageClient
                .createBucketWithWrapper(TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        assertEquals(TEST_BUCKET_NAME, responseWrapper.body());
    }

    @Test
    @Order(14)
    @Disabled
    void createBucketWithWrapperWithDuplicateNameReturnsErrorResponse() {
        String statusCode = "409";
        String error = "Duplicate";
        String message = "The resource already exists";

        ResponseWrapper<String> responseWrapper = storageClient
                .createBucketWithWrapper(TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);

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
    @Order(17)
    @Disabled
    void createBucketWithWrapperWithInvalidNameReturnsErrorResponse() {
        ResponseWrapper<String> responseWrapper = storageClient
                .createBucketWithWrapper(null, null, false, null, null);

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
        assertTrue(buckets.stream().anyMatch(b -> TEST_BUCKET_NAME.equals(b.name())));
    }

    @Test
    @Order(20)
    @Disabled
    void listBucketsWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<List<Bucket>> responseWrapper = storageClient.listBucketsWithWrapper();

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        List<Bucket> buckets = responseWrapper.body();
        assertNotNull(buckets);
        assertTrue(buckets.size() > 1);
        assertTrue(buckets.stream().anyMatch(b -> TEST_BUCKET_NAME.equals(b.name())));
    }

    @Test
    @Order(25)
    @Disabled
    void getBucket() throws InterruptedException {
        Thread.sleep(100);
        Bucket bucket = storageClient.getBucket(TEST_BUCKET_ID);
        assertNotNull(bucket);
        assertEquals(TEST_BUCKET_ID, bucket.id());
    }

    @Test
    @Order(25)
    @Disabled
    void getBucketWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<Bucket> responseWrapper = storageClient.getBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        Bucket bucket = responseWrapper.body();
        assertNotNull(bucket);
        assertEquals(TEST_BUCKET_ID, bucket.id());
    }

    @Test
    @Order(200)
    @Disabled
    void emptyBucket() throws InterruptedException {
        Thread.sleep(100);
        String message = storageClient.emptyBucket(TEST_BUCKET_ID);
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }

    @Test
    @Order(210)
    @Disabled
    void emptyBucketWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient.emptyBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        String message = responseWrapper.body();
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }

    @Test
    @Order(40)
    @Disabled
    void updateBucket() throws InterruptedException {
        Thread.sleep(100);
        String message = storageClient.updateBucket(TEST_BUCKET_ID, false, 0, null);
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }

    @Test
    @Order(40)
    @Disabled
    void updateBucketWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient
                .updateBucketWithWrapper(TEST_BUCKET_ID, null, true, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        String message = responseWrapper.body();
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }

    @Test
    @Order(200)
    @Disabled
    void deleteNonEmptyBucketWithWrapperReturnsError() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals("409", errorResponse.statusCode());
        assertEquals("InvalidRequest", errorResponse.error());
        assertEquals("The bucket you tried to delete is not empty", errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(220)
    @Disabled
    void deleteEmptyBucketWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        String message = responseWrapper.body();
        assertNotNull(message);
        assertFalse(message.isEmpty());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(55)
    @Disabled
    void uploadFile() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .uploadFile(TEST_BUCKET_ID, TEST_FILE_NAME, TEST_FILE_CONTENTS.getBytes());

        assertNotNull(responseWrapper);
        FileObjectIdentity identity = responseWrapper.body();
        assertEquals(TEST_BUCKET_ID + "/" + TEST_FILE_NAME, identity.key());
        assertNotNull(identity.id());
        assertFalse(identity.id().isEmpty());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    // todo: upload file with folder (prefix)

    @Test
    @Order(56)
    @Disabled
    void uploadFileWithDuplicateNameReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);

        final String statusCode = "409";
        final String error = "Duplicate";
        final String message = "The resource already exists";

        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .uploadFile(TEST_BUCKET_ID, TEST_FILE_NAME, TEST_FILE_CONTENTS.getBytes());

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(statusCode, errorResponse.statusCode());
        assertEquals(error, errorResponse.error());
        assertEquals(message, errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(57)
    @Disabled
    void uploadFileWithWrongBucketReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);

        final String statusCode = "404";
        final String error = "Bucket not found";
        final String message = "Bucket not found";

        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .uploadFile(NONEXISTENT_BUCKET_ID, TEST_FILE_NAME, TEST_FILE_CONTENTS.getBytes());

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(statusCode, errorResponse.statusCode());
        assertEquals(error, errorResponse.error());
        assertEquals(message, errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    // todo: upload file with wrong mime type

    // todo: upload file that's too large

    @Test
    @Order(60)
    @Disabled
    void listFilesInBucket() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertFalse(responseWrapper.body().isEmpty());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(62)
    @Disabled
    void listFilesInBucketWithFolder() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(
                TEST_BUCKET_ID,
                new ListFilesOptions(TEST_FOLDER_NAME, null, null));

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertFalse(responseWrapper.body().isEmpty());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(70)
    @Disabled
    void downloadFile() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient
                .downloadFile(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(TEST_FILE_CONTENTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(71)
    @Disabled
    void downloadFileBytes() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<byte[]> responseWrapper = storageClient
                .downloadFileBytes(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        final String stringBody = new String(responseWrapper.body());
        assertEquals(TEST_FILE_CONTENTS, stringBody);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(72)
    @Disabled
    void downloadFileWithWrongBucketNameReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient
                .downloadFile(NONEXISTENT_BUCKET_ID, NONEXISTENT_FILE_NAME);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals("404", errorResponse.statusCode());
        assertEquals("Bucket not found", errorResponse.error());
        assertEquals("Bucket not found", errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(72)
    @Disabled
    void downloadFileWithWrongFileNameReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient
                .downloadFile(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals("404", errorResponse.statusCode());
        assertEquals("not_found", errorResponse.error());
        assertEquals("Object not found", errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(75)
    @Disabled
    void updateFile() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .updateFile(TEST_BUCKET_ID, TEST_FILE_NAME, TEST_FILE_MODIFIED_CONTENTS.getBytes());

        assertNotNull(responseWrapper);
        FileObjectIdentity identity = responseWrapper.body();
        assertNotNull(identity);
        assertEquals(TEST_BUCKET_ID + "/" + TEST_FILE_NAME, identity.key());
        assertNotNull(identity.id());
        assertFalse(identity.id().isEmpty());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(76)
    @Disabled
    void updateFileWithWrongBucketNameReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .updateFile(NONEXISTENT_BUCKET_ID, NONEXISTENT_FILE_NAME, TEST_FILE_MODIFIED_CONTENTS.getBytes());

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals("404", errorResponse.statusCode());
        assertEquals("Bucket not found", errorResponse.error());
        assertEquals("Bucket not found", errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(76)
    @Disabled
    void updateFileWithWrongFileNameReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .updateFile(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME, TEST_FILE_MODIFIED_CONTENTS.getBytes());

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals("404", errorResponse.statusCode());
        assertEquals("not_found", errorResponse.error());
        assertEquals("Object not found", errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(78)
    @Disabled
    void moveFile() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                TEST_BUCKET_ID, TEST_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH));

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals("Successfully moved", responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(79)
    @Disabled
    void moveFileWithWrongSourceBucketReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                NONEXISTENT_BUCKET_ID, TEST_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH));

        assertNotNull(responseWrapper);
        final ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals("404", errorResponse.statusCode());
        assertEquals("not_found", errorResponse.error());
        assertEquals("Object not found", errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(83)
    @Disabled
    void deleteFile() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient.deleteFile(TEST_BUCKET_ID, MOVED_TEST_FILE_PATH);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals("Successfully deleted", responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(81)
    @Disabled
    void deleteFileWithWrongFileNameReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient
                .deleteFile(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals("404", errorResponse.statusCode());
        assertEquals("not_found", errorResponse.error());
        assertEquals("Object not found", errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(60)
    @Disabled
    void healthCheck() {
        boolean isHealthy = storageClient.isHealthy();
        assertTrue(isHealthy);
    }
}
