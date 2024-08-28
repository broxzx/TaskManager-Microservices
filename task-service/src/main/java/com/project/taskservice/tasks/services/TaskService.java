package com.project.taskservice.tasks.services;

import com.project.taskservice.feigns.UserFeign;
import com.project.taskservice.tasks.data.Task;
import com.project.taskservice.tasks.data.requests.TaskRequest;
import com.project.taskservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final JwtUtils jwtUtils;
    private final UserFeign userFeign;
    private final ModelMapper modelMapper;

    public Task createTask(TaskRequest taskRequest, String authorizationHeader) {
        String userId = getUserIdByTokenUsingFeign(authorizationHeader);
        Task createdTaskByRequest = createTaskByTaskRequest(taskRequest, userId);

        return taskRepository.save(createdTaskByRequest);
    }

    private String getUserIdByTokenUsingFeign(String authorizationHeader) {
        return userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));
    }

    private Task createTaskByTaskRequest(TaskRequest taskRequest, String userId) {
        Task mappedTask = modelMapper.map(taskRequest, Task.class);
        mappedTask.setCreatedById(userId);

        log.info("{}", mappedTask);
        return mappedTask;
    }
}
