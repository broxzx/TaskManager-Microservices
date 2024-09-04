package com.project.taskservice.feigns;

import com.project.taskservice.config.BeanConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-service", url = "${url.user-service}", path = "/users", configuration = BeanConfiguration.class)
public interface UserFeign {

    @GetMapping("/getUserIdByToken")
    String getUserIdByToken(@RequestParam("token") String token);

}
