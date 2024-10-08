package com.project.projectservice.project.service;

import com.project.projectservice.config.MongoDBBaseIntegrationConnection;
import com.project.projectservice.project.data.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectRepositoryTest extends MongoDBBaseIntegrationConnection {

    @Autowired
    private ProjectRepository projectRepository;


    @AfterEach
    public void tearDown() {
        projectRepository.deleteAll();
    }

    @Test
    void givenExistedProjectId_whenGetProjectById_thenSuccess() {
        Project project = buildProject();
        Project savedProject = projectRepository.save(project);

        Project obtainedProject = projectRepository.findById(savedProject.getId())
                .orElse(null);

        assertThat(obtainedProject).isNotNull();
        assertProjectsAreEqual(obtainedProject, savedProject);
    }

    @Test
    void givenNotExistedProjectId_whenGetProjectById_thenProjectIsNull() {
        Optional<Project> obtainedProject = projectRepository.findById(UUID.randomUUID().toString());

        assertThat(obtainedProject).isEmpty();
    }

    @Test
    void givenProject_whenProjectSave_thenSuccess() {
        Project project = buildProject();
        Project savedProject = projectRepository.save(project);

        Project obtainedProject = projectRepository.findById(savedProject.getId())
                .orElse(null);

        assertThat(obtainedProject).isNotNull();
        assertProjectsAreEqual(savedProject, obtainedProject);
    }

    @Test
    void givenListOfProjects_whenGetAllProject_thenSuccess() {
        List<Project> givenProjects = new ArrayList<>(List.of(buildProject(), buildProject()));

        projectRepository.saveAll(givenProjects);

        List<Project> obtainedProjects = projectRepository.findAll();
        assertThat(obtainedProjects).containsAll(obtainedProjects);
    }

    private void assertProjectsAreEqual(Project expected, Project actual) {
        assertThat(actual.getCreatedAt()).isEqualToIgnoringNanos(expected.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualToIgnoringNanos(expected.getUpdatedAt());
        assertThat(actual.getStartDate()).isEqualToIgnoringNanos(expected.getStartDate());
        assertThat(actual.getEndDate()).isEqualToIgnoringNanos(expected.getEndDate());

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt", "startDate", "endDate")
                .isEqualTo(expected);
    }

    private Project buildProject() {
        return Project.builder()
                .name("test")
                .description("test description")
                .ownerId(UUID.randomUUID().toString())
                .memberIds(Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .status("Pending")
                .position(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
