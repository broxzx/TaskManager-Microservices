package com.project.projectservice.project.data.dto;

import com.project.projectservice.tags.data.dto.TagQueryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectQueryResponseDto {

    private String id;
    private String name;
    private String description;
    private String ownerId;
    private Set<String> memberIds;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Integer position = 0;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagQueryResponse> tags;

}
