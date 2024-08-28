package com.project.projectservice.project.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectRequestDto {

    private String name;
    private String description;
    private Set<String> memberIds;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;

}