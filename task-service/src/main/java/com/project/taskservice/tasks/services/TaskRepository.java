package com.project.taskservice.tasks.services;

import com.project.taskservice.tasks.data.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByColumnIdOrderByPosition(String columnId);

    @Query("{$or: [ {createdById: '?0'}, {assigneeId: '?0'}]}")
    List<Task> getPersonalTasks(String userId);

}
