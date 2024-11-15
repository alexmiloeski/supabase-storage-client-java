package dev.alexmiloeski.supabasestorageclient.model.responses;

public record ResponseWrapper(String body, ErrorResponse errorResponse, String exception) {
}
