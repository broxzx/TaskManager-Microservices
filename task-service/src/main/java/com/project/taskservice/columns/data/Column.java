package com.project.taskservice.columns.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "columns")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Column {

    private String id;

    private String columnName;

    private int position;

    private String projectId;

}
