package com.project.taskservice.tasks.services;

import com.project.taskservice.columns.data.Column;
import com.project.taskservice.columns.services.ColumnRepository;
import com.project.taskservice.exceptions.EntityNotFoundException;
import com.project.taskservice.exceptions.ForbiddenException;
import com.project.taskservice.feigns.ProjectFeign;
import com.project.taskservice.feigns.UserFeign;
import com.project.taskservice.model.ProjectAccessDto;
import com.project.taskservice.tasks.data.Task;
import com.project.taskservice.tasks.data.dto.TaskRequest;
import com.project.taskservice.utils.JwtUtils;
import feign.FeignException;
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
    private final ColumnRepository columnRepository;

    public Task createTask(TaskRequest taskRequest, String authorizationHeader) {
        String userId = getUserIdByTokenUsingFeign(authorizationHeader);
        checkAccessToProject(taskRequest.getProjectId(), userId);
        Task createdTaskByRequest = createTaskByTaskRequest(taskRequest, userId);

        return taskRepository.save(createdTaskByRequest);
    }

    public Task getTaskById(String taskId, String authorizationHeader) {
        Task obtainedTask = getTaskById(taskId);
        String userId = getUserIdByTokenUsingFeign(authorizationHeader);
        checkAccessToProject(obtainedTask.getProjectId(), userId);

        return obtainedTask;
    }

    private String getUserIdByTokenUsingFeign(String authorizationHeader) {
        return userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));
    }

    private Task getTaskById(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("task with id '%s' is not found".formatted(taskId)));
    }

    private void checkAccessToProject(String projectId, String userId) {
        ProjectAccessDto projectAccess;
        try {
            projectAccess = projectFeign.getProjectAccessFeign(projectId, userId);
        } catch (FeignException exception) {
            throw new ForbiddenException(exception.getMessage());
        }

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
        Column taskColumn = columnRepository.findById(columnId)
                .orElseThrow(() -> new EntityNotFoundException("column with id '%s' doesn't exists".formatted(columnId)));
        String ownerId = taskColumn.getCreatedById();
        String userId = userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));

        if (!isUserIdAndOwnerIdEqual(userId, ownerId)) {
            throw new ForbiddenException("You don't have access to this project");
        }

        return taskRepository.findByColumnIdOrderByPosition(columnId);
    }

    private boolean isUserIdAndOwnerIdEqual(String userId, String ownerId) {
        return userId.equals(ownerId);
    }

    public void assignUserToTask(String taskId, String assigneeId, String authorizationHeader) {
        Task obtainedTaskById = getTaskById(taskId);
        String userId = userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));
        checkAccessToProject(obtainedTaskById.getProjectId(), assigneeId);
        checkAccessToProject(obtainedTaskById.getProjectId(), userId);

        obtainedTaskById.setAssigneeId(assigneeId);

        taskRepository.save(obtainedTaskById);
    }
}
