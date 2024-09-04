package com.project.taskservice.feigns;

import com.project.taskservice.config.BeanConfiguration;
import com.project.taskservice.model.ProjectAccessDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "project-service", url = "${url.project-service}", path = "/projects", configuration = BeanConfiguration.class)
public interface ProjectFeign {

    @GetMapping("/{projectId}/access")
    ResponseEntity<ProjectAccessDto> getProjectAccess(@PathVariable("projectId") String projectId,
                                                      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);

}
