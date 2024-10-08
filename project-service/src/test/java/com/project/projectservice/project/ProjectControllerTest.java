package com.project.projectservice.project;

import com.project.projectservice.config.SecurityBeans;
import com.project.projectservice.config.SecurityTestBeans;
import com.project.projectservice.project.data.dto.ProjectQueryResponseDto;
import com.project.projectservice.project.service.ProjectService;
import com.project.projectservice.utils.ProjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ProjectController.class})
@Import(value = {SecurityBeans.class, SecurityTestBeans.class})
public class ProjectControllerTest {

    @MockBean
    ProjectService projectService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void givenAuthorizationHeader_whenGetUserProjects_thenSuccess() throws Exception {
        final String url = "/projects/getUserProjects";
        List<ProjectQueryResponseDto> projects = List.of(
                ProjectUtils.buildProjectQueryResponseDto(),
                ProjectUtils.buildProjectQueryResponseDto()
        );

        when(projectService.getUserProjects(any(String.class))).thenReturn(projects);

        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer mockedToken")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpectAll(jsonPath("$").isArray(),
                        jsonPath("$[0].id").isNotEmpty(),
                        jsonPath("$[0].name").isNotEmpty(),
                        jsonPath("$[0].tags").isArray(),
                        jsonPath("$[1].id").isNotEmpty(),
                        jsonPath("$[1].name").isNotEmpty(),
                        jsonPath("$[1].tags").isArray());
    }

    @Test
    void givenInvalidAuthorizationHeader_whenGetUserProjects_thenFailure() throws Exception {
        final String url = "/projects/getUserProjects";

        this.mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer mockedToken")
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andDo(print())
                .andExpectAll(status().is4xxClientError());

    }

    @Test
    void givenInvalidAuthorizationHeader_whenGetUserProject_thenFailure() {

    }

}
