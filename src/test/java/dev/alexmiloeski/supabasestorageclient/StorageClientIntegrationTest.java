package dev.alexmiloeski.supabasestorageclient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.alexmiloeski.supabasestorageclient.model.Bucket;
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
        stubFor(get("/storage/v1/bucket/" + TEST_BUCKET_NAME)
                .willReturn(ok().withBody(BUCKET_JSON)));

        final ResponseWrapper<Bucket> responseWrapper = storageClient.getBucketWithWrapper(TEST_BUCKET_NAME);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKET, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void getBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(get("/storage/v1/bucket/" + NONEXISTENT_BUCKET_NAME)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<Bucket> responseWrapper =
                storageClient.getBucketWithWrapper(NONEXISTENT_BUCKET_NAME);

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
                .createBucketWithWrapper(TEST_BUCKET_NAME, TEST_BUCKET_NAME, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        assertEquals(TEST_BUCKET_NAME, responseWrapper.body());
    }

    @Test
    void createBucketWithDuplicateNameReturnsErrorResponse() {
        stubFor(post("/storage/v1/bucket").willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .createBucketWithWrapper(TEST_BUCKET_NAME, TEST_BUCKET_NAME, false, null, null);

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
        stubFor(delete("/storage/v1/bucket/" + TEST_BUCKET_NAME)
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(TEST_BUCKET_NAME);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void deleteNonEmptyBucketReturnsErrorResponse() {
        stubFor(delete("/storage/v1/bucket/" + TEST_BUCKET_NAME)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(TEST_BUCKET_NAME);

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
        stubFor(post("/storage/v1/bucket/" + TEST_BUCKET_NAME + "/empty")
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient.emptyBucketWithWrapper(TEST_BUCKET_NAME);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void emptyBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(post("/storage/v1/bucket/" + TEST_BUCKET_NAME + "/empty")
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.emptyBucketWithWrapper(TEST_BUCKET_NAME);

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
        stubFor(put("/storage/v1/bucket/" + TEST_BUCKET_NAME)
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient
                .updateBucketWithWrapper(TEST_BUCKET_NAME, null, false, 0, null);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(put("/storage/v1/bucket/" + TEST_BUCKET_NAME)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .updateBucketWithWrapper(TEST_BUCKET_NAME, null, false, 0, null);

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
