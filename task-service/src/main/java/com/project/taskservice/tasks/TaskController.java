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

    @GetMapping
    public ResponseEntity<List<Task>> getAllUserTasks(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(taskService.getAllUserTasks(authorizationHeader));
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

    @PutMapping("/{taskId}")
    public void assignUserToTask(@PathVariable("taskId") String taskId,
                                 @RequestBody String assigneeId,
                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        taskService.assignUserToTask(taskId, assigneeId, authorizationHeader);
    }

}
