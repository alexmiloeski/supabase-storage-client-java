package dev.alexmiloeski.supabasestorageclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.alexmiloeski.supabasestorageclient.model.Bucket;
import dev.alexmiloeski.supabasestorageclient.model.FileObject;

import java.util.List;

class Mapper {
    static final ObjectMapper mapper = new ObjectMapper();

    private Mapper() {}

    static Bucket toBucket(String string) {
        if (string == null) return null;
        try {
            return mapper.readValue(string, Bucket.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("COULDN'T MAP YO!");
            return null;
        }
    }

    static List<Bucket> toBuckets(String string) {
        if (string == null) return null;
        try {
            return mapper.readValue(string, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("COULDN'T MAP YO!");
            return null;
        }
    }

    static List<FileObject> toObjects(String string) {
        if (string == null) return null;
        try {
            return mapper.readValue(string, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("COULDN'T MAP YO!");
            return null;
        }
    }
}
