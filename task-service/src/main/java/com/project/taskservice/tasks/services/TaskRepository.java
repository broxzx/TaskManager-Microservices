package com.project.taskservice.tasks.services;

import com.project.taskservice.tasks.data.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRepository extends MongoRepository<Task, String> {



}
