package com.project.projectservice.project.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectRequestDto {

    @NotBlank
    private String name;
    private String description;
    private Set<String> memberIds;
    @Builder.Default
    private LocalDateTime startDate = LocalDateTime.now();
    private LocalDateTime endDate;
    private String status;

}