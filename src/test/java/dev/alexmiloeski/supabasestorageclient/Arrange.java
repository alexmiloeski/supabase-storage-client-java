package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.responses.FileObjectIdentity;
import org.mockito.ArgumentMatcher;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Arrange {
    static final String MOCK_ERROR_STATUS = "499";
    static final String MOCK_ERROR = "some_error";
    static final String MOCK_ERROR_MESSAGE = "Some error message";
    static final String TEST_BUCKET_ID = "test-bucket-id-" + System.currentTimeMillis();
    static final String TEST_BUCKET_NAME = "test-bucket-name-" + System.currentTimeMillis();
    static final String TEST_BUCKET_2_NAME = "test-bucket-2-name-" + System.currentTimeMillis();
    static final String TEST_BUCKET_2_ID = "test-bucket-2-id-" + System.currentTimeMillis();
    static final String TEST_FILE_NAME = "test-file-" + System.currentTimeMillis();
    static final String TEST_FOLDER_NAME = "test-folder-" + System.currentTimeMillis();
    static final String TEST_FOLDER_ID = "b301b87d-3c57-4f40-988f-20d8691382db";
    static final String TEST_FILE_ID = "f1c8e70a-95f8-47df-9122-d3f152f95f70";
    static final String MOVED_TEST_FILE_PATH = TEST_FOLDER_NAME + "/" + TEST_FILE_NAME;
    static final String NONEXISTENT_FILE_NAME = "test-bucket";
    static final String NONEXISTENT_BUCKET_NAME = "nonexistent-bucket";
    static final String TEST_FILE_CONTENTS = "Test file contents.";
    static final String TEST_FILE_MODIFIED_CONTENTS = "Test file modified contents.";
    static final Bucket EXPECTED_BUCKET = new Bucket(TEST_BUCKET_NAME, TEST_BUCKET_NAME, null,
            false, 0, null, "", "");
    static final String BUCKET_JSON = Mapper.toJson(EXPECTED_BUCKET);
    static final String BUCKET_CREATED_JSON_RESPONSE = """
            {"name":"%s"}""".formatted(TEST_BUCKET_NAME);
    static final String HEALTHY_JSON = Mapper.toJson(Map.of("healthy",true));
    static final String UNHEALTHY_JSON = Mapper.toJson(Map.of("healthy",false));
    static final String MOCK_ERROR_JSON_RESPONSE = """
                {"statusCode":"%s","error":"%s","message":"%s"}"""
            .formatted(MOCK_ERROR_STATUS, MOCK_ERROR, MOCK_ERROR_MESSAGE);
    static final String TEST_DATE = "2024-11-12T19:14:12.167Z";
    private static final String E_TAG = "88c163864a2335ddbc8d6132a4db382c-1";
    private static final String E_TAG_ACTUAL = "\\\"%s\\\"".formatted(E_TAG);
    private static final String E_TAG_EXPECTED = "\"%s\"".formatted(E_TAG);
    private static final int CONTENT_LENGTH = 41076;
    private static final String MIME_TYPE = "image/jpeg";
    private static final String CACHE_CONTROL = "max-age=3600";
    private static final int STATUS_CODE = 200;
    static final List<FileObject> EXPECTED_LIST_FILES_OBJECTS = List.of(
            new FileObject(TEST_FOLDER_ID, TEST_FOLDER_NAME, TEST_DATE, TEST_DATE, TEST_DATE, null),
            new FileObject(TEST_FILE_ID, TEST_FILE_NAME, TEST_DATE, TEST_DATE, TEST_DATE,
                    new FileObject.Metadata(E_TAG_EXPECTED, CONTENT_LENGTH, MIME_TYPE,
                            CACHE_CONTROL, TEST_DATE, CONTENT_LENGTH, STATUS_CODE))
    );
    static final String LIST_FILES_JSON_RESPONSE = """
                [
                    {
                        "name": "%s",
                        "id": "%s",
                        "updated_at": "%s",
                        "created_at": "%s",
                        "last_accessed_at": "%s",
                        "metadata": null
                    },
                    {
                        "name": "%s",
                        "id": "%s",
                        "updated_at": "%s",
                        "created_at": "%s",
                        "last_accessed_at": "%s",
                        "metadata": {
                            "eTag": "%s",
                            "size": %d,
                            "mimetype": "%s",
                            "cacheControl": "%s",
                            "lastModified": "%s",
                            "contentLength": %d,
                            "httpStatusCode": %d
                        }
                    }
                ]""".formatted(TEST_FOLDER_NAME, TEST_FOLDER_ID, TEST_DATE, TEST_DATE, TEST_DATE,
            TEST_FILE_NAME, TEST_FILE_ID, TEST_DATE, TEST_DATE, TEST_DATE, E_TAG_ACTUAL, CONTENT_LENGTH,
            MIME_TYPE, CACHE_CONTROL, TEST_DATE, CONTENT_LENGTH, STATUS_CODE);
    static final List<Bucket> EXPECTED_BUCKETS = List.of(
            new Bucket(TEST_BUCKET_NAME, TEST_BUCKET_NAME, "", false,
                    null, null, TEST_DATE, TEST_DATE),
            new Bucket(TEST_BUCKET_2_NAME, TEST_BUCKET_2_NAME, "", false,
                    null, null, TEST_DATE, TEST_DATE)
    );
    static final String LIST_BUCKETS_JSON_RESPONSE = """
            [
                {
                    "id": "%s",
                    "name": "%s",
                    "owner": "",
                    "public": false,
                    "file_size_limit": null,
                    "allowed_mime_types": null,
                    "created_at": "%s",
                    "updated_at": "%s"
                },
                {
                    "id": "%s",
                    "name": "%s",
                    "owner": "",
                    "public": false,
                    "file_size_limit": null,
                    "allowed_mime_types": null,
                    "created_at": "%s",
                    "updated_at": "%s"
                }
            ]""".formatted(TEST_BUCKET_NAME, TEST_BUCKET_NAME, TEST_DATE, TEST_DATE,
            TEST_BUCKET_2_NAME, TEST_BUCKET_2_NAME, TEST_DATE, TEST_DATE);
    static final FileObjectIdentity EXPECTED_OBJECT_IDENTITY =
            new FileObjectIdentity(TEST_BUCKET_NAME + "/" + TEST_FILE_NAME, TEST_FILE_ID);
    static final String KEY_N_ID_JSON_RESPONSE = """
            {"Key":"%s","Id":"%s"}""".formatted(EXPECTED_OBJECT_IDENTITY.key(), EXPECTED_OBJECT_IDENTITY.id());

    static String MESSAGE_RESPONSE(String successfullyEmptied) {
        return """
                {"message":"%s"}""".formatted(successfullyEmptied);
    }

    public static void mockResponse(HttpClient mockHttpClient, String returnData, ArgumentMatcher<String> matcher) {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(returnData);
        try {
            when(mockHttpClient.send(
                    argThat(request -> matcher.matches(request.uri().toString())),
                    any(HttpResponse.BodyHandler.class)
            )).thenReturn(mockResponse);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION IN MOCK HTTP CLIENT SEND !!!");
        }
    }

    public static void mockResponse(HttpClient mockHttpClient,
                                    String returnData,
                                    String httpMethod,
                                    ArgumentMatcher<String> matcher) {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(returnData);
        try {
            when(mockHttpClient.send(
                    argThat(request -> matcher.matches(request.uri().toString()) && request.method().equalsIgnoreCase(httpMethod)),
                    any(HttpResponse.BodyHandler.class)
            )).thenReturn(mockResponse);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION IN MOCK HTTP CLIENT SEND !!!");
        }
    }
}
