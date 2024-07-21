package com.project.projectservice.tags.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tags")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Tag {

    private String id;

    private String name;

    private String projectId;

}
