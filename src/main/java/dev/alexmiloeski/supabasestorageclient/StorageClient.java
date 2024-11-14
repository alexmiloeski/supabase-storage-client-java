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

    public String createBucket(String id, String name, boolean isPublic,
                                     Integer fileSizeLimit, List<String> allowedMimeTypes) {
        // POST url/storage/v1/bucket
        Bucket newBucket = new Bucket(
                id, name, null, isPublic, fileSizeLimit, allowedMimeTypes, null, null);
        String json;
        try {
            json = Mapper.mapper.writeValueAsString(newBucket);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return null;
        }
        String body = newRequest()
                .bucket()
                .post(json)
                .jsonContent()
                .make();
        Bucket bucket = Mapper.toBucket(body);
        System.out.println("bucket = " + bucket);
        return bucket == null ? null : bucket.name();
    }

    public String deleteBucket(String id) {
        String body = newRequest()
                .bucket()
                .delete()
                .path(id)
                .make();
//        Bucket bucket = Mapper.toBucket(body);
//        return bucket == null ? null : bucket.name();
        try {
            HashMap<String, String> resMap = Mapper.mapper.readValue(body, new TypeReference<>() {});
            return resMap.get("message");
        } catch (Exception ignore) {}
        return null;
        // response: {"message":"Successfully deleted"}
    }

    public String emptyBucket(String id) {
        String body = newRequest()
                .bucket()
                .post()
                .path(id + "/empty")
                .make();
//        Bucket bucket = Mapper.toBucket(body);
//        return bucket == null ? null : bucket.name();
        // response: {"message":"Successfully emptied"}
        return body;
    }

    public String updateBucket(String id, boolean isPublic, Integer fileSizeLimit, List<String> allowedMimeTypes) {
        // POST url/storage/v1/bucket
        Bucket newBucket = new Bucket(
                null, null, null, isPublic, fileSizeLimit, allowedMimeTypes, null, null);
        String json;
        try {
            json = Mapper.mapper.writeValueAsString(newBucket);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return null;
        }
        System.out.println("json = " + json);
        String body = newRequest()
                .bucket()
                .put(json)
                .jsonContent()
                .path(id)
                .make();
//        Bucket bucket = Mapper.toBucket(body);
//        return bucket == null ? null : bucket.name();
        // response = {"message":"Successfully updated"}
        return body;
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
