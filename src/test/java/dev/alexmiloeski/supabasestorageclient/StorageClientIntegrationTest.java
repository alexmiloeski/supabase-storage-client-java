package dev.alexmiloeski.supabasestorageclient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        String statusCode = "409";
        String error = "Duplicate";
        String message = "The resource already exists";
        final String bucketDuplicateNameJson = """
                {"statusCode":"%s","error":"%s","message":"%s"}""".formatted(statusCode, error, message);

        stubFor(post("/storage/v1/bucket").willReturn(badRequest().withBody(bucketDuplicateNameJson)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .createBucketWithWrapper(TEST_BUCKET_NAME, TEST_BUCKET_NAME, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.exception());
        ErrorResponse errorResponse = responseWrapper.errorResponse();
        assertNotNull(errorResponse);
        assertEquals(statusCode, errorResponse.statusCode());
        assertEquals(error, errorResponse.error());
        assertEquals(message, errorResponse.message());
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
