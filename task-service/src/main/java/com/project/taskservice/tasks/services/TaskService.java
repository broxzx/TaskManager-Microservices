package com.project.taskservice.tasks.services;

import com.project.taskservice.tasks.data.Task;
import com.project.taskservice.tasks.data.requests.TaskRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task createTask(TaskRequest taskRequest, String authorizationHeader) {

        return null;
    }
}
