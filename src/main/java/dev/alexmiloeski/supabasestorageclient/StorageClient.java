package dev.alexmiloeski.supabasestorageclient;

public class StorageClient {
    private final String apiUrl;
    private final String apiKey;

    public StorageClient(String projectId, String apiKey) {
        this.apiUrl = "https://" + projectId + ".supabase.co";
        this.apiKey = apiKey;
    }
}
