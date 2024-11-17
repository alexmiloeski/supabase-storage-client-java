package dev.alexmiloeski.supabasestorageclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.options.ListFilesOptions;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;

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

    /**
     * REST GET url/storage/v1/bucket
     * REST response body example:
     * [
     *     {
     *         "id": "test-bucket-1",
     *         "name": "test-bucket-1",
     *         "owner": "",
     *         "public": false,
     *         "file_size_limit": null,
     *         "allowed_mime_types": null,
     *         "created_at": "2024-11-12T19:13:16.984Z",
     *         "updated_at": "2024-11-12T19:13:16.984Z"
     *     },
     *     {
     *         "id": "test-bucket-2",
     *         "name": "test-bucket-2",
     *         "owner": "",
     *         "public": true,
     *         "file_size_limit": 0,
     *         "allowed_mime_types": [
     *             "image/jpeg"
     *         ],
     *         "created_at": "2024-11-15T08:01:18.713Z",
     *         "updated_at": "2024-11-15T08:01:18.713Z"
     *     }
     * ]
     */
    public List<Bucket> listBuckets() {
        String body = newRequest()
                .bucket()
                .make();
        return Mapper.toBuckets(body);
        // todo: if mapper throws, return error response with exception
    }

    /**
     * REST GET url/storage/v1/bucket/some-bucket
     * REST response body example:
     * {
     *     "id": "test-bucket",
     *     "name": "test-bucket",
     *     "owner": "",
     *     "public": false,
     *     "file_size_limit": null,
     *     "allowed_mime_types": null,
     *     "created_at": "2024-11-12T19:13:16.984Z",
     *     "updated_at": "2024-11-12T19:13:16.984Z"
     * }
     */
    public Bucket getBucket(final String bucketId) {
        String body = newRequest()
                .bucket()
                .path(bucketId)
                .make();
        return Mapper.toBucket(body);
        // todo: if mapper throws, return error response with exception
    }

    /**
     * REST POST url/storage/v1/bucket
     * REST request body example:
     * {
     *   "id": "test-bucket",
     *   "name": "test-bucket",
     *   "public": true,
     *   "file_size_limit": 0,
     *   "allowed_mime_types": ["image/jpeg"]
     * }
     * REST response body example:
     * {
     *     "name": "test-bucket"
     * }
     */
    public String createBucket(String id, String name, boolean isPublic,
                               Integer fileSizeLimit, List<String> allowedMimeTypes) {
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
        // todo: if mapper throws, return error response with exception
        System.out.println("bucket = " + bucket);
        return bucket == null ? null : bucket.name();
    }

    public ResponseWrapper createBucketWithWrapper(String id, String name, boolean isPublic,
                                                   Integer fileSizeLimit, List<String> allowedMimeTypes) {
        // POST url/storage/v1/bucket
        Bucket newBucket = new Bucket(
                id, name, null, isPublic, fileSizeLimit, allowedMimeTypes, null, null);
        String json;
        try {
            json = Mapper.mapper.writeValueAsString(newBucket);
            ResponseWrapper rw = newRequest()
                    .bucket()
                    .post(json)
                    .jsonContent()
                    .makeWithWrapper();
            if (rw.body() != null) {
                Bucket bucket = Mapper.toBucket(rw.body());
                return new ResponseWrapper(bucket.name(), null, null);
            }
            return rw;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper(null, null, e.getMessage());
        }
    }

    /**
     * REST DELETE url/storage/v1/bucket/some-bucket
     * REST response body example: {"message":"Successfully deleted"}
     */
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
        // response:
    }

    /**
     * REST POST url/storage/v1/bucket/some-bucket/empty
     * REST response body example: {"message":"Successfully emptied"}
     */
    public String emptyBucket(String id) {
        String body = newRequest()
                .bucket()
                .post()
                .path(id + "/empty")
                .make();
//        Bucket bucket = Mapper.toBucket(body);
//        return bucket == null ? null : bucket.name();
        try {
            HashMap<String, String> resMap = Mapper.mapper.readValue(body, new TypeReference<>() {});
            return resMap.get("message");
        } catch (Exception ignore) {}
        return null;
        // response: {"message":"Successfully emptied"}
    }

    /**
     * REST PUT url/storage/v1/bucket/test-bucket
     * REST request body example:
     *  {
     *   "id": "test-bucket",
     *   "name": "test-bucket",
     *   "public": false,
     *   "file_size_limit": 0,
     *   "allowed_mime_types": ["image/jpeg"]
     * }
     * REST response body example: {"message":"Successfully updated"}
     */
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
        try {
            HashMap<String, String> resMap = Mapper.mapper.readValue(body, new TypeReference<>() {});
            return resMap.get("message");
        } catch (Exception ignore) {}
        return null;
        // response = {"message":"Successfully updated"}
    }

    /**
     * Use this method if you want to list all files in the bucket with the possibility of
     * specifying custom values for any of the following: folder prefix, offset, and limit.
     * If you want to leave any of them to the default value, just pass null for that field.
     * <pre>
     * REST POST url/storage/v1/object/list/test-bucket
     * REST request body example: {"limit":100,"offset":0,"sortBy":{"column":"name","order":"asc"},"prefix":""}
     * REST response body example:
     * [
     *     {
     *         "name": "some-folder",
     *         "id": null,
     *         "updated_at": null,
     *         "created_at": null,
     *         "last_accessed_at": null,
     *         "metadata": null
     *     },
     *     {
     *         "name": "some-image-file",
     *         "id": "b301b87d-3c57-4f40-988f-20d8691382df",
     *         "updated_at": "2024-11-12T19:14:12.167Z",
     *         "created_at": "2024-11-12T19:14:12.167Z",
     *         "last_accessed_at": "2024-11-12T19:14:12.167Z",
     *         "metadata": {
     *             "eTag": "\"88c163864a2335ddbc8d6132a4db382c-1\"",
     *             "size": 41076,
     *             "mimetype": "image/jpeg",
     *             "cacheControl": "max-age=3600",
     *             "lastModified": "2024-11-12T19:14:12.000Z",
     *             "contentLength": 41076,
     *             "httpStatusCode": 200
     *         }
     *     },
     *     {
     *         "name": "some-text-file.txt",
     *         "id": "cf522b79-7cdb-4c08-b454-badd27f0ea86",
     *         "updated_at": "2024-11-12T22:36:52.504Z",
     *         "created_at": "2024-11-12T22:36:52.504Z",
     *         "last_accessed_at": "2024-11-12T22:36:52.504Z",
     *         "metadata": {
     *             "eTag": "\"5f2b51ca2fdc5baa31ec02e002f69aec\"",
     *             "size": 10,
     *             "mimetype": "text/plain",
     *             "cacheControl": "no-cache",
     *             "lastModified": "2024-11-12T22:36:53.000Z",
     *             "contentLength": 10,
     *             "httpStatusCode": 200
     *         }
     *     }
     * ]</pre>
     */
    public List<FileObject> listFilesInBucket(final String bucketId, final ListFilesOptions options) {
        int limit = 100;
        int offset = 0;
        String folderId = "";
        if (options != null) {
            if (options.limit() != null) limit = options.limit();
            if (options.offset() != null) offset = options.offset();
            if (options.folderId() != null) folderId = options.folderId();
        }
        String body = newRequest()
                .object()
                .path("/list/" + bucketId)
                .post("""
                        {"limit":%d,"offset":%d,"sortBy":{"column":"name","order":"asc"},"prefix":"%s"}"""
                        .formatted(limit, offset, folderId))
                .jsonContent()
                .make();
        return Mapper.toObjects(body);
        // todo: if mapper throws, return error response with exception
    }

    /**
     * Use this method if you want to list all files in the bucket with the default values of
     * <br> - no folder prefix
     * <br> - offset 0
     * <br> - limit 100
     * <br><br>For custom options, see {@link #listFilesInBucket(String, ListFilesOptions)}
     */
    public List<FileObject> listFilesInBucket(final String bucketId) {
        return listFilesInBucket(bucketId, null);
    }

    /**
     * REST GET url/storage/v1/object/test-bucket/some-file
     */
    public String downloadFile(final String bucketId, final String fileId) {
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
