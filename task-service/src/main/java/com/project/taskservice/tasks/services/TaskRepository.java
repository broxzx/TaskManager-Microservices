package com.project.taskservice.tasks.services;

import com.project.taskservice.tasks.data.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByColumnId(String columnId);

}
