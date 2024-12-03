package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.options.FileMoveOptions;
import dev.alexmiloeski.supabasestorageclient.model.options.ListFilesOptions;
import dev.alexmiloeski.supabasestorageclient.model.responses.FileObjectIdentity;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;

import static dev.alexmiloeski.supabasestorageclient.Arrange.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * StorageClient-RequestMaker integration tests with a mocked HttpClient object.
 * Everything's real except for the HttpClient, which is mocked so that it matches specific
 * requests (uri, method) and returns mocked responses (with specific status code and body).
 */
class StorageClientRequestMakerIntegrationTest {

    String apiUrl = "";

    HttpClient mockHttpClient;
    StorageClient storageClient;

    @BeforeEach
    void setUp() {
        mockHttpClient = mock(HttpClient.class);
        storageClient = new TestStorageClient("fakeProjectId", "fakeApiKey", mockHttpClient);
        apiUrl = storageClient.getApiUrl();
    }

    @Test
    void healthCheckReturnsTrueWhenServerSaysTrue() {
        mockResponse(mockHttpClient, "GET",
                (uri) -> uri.equals(apiUrl + STORAGE_PATH + "/health"), HEALTHY_JSON);

        final ResponseWrapper<Boolean> responseWrapper = storageClient.isHealthy();

        assertNotNull(responseWrapper);
        assertTrue(responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void healthCheckReturnsFalseWhenServerSaysFalse() {
        mockResponse(mockHttpClient, "GET",
                (uri) -> uri.equals(apiUrl + STORAGE_PATH + "/health"), UNHEALTHY_JSON);

        final ResponseWrapper<Boolean> responseWrapper = storageClient.isHealthy();

        assertNotNull(responseWrapper);
        assertFalse(responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void healthCheckReturnsErrorWhenServerErrorsOut() {
        mockResponse(mockHttpClient, "GET",
                (uri) -> uri.equals(apiUrl + STORAGE_PATH + "/health"), 500, "{}");

        final ResponseWrapper<Boolean> responseWrapper = storageClient.isHealthy();

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.exception());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
    }

    @Test
    void createBucketShouldReturnProperResponse() {
        mockResponse(mockHttpClient, "POST",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH), BUCKET_CREATED_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient.createBucket(
                TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        assertEquals(TEST_BUCKET_NAME, responseWrapper.body());
    }

    @Test
    void createBucketWithDuplicateNameReturnsErrorResponse() {
        mockResponse(mockHttpClient, "POST",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH), 400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient.createBucket(
                TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void createBucketWithInvalidNameReturnsErrorResponse() {
        mockResponse(mockHttpClient, "POST",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH), 400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient.createBucket(
                null, null, false, null, null);

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void listBucketsReturnsBuckets() {
        mockResponse(mockHttpClient, "GET",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH), LIST_BUCKETS_JSON_RESPONSE);

        final ResponseWrapper<List<Bucket>> responseWrapper = storageClient.listBuckets();

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKETS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void getBucketReturnsBucket() {
        mockResponse(mockHttpClient, "GET",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH + "/" + TEST_BUCKET_ID), BUCKET_JSON);

        final ResponseWrapper<Bucket> responseWrapper = storageClient.getBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKET, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void emptyBucketReturnsSuccessMessage() {
        final String expectedMessage = "Successfully emptied";
        mockResponse(mockHttpClient, "POST",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH + "/" + TEST_BUCKET_ID + "/empty"),
                MESSAGE_RESPONSE(expectedMessage));

        final ResponseWrapper<String> responseWrapper = storageClient.emptyBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateBucketReturnsSuccessMessage() {
        final String expectedMessage = "Successfully updated";
        mockResponse(mockHttpClient, "PUT",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH + "/" + TEST_BUCKET_ID),
                MESSAGE_RESPONSE(expectedMessage));

        final ResponseWrapper<String> responseWrapper = storageClient.updateBucket(
                TEST_BUCKET_ID, null, false, 0, null);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void deleteEmptyBucketReturnsSuccessMessage() {
        mockResponse(mockHttpClient, "DELETE",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH + "/" + TEST_BUCKET_ID),
                400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucket(TEST_BUCKET_ID);

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void deleteNonEmptyBucketReturnsError() {
        mockResponse(mockHttpClient, "DELETE",
                (uri) -> uri.equals(apiUrl + BUCKET_PATH + "/" + TEST_BUCKET_ID),
                400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucket(TEST_BUCKET_ID);

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void uploadFileReturnsIdentity() {
        mockResponse(mockHttpClient, "POST", (uri) -> uri.equals(
                apiUrl + OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME),
                IDENTITY_JSON_RESPONSE);

        final ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient.uploadFile(
                TEST_BUCKET_ID, TEST_FILE_NAME, TEST_FILE_CONTENTS_SHORTER.getBytes());

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_OBJECT_IDENTITY, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void uploadFileWithFolderReturnsIdentity() {
        mockResponse(mockHttpClient, "POST",
                (uri) -> uri.equals(apiUrl + OBJECT_PATH + "/" +
                        TEST_BUCKET_ID + "/" + TEST_FOLDER_NAME + "/" + TEST_FILE_NAME),
                IDENTITY_JSON_RESPONSE);

        final ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient.uploadFile(
                TEST_BUCKET_ID, TEST_FOLDER_NAME + "/" + TEST_FILE_NAME,
                TEST_FILE_CONTENTS_SHORTER.getBytes());

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_OBJECT_IDENTITY, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void listFilesInBucketReturnsObjects() {
        mockResponse(mockHttpClient, "POST",
                (uri) -> uri.equals(apiUrl + OBJECT_PATH + "/list/" + TEST_BUCKET_ID),
                LIST_FILES_JSON_RESPONSE);

        final ResponseWrapper<List<FileObject>> responseWrapper = storageClient
                .listFilesInBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_LIST_FILES_OBJECTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void listFilesInBucketWithFolderReturnsObjects() {
        mockResponse(mockHttpClient, "POST",
                (uri) -> uri.equals(apiUrl + OBJECT_PATH + "/list/" + TEST_BUCKET_ID),
                LIST_FILES_JSON_RESPONSE);

        final ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(
                TEST_BUCKET_ID, new ListFilesOptions(TEST_FOLDER_NAME, null, null));

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertFalse(responseWrapper.body().isEmpty());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileReturnsFileContents() {
        mockResponse(mockHttpClient, "GET", (uri) -> uri.equals(
                apiUrl + OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME),
                TEST_FILE_CONTENTS_SHORTER);

        final ResponseWrapper<String> responseWrapper = storageClient
                .downloadFile(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertEquals(TEST_FILE_CONTENTS_SHORTER, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileBytesReturnsFileContentsInBytes() {
        mockResponse(mockHttpClient, "GET", (uri) -> uri.equals(
                apiUrl + OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME),
                TEST_FILE_CONTENTS_SHORTER);

        final ResponseWrapper<byte[]> responseWrapper = storageClient
                .downloadFileBytes(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(TEST_FILE_CONTENTS_SHORTER, new String(responseWrapper.body()));
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileWithWrongBucketNameReturnsErrorResponse() {
        mockResponse(mockHttpClient, "GET", (uri) -> uri.startsWith(
                apiUrl + OBJECT_PATH + "/" + NONEXISTENT_BUCKET_ID + "/"),
                400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient
                .downloadFile(NONEXISTENT_BUCKET_ID, TEST_FILE_NAME);

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void downloadFileWithWrongFileNameReturnsErrorResponse() {
        mockResponse(mockHttpClient, "GET", (uri) -> uri.equals(
                apiUrl + OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + NONEXISTENT_FILE_NAME),
                400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient
                .downloadFile(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME);

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void updateFileReturnsIdentity() {
        mockResponse(mockHttpClient, "PUT", (uri) -> uri.equals(
                apiUrl + OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME),
                IDENTITY_JSON_RESPONSE);

        final ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient.updateFile(
                TEST_BUCKET_ID, TEST_FILE_NAME, TEST_FILE_CONTENTS_MODIFIED.getBytes());

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_OBJECT_IDENTITY, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateFileWithWrongBucketNameReturnsErrorResponse() {
        mockResponse(mockHttpClient, "PUT",
                (uri) -> uri.startsWith(apiUrl + OBJECT_PATH + "/" + NONEXISTENT_BUCKET_ID),
                400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .updateFile(NONEXISTENT_BUCKET_ID, NONEXISTENT_FILE_NAME,
                        TEST_FILE_CONTENTS_MODIFIED.getBytes());

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void updateFileWithWrongFileNameReturnsErrorResponse() {
        mockResponse(mockHttpClient, "PUT", (uri) -> uri.equals(
                apiUrl + OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + NONEXISTENT_FILE_NAME),
                400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient.updateFile(
                TEST_BUCKET_ID, NONEXISTENT_FILE_NAME, TEST_FILE_CONTENTS_MODIFIED.getBytes());

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void deleteFileReturnsSuccessMessage() {
        final String expectedMessage = "Successfully deleted";
        mockResponse(mockHttpClient, "DELETE", (uri) -> uri.startsWith(
                apiUrl + OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME),
                MESSAGE_RESPONSE(expectedMessage));

        final ResponseWrapper<String> responseWrapper = storageClient
                .deleteFile(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void deleteFileWithWrongBucketIdReturnsErrorResponse() {
        mockResponse(mockHttpClient, "DELETE",
                (uri) -> uri.startsWith(apiUrl + OBJECT_PATH + "/" + NONEXISTENT_BUCKET_ID),
                400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient
                .deleteFile(NONEXISTENT_BUCKET_ID, TEST_FILE_NAME);

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void deleteFileWithWrongFileNameReturnsErrorResponse() {
        mockResponse(mockHttpClient, "DELETE", (uri) -> uri.equals(
                apiUrl + OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + NONEXISTENT_FILE_NAME),
                400, MOCK_ERROR_JSON_RESPONSE);

        final ResponseWrapper<String> responseWrapper = storageClient
                .deleteFile(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME);

        assertEquals(EXPECTED_MOCK_ERROR_RESPONSE_WRAPPER, responseWrapper);
    }

    @Test
    void moveFileReturnsSuccessMessage() {
        final String expectedMessage = "Successfully moved";
        mockResponse(mockHttpClient, "POST",
                (uri) -> uri.startsWith(apiUrl + OBJECT_PATH + "/move"),
                MESSAGE_RESPONSE(expectedMessage));

        final ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                TEST_BUCKET_ID, TEST_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH));

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    private static class TestStorageClient extends StorageClient {

        private final HttpClient mockHttpClient;

        public TestStorageClient(String projectId, String apiKey, HttpClient httpClient) {
            super(projectId, apiKey);
            this.mockHttpClient = httpClient;
        }

        @Override
        protected RequestMaker newRequest() {
            return new RequestMaker(apiUrl, apiKey, mockHttpClient);
        }
    }
}
