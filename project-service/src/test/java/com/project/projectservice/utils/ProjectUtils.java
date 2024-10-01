package com.project.projectservice.utils;

import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.ProjectRequestDto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProjectUtils {

    public static ProjectRequestDto buildTestProjectRequestDto() {
        return ProjectRequestDto.builder()
                .name("test project")
                .description("test project description")
                .memberIds(new HashSet<>(Set.of(generateRandomId(), generateRandomId(), generateRandomId(),
                        generateRandomId())))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .status("CREATED")
                .build();
    }

    public static Project buildProjectBasedOnProjectRequestDto(ProjectRequestDto projectRequestDto) {
        return Project.builder()
                .name(projectRequestDto.getName())
                .description(projectRequestDto.getDescription())
                .memberIds(projectRequestDto.getMemberIds())
                .startDate(projectRequestDto.getStartDate())
                .endDate(projectRequestDto.getEndDate())
                .status(projectRequestDto.getStatus())
                .build();
    }

    public static Project buildProjectBasedOnMappedProject(String projectId, Project mappedProject) {
        return Project.builder()
                .id(projectId)
                .name(mappedProject.getName())
                .description(mappedProject.getDescription())
                .memberIds(mappedProject.getMemberIds())
                .startDate(mappedProject.getStartDate())
                .endDate(mappedProject.getEndDate())
                .status(mappedProject.getStatus())
                .position(mappedProject.getPosition())
                .build();
    }

    public static Project buildPersistedProject(String projectId, String userId) {
        return Project.builder()
                .id(projectId)
                .name("test project")
                .description("test project description")
                .memberIds(new HashSet<>(Set.of(generateRandomId(), generateRandomId(), generateRandomId(),
                        userId)))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .status("CREATED")
                .ownerId(userId)
                .position(1)
                .createdAt(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .build();
    }

    private static String generateRandomId() {
        return UUID.randomUUID().toString();
    }

}
