package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse.MISSING_FIELDS_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;

class MapperTest {
    @Test
    void mapsToBucket() {
        final String testBucketId = "test-bucket-4";
        final Bucket expectedBucket = new Bucket(testBucketId, testBucketId, null, true,
                0, List.of("image/jpeg"), null, null);
        String json = """
                {
                  "id": "%s",
                  "name": "%s",
                  "public": true,
                  "file_size_limit": 0,
                  "allowed_mime_types": ["image/jpeg"]
                }""".formatted(testBucketId, testBucketId);
        Bucket bucket = Mapper.toBucket(json);
        assertNotNull(bucket);
        assertEquals(expectedBucket, bucket);
    }

    @Test
    void throwsWhenMappingBadBucket() {
        String json = """
                {
                  "id": "",
                  "name": "",
                  "public": true,
                  "file_size_limit": ".",
                  "allowed_mime_types": ""
                }""";
        Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toBucket(json));
        assertTrue(exception.getMessage().contains("Cannot"));
    }

    @Test
    void mapsToBuckets() {
        final String testBucketId = "test-bucket-4";
        final List<Bucket> expectedBucket = List.of(new Bucket(testBucketId, testBucketId, null, true,
                0, List.of("image/jpeg"), null, null));
        String json = """
                [{
                  "id": "%s",
                  "name": "%s",
                  "public": true,
                  "file_size_limit": 0,
                  "allowed_mime_types": ["image/jpeg"]
                }]""".formatted(testBucketId, testBucketId);
        List<Bucket> bucket = Mapper.toBuckets(json);
        assertNotNull(bucket);
        assertEquals(expectedBucket, bucket);
    }

    @Test
    void throwsWhenMappingBadBuckets() {
        String json = """
                [{
                  "id": "",
                  "name": "",
                  "public": true,
                  "file_size_limit": ".",
                  "allowed_mime_types": ""
                }]""";
        Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toBuckets(json));
        assertTrue(exception.getMessage().contains("Cannot"));
    }

    @Test
    void mapsToObjects() {
        final String testFolderName = "some-folder";
        final String testFolderId = "b301b87d-3c57-4f40-988f-20d8691382da";
        final String testFileName = "some-file";
        final String testFileId = "b301b87d-3c57-4f40-988f-20d8691382df";
        final String someDate = "2024-11-12T19:14:12.167Z";
        final String eTag = "88c163864a2335ddbc8d6132a4db382c-1";
        final String eTagActual = "\\\"%s\\\"".formatted(eTag);
        final String eTagExpected = "\"%s\"".formatted(eTag);
        final int size = 41076;
        final String mimeType = "image/jpeg";
        final String cacheControl = "max-age=3600";
        final int statusCode = 200;
        final List<FileObject> expectedObjects = List.of(
                new FileObject(testFolderId, testFolderName, someDate, someDate, someDate, null),
                new FileObject(testFileId, testFileName, someDate, someDate, someDate,
                        new FileObject.Metadata(eTagExpected, size, mimeType, cacheControl, someDate, size, statusCode))
        );
        String json = """
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
                ]""".formatted(testFolderName, testFolderId, someDate, someDate, someDate, testFileName, testFileId,
                someDate, someDate, someDate, eTagActual, size, mimeType, cacheControl, someDate, size, statusCode);
        List<FileObject> objects = Mapper.toObjects(json);
        assertNotNull(objects);
        assertEquals(expectedObjects, objects);
    }

    @Test
    void throwsWhenMappingBadObjects() {
        String json = """
                {
                    "name": "some-name",
                    "id": "some-id",
                    "updated_at": "some-date",
                    "created_at": "some-date",
                    "last_accessed_at": "some-date",
                    "metadata": null
                }""";
        Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toObjects(json));
        assertTrue(exception.getMessage().contains("Cannot"));
    }

    @Test
    void throwsWhenMappingObjectsWithBadMetadata() {
        String json = """
                [{
                    "name": "some-name",
                    "id": "some-id",
                    "updated_at": "some-date",
                    "created_at": "some-date",
                    "last_accessed_at": "some-date",
                    "metadata": "some-metadata"
                }]""";
        Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toObjects(json));
        assertTrue(exception.getMessage().contains("Cannot"));
        assertTrue(exception.getMessage().contains("metadata"));
    }

    @Test
    void mapsToErrorResponse() {
        String statusCode = "409";
        String error = "Duplicate";
        String message = "The resource already exists";
        String errorJson = """
                {"statusCode":"%s","error":"%s","message":"%s"}""".formatted(statusCode, error, message);
        ErrorResponse errorResponse = Mapper.toErrorResponse(errorJson);
        assertNotNull(errorResponse);
        assertEquals(statusCode, errorResponse.statusCode());
        assertEquals(error, errorResponse.error());
        assertEquals(message, errorResponse.message());
    }

    @Test
    void throwsWhenMappingBadErrorResponse() {
        String errorJson = """
                [{"statusCode":"","error":"","message":""}]""";
        Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toErrorResponse(errorJson));
        assertTrue(exception.getMessage().contains("Cannot"));
    }

    @Test
    void throwsWhenMappingErrorResponseWithAllMissingFields() {
        String errorJson = """
                {"anotherField":""}""";
        Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toErrorResponse(errorJson));
        System.out.println("exception = " + exception);
        assertTrue(exception.getMessage().contains(MISSING_FIELDS_MESSAGE));
    }
}