package dev.alexmiloeski.supabasestorageclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.options.FileMoveOptions;
import dev.alexmiloeski.supabasestorageclient.model.options.ListFilesOptions;
import dev.alexmiloeski.supabasestorageclient.model.responses.FileObjectIdentity;
import dev.alexmiloeski.supabasestorageclient.model.responses.ResponseWrapper;

import java.util.HashMap;
import java.util.List;

public class StorageClient {
    protected final String apiUrl;
    protected final String apiKey;

    public StorageClient(String projectId, String apiKey) {
        this.apiUrl = "https://" + projectId + ".supabase.co";
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
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
    public ResponseWrapper<List<Bucket>> listBucketsWithWrapper() {
        ResponseWrapper<String> rw = newRequest()
                .bucket()
                .makeWithWrapper();
        try {
            if (rw.body() != null) {
                List<Bucket> buckets = Mapper.toBuckets(rw.body());
                return new ResponseWrapper<>(buckets, null, null);
            }
            return new ResponseWrapper<>(null, rw.errorResponse(), null);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
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
    public ResponseWrapper<Bucket> getBucketWithWrapper(final String bucketId) {
        try {
            ResponseWrapper<String> rw = newRequest()
                    .bucket()
                    .path(bucketId)
                    .makeWithWrapper();
            if (rw.body() != null) {
                Bucket bucket = Mapper.toBucket(rw.body());
                return new ResponseWrapper<>(bucket, null, null);
            }
            return new ResponseWrapper<>(null, rw.errorResponse(), null);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
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
            // todo: put the mapper functions (using the Mapper) inside the DTO classes
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
    public ResponseWrapper<String> createBucketWithWrapper(String id, String name, boolean isPublic,
                                                   Integer fileSizeLimit, List<String> allowedMimeTypes) {
        // POST url/storage/v1/bucket
        Bucket newBucket = new Bucket(
                id, name, null, isPublic, fileSizeLimit, allowedMimeTypes, null, null);
        String json;
        try {
            json = Mapper.mapper.writeValueAsString(newBucket);
            ResponseWrapper<String> rw = newRequest()
                    .bucket()
                    .post(json)
                    .jsonContent()
                    .makeWithWrapper();
            if (rw.body() != null) {
                Bucket bucket = Mapper.toBucket(rw.body());
                return new ResponseWrapper<>(bucket.name(), null, null);
            }
            return rw;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
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
     * REST DELETE url/storage/v1/bucket/some-bucket
     * REST response body example: {"message":"Successfully deleted"}
     */
    public ResponseWrapper<String> deleteBucketWithWrapper(String id) {
        ResponseWrapper<String> rw = newRequest()
                .bucket()
                .delete()
                .path(id)
                .makeWithWrapper();
        try {
            if (rw.body() != null) {
                HashMap<String, String> resMap = Mapper.mapper.readValue(rw.body(), new TypeReference<>() {});
                return new ResponseWrapper<>(resMap.get("message"), null, null);
            }
            return rw;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
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
     * REST POST url/storage/v1/bucket/some-bucket/empty
     * REST response body example: {"message":"Successfully emptied"}
     */
    public ResponseWrapper<String> emptyBucketWithWrapper(String id) {
        ResponseWrapper<String> rw = newRequest()
                .bucket()
                .post()
                .path(id + "/empty")
                .makeWithWrapper();
        try {
            if (rw.body() != null) {
                HashMap<String, String> resMap = Mapper.mapper.readValue(rw.body(), new TypeReference<>() {});
                return new ResponseWrapper<>(resMap.get("message"), null, null);
            }
            return rw;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
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
    public ResponseWrapper<String> updateBucketWithWrapper(
            String id, String owner, boolean isPublic, Integer fileSizeLimit, List<String> allowedMimeTypes
    ) {
        Bucket newBucket = new Bucket(
                null, null, owner, isPublic, fileSizeLimit, allowedMimeTypes, null, null);
        String json;
        try {
            json = Mapper.mapper.writeValueAsString(newBucket);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
        ResponseWrapper<String> rw = newRequest()
                .bucket()
                .put(json)
                .jsonContent()
                .path(id)
                .makeWithWrapper();
        try {
            if (rw.body() != null) {
                HashMap<String, String> resMap = Mapper.mapper.readValue(rw.body(), new TypeReference<>() {});
                return new ResponseWrapper<>(resMap.get("message"), null, null);
            }
            return rw;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
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
    public ResponseWrapper<List<FileObject>> listFilesInBucket(final String bucketId, final ListFilesOptions options) {
        int limit = 100;
        int offset = 0;
        String folderId = "";
        if (options != null) {
            if (options.limit() != null) limit = options.limit();
            if (options.offset() != null) offset = options.offset();
            if (options.folderId() != null) folderId = options.folderId();
        }
        ResponseWrapper<String> rw = newRequest()
                .object()
                .path("list/" + bucketId)
                .post("""
                        {"limit":%d,"offset":%d,"sortBy":{"column":"name","order":"asc"},"prefix":"%s"}"""
                        .formatted(limit, offset, folderId))
                .jsonContent()
                .makeWithWrapper();
        try {
            if (rw.body() != null) {
                List<FileObject> objects = Mapper.toObjects(rw.body());
                return new ResponseWrapper<>(objects, null, null);
            }
            return new ResponseWrapper<>(null, rw.errorResponse(), null);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
    }

    /**
     * Use this method if you want to list all files in the bucket with the default values of
     * <br> - no folder prefix
     * <br> - offset 0
     * <br> - limit 100
     * <br><br>For custom options, see {@link #listFilesInBucket(String, ListFilesOptions)}
     */
    public ResponseWrapper<List<FileObject>> listFilesInBucket(final String bucketId) {
        return listFilesInBucket(bucketId, null);
    }

    /**
     * REST GET url/storage/v1/object/test-bucket/some-file
     * REST error response body for wrong file name:
     * {
     *     "statusCode": "404",
     *     "error": "not_found",
     *     "message": "Object not found"
     * }
     * REST error response body for wrong bucket id:
     * {
     *     "statusCode": "404",
     *     "error": "Bucket not found",
     *     "message": "Bucket not found"
     * }
     */
    public ResponseWrapper<String> downloadFile(final String bucketId, final String fileId) {
        return newRequest()
                .object()
                .path(bucketId + "/" + fileId)
                .makeWithWrapper();
    }

    public ResponseWrapper<byte[]> downloadFileBytes(final String bucketId, final String fileId) {
        ResponseWrapper<String> rw = downloadFile(bucketId, fileId);
        if (rw.hasBody()) {
            return new ResponseWrapper<>(rw.body().getBytes(), null, null);
        }
        return new ResponseWrapper<>(null, rw.errorResponse(), rw.exception());
    }

    /**
     * REST POST url/storage/v1/object/test-bucket/some-file
     * REST response body example:
     * {"Key":"test-bucket/some-file","Id":"f1c8e70a-95f8-47df-9122-d3f152f95f70"}
     * REST error response body for duplicate file name:
     * {
     *     "statusCode": "409",
     *     "error": "Duplicate",
     *     "message": "The resource already exists"
     * }
     * REST error response body example for wrong bucket id:
     * {
     *     "statusCode": "404",
     *     "error": "Bucket not found",
     *     "message": "Bucket not found"
     * }
     */
    public ResponseWrapper<FileObjectIdentity> uploadFile(
            final String bucketId, final String fileId, byte[] bytes
    ) {
        ResponseWrapper<String> rw = newRequest()
                .object()
                .path(bucketId + "/" + fileId)
                .post(bytes)
                .makeWithWrapper();
        try {
            if (rw.body() != null) {
                FileObjectIdentity identity = Mapper.toIdentity(rw.body());
                return new ResponseWrapper<>(identity, null, null);
            }
            return new ResponseWrapper<>(null, rw.errorResponse(), null);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
    }

    /**
     * REST DELETE url/storage/v1/object/test-bucket/some-file
     * REST response body: {"message": "Successfully deleted"}
     * REST error response body for nonexistent file name:
     * {
     *   "statusCode": "404",
     *   "error": "not_found",
     *   "message": "Object not found"
     * }
     */
    public ResponseWrapper<String> deleteFile(final String bucketId, final String fileId) {
        ResponseWrapper<String> rw = newRequest()
                .object()
                .path(bucketId + "/" + fileId)
                .delete()
                .makeWithWrapper();
        try {
            if (rw.body() != null) {
                HashMap<String, String> resMap = Mapper.mapper.readValue(rw.body(), new TypeReference<>() {});
                return new ResponseWrapper<>(resMap.get("message"), null, null);
                // todo: might wanna replace with record with message only
            }
            return rw;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
    }

    /**
     * REST PUT url/storage/v1/object/test-bucket/some-file
     * REST response body example:
     * {"Key":"test-bucket/some-file","Id":"f1c8e70a-95f8-47df-9122-d3f152f95f70"}
     * REST error response body example for wrong bucket id:
     * {
     *     "statusCode": "404",
     *     "error": "Bucket not found",
     *     "message": "Bucket not found"
     * }
     * REST error response body example for wrong file name:
     * {
     *     "statusCode": "404",
     *     "error": "not_found",
     *     "message": "Object not found"
     * }
     */
    public ResponseWrapper<FileObjectIdentity> updateFile(
            final String bucketId, final String fileId, byte[] bytes
    ) {
        ResponseWrapper<String> rw = newRequest()
                .object()
                .path(bucketId + "/" + fileId)
                .put(bytes)
                .makeWithWrapper();
        try {
            if (rw.body() != null) {
                FileObjectIdentity identity = Mapper.toIdentity(rw.body());
                return new ResponseWrapper<>(identity, null, null);
            }
            return new ResponseWrapper<>(null, rw.errorResponse(), null);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
    }

    /**
     * REST POST url/storage/v1/object/move
     * REST response body: {"message": "Successfully moved"}
     * REST error response body example for wrong file name:
     * {
     *     "statusCode": "404",
     *     "error": "not_found",
     *     "message": "Object not found"
     * }
     */
    public ResponseWrapper<String> moveFile(FileMoveOptions moveOptions) {
        try {
            String json = moveOptions.toJson();
            ResponseWrapper<String> rw = newRequest()
                    .object()
                    .path("move")
                    .post(json)
                    .jsonContent()
                    .makeWithWrapper();
            if (rw.body() != null) {
                HashMap<String, String> resMap = Mapper.mapper.readValue(rw.body(), new TypeReference<>() {});
                return new ResponseWrapper<>(resMap.get("message"), null, null);
                // todo: might wanna replace with record with message only
            }
            return rw;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("EXCEPTION CONVERTING TO JSON!");
            return new ResponseWrapper<>(null, null, e.getMessage());
        }
    }

    protected RequestMaker newRequest() {
        return new RequestMaker(apiUrl, apiKey);
    }
}
