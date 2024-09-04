package com.project.entity;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.project.utils.MongoDbConnection;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageUtil;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.federated.UserFederatedStorageProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class UserAdapter extends AbstractUserAdapter {

    private final User user;
    private final MongoCollection<Document> usersCollection;
    private final MongoDbConnection mongoDbConnection;

    public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, User user) {
        super(session, realm, model);
        this.storageId = new StorageId(storageProviderModel.getId(), user.getUsername());
        this.user = user;
        this.mongoDbConnection = new MongoDbConnection(model);
        try {
            this.usersCollection = mongoDbConnection.getUserCollection(model);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getFirstName() {
        return user.getFirstName();
    }

    @Override
    public String getLastName() {
        return user.getLastName();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
    }

    @Override
    public String getFirstAttribute(String name) {
        List<String> list = getAttributes().getOrDefault(name, List.of());
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        attributes.add("id", user.getId().toHexString());
        attributes.add(UserModel.USERNAME, getUsername());
        attributes.add(UserModel.EMAIL, getEmail());
        attributes.add(UserModel.FIRST_NAME, getFirstName());
        attributes.add(UserModel.LAST_NAME, getLastName());
        attributes.add("emailVerified", String.valueOf(user.isEmailVerified()));
        if (user.getBirthDate() != null) {
            attributes.add("birthDate", user.getBirthDate().toString());
        }
        attributes.add("profilePictureUrl", user.getProfilePictureUrl());
        attributes.add("googleAccountId", user.getGoogleAccountId());
        attributes.add("calendarSyncEnabled", String.valueOf(user.isCalendarSyncEnabled()));
        if (user.getCreatedDate() != null) {
            attributes.add("createdDate", String.valueOf(user.getCreatedDate()));
        }
        if (user.getLastLoginDate() != null) {
            attributes.add("lastLoginDate", String.valueOf(user.getLastLoginDate()));
        }
        attributes.add("taskCompletionRate", String.valueOf(user.getTaskCompletionRate()));
        if (user.getAchievements() != null) {
            attributes.addAll("achievements", user.getAchievements());
        }
        attributes.add("points", String.valueOf(user.getPoints()));
        attributes.add("level", String.valueOf(user.getLevel()));
        attributes.add("isDeleted", String.valueOf(user.isDeleted()));
        return attributes;
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        log.info("getAttributeStream");
        Map<String, List<String>> attributes = getAttributes();
        return (attributes.containsKey(name)) ? attributes.get(name).stream() : Stream.empty();
    }

    @Override
    protected Set<GroupModel> getGroupsInternal() {
        log.info("getGroupsInternal");
        return Set.of();
    }

    @Override
    protected Set<RoleModel> getRoleMappingsInternal() {
        log.info("getRoleMappingsInternal");
        log.info("roles: {}", user.getRoles());
        if (user.getRoles() != null) {
            return user.getRoles().stream().map(roleName -> new UserRoleModel(roleName, realm)).collect(Collectors.toSet());
        }
        return Set.of();
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        return getFederatedStorage().getRequiredActionsStream(realm, this.getId());
    }

    @Override
    public void addRequiredAction(String action) {
        getFederatedStorage().addRequiredAction(realm, this.getId(), action);
    }

    @Override
    public void removeRequiredAction(String action) {
        getFederatedStorage().removeRequiredAction(realm, this.getId(), action);
    }

    @Override
    public void addRequiredAction(RequiredAction action) {
        getFederatedStorage().addRequiredAction(realm, this.getId(), action.name());
    }

    @Override
    public void removeRequiredAction(RequiredAction action) {
        getFederatedStorage().removeRequiredAction(realm, this.getId(), action.name());
    }


    @Override
    public void setAttribute(String name, List<String> values) {
        Bson filter = Filters.eq("username", user.getUsername());
        switch (name) {
            case UserModel.USERNAME, UserModel.EMAIL, UserModel.FIRST_NAME, UserModel.LAST_NAME,
                 "profilePictureUrl", "googleAccountId" -> {
                Bson updateUsername = Updates.set(name, values.get(0));
                usersCollection.updateOne(filter, updateUsername);
            }
            case "level" -> {
                Bson updateUsername = Updates.set(name, Integer.valueOf(values.get(0)));
                usersCollection.updateOne(filter, updateUsername);
            }
            case "points" -> {
                Bson updateUsername = Updates.set(name, Long.valueOf(values.get(0)));
                usersCollection.updateOne(filter, updateUsername);

            }
            case "taskCompletionRate" -> {
                Bson updateUsername = Updates.set(name, Double.valueOf(values.get(0)));
                usersCollection.updateOne(filter, updateUsername);
            }
            case "emailVerified", "calendarSyncEnabled", "isDeleted" -> {
                Bson updateUsername = Updates.set(name, Boolean.valueOf(values.get(0)));
                usersCollection.updateOne(filter, updateUsername);
            }
            case "createdDate", "lastLoginDate" -> {
                Bson updateUsername = Updates.set(name, LocalDateTime.parse(values.get(0)));
                usersCollection.updateOne(filter, updateUsername);
            }
            case "birthDate" -> {
                Bson updateUsername = Updates.set(name, LocalDate.parse(values.get(0)));
                usersCollection.updateOne(filter, updateUsername);
            }
            case "achievements" -> {
                Bson updateUsername = Updates.set(name, values);
                usersCollection.updateOne(filter, updateUsername);
            }
        }
    }

    @Override
    public void setFirstName(String firstName) {
    }

    @Override
    public void setLastName(String lastName) {
    }

    @Override
    public void setEmail(String email) {
    }

    @Override
    public void setEmailVerified(boolean verified) {
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
    }

    @Override
    public void setSingleAttribute(String name, String value) {
    }

    @Override
    public void removeAttribute(String name) {
    }

    UserFederatedStorageProvider getFederatedStorage() {
        return UserStorageUtil.userFederatedStorage(session);
    }

}
