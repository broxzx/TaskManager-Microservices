package com.project.taskservice.tasks.data;

import com.project.taskservice.tasks.data.enums.SecurityLevel;
import com.project.taskservice.tasks.data.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Task {

    private String id;

    private String title;

    private String description;

    @Builder.Default
    private Status status = Status.IDLE;

    private int priority = 0;

    private Integer position;

    private String assigneeId;

    private String createdById;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime dueDate = LocalDateTime.now();

    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Builder.Default
    private List<String> comments = new ArrayList<>();

    @Builder.Default
    private List<String> attachments = new ArrayList<>();

    private int timeSpent;

    private int timeEstimated;

    @Builder.Default
    private int gamificationPoints = 10;

    @Builder.Default
    private SecurityLevel securityLevel = SecurityLevel.PUBLIC;

    private String columnId;

    private String projectId;

}
