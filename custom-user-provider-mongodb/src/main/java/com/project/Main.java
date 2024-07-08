package com.project;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

public class Main {
    public static void main(String[] args) {

        MongoClient mongoClient = MongoClients.create("mongodb://admin:admin@localhost:27022");
        MongoDatabase database = mongoClient.getDatabase("user-db");
        MongoCollection<Document> users = database.getCollection("users");


        ObjectId id = new ObjectId();
        Document newUser = new Document("_id", id)
                .append("email", "anyemail@gmail.com");
        users.insertOne(newUser);

        System.out.println(users.find().first());
    }
}