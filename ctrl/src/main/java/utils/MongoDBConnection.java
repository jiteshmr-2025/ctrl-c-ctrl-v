package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * MongoDB connection utility for journal storage.
 * Provides a singleton connection to the SmartJournalDB database.
 */
public class MongoDBConnection {
    // Connection string for MongoDB Atlas
    private static final String CONNECTION_STRING = "mongodb+srv://tankeat0613_db_user:f6VaAhnTNR5V9cf8@cluster0.e3ekunw.mongodb.net/";
    private static final String DATABASE_NAME = "SmartJournalDB";
    
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    /**
     * Gets the MongoDB database instance.
     * Creates a new connection if one doesn't exist.
     * 
     * @return MongoDatabase instance for journal operations
     */
    public static MongoDatabase getDatabase() {
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
