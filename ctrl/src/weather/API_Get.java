package weather;

/**
 *
 * @author ekitstrap
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class API_Get {

    /**
     * Sends a GET request to the specified API URL.
     *
     * @param apiURL the URL to send the GET request to
     * @return the response body as a String
     * @throws Exception if the request fails
     */
    public String get(String apiURL) throws Exception {
        URL url = new URL(apiURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set HTTP method and headers
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        // Check for successful response
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("GET failed. HTTP error code: " + conn.getResponseCode());
        }

        // Read response
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }

        conn.disconnect();
        return sb.toString();
    }

    public static String translateForecast(String malayForecast) {
        if (malayForecast == null) {
            return "Unknown";
        }

        malayForecast = malayForecast.toLowerCase();

        if (malayForecast.contains("tiada hujan")) {
            return "No rain";
        } else if (malayForecast.contains("hujan lebat")) {
            return "Heavy rain";
        } else if (malayForecast.contains("hujan renyai")) {
            return "Drizzle";
        } else if (malayForecast.contains("hujan")) {
            return "Rain";
        } else if (malayForecast.contains("ribut petir")) {
            return "Thunderstorms";
        } else if (malayForecast.contains("mendung")) {
            return "Cloudy";
        } else if (malayForecast.contains("cerah sebahagian")) {
            return "Partly cloudy";
        } else if (malayForecast.contains("cerah")) {
            return "Sunny";
        } else {
            return "Unknown";
        }
    }

    // --- NEW: Static Helper to fetch current weather for the journal ---
    public static String getCurrentWeather() {
        API_Get api = new API_Get();
        try {
            // 1. URL for Kuala Lumpur
            String getUrl = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
            
            // 2. Fetch Data
            String response = api.get(getUrl);
            
            // 3. Parse JSON
            JSONArray jsonArray = new JSONArray(response);
            JSONObject firstItem = jsonArray.getJSONObject(0);
            String summary = firstItem.getString("summary_forecast");
            
            // 4. Translate and Return (e.g., "Sunny")
            return translateForecast(summary);
            
        } catch (Exception e) {
            System.out.println("Weather fetch failed: " + e.getMessage());
            return "Unknown"; // Fallback if internet fails
        }
    }

}
