package com.project.projectservice.project.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projects")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Project {

    private String id;

    

}
