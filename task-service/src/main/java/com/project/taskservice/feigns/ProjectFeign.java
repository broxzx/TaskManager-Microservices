package com.project.taskservice.feigns;

import com.project.taskservice.config.BeanConfiguration;
import com.project.taskservice.model.ProjectAccessDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "project-service", url = "${url.project-service}", path = "/projects", configuration = BeanConfiguration.class)
public interface ProjectFeign {

    @PostMapping("/feign/{projectId}/access")
    ProjectAccessDto getProjectAccessFeign(@PathVariable("projectId") String projectId,
                                           @RequestBody String userId);
}
