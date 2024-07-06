package com.project.userservice.user.data.service;

import com.project.userservice.user.data.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {

    @Query("{ 'isDeleted': false }")
    List<UserEntity> findAllActiveUsers();

    Optional<UserEntity> findByUsername(String username);

}
