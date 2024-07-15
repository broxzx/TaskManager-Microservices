package com.project.projectservice.project.service;

import com.project.projectservice.exceptions.EntityNotFoundException;
import com.project.projectservice.feings.UserFeign;
import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.request.ProjectRequestDto;
import com.project.projectservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        return projectRepository.findByMemberIdsContainingOrderByOwnerId(userId);
    }

    public Project createProject(ProjectRequestDto projectRequestDto, String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.extractTokenFromAuthorizationHeader(authorizationHeader));

        Project mappedProject = modelMapper.map(projectRequestDto, Project.class);
        mappedProject.setOwnerId(userId);

        return projectRepository.save(mappedProject);
    }

    public Project updateProject(String projectIdToUpdate, ProjectRequestDto projectRequestDto, String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.extractTokenFromAuthorizationHeader(authorizationHeader));

        Project project = projectRepository.findByIdAndOwnerId(projectIdToUpdate, userId)
                .orElseThrow(() -> new EntityNotFoundException("user with id '%s' is not found".formatted(userId)));

        return projectRepository.save(updateProjectFromProjectRequestDto(project, projectRequestDto));
    }

    public void deleteProjectById(String projectId) {
        projectRepository.deleteById(projectId);
    }

    private Project updateProjectFromProjectRequestDto(Project projectToUpdate, ProjectRequestDto projectRequestDto) {
        projectToUpdate.setName(projectRequestDto.getName());
        projectToUpdate.setDescription(projectRequestDto.getDescription());
        projectToUpdate.setUpdatedAt(LocalDateTime.now());
        projectToUpdate.setMemberIds(projectRequestDto.getMemberIds());
        projectToUpdate.setStatus(projectRequestDto.getStatus());
        projectToUpdate.setTaskIds(projectRequestDto.getTaskIds());

        return projectToUpdate;
    }
}
