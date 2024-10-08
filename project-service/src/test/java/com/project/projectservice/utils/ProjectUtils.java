package com.project.projectservice.utils;

import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.ProjectQueryResponseDto;
import com.project.projectservice.project.data.dto.ProjectRequestDto;
import com.project.projectservice.tags.data.dto.TagQueryResponse;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ProjectUtils {

    private static final List<String> randomIds = List.of(
            generateRandomId(), generateRandomId(), generateRandomId()
    );

    public static ProjectRequestDto buildTestProjectRequestDto() {
        return ProjectRequestDto.builder()
                .name("test project")
                .description("test project description")
                .memberIds(new HashSet<>(randomIds))
                .startDate(LocalDateTime.of(2024, 7, 7, 13, 0))
                .endDate(LocalDateTime.of(2024, 7, 8, 13, 0))
                .status("CREATED")
                .build();
    }

    public static String buildProjectRequestJson(Project project) {
        return """
                {
                    "name": "%s",
                    "description": "%s",
                    "memberIds": ["%s", "%s", "%s"],
                    "startDate": "2024-07-07T13:00:00",
                    "endDate": "2024-07-08T13:00:00",
                    "status": "%s"
                }
                """.formatted(
                project.getName(),
                project.getDescription(),
                randomIds.get(0), randomIds.get(1), randomIds.get(2),
                project.getStatus()
        );
    }

    public static ProjectRequestDto buildProjectRequestDtoWithInvalidFields() {
        return ProjectRequestDto.builder()
                .name(" ")
                .description("test description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .status("Pending")
                .build();
    }

    public static String buildProjectRequestJsonWithInvalidFields() {
        return """
                {
                    "name": " ",
                    "description": "test description",
                    "startDate": "2024-07-07T13:00:00",
                    "endDate": "2024-07-08T13:00:00"
                }
                """;
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

    public static ProjectQueryResponseDto buildProjectQueryResponseDto() {
        return ProjectQueryResponseDto.builder()
                .id(generateRandomId())
                .name("test")
                .description("test description")
                .ownerId(generateRandomId())
                .memberIds(Set.of(generateRandomId(), generateRandomId(), generateRandomId()))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .status("Pending")
                .position(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .tags(List.of(
                        TagQueryResponse.builder()
                                .id(generateRandomId())
                                .name("test")
                                .build())
                )
                .build();
    }

    public static String generateRandomId() {
        return UUID.randomUUID().toString();
    }

}
