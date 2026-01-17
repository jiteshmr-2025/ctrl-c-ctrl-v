package utils;

/**
 *
 * @author ekitstrap
 */


import weather.API_Get;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeatherBackgroundManager {
    // --- CACHING VARIABLES ---
    private static String cachedWeather = null;

    // Original method - gets today's weather
    public static String getCurrentWeather() {
        return getWeatherForDate(LocalDate.now());
    }

    // NEW METHOD - gets weather for a specific date
    public static String getWeatherForDate(LocalDate date) {
        // Note: Caching is less useful here since we're querying different dates
        // But we can still use it for the most recently queried date
        
        API_Get api = new API_Get();
        try {
            System.out.println("Fetching weather for date: " + date);
            
            // Format the date as YYYY-MM-DD
            String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            // Build the URL dynamically
            String baseUrl = "https://api.data.gov.my/weather/forecast/";
            String queryParams = "?contains=WP%20Kuala%20Lumpur@location__location_name";
            String dateFilter = "&filter=" + dateString + "@date";
            
            // Combine them
            String getUrl = baseUrl + queryParams + dateFilter;
            String getResponse = api.get(getUrl);
            System.out.println(getResponse);
            
            JSONArray jsonArray = new JSONArray(getResponse);
            
            if (jsonArray.length() == 0) {
                System.out.println("No weather data found for " + dateString);
                return "Unknown";
            }
            
            JSONObject firstItem = jsonArray.getJSONObject(0);
            String summary = firstItem.getString("summary_forecast");
            
            String weather = API_Get.translateForecast(summary);
            
            // Update cache if this is today's date
            if (date.equals(LocalDate.now())) {
                cachedWeather = weather;
                System.currentTimeMillis();
            }
            
            return weather;
            
        } catch (Exception e) {
            e.printStackTrace();
            // If API fails, return default or cached weather
            return (cachedWeather != null) ? cachedWeather : "Cloudy";
        }
    }

    public static String getVideoFileForWeather(String weather) {
        if (weather == null) {
            weather = "Unknown";
        }
        return switch (weather) {
            case "Sunny", "No rain", "Partly cloudy" -> "clear.mp4";
            case "Rain", "Heavy rain", "Drizzle", "Thunderstorms" -> "rain.mp4";
            default -> "cloudy.mp4";
        };
    }
}