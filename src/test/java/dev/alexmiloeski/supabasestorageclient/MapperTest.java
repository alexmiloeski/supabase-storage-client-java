package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import dev.alexmiloeski.supabasestorageclient.model.responses.FileObjectIdentity;
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
        final String json = """
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
        final String json = """
                {
                  "id": "",
                  "name": "",
                  "public": true,
                  "file_size_limit": ".",
                  "allowed_mime_types": ""
                }""";
        final Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toBucket(json));
        assertTrue(exception.getMessage().contains("Cannot"));
    }

    @Test
    void mapsToBuckets() {
        final List<Bucket> bucket = Mapper.toBuckets(LIST_BUCKETS_JSON_RESPONSE);
        assertNotNull(bucket);
        assertEquals(EXPECTED_BUCKETS, bucket);
    }

    @Test
    void throwsWhenMappingBadBuckets() {
        final String json = """
                [{
                  "id": "",
                  "name": "",
                  "public": true,
                  "file_size_limit": ".",
                  "allowed_mime_types": ""
                }]""";
        final Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toBuckets(json));
        assertTrue(exception.getMessage().contains("Cannot"));
    }

    @Test
    void mapsToObjects() {
        final List<FileObject> objects = Mapper.toObjects(LIST_FILES_JSON_RESPONSE);
        assertNotNull(objects);
        assertEquals(EXPECTED_LIST_FILES_OBJECTS, objects);
    }

    @Test
    void throwsWhenMappingBadObjects() {
        final String json = """
                {
                    "name": "some-name",
                    "id": "some-id",
                    "updated_at": "some-date",
                    "created_at": "some-date",
                    "last_accessed_at": "some-date",
                    "metadata": null
                }""";
        final Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toObjects(json));
        assertTrue(exception.getMessage().contains("Cannot"));
    }

    @Test
    void throwsWhenMappingObjectsWithBadMetadata() {
        final String json = """
                [{
                    "name": "some-name",
                    "id": "some-id",
                    "updated_at": "some-date",
                    "created_at": "some-date",
                    "last_accessed_at": "some-date",
                    "metadata": "some-metadata"
                }]""";
        final Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toObjects(json));
        assertTrue(exception.getMessage().contains("Cannot"));
        assertTrue(exception.getMessage().contains("metadata"));
    }

    @Test
    void mapsToErrorResponse() {
        final String statusCode = "409";
        final String error = "Duplicate";
        final String message = "The resource already exists";
        final String errorJson = """
                {"statusCode":"%s","error":"%s","message":"%s"}""".formatted(statusCode, error, message);
        final ErrorResponse errorResponse = Mapper.toErrorResponse(errorJson);
        assertNotNull(errorResponse);
        assertEquals(statusCode, errorResponse.statusCode());
        assertEquals(error, errorResponse.error());
        assertEquals(message, errorResponse.message());
    }

    @Test
    void throwsWhenMappingBadErrorResponse() {
        final String errorJson = """
                [{"statusCode":"","error":"","message":""}]""";
        final Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toErrorResponse(errorJson));
        assertTrue(exception.getMessage().contains("Cannot"));
    }

    @Test
    void throwsWhenMappingErrorResponseWithAllMissingFields() {
        final String errorJson = """
                {"anotherField":""}""";
        final Exception exception = assertThrows(RuntimeException.class, () -> Mapper.toErrorResponse(errorJson));
        assertTrue(exception.getMessage().contains(MISSING_FIELDS_MESSAGE));
    }

    @Test
    void mapsToFileObjectIdentity() {
        final String key = "test-bucket/test-file.txt";
        final String id = "2650a5da-be5d-49f0-919f-28ca78bffb99";
        final String json = """
                {
                    "Key": "%s",
                    "Id": "%s"
                }""".formatted(key, id);
        final FileObjectIdentity expected = new FileObjectIdentity(key, id);

        FileObjectIdentity actual = Mapper.toIdentity(json);
        assertEquals(expected, actual);
    }
}
