package com.project.projectservice.project;

import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.ProjectAccessDto;
import com.project.projectservice.project.data.dto.ProjectQueryResponseDto;
import com.project.projectservice.project.data.dto.ProjectRequestDto;
import com.project.projectservice.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/getUserProjects")
    public ResponseEntity<List<ProjectQueryResponseDto>> getUserProjects(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(projectService.getUserProjects(authorizationHeader));
    }

    @PostMapping("/createProject")
    public ResponseEntity<Project> createProject(@RequestBody @Valid ProjectRequestDto projectRequestDto,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(projectService.createProject(projectRequestDto, authorizationHeader));
    }

    @PutMapping("/updateProject/{projectId}")
    public ResponseEntity<Project> updateProject(@PathVariable String projectId,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                 @RequestBody ProjectRequestDto projectRequestDto) {
        return ResponseEntity.ok(projectService.updateProject(projectId, projectRequestDto, authorizationHeader));
    }

    @DeleteMapping("/deleteProject/{id}")
    public void deleteProject(@PathVariable("id") String projectId,
                              @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        projectService.deleteProjectById(projectId, authorizationHeader);
    }

    @PostMapping("/changeProjectPosition")
    public ResponseEntity<List<Project>> changeProjectPosition(@RequestParam String projectId,
                                                               @RequestParam("position") int newProjectPosition,
                                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(projectService.updateProjectPosition(projectId, newProjectPosition, authorizationHeader));
    }

    @PostMapping("/{projectId}/addMembers")
    public ResponseEntity<Project> addMembersToProject(@PathVariable("projectId") String projectId,
                                                       @RequestBody List<String> memberIds,
                                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(projectService.addMembersToProject(projectId, memberIds, authorizationHeader));
    }

    @DeleteMapping("/{projectId}/deleteMembers")
    public ResponseEntity<Project> deleteMembersFromProject(@PathVariable("projectId") String projectId,
                                                            @RequestBody List<String> memberIds,
                                                            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(projectService.deleteMembersFromProject(projectId, memberIds, authorizationHeader));
    }

    @PostMapping("/{projectId}/statuses")
    public ResponseEntity<Project> changeProjectStatus(@PathVariable("projectId") String projectId,
                                                       @RequestParam("status") String status,
                                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(projectService.changeProjectStatus(projectId, status, authorizationHeader));
    }

    @PostMapping("/{projectId}/access")
    public ResponseEntity<ProjectAccessDto> getProjectAccess(@PathVariable("projectId") String projectId,
                                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(projectService.getProjectAccess(projectId, authorizationHeader));
    }

    //todo: add another scope for obtaining list of access users between services
    @PostMapping("/feign/{projectId}/access")
    public ProjectAccessDto getProjectAccessFeign(@PathVariable("projectId") String projectId,
                                                  @RequestBody String userId) {
        return projectService.getProjectAccessFeign(projectId, userId);
    }

}
