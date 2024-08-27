package com.project;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27016");
        MongoDatabase database = mongoClient.getDatabase("user-db");
        MongoCollection<Document> users = database.getCollection("users");

        Document userDoc = users.find(Filters.eq("username", "admin")).first();
        Objects.requireNonNull(userDoc).getString("username");
        Objects.requireNonNull(userDoc).getLong("points");
        Objects.requireNonNull(userDoc).getInteger("level");

        System.out.println(users.find().first());
    }
}