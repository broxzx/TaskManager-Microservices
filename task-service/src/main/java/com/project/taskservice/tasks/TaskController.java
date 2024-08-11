package com.project.taskservice.tasks;

import com.project.taskservice.tasks.data.Task;
import com.project.taskservice.tasks.data.requests.TaskRequest;
import com.project.taskservice.tasks.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskRequest taskRequest,
                                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(taskService.createTask(taskRequest, authorizationHeader));
    }

}
