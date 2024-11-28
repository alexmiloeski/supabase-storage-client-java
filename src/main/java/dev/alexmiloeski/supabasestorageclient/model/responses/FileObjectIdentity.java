package dev.alexmiloeski.supabasestorageclient.model.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FileObjectIdentity(@JsonProperty("Key") String key, @JsonProperty("Id") String id) {
    public static final String BAD_ID_MESSAGE = "ID does not conform to the UUID format";
    @JsonCreator
    public FileObjectIdentity {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ignore) {
            throw new IllegalArgumentException(BAD_ID_MESSAGE);
        }
    }
}
