package dev.alexmiloeski.supabasestorageclient.model.responses;

public record ResponseWrapper(Object body, ErrorResponse errorResponse, String exception) {
}
