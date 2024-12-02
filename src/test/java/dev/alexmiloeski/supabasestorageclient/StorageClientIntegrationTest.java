package dev.alexmiloeski.supabasestorageclient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.options.FileMoveOptions;
import dev.alexmiloeski.supabasestorageclient.model.options.ListFilesOptions;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import dev.alexmiloeski.supabasestorageclient.model.responses.FileObjectIdentity;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static dev.alexmiloeski.supabasestorageclient.Arrange.*;
import static dev.alexmiloeski.supabasestorageclient.matchers.BodyLengthMatcher.withSizeGreaterThan;
import static dev.alexmiloeski.supabasestorageclient.matchers.BodyLengthMatcher.withSizeLessThan;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
public class StorageClientIntegrationTest {

    final static String TEST_API_KEY = "testApiKey";
    final static String STORAGE_PATH = "/storage/v1";
    final static String BUCKET_PATH = STORAGE_PATH + "/bucket";
    final static String OBJECT_PATH = STORAGE_PATH + "/object";

    StorageClient storageClient;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        final int port = wmRuntimeInfo.getHttpPort();
        storageClient = new TestStorageClient("any", TEST_API_KEY, port);
    }

    @Test
    void healthCheckReturnsTrue() {
        stubFor(get(STORAGE_PATH + "/health").willReturn(ok().withBody(HEALTHY_JSON)));

        ResponseWrapper<Boolean> responseWrapper = storageClient.isHealthy();

        assertNotNull(responseWrapper);
        boolean isHealthy = responseWrapper.body();
        assertTrue(isHealthy);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void healthCheckReturnsFalseWhenServerSaysFalse() {
        stubFor(get(STORAGE_PATH + "/health").willReturn(ok().withBody(UNHEALTHY_JSON)));

        ResponseWrapper<Boolean> responseWrapper = storageClient.isHealthy();

        assertNotNull(responseWrapper);
        boolean isHealthy = responseWrapper.body();
        assertFalse(isHealthy);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void healthCheckReturnsNullAndErrorWhenServerFails() {
        stubFor(get(STORAGE_PATH + "/health").willReturn(serverError().withBody("{}")));

        ResponseWrapper<Boolean> responseWrapper = storageClient.isHealthy();

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.exception());
        assertNull(responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
    }

    @Test
    void listBucketsReturnsBuckets() {
        stubFor(get(BUCKET_PATH).willReturn(ok().withBody(LIST_BUCKETS_JSON_RESPONSE)));

        final ResponseWrapper<List<Bucket>> responseWrapper = storageClient.listBuckets();

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKETS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void getBucketReturnsBucket() {
        stubFor(get(BUCKET_PATH + "/" + TEST_BUCKET_ID)
                .willReturn(ok().withBody(BUCKET_JSON)));

        final ResponseWrapper<Bucket> responseWrapper = storageClient.getBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_BUCKET, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void getBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(get(BUCKET_PATH + "/" + NONEXISTENT_BUCKET_ID)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<Bucket> responseWrapper =
                storageClient.getBucket(NONEXISTENT_BUCKET_ID);

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
        stubFor(post(BUCKET_PATH).willReturn(ok().withBody(BUCKET_CREATED_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .createBucket(TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);

        assertNotNull(responseWrapper);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
        assertEquals(TEST_BUCKET_NAME, responseWrapper.body());
    }

    @Test
    void createBucketWithDuplicateNameReturnsErrorResponse() {
        stubFor(post(BUCKET_PATH).willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .createBucket(TEST_BUCKET_ID, TEST_BUCKET_NAME, false, null, null);

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
        stubFor(delete(BUCKET_PATH + "/" + TEST_BUCKET_ID)
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void deleteNonEmptyBucketReturnsErrorResponse() {
        stubFor(delete(BUCKET_PATH + "/" + TEST_BUCKET_ID)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.deleteBucket(TEST_BUCKET_ID);

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
        stubFor(post(BUCKET_PATH + "/" + TEST_BUCKET_ID + "/empty")
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient.emptyBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void emptyBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(post(BUCKET_PATH + "/" + TEST_BUCKET_ID + "/empty")
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.emptyBucket(TEST_BUCKET_ID);

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
        stubFor(put(BUCKET_PATH + "/" + TEST_BUCKET_ID)
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient
                .updateBucket(TEST_BUCKET_ID, null, false, 0, null);

        assertNotNull(responseWrapper);
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateBucketWithWrongParamsReturnsErrorResponse() {
        stubFor(put(BUCKET_PATH + "/" + TEST_BUCKET_ID)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient
                .updateBucket(TEST_BUCKET_ID, null, false, 0, null);

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
        stubFor(post(OBJECT_PATH + "/list/" + TEST_BUCKET_ID)
                .willReturn(ok().withBody(LIST_FILES_JSON_RESPONSE)));

        final ResponseWrapper<List<FileObject>> responseWrapper = storageClient.listFilesInBucket(TEST_BUCKET_ID);

        assertNotNull(responseWrapper);
        assertEquals(EXPECTED_LIST_FILES_OBJECTS, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void listFilesInBucketWithFolderReturnsObjects() {
        stubFor(post(OBJECT_PATH + "/list/" + TEST_BUCKET_ID)
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
        stubFor(post(OBJECT_PATH + "/list/" + NONEXISTENT_BUCKET_ID)
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
        stubFor(get(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .willReturn(ok().withBody(TEST_FILE_CONTENTS_SHORTER)));

        final ResponseWrapper<String> responseWrapper =
                storageClient.downloadFile(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertEquals(TEST_FILE_CONTENTS_SHORTER, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileBytesReturnsFileContentsInBytes() {
        stubFor(get(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .willReturn(ok().withBody(TEST_FILE_CONTENTS_SHORTER)));

        final ResponseWrapper<byte[]> responseWrapper =
                storageClient.downloadFileBytes(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertArrayEquals(TEST_FILE_CONTENTS_SHORTER.getBytes(), responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void downloadFileWithWrongBucketIdReturnsErrorResponse() {
        stubFor(get(urlPathMatching(OBJECT_PATH + "/" + NONEXISTENT_BUCKET_ID + "/.*"))
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
        stubFor(get(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + NONEXISTENT_FILE_NAME)
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

    @Test
    void deleteFileReturnsSuccessMessage() {
        final String expectedMessage = "Successfully deleted";
        stubFor(delete(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper =
                storageClient.deleteFile(TEST_BUCKET_ID, TEST_FILE_NAME);

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void deleteFileWithWrongBucketIdReturnsErrorResponse() {
        stubFor(delete(OBJECT_PATH + "/" + NONEXISTENT_BUCKET_ID + "/" + TEST_FILE_NAME)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper =
                storageClient.deleteFile(NONEXISTENT_BUCKET_ID, TEST_FILE_NAME);

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
    void deleteFileWithWrongFileNameReturnsErrorResponse() {
        stubFor(delete(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + NONEXISTENT_FILE_NAME)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper =
                storageClient.deleteFile(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME);

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
    void updateFileReturnsIdentity() {
        stubFor(put(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .willReturn(ok().withBody(KEY_N_ID_JSON_RESPONSE)));

        final ResponseWrapper<FileObjectIdentity> responseWrapper =
                storageClient.updateFile(TEST_BUCKET_ID, TEST_FILE_NAME, new byte[0]);

        assertNotNull(responseWrapper);
        final FileObjectIdentity fileObjectIdentity = responseWrapper.body();
        assertEquals(EXPECTED_OBJECT_IDENTITY, fileObjectIdentity);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void updateFileWithWrongBucketNameReturnsErrorResponse() {
        stubFor(put(urlPathMatching(OBJECT_PATH + "/" + NONEXISTENT_BUCKET_ID + "/.*"))
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .updateFile(NONEXISTENT_BUCKET_ID, TEST_FILE_NAME, new byte[0]);

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
    void updateFileWithWrongFileNameReturnsErrorResponse() {
        stubFor(put(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + NONEXISTENT_FILE_NAME)
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        ResponseWrapper<FileObjectIdentity> responseWrapper = storageClient
                .updateFile(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME, new byte[0]);

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
    void uploadFileWithRightSizeReturnsIdentity() {
        stubFor(post(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .withRequestBody(withSizeGreaterThan(1))
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));
        stubFor(post(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .withRequestBody(withSizeLessThan(2))
                .willReturn(ok().withBody(KEY_N_ID_JSON_RESPONSE)));

        final ResponseWrapper<FileObjectIdentity> responseWrapper =
                storageClient.uploadFile(TEST_BUCKET_ID, TEST_FILE_NAME, new byte[1]);

        assertNotNull(responseWrapper);
        final FileObjectIdentity fileObjectIdentity = responseWrapper.body();
        assertEquals(EXPECTED_OBJECT_IDENTITY, fileObjectIdentity);
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void uploadOversizedFileReturnsErrorResponse() {
        stubFor(post(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .withRequestBody(withSizeGreaterThan(1))
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));
        stubFor(post(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .withRequestBody(withSizeLessThan(2))
                .willReturn(ok().withBody(KEY_N_ID_JSON_RESPONSE)));

        final ResponseWrapper<FileObjectIdentity> responseWrapper =
                storageClient.uploadFile(TEST_BUCKET_ID, TEST_FILE_NAME, new byte[3]);

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
    void uploadFileWithWrongMimeTypeReturnsErrorResponse() {
        stubFor(post(OBJECT_PATH + "/" + TEST_BUCKET_ID + "/" + TEST_FILE_NAME)
                .withHeader("Content-Type", notMatching("text/plain"))
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<FileObjectIdentity> responseWrapper =
                storageClient.uploadFile(TEST_BUCKET_ID, TEST_FILE_NAME, new byte[0], "image/jpeg");

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
    void moveFileReturnsSuccessMessage() {
        final String expectedMessage = "Successfully moved";
        stubFor(post(OBJECT_PATH + "/move")
                .withRequestBody(equalToJson("""
                         {
                             "bucketId": "%s",
                             "sourceKey": "%s",
                             "destinationBucket": "%s",
                             "destinationKey": "%s"
                         }"""
                    .formatted(TEST_BUCKET_ID, TEST_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH)))
                .willReturn(ok().withBody(MESSAGE_RESPONSE(expectedMessage))));

        final ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                TEST_BUCKET_ID, TEST_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH));

        assertNotNull(responseWrapper);
        assertNotNull(responseWrapper.body());
        assertEquals(expectedMessage, responseWrapper.body());
        assertNull(responseWrapper.errorResponse());
        assertNull(responseWrapper.exception());
    }

    @Test
    void moveFileWithWrongSourceBucketReturnsErrorResponse() {
        stubFor(post(OBJECT_PATH + "/move")
                .withRequestBody(equalToJson("""
                         {
                             "bucketId": "%s",
                             "sourceKey": "%s",
                             "destinationBucket": "%s",
                             "destinationKey": "%s"
                         }"""
                        .formatted(NONEXISTENT_BUCKET_ID, TEST_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH)))
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                NONEXISTENT_BUCKET_ID, TEST_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH));

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
    void moveFileWithWrongSourceFileReturnsErrorResponse() {
        stubFor(post(OBJECT_PATH + "/move")
                .withRequestBody(equalToJson("""
                         {
                             "bucketId": "%s",
                             "sourceKey": "%s",
                             "destinationBucket": "%s",
                             "destinationKey": "%s"
                         }"""
                        .formatted(TEST_BUCKET_ID, NONEXISTENT_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH)))
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                TEST_BUCKET_ID, NONEXISTENT_FILE_NAME, TEST_BUCKET_ID, MOVED_TEST_FILE_PATH));

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
    void moveFileWithWrongDestinationBucketReturnsErrorResponse() {
        stubFor(post(OBJECT_PATH + "/move")
                .withRequestBody(equalToJson("""
                         {
                             "bucketId": "%s",
                             "sourceKey": "%s",
                             "destinationBucket": "%s",
                             "destinationKey": "%s"
                         }"""
                        .formatted(TEST_BUCKET_ID, TEST_FILE_NAME, NONEXISTENT_BUCKET_ID, MOVED_TEST_FILE_PATH)))
                .willReturn(badRequest().withBody(MOCK_ERROR_JSON_RESPONSE)));

        final ResponseWrapper<String> responseWrapper = storageClient.moveFile(new FileMoveOptions(
                TEST_BUCKET_ID, TEST_FILE_NAME, NONEXISTENT_BUCKET_ID, MOVED_TEST_FILE_PATH));

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
