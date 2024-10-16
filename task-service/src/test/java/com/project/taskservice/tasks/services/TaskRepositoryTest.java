package com.project.taskservice.tasks.services;

import com.project.taskservice.tasks.data.Task;
import com.project.taskservice.tasks.testUtils.MongoDbContainerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskRepositoryTest extends MongoDbContainerMock {

    @Autowired
    private TaskRepository taskRepository;

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
    }

    @Test
    void givenTask_whenTaskSave_thenSuccess() {
        Task.builder()
                .build();
    }

}
