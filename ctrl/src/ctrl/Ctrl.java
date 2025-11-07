package ctrl;
/**
 *
 * @author ctrl-c-ctrl-v
 */

// Imports setup
import weather.API_Get;
import org.json.JSONArray;
import org.json.JSONObject;
import static weather.API_Get.translateForecast;
import static welcome.welcome.welcome_user;
import mood.MoodAnalyzer;


public class Ctrl {
    public static void main(String[] args) {
        welcome_user();


        // Analyzing mood from journal entry
        String journalEntry = "I’m feeling really tired and unmotivated today."; // Example journal text
        String moodResult = MoodAnalyzer.analyzeMood(journalEntry); // Call the mood analysis method
        System.out.println("\nDetected mood: " + moodResult); 
    }
    
    
    // Getting the weather values
    public static void weather_value(){
        API_Get api = new API_Get();

        try {
            String getUrl = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
            String getResponse = api.get(getUrl);
            
            // Set to unicode
            System.setProperty("file.encoding", "UTF-8");

            // Parse JSON array
            JSONArray jsonArray = new JSONArray(getResponse);
            JSONObject firstItem = jsonArray.getJSONObject(0);

            // Extract fields
            JSONObject location = firstItem.getJSONObject("location");
            String locationName = location.getString("location_name");
            String date = firstItem.getString("date");
            String summary = firstItem.getString("summary_forecast");
            int minTemp = firstItem.getInt("min_temp");
            int maxTemp = firstItem.getInt("max_temp");
            
            // Translation and mapping the summary to english values
            String englishSummary = translateForecast(summary);
            
            // Display info
            System.out.println("\n--- Parsed Values ---");
            System.out.println("Location: " + locationName);
            System.out.println("Date: " + date);
            System.out.println("Forecast: " + summary + " (" + englishSummary + ")");
            System.out.println("Temperature: " + minTemp + "\u00B0C - " + maxTemp + "\u00B0C");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
