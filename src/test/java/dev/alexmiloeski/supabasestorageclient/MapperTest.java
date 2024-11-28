package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.alexmiloeski.supabasestorageclient.Arrange.*;
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
        List<Bucket> bucket = Mapper.toBuckets(LIST_BUCKETS_JSON_RESPONSE);
        assertNotNull(bucket);
        assertEquals(EXPECTED_BUCKETS, bucket);
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
        List<FileObject> objects = Mapper.toObjects(LIST_FILES_JSON_RESPONSE);
        assertNotNull(objects);
        assertEquals(EXPECTED_LIST_FILES_OBJECTS, objects);
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