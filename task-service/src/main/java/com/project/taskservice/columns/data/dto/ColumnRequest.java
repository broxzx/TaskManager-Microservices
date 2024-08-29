package com.project.taskservice.columns.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ColumnRequest {

    private String columnName;

    private Integer position;

    private String projectId;

}
