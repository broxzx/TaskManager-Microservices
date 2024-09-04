package com.project.keycloak;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.project.entity.User;
import com.project.entity.UserAdapter;
import com.project.utils.MongoDbConnection;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class MongoUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, UserQueryProvider,
        CredentialInputUpdater, CredentialInputValidator,
        UserRegistrationProvider {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final MongoCollection<Document> usersCollection;
    private final MongoDbConnection mongoDbConnection;

    public MongoUserStorageProvider(KeycloakSession session, ComponentModel model) {
        log.info("MongoUserStorageProvider constructor");
        this.session = session;
        this.model = model;
        this.mongoDbConnection = new MongoDbConnection(model);
        try {
            log.info("before db init");
            this.usersCollection = mongoDbConnection.getUserCollection(model);
            log.info("after db init");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        log.debug("getUserById: {}", id);
        return findUser(realm, id);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        log.info("Getting user by username: {}", username);
        try {
            log.info("username: {}", username);
            Document userDoc = usersCollection.find(Filters.eq("username", username)).first();
            if (userDoc != null) {
                return new UserAdapter(session, realm, model, buildUserAdapter(userDoc));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        log.info("Getting user by email: {}", email);
        try {
            log.info("email: {}", email);
            Document userDoc = usersCollection.find(Filters.eq("email", email)).first();
            if (userDoc != null) {
                return new UserAdapter(session, realm, model, buildUserAdapter(userDoc));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        log.info("Adding user with username: {}", username);
        try {
            ObjectId id = new ObjectId();
            Document newUser = new Document("_id", id)
                    .append("username", username);
            usersCollection.insertOne(newUser);
            return getUserById(realm, id.toHexString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        log.info("Removing user with ID: {}", user.getId());
        try {
            String email = user.getUsername();
            usersCollection.deleteOne(Filters.eq("username", email));
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preRemove(RealmModel realm) {
        // Not needed
    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {
        // Not needed
    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {
        // Not needed
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        log.info("isValid: {}", credentialInput);
        if (!(credentialInput instanceof UserCredentialModel)) {
            return false;
        }

        String username = user.getUsername();
        try {
            Document userDoc = usersCollection.find(Filters.eq("username", username)).first();
            if (userDoc != null) {
                UserCredentialModel cred = (UserCredentialModel) credentialInput;
                String storedPassword = userDoc.getString("password");
                return BCrypt.checkpw(cred.getChallengeResponse(), storedPassword);
            }
        } catch (Exception e) {
            log.error("Error validating user credentials: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (input.getType().equals(PasswordCredentialModel.TYPE)) {
            UserCredentialModel cred = (UserCredentialModel) input;
            String hashedPassword = BCrypt.hashpw(cred.getChallengeResponse(), BCrypt.gensalt());
            log.info("Updating credentials for user ID: {}", user.getId());
            String userEmail = user.getEmail();
            try {
                usersCollection.updateOne(Filters.eq("email", userEmail),
                        new Document("$set", new Document("password", hashedPassword)));
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        log.info("Searching users with parameters: {}", params);
        params.forEach((k, v) -> log.info("key: {}, value: {}", k, v));

        List<UserModel> users = new ArrayList<>();
        Bson filter = createFilter(params);

        FindIterable<Document> findIterable = usersCollection.find(filter);
        applyPagination(findIterable, firstResult, maxResults)
                .forEach(data -> users.add(new UserAdapter(session, realm, model, buildUserAdapter(data))));

        return users.stream();
    }

    private Bson createFilter(Map<String, String> params) {
        if (params.containsKey("keycloak.session.realm.users.query.search")) {
            String searchValue = params.get("keycloak.session.realm.users.query.search");
            if (searchValue.equals("*")) {
                return new BsonDocument();
            } else {
                return Filters.regex("email", searchValue, "i");
            }
        }

        if (params.containsKey("username")) {
            return Filters.regex("username", params.get("username"), "i");
        }
        if (params.containsKey("email")) {
            return Filters.regex("email", params.get("email"), "i");
        }
        if (params.containsKey("firstName")) {
            return Filters.regex("firstName", params.get("firstName"), "i");
        }
        if (params.containsKey("lastName")) {
            return Filters.regex("lastName", params.get("lastName"), "i");
        }

        return new BsonDocument();
    }

    private FindIterable<Document> applyPagination(FindIterable<Document> findIterable, Integer firstResult, Integer maxResults) {
        if (firstResult != null) {
            findIterable = findIterable.skip(firstResult);
        }
        if (maxResults != null) {
            findIterable = findIterable.limit(maxResults);
        }
        return findIterable;
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        log.info("Searching users by attribute: {} = {}", attrName, attrValue);
        return Stream.empty();
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        log.info("Getting user count");
        return UserQueryProvider.super.getUsersCount(realm);
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realmModel, UserModel userModel) {
        return Stream.empty();
    }

    private User buildUserAdapter(Document userDoc) {
        User user = new User();
        user.setId(userDoc.getObjectId("_id"));
        user.setUsername(userDoc.getString("username"));
        user.setPassword(userDoc.getString("password"));
        user.setEmail(userDoc.getString("email"));
        user.setFirstName(userDoc.getString("firstName"));
        user.setLastName(userDoc.getString("lastName"));
        user.setRoles(userDoc.getList("roles", String.class));
        user.setEmailVerified(userDoc.getBoolean("emailVerified"));
        Date birthDate = userDoc.getDate("birthDate");
        if (birthDate != null) {
            LocalDate localDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            user.setBirthDate(localDate);
        }
        user.setProfilePictureUrl(userDoc.getString("profilePictureUrl"));
        user.setGoogleAccountId(userDoc.getString("googleAccountId"));
        user.setCalendarSyncEnabled(userDoc.getBoolean("calendarSyncEnabled"));
        Date createdDate = userDoc.getDate("createdDate");
        if (createdDate != null) {
            LocalDateTime localDate = createdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            user.setCreatedDate(localDate);
        }
        Date lastLoginDate = userDoc.getDate("lastLoginDate");
        if (lastLoginDate != null) {
            LocalDateTime localDate = lastLoginDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            user.setLastLoginDate(localDate);
        }
        user.setTaskCompletionRate(userDoc.getDouble("taskCompletionRate"));
        user.setAchievements(userDoc.getList("achievements", String.class));
        user.setPoints(userDoc.getLong("points"));
        user.setLevel(userDoc.getInteger("level"));
        user.setDeleted(userDoc.getBoolean("isDeleted"));
        return user;
    }

    private UserModel findUser(RealmModel realm, String identifier) {
        log.info("Getting user by ID: {}", identifier);
        log.info("storage id: {}", StorageId.externalId(identifier));

        try {
            log.info("External ID: {}", identifier);
            Document userDoc = usersCollection.find(Filters.eq("username", StorageId.externalId(identifier))).first();

            if (userDoc != null) {
                return new UserAdapter(session, realm, model, buildUserAdapter(userDoc));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
