package dev.alexmiloeski.supabasestorageclient.model.responses;

public record ResponseWrapper<T>(T body, ErrorResponse errorResponse, String exception) {
    public boolean hasBody() {
        return body != null;
    }

    public boolean hasError() {
        return errorResponse != null;
    }

    public boolean hasException() {
        return exception != null;
    }
}
