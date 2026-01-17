package utils;
/**
 *
 * @author chee
 */

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
    
    private static volatile MongoClient mongoClient = null;
    private static volatile MongoDatabase database = null;
    private static final Object lock = new Object();

    /**
     * Gets the MongoDB connection string from environment variables.
     * Looks for MONGODB_URI in .env file or system environment variables.
     * 
     * @return the MongoDB connection string
     */
    private static String getConnectionString() {
        // Load from .env file (auto-finds the file)
        Map<String, String> env = EnvLoader.loadEnv();
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
     * Thread-safe implementation using double-checked locking.
     * 
     * @return MongoDatabase instance for journal operations
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            synchronized (lock) {
                if (database == null) {
                    try {
                        String connectionString = getConnectionString();
                        mongoClient = MongoClients.create(connectionString);
                        database = mongoClient.getDatabase(DATABASE_NAME);
                        System.out.println("Successfully connected to MongoDB.");
                    } catch (Exception e) {
                        System.err.println("Error connecting to MongoDB: " + e.getMessage());
                        System.err.println("Please check: 1) MONGODB_URI is set in .env file or environment, 2) Network connectivity, 3) MongoDB Atlas whitelist settings");
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return database;
    }
    
    /**
     * Closes the MongoDB connection.
     * Should be called when the application shuts down.
     */
    public static synchronized void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            System.out.println("MongoDB connection closed.");
        }
    }
}
