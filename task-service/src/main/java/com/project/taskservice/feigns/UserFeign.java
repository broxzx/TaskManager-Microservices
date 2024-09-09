package com.project.taskservice.feigns;

import com.project.taskservice.config.BeanConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "user-service", url = "${url.user-service}", path = "/users", configuration = BeanConfiguration.class)
public interface UserFeign {

    @PostMapping("/getUserIdByToken")
    String getUserIdByToken(@RequestBody String token);

}
