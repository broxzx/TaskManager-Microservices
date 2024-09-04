package com.project.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoUserStorageProviderFactory implements UserStorageProviderFactory<MongoUserStorageProvider> {

    @Override
    public MongoUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new MongoUserStorageProvider(session, model);
    }

    public static final String DB_URL = "dbUrl";
    public static final String DB_NAME = "dbName";
    public static final String DB_COLLECTION = "dbCollection";

    @Override
    public String getId() {
        return "mongo-user-provider";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();

        ProviderConfigProperty dbUrlProperty = new ProviderConfigProperty();
        dbUrlProperty.setName(DB_URL);
        dbUrlProperty.setLabel("Database URL");
        dbUrlProperty.setType(ProviderConfigProperty.STRING_TYPE);
        dbUrlProperty.setHelpText("The URL of the MySQL database (e.g., jdbc:mysql://localhost:3306/mydb)");
        configProperties.add(dbUrlProperty);

        ProviderConfigProperty dbNameProperty = new ProviderConfigProperty();
        dbNameProperty.setName(DB_NAME);
        dbNameProperty.setLabel("Database name");
        dbNameProperty.setType(ProviderConfigProperty.STRING_TYPE);
        dbNameProperty.setHelpText("The collection to connect to the MongoDB database");
        configProperties.add(dbNameProperty);

        ProviderConfigProperty dbCollectionProperty = new ProviderConfigProperty();
        dbCollectionProperty.setName(DB_COLLECTION);
        dbCollectionProperty.setLabel("Database collection");
        dbCollectionProperty.setType(ProviderConfigProperty.STRING_TYPE);
        dbCollectionProperty.setHelpText("The collection to connect to the MongoDB database");
        configProperties.add(dbCollectionProperty);

        return configProperties;
    }
}
