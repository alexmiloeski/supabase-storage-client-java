package dev.alexmiloeski.supabasestorageclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;
import dev.alexmiloeski.supabasestorageclient.model.responses.ErrorResponse;
import dev.alexmiloeski.supabasestorageclient.model.responses.FileObjectIdentity;

import java.util.List;

class Mapper {
    static final ObjectMapper mapper = new ObjectMapper();

    // todo: replace RuntimeException with MapperException

    // todo: there's a lot of repeated code that can be extracted

    private Mapper() {}

    static Bucket toBucket(String json) {
        if (json == null) throw new RuntimeException("received json was null");
        try {
            return mapper.readValue(json, Bucket.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("COULDN'T MAP YO!");
            throw new RuntimeException(e.getMessage());
        }
    }

    static List<Bucket> toBuckets(String json) {
        if (json == null) return null;
        try {
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("COULDN'T MAP YO!");
            throw new RuntimeException(e.getMessage());
        }
    }

    static List<FileObject> toObjects(String json) {
        if (json == null) return null;
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("COULDN'T MAP YO!");
            throw new RuntimeException(e.getMessage());
        }
    }

    static ErrorResponse toErrorResponse(String json) {
        if (json == null) return null;
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("COULDN'T MAP YO!");
            throw new RuntimeException(e.getMessage());
        }
    }

    static FileObjectIdentity toIdentity(String json) {
        if (json == null) return null;
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("COULDN'T MAP YO!");
            throw new RuntimeException(e);
        }
    }
}
