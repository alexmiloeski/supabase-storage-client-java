package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.options.FileMoveOptions;
import dev.alexmiloeski.supabasestorageclient.model.options.ListFilesOptions;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import dev.alexmiloeski.supabasestorageclient.model.responses.FileObjectIdentity;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.alexmiloeski.supabasestorageclient.Arrange.*;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void listBucketsReturnsBuckets() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.LIST_BUCKETS_JSON_RESPONSE, null, null));

        final ResponseWrapper<List<Bucket>> responseWrapper = storageClient.listBucketsWithWrapper();

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKETS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void getBucketReturnsBucket() {
        when(mockRequestMaker.makeWithWrapper())
                .thenReturn(new ResponseWrapper<>(Arrange.BUCKET_JSON, null, null));

        final ResponseWrapper<Bucket> responseWrapper = storageClient.getBucketWithWrapper(TEST_BUCKET_NAME);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKET, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void getBucketWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

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
    void createBucketReturnsProperResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.BUCKET_CREATED_JSON_RESPONSE, null, null));

        final ResponseWrapper<String> responseWrapper = storageClient.createBucketWithWrapper(
                TEST_BUCKET_NAME, TEST_BUCKET_NAME, false, null, null);
        assertNotNull(responseWrapper);
        assertEquals(TEST_BUCKET_NAME, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void createBucketWithDuplicateNameReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

        final ResponseWrapper<String> responseWrapper = storageClient.createBucketWithWrapper(
                TEST_BUCKET_NAME, TEST_BUCKET_NAME, false, null, null);

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
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.MESSAGE_RESPONSE(expectedMessage), null, null));

        final ResponseWrapper<String> responseWrapper = storageClient.emptyBucketWithWrapper(TEST_BUCKET_NAME);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void emptyBucketWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

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
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.MESSAGE_RESPONSE(expectedMessage), null, null));

        final ResponseWrapper<String> responseWrapper = storageClient
                .updateBucketWithWrapper(TEST_BUCKET_NAME, false, 0, null);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateBucketWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

        final ResponseWrapper<String> responseWrapper = storageClient
                .updateBucketWithWrapper(TEST_BUCKET_NAME, false, 0, null);

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
    void deleteEmptyBucketReturnsSuccessMessage() {
        final String expectedMessage = "Successfully deleted";
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.MESSAGE_RESPONSE(expectedMessage), null, null));

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucketWithWrapper(TEST_BUCKET_NAME);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void deleteNonEmptyBucketReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

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
    void listFilesInBucketReturnsObjects() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.LIST_FILES_JSON_RESPONSE, null, null));

        final ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(TEST_BUCKET_NAME);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_LIST_FILES_OBJECTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void listFilesInBucketWithFolderReturnsObjects() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.LIST_FILES_JSON_RESPONSE, null, null));

        final ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(
                TEST_BUCKET_NAME, new ListFilesOptions(TEST_FOLDER_NAME, null, null));

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_LIST_FILES_OBJECTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void listFilesWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

        final ResponseWrapper<List<FileObject>> responseWrapper =
                storageClient.listFilesInBucket(NONEXISTENT_BUCKET_NAME);

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
        when(mockRequestMaker.makeWithWrapper())
                .thenReturn(new ResponseWrapper<>(Arrange.TEST_FILE_CONTENTS, null, null));

        final ResponseWrapper<String> responseWrapper =
                storageClient.downloadFile(TEST_BUCKET_NAME, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertEquals(TEST_FILE_CONTENTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileBytesReturnsFileContentsInBytes() {
        when(mockRequestMaker.makeWithWrapper())
                .thenReturn(new ResponseWrapper<>(Arrange.TEST_FILE_CONTENTS, null, null));

        final ResponseWrapper<byte[]> responseWrapper =
                storageClient.downloadFileBytes(TEST_BUCKET_NAME, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertArrayEquals(TEST_FILE_CONTENTS.getBytes(), responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

        final ResponseWrapper<String> responseWrapper =
                storageClient.downloadFile(NONEXISTENT_BUCKET_NAME, TEST_FILE_NAME);

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
    void updateFileReturnsProperResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.KEY_N_ID_JSON_RESPONSE, null, null));

        final ResponseWrapper<FileObjectIdentity> responseWrapper =
                storageClient.updateFile(TEST_BUCKET_NAME, TEST_FILE_ID, "".getBytes());

        assertNotNull(responseWrapper);
        final FileObjectIdentity fileObjectIdentity = responseWrapper.body();
        assertEquals(EXPECTED_OBJECT_IDENTITY, fileObjectIdentity);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateFileWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .updateFile(NONEXISTENT_BUCKET_NAME, NONEXISTENT_FILE_NAME, "".getBytes());

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
    void uploadFileReturnsProperResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.KEY_N_ID_JSON_RESPONSE, null, null));

        final ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .uploadFile(TEST_BUCKET_NAME, TEST_FILE_NAME, "".getBytes());

        assertNotNull(responseWrapper);
        final FileObjectIdentity fileObjectIdentity = responseWrapper.body();
        assertEquals(EXPECTED_OBJECT_IDENTITY, fileObjectIdentity);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void uploadFileWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

        final ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .uploadFile(TEST_BUCKET_NAME, TEST_FILE_NAME, "".getBytes());

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
    void deleteFileReturnsProperResponse() {
        final String expectedMessage = "Successfully deleted";
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.MESSAGE_RESPONSE(expectedMessage), null, null));

        final ResponseWrapper<String> responseWrapper =
                storageClient.deleteFile(TEST_BUCKET_NAME, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void deleteFileWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

        final ResponseWrapper<String> responseWrapper =
                storageClient.deleteFile(TEST_BUCKET_NAME, NONEXISTENT_FILE_NAME);

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
    void moveFileReturnsProperResponse() {
        final String expectedMessage = "Successfully moved";
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(Arrange.MESSAGE_RESPONSE(expectedMessage), null, null));

        final ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                TEST_BUCKET_NAME, TEST_FILE_NAME, TEST_BUCKET_NAME, MOVED_TEST_FILE_PATH));

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void moveFileWithWrongParamsReturnsErrorResponse() {
        when(mockRequestMaker.makeWithWrapper()).thenReturn(
                new ResponseWrapper<>(null, new ErrorResponse(
                        Arrange.MOCK_ERROR_STATUS, Arrange.MOCK_ERROR, Arrange.MOCK_ERROR_MESSAGE),
                        null));

        final ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                NONEXISTENT_BUCKET_NAME, NONEXISTENT_FILE_NAME, TEST_BUCKET_NAME, MOVED_TEST_FILE_PATH));

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
