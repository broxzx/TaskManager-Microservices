package com.project.projectservice.feings;

import com.project.projectservice.config.BeanConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${url.user-service}", path = "/users", configuration = BeanConfig.class)
public interface UserFeign {

    @GetMapping("/getUserIdByToken")
    String getUserIdByToken(@RequestParam("token") String token);

    @GetMapping("checkUserExists")
    boolean checkUserExists(@RequestParam("userId") String userId);

}
