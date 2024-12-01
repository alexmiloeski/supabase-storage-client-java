package dev.alexmiloeski.supabasestorageclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FileObjectInfo(String id, String name, String version, long size,
                             @JsonProperty(value = "content_type") String contentType,
                             @JsonProperty(value = "cache_control") String cacheControl,
                             String eTag,
                             // metadata? // their api is not properly documented,
                             //     this is reverse engineering at this point
                             @JsonProperty(value = "created_at") String createdAt) {
}
/*{
    "id": "1844b6b8-083c-47d5-b818-85fc91decfe8",
    "name": "folder1/test-file-4.txt",
    "version": "fdf2ffd3-dbde-40d2-b48c-8ed263fd4681",
    "size": 10,
    "content_type": "text/plain",
    "cache_control": "no-cache",
    "etag": "\"5f2b51ca2fdc5baa31ec02e002f69aec\"",
    "metadata": {},
    "created_at": "2024-12-01T13:58:02.435Z"
}
* */
