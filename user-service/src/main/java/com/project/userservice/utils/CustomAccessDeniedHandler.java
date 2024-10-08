package com.project.userservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final List<String> anonymousUrls = new ArrayList<>(List.of(
            "/users/register",
            "/users/changePassword",
            "/users/login",
            "/users/grantCode"
    ));


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        String servletPath = request.getRequestURI();

        boolean contains = anonymousUrls
                .stream()
                .anyMatch(servletPath::endsWith);

        if (contains) {
            response.setHeader(HttpHeaders.LOCATION, "http://localhost:8081/projects/getUserProjects");
            response.setStatus(HttpStatus.FOUND.value());
            response.sendRedirect("http://localhost:8081/projects/getUserProjects"); // here should be frontend url
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON.toString());
            response.getWriter().write("""
                    {
                        "error": "You don't have access to this link"
                    }
                    """);
        }
    }
}
