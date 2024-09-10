package com.project.taskservice.tasks.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TaskFilterRequest {

    private String sortBy;

    private int sortOrder = 1;

}
