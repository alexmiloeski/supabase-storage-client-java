package dev.alexmiloeski.supabasestorageclient;

import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class RequestMaker {
    private static final String STORAGE_PATH = "/storage/v1";
    private static final String BUCKET_PATH = "/bucket";
    private static final String OBJECT_PATH = "/object";

    private final HttpClient client;
    private final String apiUrl;
    private final String apiKey;
    private String resource = "";
    private String path;
    private Methods method = Methods.GET;
    private String contentType;
    private HttpRequest.BodyPublisher body;

    RequestMaker(String apiUrl, String apiKey) {
        this(apiUrl, apiKey, null);
    }

    RequestMaker(String apiUrl, String apiKey, HttpClient httpClient) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        if (httpClient == null) {
            client = HttpClient.newHttpClient();
        } else {
            client = httpClient;
        }
    }

    RequestMaker bucket() {
        this.resource = BUCKET_PATH;
        return this;
    }

    RequestMaker object() {
        this.resource = OBJECT_PATH;
        return this;
    }

    RequestMaker path(String path) {
        this.path = path;
        return this;
    }

    RequestMaker post() {
        return post(null);
    }

    RequestMaker post(Object body) {
        this.method = Methods.POST;
        // note: can't use enhanced switch with pattern matching in Java 17
        if (body == null) {
            this.body = HttpRequest.BodyPublishers.noBody();
        } else if (body instanceof String sBody) {
            this.body = HttpRequest.BodyPublishers.ofString(sBody);
        } else if (body instanceof byte[] baBody) {
            this.body = HttpRequest.BodyPublishers.ofByteArray(baBody);
        } else {
            throw new IllegalArgumentException("POST body can only be String or byte array");
        }
        return this;
    }

    RequestMaker put(Object body) {
        this.method = Methods.PUT;
        // note: can't use enhanced switch with pattern matching in Java 17
        if (body == null) {
            this.body = HttpRequest.BodyPublishers.noBody();
        } else if (body instanceof String sBody) {
            this.body = HttpRequest.BodyPublishers.ofString(sBody);
        } else if (body instanceof byte[] baBody) {
            this.body = HttpRequest.BodyPublishers.ofByteArray(baBody);
        } else {
            throw new IllegalArgumentException("POST body can only be String or byte array");
        }
        return this;
    }

    RequestMaker delete() {
        this.method = Methods.DELETE;
        return this;
    }

    RequestMaker jsonContent() {
        return contentType("application/json");
    }

    RequestMaker contentType(String mimeType) {
        this.contentType = mimeType;
        return this;
    }

    String make() {
        final String _path = path == null ? "" : "/" + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + STORAGE_PATH + resource + _path));
        if (apiKey != null) {
            builder = builder.header("Authorization", "Bearer " + apiKey);
        }
        if (contentType != null) {
            builder = builder.header("Content-Type", contentType);
        }
        builder = switch (method) {
            default -> builder.GET();
            case POST -> builder.POST(body);
            case PUT -> builder.PUT(body);
            case DELETE -> builder.DELETE();
        };

        HttpRequest request = builder.build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            System.out.println("IOException YO!");
        }
        return null;
    }

    ResponseWrapper<String> makeWithWrapper() {
        final String _path = path == null ? "" : "/" + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + STORAGE_PATH + resource + _path));
        if (apiKey != null) {
            builder = builder.header("Authorization", "Bearer " + apiKey);
        }
        if (contentType != null) {
            builder = builder.header("Content-Type", contentType);
        }
        builder = switch (method) {
            default -> builder.GET();
            case POST -> builder.POST(body);
            case PUT -> builder.PUT(body);
            case DELETE -> builder.DELETE();
        };

        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return new ResponseWrapper<>(null, Mapper.toErrorResponse(response.body()), null);
            }
            return new ResponseWrapper<>(response.body(), null, null);
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            System.out.println("IOException YO!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
    }

    private enum Methods {
        GET, POST, PUT, DELETE
    }
}
