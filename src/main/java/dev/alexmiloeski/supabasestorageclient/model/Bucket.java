package dev.alexmiloeski.supabasestorageclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Bucket(String id, String name, String owner,
                     @JsonProperty(value = "public") boolean isPublic,
                     @JsonProperty(value = "file_size_limit") Integer fileSizeLimit,
                     @JsonProperty(value = "allowed_mime_types") String allowedMimeTypes,
                     @JsonProperty(value = "created_at") String createdAt,
                     @JsonProperty(value = "updated_at") String updatedAt) {
}
//[{"id":"test-bucket-1","name":"test-bucket-1","owner":"","public":false,"file_size_limit":null,"allowed_mime_types":null,"created_at":"2024-11-12T19:13:16.984Z","updated_at":"2024-11-12T19:13:16.984Z"}]