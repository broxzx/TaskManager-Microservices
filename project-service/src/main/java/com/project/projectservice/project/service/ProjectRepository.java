package com.project.projectservice.project.service;

import com.project.projectservice.project.data.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {

    @Query(value = "{ $or: [ {ownerId: ?0}, {memberIds:  ?0} ] }")
    List<Project> findUserProjects(String userId);

    int countByOwnerId(String ownerId);

    List<Project> findByOwnerId(String ownerId);

    void deleteByIdAndOwnerId(String id, String ownerId);

}
