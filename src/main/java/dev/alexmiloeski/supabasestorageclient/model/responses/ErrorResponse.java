package dev.alexmiloeski.supabasestorageclient.model.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(String statusCode, String error, String message) {}
