package utils;

import weather.API_Get;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherBackgroundManager {

    // --- CACHING VARIABLES ---
    private static String cachedWeather = null;
    private static long lastFetchTime = 0;
    private static final long CACHE_DURATION = 15 * 60 * 1000; // 15 Minutes in milliseconds

    public static String getCurrentWeather() {
        // 1. Check if we have a valid cache
        long currentTime = System.currentTimeMillis();
        if (cachedWeather != null && (currentTime - lastFetchTime < CACHE_DURATION)) {
            System.out.println("Using Cached Weather: " + cachedWeather);
            return cachedWeather;
        }

        // 2. If no cache, fetch from API
        API_Get api = new API_Get();
        try {
            System.out.println("Fetching fresh weather from API...");
            String getUrl = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
            String getResponse = api.get(getUrl);
            
            JSONArray jsonArray = new JSONArray(getResponse);
            JSONObject firstItem = jsonArray.getJSONObject(0);
            String summary = firstItem.getString("summary_forecast");
            
            // 3. Save result to cache
            cachedWeather = API_Get.translateForecast(summary);
            lastFetchTime = currentTime;
            
            return cachedWeather;

        } catch (Exception e) {
            e.printStackTrace();
            // If API fails (e.g., Error 429), return the last known good weather, or default
            return (cachedWeather != null) ? cachedWeather : "Cloudy"; 
        }
    }

    public static String getVideoFileForWeather(String weather) {
        if (weather == null) weather = "Unknown";
        
        switch (weather) {
            case "Sunny":
            case "No rain":
            case "Partly cloudy":
                return "clear.mp4"; 

            case "Rain":
            case "Heavy rain":
            case "Drizzle":
            case "Thunderstorms":
                return "rain.mp4";

            default:
                return "cloudy.mp4";
        }
    }
}