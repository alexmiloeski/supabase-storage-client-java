package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperTest {

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
}