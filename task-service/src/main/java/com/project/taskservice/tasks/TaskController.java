package com.project.taskservice.tasks;

import com.project.taskservice.tasks.data.Task;
import com.project.taskservice.tasks.data.dto.TaskRequest;
import com.project.taskservice.tasks.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable String taskId,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(taskService.getTaskById(taskId, authorizationHeader));
    }

    @GetMapping("/column/{columnId}")
    public ResponseEntity<List<Task>> getTasksByColumnId(@PathVariable("columnId") String columnId,
                                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(taskService.getTasksByColumnId(columnId, authorizationHeader));
    }

}
