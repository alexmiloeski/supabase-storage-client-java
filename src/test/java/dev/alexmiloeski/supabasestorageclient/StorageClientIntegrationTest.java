package dev.alexmiloeski.supabasestorageclient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.options.ListFilesOptions;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static dev.alexmiloeski.supabasestorageclient.Arrange.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
public class StorageClientIntegrationTest {

    final static String TEST_API_KEY = "testApiKey";

    StorageClient storageClient;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        final int port = wmRuntimeInfo.getHttpPort();
        storageClient = new TestStorageClient("any", TEST_API_KEY, port);
    }

    @Test
    void healthCheckReturnsTrue() {
        stubFor(get("/storage/v1/health").willReturn(ok().withBody(HEALTHY_JSON)));

        final boolean isHealthy = storageClient.isHealthy();
        assertTrue(isHealthy);
    }

    @Test
    void healthCheckReturnsFalseWhenServerSaysFalse() {
        stubFor(get("/storage/v1/health").willReturn(ok().withBody(UNHEALTHY_JSON)));

        final boolean isHealthy = storageClient.isHealthy();
        assertFalse(isHealthy);
    }

    @Test
    void healthCheckReturnsFalseWhenServerErrors() {
        stubFor(get("/storage/v1/health").willReturn(serverError()));

        final boolean isHealthy = storageClient.isHealthy();
        assertFalse(isHealthy);
    }

    @Test
    void listBucketsReturnsBuckets() {
        stubFor(get("/storage/v1/bucket").willReturn(ok().withBody(LIST_BUCKETS_JSON_RESPONSE)));

        final ResponseWrapper<List<Bucket>> responseWrapper = storageClient.listBucketsWithWrapper();

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKETS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void getBucketReturnsBucket() {
        stubFor(get("/storage/v1/bucket/" + TEST_BUCKET_ID)
                .willReturn(ok().withBody(BUCKET_JSON)));

        final ResponseWrapper<Bucket> responseWrapper = storageClient.getBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKET, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void getBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(get("/storage/v1/bucket/" + NONEXISTENT_BUCKET_ID)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<Bucket> responseWrapper =
                storageClient.getBucketWithWrapper(NONEXISTENT_BUCKET_ID);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(MOCK_ERROR_STATUS, errorResponse.statusCode());
        assertEquals(MOCK_ERROR, errorResponse.error());
        assertEquals(MOCK_ERROR_MESSAGE, errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    void createBucketWithValidNameReturnsBodyWithName() {
        stubFor(post("/storage/v1/bucket").willReturn(ok().withBody(BUCKET_CREATED_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .createBucketWithWrapper(TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        assertEquals(TEST_BUCKET_NAME, responseWrapper.body());
    }

    @Test
    void createBucketWithDuplicateNameReturnsErrorResponse() {
        stubFor(post("/storage/v1/bucket").willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .createBucketWithWrapper(TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(MOCK_ERROR_STATUS, errorResponse.statusCode());
        assertEquals(MOCK_ERROR, errorResponse.error());
        assertEquals(MOCK_ERROR_MESSAGE, errorResponse.message());
    }

    @Test
    void deleteEmptyBucketReturnsSuccessMessage() {
        final String expectedMessage = "Successfully deleted";
        stubFor(delete("/storage/v1/bucket/" + TEST_BUCKET_ID)
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void deleteNonEmptyBucketReturnsErrorResponse() {
        stubFor(delete("/storage/v1/bucket/" + TEST_BUCKET_ID)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(MOCK_ERROR_STATUS, errorResponse.statusCode());
        assertEquals(MOCK_ERROR, errorResponse.error());
        assertEquals(MOCK_ERROR_MESSAGE, errorResponse.message());
    }

    @Test
    void emptyBucketReturnsSuccessMessage() {
        final String expectedMessage = "Successfully emptied";
        stubFor(post("/storage/v1/bucket/" + TEST_BUCKET_ID + "/empty")
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient.emptyBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void emptyBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(post("/storage/v1/bucket/" + TEST_BUCKET_ID + "/empty")
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.emptyBucketWithWrapper(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(MOCK_ERROR_STATUS, errorResponse.statusCode());
        assertEquals(MOCK_ERROR, errorResponse.error());
        assertEquals(MOCK_ERROR_MESSAGE, errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateBucketReturnsSuccessMessage() {
        final String expectedMessage = "Successfully updated";
        stubFor(put("/storage/v1/bucket/" + TEST_BUCKET_ID)
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient
                .updateBucketWithWrapper(TEST_BUCKET_ID, null, false, 0, null);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(put("/storage/v1/bucket/" + TEST_BUCKET_ID)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .updateBucketWithWrapper(TEST_BUCKET_ID, null, false, 0, null);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(MOCK_ERROR_STATUS, errorResponse.statusCode());
        assertEquals(MOCK_ERROR, errorResponse.error());
        assertEquals(MOCK_ERROR_MESSAGE, errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    void listFilesInBucketReturnsObjects() {
        stubFor(post("/storage/v1/object/list/" + TEST_BUCKET_ID)
                .willReturn(ok().withBody(LIST_FILES_JSON_RESPONSE)));

        final ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_LIST_FILES_OBJECTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void listFilesInBucketWithFolderReturnsObjects() {
        stubFor(post("/storage/v1/object/list/" + TEST_BUCKET_ID)
                .withRequestBody(equalToJson("""
                {"limit":100,"offset":0,"sortBy":{"column":"name","order":"asc"},"prefix":"%s"}"""
                        .formatted(TEST_FOLDER_NAME)))
                .willReturn(ok().withBody(LIST_FILES_JSON_RESPONSE)));

        final ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(
                TEST_BUCKET_ID, new ListFilesOptions(TEST_FOLDER_NAME, null, null));

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_LIST_FILES_OBJECTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void listFilesWithWrongParamsReturnsErrorResponse() {
        stubFor(post("/storage/v1/object/list/" + NONEXISTENT_BUCKET_ID)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<List<FileObject>> responseWrapper =
                storageClient.listFilesInBucket(NONEXISTENT_BUCKET_ID);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(MOCK_ERROR_STATUS, errorResponse.statusCode());
        assertEquals(MOCK_ERROR, errorResponse.error());
        assertEquals(MOCK_ERROR_MESSAGE, errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileReturnsFileContents() {
        stubFor(get("/storage/v1/object/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .willReturn(ok().withBody(TEST_FILE_CONTENTS)));

        final ResponseWrapper<String> responseWrapper =
                storageClient.downloadFile(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertEquals(TEST_FILE_CONTENTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileBytesReturnsFileContentsInBytes() {
        stubFor(get("/storage/v1/object/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .willReturn(ok().withBody(TEST_FILE_CONTENTS)));

        final ResponseWrapper<byte[]> responseWrapper =
                storageClient.downloadFileBytes(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertArrayEquals(TEST_FILE_CONTENTS.getBytes(), responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileWithWrongBucketIdReturnsErrorResponse() {
        stubFor(get(urlPathMatching("/storage/v1/object/" + NONEXISTENT_BUCKET_ID + "/.*"))
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper =
                storageClient.downloadFile(NONEXISTENT_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(MOCK_ERROR_STATUS, errorResponse.statusCode());
        assertEquals(MOCK_ERROR, errorResponse.error());
        assertEquals(MOCK_ERROR_MESSAGE, errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileWithWrongFileNameReturnsErrorResponse() {
        stubFor(get("/storage/v1/object/" + TEST_BUCKET_ID + "/" + NONEXISTENT_FILE_NAME)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper =
                storageClient.downloadFile(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME);

        assertNotNull(responseWrapper);
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(MOCK_ERROR_STATUS, errorResponse.statusCode());
        assertEquals(MOCK_ERROR, errorResponse.error());
        assertEquals(MOCK_ERROR_MESSAGE, errorResponse.message());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
    }

    private static class TestStorageClient extends StorageClient {

        final int port;

        public TestStorageClient(String projectId, String apiKey, int port) {
            super(projectId, apiKey);
            this.port = port;
        }

        @Override
        protected RequestMaker newRequest() {
            return new RequestMaker("http://localhost:" + port, apiKey);
        }
    }
}
