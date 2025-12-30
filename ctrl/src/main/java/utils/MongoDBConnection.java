package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.Map;

/**
 * MongoDB connection utility for journal storage.
 * Provides a singleton connection to the SmartJournalDB database.
 */
public class MongoDBConnection {
    private static final String DATABASE_NAME = "SmartJournalDB";
    private static final String ENV_FILE = ".env";
    
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    /**
     * Gets the MongoDB connection string from environment variables.
     * Looks for MONGODB_URI in .env file or system environment variables.
     * 
     * @return the MongoDB connection string
     */
    private static String getConnectionString() {
        // First, try to load from .env file
        Map<String, String> env = EnvLoader.loadEnv(ENV_FILE);
        String connectionString = env.get("MONGODB_URI");
        
        // If not in .env, try system environment variable
        if (connectionString == null || connectionString.isEmpty()) {
            connectionString = System.getenv("MONGODB_URI");
        }
        
        // Throw error if not configured
        if (connectionString == null || connectionString.isEmpty()) {
            throw new RuntimeException("MONGODB_URI not configured. Please set it in .env file or as environment variable.");
        }
        
        return connectionString;
    }

    /**
     * Gets the MongoDB database instance.
     * Creates a new connection if one doesn't exist.
     * 
     * @return MongoDatabase instance for journal operations
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                String connectionString = getConnectionString();
                mongoClient = MongoClients.create(connectionString);
                database = mongoClient.getDatabase(DATABASE_NAME);
                System.out.println("Successfully connected to MongoDB.");
            } catch (Exception e) {
                System.err.println("Error connecting to MongoDB: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return database;
    }
    
    /**
     * Closes the MongoDB connection.
     * Should be called when the application shuts down.
     */
    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            System.out.println("MongoDB connection closed.");
        }
    }
}
