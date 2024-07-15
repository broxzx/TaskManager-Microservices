package com.project.projectservice.project;

import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.request.ProjectRequestDto;
import com.project.projectservice.project.service.ProjectService;
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
    public ResponseEntity<List<Project>> getUserProjects(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(projectService.getUserProjects(authorizationHeader));
    }

    @PostMapping("/createProject")
    public ResponseEntity<Project> createProject(@RequestBody ProjectRequestDto projectRequestDto,
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
    public void deleteProject(@PathVariable("id") String projectId) {
        projectService.deleteProjectById(projectId);
    }

}
