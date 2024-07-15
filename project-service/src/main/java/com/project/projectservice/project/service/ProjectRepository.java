package com.project.projectservice.project.service;

import com.project.projectservice.project.data.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {

    @Query(value = "{ $or: [ {ownerId: ?0}, {memberIds:  ?0} ] }")
    List<Project> findByMemberIdsContainingOrOwnerId(String userId);

    Optional<Project> findByIdAndOwnerId(String id, String ownerId);

    int countByOwnerId(String ownerId);

    List<Project> findByOwnerId(String ownerId);

    void deleteByIdAndOwnerId(String id, String ownerId);

}
