package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    // Standard connection string for local MongoDB
    private static final String CONNECTION_STRING = "mongodb+srv://tankeat0613_db_user:f6VaAhnTNR5V9cf8@cluster0.e3ekunw.mongodb.net/";
    private static final String DATABASE_NAME = "SmartJournalDB";
    
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

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
}