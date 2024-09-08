package com.project.taskservice.tasks.services;

import com.project.taskservice.exceptions.EntityNotFoundException;
import com.project.taskservice.exceptions.ForbiddenException;
import com.project.taskservice.feigns.ProjectFeign;
import com.project.taskservice.feigns.UserFeign;
import com.project.taskservice.model.ProjectAccessDto;
import com.project.taskservice.tasks.data.Task;
import com.project.taskservice.tasks.data.dto.TaskRequest;
import com.project.taskservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final JwtUtils jwtUtils;
    private final UserFeign userFeign;
    private final ModelMapper modelMapper;
    private final ProjectFeign projectFeign;

    public Task createTask(TaskRequest taskRequest, String authorizationHeader) {
        String userId = getUserIdByTokenUsingFeign(authorizationHeader);
        checkAccessToProject(taskRequest.getProjectId(), userId, authorizationHeader);
        Task createdTaskByRequest = createTaskByTaskRequest(taskRequest, userId);

        return taskRepository.save(createdTaskByRequest);
    }

    public Task getTaskById(String taskId, String authorizationHeader) {
        Task obtainedTask = getTaskById(taskId);
        String userId = getUserIdByTokenUsingFeign(authorizationHeader);
        checkAccessToProject(obtainedTask.getProjectId(), userId, authorizationHeader);

        return obtainedTask;
    }

    private String getUserIdByTokenUsingFeign(String authorizationHeader) {
        return userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));
    }

    private Task getTaskById(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("task with id '%s' is not found".formatted(taskId)));
    }

    private void checkAccessToProject(String projectId, String userId, String authorizationHeader) {
        ProjectAccessDto projectAccess = projectFeign.getProjectAccess(projectId, authorizationHeader).getBody();

        if (projectAccess != null) {
            if (!projectAccess.getOwnerId().equals(userId) && !projectAccess.getMemberIds().contains(userId)) {
                throw new ForbiddenException("You don't have access to this project");
            }
        }
    }

    private Task createTaskByTaskRequest(TaskRequest taskRequest, String userId) {
        Task mappedTask = modelMapper.map(taskRequest, Task.class);
        mappedTask.setCreatedById(userId);

        log.info("{}", mappedTask);
        return mappedTask;
    }

    public List<Task> getTasksByColumnId(String columnId, String authorizationHeader) {
        return taskRepository.findByColumnId(columnId);
    }
}
