package dev.alexmiloeski.supabasestorageclient;

import org.junit.jupiter.api.BeforeEach;

import java.net.http.HttpClient;

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
