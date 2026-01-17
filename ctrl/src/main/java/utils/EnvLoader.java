package utils;
/**
 *
 * @author chee
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static Map<String, String> cachedEnv = null;
    private static String cachedEnvPath = null;

    /**
     * Finds the .env file by checking multiple possible locations.
     * This handles different working directories when running CLI vs GUI.
     * 
     * @return the absolute path to the .env file, or null if not found
     */
    public static String findEnvFile() {
        String userDir = System.getProperty("user.dir");
        
        // Possible locations for .env file
        String[] possiblePaths = {
            ".env",                                    // Current directory
            "../.env",                                 // Parent directory (when running from ctrl/)
            "ctrl/.env",                               // If running from parent directory
            userDir + "/.env",                         // Explicit user directory
            userDir + "/../.env",                      // Parent of user directory
            userDir + "/ctrl/.env"                     // user.dir/ctrl/.env
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                return file.getAbsolutePath();
            }
        }
        
        return null;
    }

    /**
     * Loads environment variables from .env file, automatically finding it.
     * Results are cached after first load.
     * 
     * @return a Map containing the environment variables as key-value pairs
     */
    public static Map<String, String> loadEnv() {
        if (cachedEnv != null) {
            return cachedEnv;
        }
        
        String envPath = findEnvFile();
        if (envPath != null) {
            System.out.println("Found .env file at: " + envPath);
            cachedEnv = loadEnv(envPath);
            cachedEnvPath = envPath;
            return cachedEnv;
        }
        
        System.err.println("Could not find .env file in any expected location");
        return new HashMap<>();
    }

    /**
     * Loads environment variables from a .env file into a Map.
     * Each line should be in KEY=VALUE format.
     * Lines starting with '#' or empty lines are ignored.
     * 
     * @param filePath the path to the .env file
     * @return a Map containing the environment variables as key-value pairs
     */
    public static Map<String, String> loadEnv(String filePath) {
        Map<String, String> env = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                // Skip empty lines or comments
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Split on the first '=' only
                String[] parts = line.split("=", 2);

                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    // Optionally, remove quotes from value (if you want to support that)
                    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }

                    env.put(key, value);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load .env file: " + e.getMessage());
        }
        
        return env;
    }
}