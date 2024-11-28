package dev.alexmiloeski.supabasestorageclient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static dev.alexmiloeski.supabasestorageclient.Arrange.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        boolean isHealthy = storageClient.isHealthy();
        assertTrue(isHealthy);
    }

    @Test
    void healthCheckReturnsFalseWhenServerSaysFalse() {
        stubFor(get("/storage/v1/health").willReturn(ok().withBody(UNHEALTHY_JSON)));

        boolean isHealthy = storageClient.isHealthy();
        assertFalse(isHealthy);
    }

    @Test
    void healthCheckReturnsFalseWhenServerErrors() {
        stubFor(get("/storage/v1/health").willReturn(serverError()));

        boolean isHealthy = storageClient.isHealthy();
        assertFalse(isHealthy);
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
