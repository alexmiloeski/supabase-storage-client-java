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

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageClientE2ETest {

    final String testBucketId = "test-bucket-" + System.currentTimeMillis();
    final String testFileId = "test-file-" + System.currentTimeMillis();
    final String testFolderId = "test-folder-" + System.currentTimeMillis();
    final String movedTestFilePath = testFolderId + "/" + testFileId;
    final String nonexistentFileId = "test-bucket";
    final String nonexistentBucketId = "nonexistent-bucket";
    final String testFileContents = "Test file contents.";
    final String testFileModifiedContents = "Test file modified contents.";

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
        ResponseWrapper<String> responseWrapper = storageClient
                .createBucketWithWrapper(testBucketId, testBucketId, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        assertEquals(testBucketId, responseWrapper.body());
    }

    @Test
    @Order(14)
    @Disabled
    void createBucketWithWrapperWithDuplicateNameReturnsErrorResponse() {
        String statusCode = "409";
        String error = "Duplicate";
        String message = "The resource already exists";

        ResponseWrapper<String> responseWrapper = storageClient
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
        assertTrue(buckets.stream().anyMatch(b -> testBucketId.equals(b.name())));
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
    @Order(25)
    @Disabled
    void getBucketWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<Bucket> responseWrapper = storageClient.getBucketWithWrapper(testBucketId);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        Bucket bucket = responseWrapper.body();
        assertNotNull(bucket);
        assertEquals(testBucketId, bucket.id());
    }

    @Test
    @Order(200)
    @Disabled
    void emptyBucket() throws InterruptedException {
        Thread.sleep(100);
        String message = storageClient.emptyBucket(testBucketId);
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }

    @Test
    @Order(200)
    @Disabled
    void emptyBucketWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient.emptyBucketWithWrapper(testBucketId);

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
        String message = storageClient.updateBucket(testBucketId, false, 0, null);
        assertNotNull(message);
        assertFalse(message.isEmpty());
    }

    @Test
    @Order(40)
    @Disabled
    void updateBucketWithWrapper() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient
                .updateBucketWithWrapper(testBucketId, null, false, 0, null);

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
        ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(testBucketId);

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
        ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(testBucketId);

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
                .uploadFile(testBucketId, testFileId, testFileContents.getBytes());

        assertNotNull(responseWrapper);
        FileObjectIdentity identity = responseWrapper.body();
        assertEquals(testBucketId + "/" + testFileId, identity.key());
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
                .uploadFile(testBucketId, testFileId, testFileContents.getBytes());

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
                .uploadFile(nonexistentBucketId, testFileId, testFileContents.getBytes());

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
        ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(testBucketId);

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
                testBucketId,
                new ListFilesOptions(testFolderId, null, null));

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
                .downloadFile(testBucketId, testFileId);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(testFileContents, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(71)
    @Disabled
    void downloadFileBytes() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<byte[]> responseWrapper = storageClient
                .downloadFileBytes(testBucketId, testFileId);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        final String stringBody = new String(responseWrapper.body());
        assertEquals(testFileContents, stringBody);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    @Order(72)
    @Disabled
    void downloadFileWithWrongBucketNameReturnsErrorResponse() throws InterruptedException {
        Thread.sleep(100);
        ResponseWrapper<String> responseWrapper = storageClient
                .downloadFile(nonexistentBucketId, nonexistentFileId);

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
                .downloadFile(testBucketId, nonexistentFileId);

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
                .updateFile(testBucketId, testFileId, testFileModifiedContents.getBytes());

        assertNotNull(responseWrapper);
        FileObjectIdentity identity = responseWrapper.body();
        assertNotNull(identity);
        assertEquals(testBucketId + "/" + testFileId, identity.key());
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
                .updateFile(nonexistentBucketId, nonexistentFileId, testFileModifiedContents.getBytes());

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
                .updateFile(testBucketId, nonexistentFileId, testFileModifiedContents.getBytes());

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
                testBucketId, testFileId, testBucketId, movedTestFilePath));

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
                nonexistentBucketId, testFileId, testBucketId, movedTestFilePath));

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
        ResponseWrapper<String> responseWrapper = storageClient.deleteFile(testBucketId, movedTestFilePath);

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
                .deleteFile(testBucketId, nonexistentFileId);

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
