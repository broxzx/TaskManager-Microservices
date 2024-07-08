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

    @Override
    public String getId() {
        return "mongo-user-provider";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>();
    }
}
