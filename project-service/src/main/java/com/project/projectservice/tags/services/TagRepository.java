package com.project.projectservice.tags.services;

import com.project.projectservice.tags.data.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TagRepository extends MongoRepository<Tag, String> {

    List<Tag> findByProjectId(String projectId);

    void deleteByProjectId(String projectId);

}
