package com.project.projectservice.project.service;

import com.project.projectservice.exceptions.EntityNotFoundException;
import com.project.projectservice.exceptions.ForbiddenException;
import com.project.projectservice.feings.UserFeign;
import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.request.ProjectRequestDto;
import com.project.projectservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserFeign userFeign;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;

    public List<Project> getUserProjects(String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.extractTokenFromAuthorizationHeader(authorizationHeader));

        return projectRepository.findUserProjects(userId);
    }

    public Project createProject(ProjectRequestDto projectRequestDto, String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.extractTokenFromAuthorizationHeader(authorizationHeader));
        int countUserProjects = projectRepository.countByOwnerId(userId);

        Project mappedProject = modelMapper.map(projectRequestDto, Project.class);
        mappedProject.setOwnerId(userId);
        mappedProject.setPosition(countUserProjects + 1);

        removeNullFieldsFromProject(projectRequestDto, mappedProject);

        return projectRepository.save(mappedProject);
    }

    public Project updateProject(String projectIdToUpdate, ProjectRequestDto projectRequestDto, String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.extractTokenFromAuthorizationHeader(authorizationHeader));

        Project project = getProjectByIdAndOwnerId(projectIdToUpdate, userId);

        Project projectToSave = updateProjectFromProjectRequestDto(project, projectRequestDto);

        return projectRepository.save(projectToSave);
    }

    public void deleteProjectById(String projectId, String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.extractTokenFromAuthorizationHeader(authorizationHeader));
        Project obtainedProject = getProjectByIdAndOwnerId(projectId, userId);

        List<Project> userProjects = projectRepository.findByOwnerId(userId);

        List<Project> projectsToChangePosition = userProjects
                .stream()
                .filter(project -> project.getPosition() > obtainedProject.getPosition())
                .peek(project -> project.setPosition(project.getPosition() - 1))
                .toList();

        projectRepository.saveAll(projectsToChangePosition);

        projectRepository.deleteByIdAndOwnerId(obtainedProject.getId(), obtainedProject.getOwnerId());
    }

    public List<Project> updateProjectPosition(String projectId, int newPosition, String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.extractTokenFromAuthorizationHeader(authorizationHeader));
        Project obtainedProject = getProjectByIdAndOwnerId(projectId, userId);
        int oldPosition = obtainedProject.getPosition();

        List<Project> userProjects = projectRepository.findByOwnerId(userId);
        List<Project> projectsToChangePosition = new ArrayList<>();

        if (newPosition > oldPosition) {
            for (Project project : userProjects) {
                if (project.getPosition() > oldPosition && project.getPosition() <= newPosition) {
                    project.setPosition(project.getPosition() - 1);
                    projectsToChangePosition.add(project);
                }
            }
        } else {
            for (Project project : userProjects) {
                if (project.getPosition() < oldPosition && project.getPosition() >= newPosition) {
                    project.setPosition(project.getPosition() + 1);
                    projectsToChangePosition.add(project);
                }
            }
        }

        obtainedProject.setPosition(newPosition);
        projectsToChangePosition.add(obtainedProject);

        return projectRepository.saveAll(projectsToChangePosition);
    }

    private Project updateProjectFromProjectRequestDto(Project projectToUpdate, ProjectRequestDto projectRequestDto) {
        projectToUpdate.setName(projectRequestDto.getName());
        projectToUpdate.setDescription(projectRequestDto.getDescription());
        projectToUpdate.setUpdatedAt(LocalDateTime.now());
        projectToUpdate.setMemberIds(projectRequestDto.getMemberIds());
        projectToUpdate.setStatus(projectRequestDto.getStatus());
        projectToUpdate.setTaskIds(projectRequestDto.getTaskIds());

        if (projectRequestDto.getMemberIds() == null) {
            projectToUpdate.setMemberIds(new HashSet<>());
        }

        if (projectRequestDto.getTaskIds() == null) {
            projectToUpdate.setTaskIds(new HashSet<>());
        }

        return projectToUpdate;
    }

    private Project getProjectByIdAndOwnerId(String projectId, String ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("projectId '%s' is not found".formatted(projectId)));

        if (!project.getMemberIds().contains(ownerId) && !project.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("you don't have access to project with name '%s'".formatted(project.getName()));
        }

        return project;
    }

    private void removeNullFieldsFromProject(ProjectRequestDto projectRequestDto, Project mappedProject) {
        if (projectRequestDto.getTaskIds() == null) {
            mappedProject.setTaskIds(new HashSet<>());
        }

        if (projectRequestDto.getMemberIds() == null) {
            mappedProject.setMemberIds(new HashSet<>());
        }
    }
}
