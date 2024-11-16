package dev.alexmiloeski.supabasestorageclient.model.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(String statusCode, String error, String message) {
    public static final String MISSING_FIELDS_MESSAGE = "Missing required fields";
    @JsonCreator
    public ErrorResponse {
        if (statusCode == null && error == null && message == null) {
            throw new IllegalArgumentException(MISSING_FIELDS_MESSAGE);
        }
    }
}
