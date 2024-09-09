package com.project.taskservice.columns.services;

import com.project.taskservice.columns.data.Column;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ColumnRepository extends MongoRepository<Column, String> {

    List<Column> findByProjectIdAndCreatedByIdOrderByPosition(String projectId, String createdById);

    Optional<Column> findByIdAndCreatedById(String id, String createdById);
}
