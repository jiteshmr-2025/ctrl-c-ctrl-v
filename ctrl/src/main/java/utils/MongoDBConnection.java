package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    // Load connection details from environment variables for security
    private static final String CONNECTION_STRING = getConnectionString();
    private static final String DATABASE_NAME = System.getenv("MONGODB_DATABASE") != null 
        ? System.getenv("MONGODB_DATABASE") 
        : "SmartJournalDB";
    
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    private static String getConnectionString() {
        // Try to get from environment variable first
        String envConnectionString = System.getenv("MONGODB_CONNECTION_STRING");
        if (envConnectionString != null && !envConnectionString.isEmpty()) {
            return envConnectionString;
        }
        
        // Fallback to default (for backwards compatibility, but should use env vars)
        System.err.println("WARNING: Using default MongoDB connection string. " +
                          "Please set MONGODB_CONNECTION_STRING environment variable for security.");
        return "mongodb+srv://tankeat0613_db_user:f6VaAhnTNR5V9cf8@cluster0.e3ekunw.mongodb.net/";
    }

    public static synchronized MongoDatabase getDatabase() {
        if (database == null) {
            try {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                database = mongoClient.getDatabase(DATABASE_NAME);
                System.out.println("Successfully connected to MongoDB.");
            } catch (Exception e) {
                System.err.println("Error connecting to MongoDB: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return database;
    }
    
    // Cleanup method to properly close the connection
    public static synchronized void closeConnection() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                System.out.println("MongoDB connection closed.");
            } catch (Exception e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            } finally {
                mongoClient = null;
                database = null;
            }
        }
    }
}