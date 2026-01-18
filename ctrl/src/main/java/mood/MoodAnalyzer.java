package mood;
/**
 *
 * @author zayn
 */
import utils.EnvLoader;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class MoodAnalyzer {

    public static String analyzeMood(String journalText) {
        API_Post api = new API_Post();

        try {
            // Load API token from .env (auto-finds the file)
            Map<String, String> env = EnvLoader.loadEnv();
            String bearerToken = env.get("BEARER_TOKEN");
            if (bearerToken == null || bearerToken.isEmpty()) {
                return "Error: BEARER_TOKEN is not set in the environment.";
            }

            // API endpoint and format JSON body
            String apiUrl = "https://router.huggingface.co/hf-inference/models/distilbert/distilbert-base-uncased-finetuned-sst-2-english";
            String jsonBody = "{\"inputs\": \"" + journalText + "\"}";

            // Call API
            String response = api.post(apiUrl, bearerToken, jsonBody);

            // Parse the returned JSON
            JSONArray outerArray = new JSONArray(response);
            JSONArray innerArray = outerArray.getJSONArray(0);
            JSONObject topResult = innerArray.getJSONObject(0);  // first (highest likelihood)

            String moodLabel = topResult.getString("label");
            double moodScore = topResult.getDouble("score");

            // Optional: simplify label (capitalize only first letter)
            moodLabel = moodLabel.substring(0, 1).toUpperCase() + moodLabel.substring(1).toLowerCase();

            // Return only mood (and optionally confidence)
            return moodLabel + " (" + String.format("%.0f%%", moodScore * 100) + ")";

        } catch (Exception e) {
            // shows full details in console/log
            return "Error: " + e.getMessage();
        }
    }
}