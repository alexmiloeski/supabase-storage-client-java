package dev.alexmiloeski.supabasestorageclient.model.responses;

public record ResponseWrapper<T>(T body, ErrorResponse errorResponse, String exception) {
}
