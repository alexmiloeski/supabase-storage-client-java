package dev.alexmiloeski.supabasestorageclient.model.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
<pre>{
    "bucketId": "test-bucket-5-id",
    "sourceKey": "folder2/empty-file-1",
    "destinationBucket": "test-bucket-1",
    "destinationKey": "folder1/empty-file-1"
}</pre>
 */
public record FileMoveOptions(@JsonProperty("bucketId") String sourceBucketId,
                              @JsonProperty("sourceKey") String sourceFilePath,
                              @JsonProperty("destinationBucket") String destinationBucketId,
                              @JsonProperty("destinationKey") String destinationFilePath) {
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
