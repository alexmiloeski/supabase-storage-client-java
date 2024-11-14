package dev.alexmiloeski.supabasestorageclient;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;

import java.util.HashMap;
import java.util.List;

public class StorageClient {
    private final String apiUrl;
    private final String apiKey;

    public StorageClient(String projectId, String apiKey) {
        this.apiUrl = "https://" + projectId + ".supabase.co";
        this.apiKey = apiKey;
    }

    public boolean isHealthy() {
        // GET url/storage/v1/health
        String body = newRequest()
                .path("health")
                .make();
        try {
            HashMap<String, Object> resMap = Mapper.mapper.readValue(body, new TypeReference<>() {});
            Object healthyO = resMap.get("healthy");
            if (healthyO instanceof Boolean) {
                return (boolean) healthyO;
            }
        } catch (Exception ignore) {}
        return false;
        //{"healthy":true}
    }

    public List<Bucket> listBuckets() {
        // GET url/storage/v1/bucket
        String body = newRequest()
                .bucket()
                .make();
        return Mapper.toBuckets(body);
    }

    public Bucket getBucket(final String bucketId) {
        // GET url/storage/v1/bucket/some-bucket
        String body = newRequest()
                .bucket()
                .path(bucketId)
                .make();
        return Mapper.toBucket(body);
    }

    public List<FileObject> listFilesInBucket(final String bucketId, final String folderId) {
        // POST url/storage/v1/object/list/test-bucket-1
        String body = newRequest()
                .object()
                .path("/list/" + bucketId)
                .post("""
                        {"limit":100,"offset":0,"sortBy":{"column":"name","order":"asc"},"prefix":"%s"}"""
                        .formatted(folderId))
                .jsonContent()
                .make();
        return Mapper.toObjects(body);
    }

    public List<FileObject> listFilesInBucket(final String bucketId) {
        return listFilesInBucket(bucketId, "");
    }

    public String downloadFile(final String bucketId, final String fileId) {
        // GET url/storage/v1/object/some-bucket/some-file
        return newRequest()
                .object()
                .path(bucketId + "/" + fileId)
                .make();
//        return Mapper.toBucket(response.body());
    }

    private RequestMaker newRequest() {
        return new RequestMaker(apiUrl, apiKey);
    }
}
