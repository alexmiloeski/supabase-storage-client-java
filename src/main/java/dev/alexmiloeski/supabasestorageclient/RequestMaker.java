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

    private final String apiUrl;
    private String resource = "";
    private String path;
    private String apiKey;
    private Methods method = Methods.GET;
    private String contentType;
    private HttpRequest.BodyPublisher body;

    RequestMaker(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    RequestMaker(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
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

    RequestMaker post(String body) {
        this.method = Methods.POST;
        this.body = body == null ? HttpRequest.BodyPublishers.noBody() :  HttpRequest.BodyPublishers.ofString(body);
        return this;
    }

    RequestMaker put(String body) {
        this.method = Methods.PUT;
        this.body = body == null ? HttpRequest.BodyPublishers.noBody() :  HttpRequest.BodyPublishers.ofString(body);
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
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            System.out.println("IOException YO!");
        }
        return null;
    }

    ResponseWrapper makeWithWrapper() {
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
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return new ResponseWrapper(null, Mapper.toErrorResponse(response.body()), null);
            }
            return new ResponseWrapper(response.body(), null, null);
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            System.out.println("IOException YO!");
            return new ResponseWrapper(null, null, e.getMessage());
        }
    }

    private enum Methods {
        GET, POST, PUT, DELETE
    }
}
