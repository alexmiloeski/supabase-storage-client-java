package dev.alexmiloeski.supabasestorageclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FileObject(String id, String name,
                         @JsonProperty(value = "created_at") String createdAt,
                         @JsonProperty(value = "updated_at") String updatedAt,
                         @JsonProperty(value = "last_accessed_at") String lastAccessedAt,
                         Metadata metadata) {
    private record Metadata(String eTag, long size, String mimetype, String cacheControl,
                            String lastModified, long contentLength, int httpStatusCode) {}
}
/*
[
    {
        "name": "folder1",
        "id": null,
        "updated_at": null,
        "created_at": null,
        "last_accessed_at": null,
        "metadata": null
    },
    {
        "name": "Alek_headshot_1.jpg",
        "id": "b301b87d-3c57-4f40-988f-20d8691382df",
        "updated_at": "2024-11-12T19:14:12.167Z",
        "created_at": "2024-11-12T19:14:12.167Z",
        "last_accessed_at": "2024-11-12T19:14:12.167Z",
        "metadata": {
            "eTag": "\"88c163864a2335ddbc8d6132a4db382c-1\"",
            "size": 41076,
            "mimetype": "image/jpeg",
            "cacheControl": "max-age=3600",
            "lastModified": "2024-11-12T19:14:12.000Z",
            "contentLength": 41076,
            "httpStatusCode": 200
        }
    },
    {
        "name": "tax-screenshot-1",
        "id": "a4074e51-6975-4ffd-9e72-de1e650b7487",
        "updated_at": "2024-11-12T22:32:25.541Z",
        "created_at": "2024-11-12T22:32:25.541Z",
        "last_accessed_at": "2024-11-12T22:32:25.541Z",
        "metadata": {
            "eTag": "\"5ac4d3d5f7c1b368acec944fa5ecab44\"",
            "size": 137880,
            "mimetype": "text/plain;charset=UTF-8",
            "cacheControl": "max-age=3600",
            "lastModified": "2024-11-12T22:32:26.000Z",
            "contentLength": 137880,
            "httpStatusCode": 200
        }
    }
]
* */