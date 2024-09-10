package com.project.taskservice.columns.data.dto;

import com.project.taskservice.tasks.data.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ColumnsTasksResponse {

    private String id;

    private String columnName;

    private int position;

    private String projectId;

    private String createdById;

    private List<Task> tasks;

}
