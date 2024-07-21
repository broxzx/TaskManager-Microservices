package com.project.projectservice.project.data;

import com.project.projectservice.tags.data.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "projects")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Project {

    private String id;
    @Builder.Default
    private String name = "Unnamed";
    private String description;
    private String ownerId;
    @Builder.Default
    private Set<String> memberIds = new HashSet<>();
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Builder.Default
    private String status = "Pending";
    @Builder.Default
    private Integer position = 0;
    @Builder.Default
    private Set<Tag> tagIds = new HashSet<>() ;
    @Builder.Default
    private Set<String> taskIds = new HashSet<>();
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

}
