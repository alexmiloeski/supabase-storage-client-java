package dev.alexmiloeski.supabasestorageclient;

import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.mock;

class StorageClientUnitTest {

    RequestMaker mockRequestMaker;
    StorageClient storageClient;

    @BeforeEach
    void setUp() {
        mockRequestMaker = mock(RequestMaker.class);
        storageClient = new TestStorageClient("", "", mockRequestMaker);
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
