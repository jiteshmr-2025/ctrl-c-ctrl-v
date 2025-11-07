package mood;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import utils.EnvLoader;

public class API_Post {

    /**
     * Sends a POST request with JSON body and Bearer token authentication.
     *
     * @param apiURL      the URL to send the POST request to
     * @param bearerToken the bearer token for Authorization header
     * @param jsonBody    the JSON payload as a string
     * @return the response body as a String
     * @throws Exception if the request fails
     */
    public String post(String apiURL, String bearerToken, String jsonBody) throws Exception {
        URL url = new URL(apiURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set HTTP method and headers
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + bearerToken);

        // Enable sending body
        conn.setDoOutput(true);

        // Write request body
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Check for success
        int responseCode = conn.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            throw new RuntimeException("POST failed. HTTP error code: " + responseCode);
        }

        // Read response
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        StringBuilder sb = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            sb.append(responseLine.trim());
        }

        conn.disconnect();
        return sb.toString();
    }

    // Example usage
    public static void main(String[] args) {
        API_Post api = new API_Post();

        // Load environment variables from .env file (custom loader)
        Map<String, String> env = EnvLoader.loadEnv(".env");

        try {
            // --- Example POST request: Perform sentiment analysis using HuggingFace model ---
            String journalInput = "I spent my free time with my friends today. We had a great time at the park and enjoyed the sunny weather.";
            String postUrl = "https://router.huggingface.co/hf-inference/models/distilbert/distilbert-base-uncased-finetuned-sst-2-english";

            // Safely get bearer token
            String bearerToken = env.get("BEARER_TOKEN");
            if (bearerToken == null || bearerToken.isEmpty()) {
                System.err.println("Error: BEARER_TOKEN is not set in the environment.");
                return;
            }

            // Format JSON body
            String jsonBody = "{\"inputs\": \"" + journalInput + "\"}";

            // Call POST
            String postResponse = api.post(postUrl, bearerToken, jsonBody);
            System.out.println("\nSentiment Analysis Response:\n" + postResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
