package com.project.taskservice.tasks.testUtils;

import com.project.taskservice.tasks.data.Task;

import java.util.UUID;

public class TaskUtils {

    public static Task buildPersistedTask(String taskId, String createdById) {
        return Task.builder()
                .id(taskId)
                .title("test")
                .description("test task description")
                .position(0)
                .createdById(createdById)
                .columnId(generateRandomId())
                .projectId(generateRandomId())
                .build();
    }

    private static String generateRandomId() {
        return UUID.randomUUID().toString();
    }

}
