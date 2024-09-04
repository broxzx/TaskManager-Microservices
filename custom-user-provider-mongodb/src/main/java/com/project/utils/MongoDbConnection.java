package com.project.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.project.keycloak.MongoUserStorageProviderFactory;
import org.bson.Document;
import org.keycloak.component.ComponentModel;

public class MongoDbConnection {

    private static volatile MongoDbConnection instance;
    private final MongoDatabase mongoDatabase;
    private final MongoClient mongoClient;

    public MongoDbConnection(ComponentModel model) {
        this.mongoClient = MongoClients.create(model.get(MongoUserStorageProviderFactory.DB_URL));
        this.mongoDatabase = mongoClient.getDatabase(model.get(MongoUserStorageProviderFactory.DB_NAME));
    }

    public MongoCollection<Document> getUserCollection(ComponentModel model) {
        return mongoDatabase.getCollection(model.get(MongoUserStorageProviderFactory.DB_COLLECTION));
    }

    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
