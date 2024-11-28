package dev.alexmiloeski.supabasestorageclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageClientUnitTest {

    RequestMaker mockRequestMaker;
    StorageClient storageClient;

    @BeforeEach
    void setUp() {
        mockRequestMaker = mock(RequestMaker.class);
        when(mockRequestMaker.path(any())).thenReturn(mockRequestMaker);
        when(mockRequestMaker.bucket()).thenReturn(mockRequestMaker);
        when(mockRequestMaker.object()).thenReturn(mockRequestMaker);
        when(mockRequestMaker.jsonContent()).thenReturn(mockRequestMaker);
        when(mockRequestMaker.post()).thenReturn(mockRequestMaker);
        when(mockRequestMaker.post(any())).thenReturn(mockRequestMaker);
        when(mockRequestMaker.put(any())).thenReturn(mockRequestMaker);
        when(mockRequestMaker.delete()).thenReturn(mockRequestMaker);
        when(mockRequestMaker.contentType(any())).thenReturn(mockRequestMaker);
        storageClient = new TestStorageClient("", "", mockRequestMaker);
    }

    @Test
    void healthCheckReturnsHealthy() {
        when(mockRequestMaker.make()).thenReturn(Arrange.HEALTHY_JSON);

        final boolean isHealthy = storageClient.isHealthy();

        assertTrue(isHealthy);
    }

    private static class TestStorageClient extends StorageClient {

        private final RequestMaker mockRequestMaker;

        public TestStorageClient(String projectId, String apiKey, RequestMaker mockRequestMaker) {
            super(projectId, apiKey);
            this.mockRequestMaker = mockRequestMaker;
        }

        @Override
        protected RequestMaker newRequest() {
            return mockRequestMaker;
        }
    }
}
