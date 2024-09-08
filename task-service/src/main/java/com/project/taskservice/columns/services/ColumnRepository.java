package com.project.taskservice.columns.services;

import com.project.taskservice.columns.data.Column;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ColumnRepository extends MongoRepository<Column, String> {

    List<Column> findByProjectIdAndCreatedByIdOrderByPosition(String projectId, String createdById);

}
