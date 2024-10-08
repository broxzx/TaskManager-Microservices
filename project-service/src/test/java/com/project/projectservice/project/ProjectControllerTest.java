package com.project.projectservice.project;

import com.github.loki4j.client.http.HttpHeader;
import com.project.projectservice.config.SecurityBeans;
import com.project.projectservice.config.SecurityTestBeans;
import com.project.projectservice.exceptions.EntityNotFoundException;
import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.ProjectQueryResponseDto;
import com.project.projectservice.project.data.dto.ProjectRequestDto;
import com.project.projectservice.project.service.ProjectService;
import com.project.projectservice.utils.ProjectUtils;
import com.project.projectservice.utils.SecurityUtils;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;

import static com.project.projectservice.utils.ProjectUtils.generateRandomId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithUserRole)
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
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithAnonymousUser)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andDo(print())
                .andExpectAll(status().is4xxClientError());

    }

    @Test
    void givenAuthorizationHeader_whenUserCreateProject_thenSuccess() throws Exception {
        final String url = "/projects/createProject";
        ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        Project project = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        String projectRequestDtoJson = ProjectUtils.buildProjectRequestJson(project);

        when(projectService.createProject(projectRequestDto, SecurityUtils.mockedAuthorizationHeaderWithUserRole))
                .thenReturn(project);

        this.mockMvc
                .perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithUserRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequestDtoJson)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value(project.getName()),
                        jsonPath("$.description").value(project.getDescription()),
                        jsonPath("$.memberIds").isNotEmpty(),
                        jsonPath("$.status").value(project.getStatus())
                );
    }

    @Test
    void givenAuthorizationHeader_whenUserCreateProjectWithInvalidFields_thenThrowException() throws Exception {
        final String url = "/projects/createProject";

        this.mockMvc
                .perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithUserRole)
                        .content(ProjectUtils.buildProjectRequestJsonWithInvalidFields())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        )
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void givenInvalidAuthorizationHeader_whenUserCreateProject_thenFailure() throws Exception {
        final String url = "/projects/createProject";
        ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        Project project = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        String projectRequestDtoJson = ProjectUtils.buildProjectRequestJson(project);

        this.mockMvc
                .perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithAnonymousUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequestDtoJson)
                        .with(SecurityMockMvcRequestPostProcessors
                                .anonymous()
                        ))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void givenProjectIdAndAuthorizationHeaderAndProjectRequestDto_whenUpdateProject_thenSuccess() throws Exception {
        String randomId = generateRandomId();
        final String url = "/projects/updateProject/%s".formatted(randomId);
        ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        Project project = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        String projectRequestDtoJson = ProjectUtils.buildProjectRequestJson(project);

        when(projectService.updateProject(randomId, projectRequestDto, SecurityUtils.mockedAuthorizationHeaderWithUserRole))
                .thenReturn(project);

        project.setName("new test name");
        project.setDescription("new test name");
        project.setStatus("New Status");
        project.setStartDate(LocalDateTime.of(2024, 12, 12, 13, 14, 15));


        this.mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequestDtoJson)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithUserRole)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        )
                )
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value(project.getName()),
                        jsonPath("$.description").value(project.getDescription()),
                        jsonPath("$.status").value(project.getStatus()),
                        jsonPath("$.startDate").value("2024-12-12T13:14:15")
                );
    }

    @Test
    void givenInvalidProjectIdAndAuthorizationHeaderAndProjectRequestDto_whenUpdateProject_thenFailure() throws Exception {
        final String randomId = generateRandomId();
        final String url = "/projects/updateProject/%s".formatted(randomId);
        ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        Project project = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        String projectRequestDtoJson = ProjectUtils.buildProjectRequestJson(project);

        when(projectService.updateProject(randomId, projectRequestDto, SecurityUtils.mockedAuthorizationHeaderWithUserRole))
                .thenReturn(null);

        this.mockMvc.perform(put(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithUserRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequestDtoJson)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").doesNotExist(),
                        jsonPath("$.description").doesNotExist(),
                        jsonPath("$.memberIds").doesNotExist(),
                        jsonPath("$.status").doesNotExist(),
                        jsonPath("$.startDate").doesNotExist(),
                        jsonPath("$.endDate").doesNotExist()

                );
    }

    @Test
    void givenProjectIdAndAuthorizationHeaderAndProjectRequestDto_whenAnonymousUserUpdateProject_thenFailure() throws Exception {
        final String randomId = generateRandomId();
        final String url = "/projects/updateProject/%s".formatted(randomId);
        ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        Project project = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        String projectRequestDtoJson = ProjectUtils.buildProjectRequestJson(project);

        this.mockMvc.perform(put(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithAnonymousUser)
                        .content(projectRequestDtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())
                )
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void givenProjectIdAndAuthorizationHeader_whenDeleteProject_thenSuccess() throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/deleteProject/%s".formatted(projectId);

        doNothing().when(projectService)
                .deleteProjectById(projectId, SecurityUtils.mockedAuthorizationHeaderWithUserRole);

        this.mockMvc.perform(delete(url)
                        .header(HttpHeader.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithUserRole)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        ))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void givenProjectIdAndAuthorizationHeader_whenAnonymousUserDeleteProject_thenFailure() throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/deleteProject/%s".formatted(projectId);

        this.mockMvc.perform(delete(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithAnonymousUser)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void givenProjectIdAndPosition_whenChangeProjectPosition_thenSuccess() throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/changeProjectPosition";
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("projectId", projectId);
        multiValueMap.add("position", "0");

        when(projectService.updateProjectPosition(projectId, 0, SecurityUtils.mockedAuthorizationHeaderWithUserRole))
                .thenReturn(List.of(
                        ProjectUtils.buildPersistedProject(projectId, generateRandomId()),
                        ProjectUtils.buildPersistedProject(generateRandomId(), generateRandomId()),
                        ProjectUtils.buildPersistedProject(generateRandomId(), generateRandomId())
                ));

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithUserRole)
                        .params(multiValueMap)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                        ))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").isArray(),
                        jsonPath("$[0]").exists(),
                        jsonPath("$[0].id").exists(),
                        jsonPath("$[1]").exists(),
                        jsonPath("$[1].id").exists(),
                        jsonPath("$[2]").exists(),
                        jsonPath("$[2].id").exists()
                );
    }

    @Test
    void givenInvalidProjectIdAndPosition_whenChangeProjectPosition_thenFailure() throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/changeProjectPosition";
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("projectId", projectId);
        multiValueMap.add("position", "0");

        when(projectService.updateProjectPosition(projectId, 0, SecurityUtils.mockedAuthorizationHeaderWithUserRole))
                .thenThrow(EntityNotFoundException.class);

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithUserRole)
                        .params(multiValueMap)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                        ))
                .andDo(print())
                .andExpectAll(status().is4xxClientError());
    }

    @Test
    void givenProjectIdAndPosition_whenAnonymousUserChangeProjectPosition_thenFailure() throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/changeProjectPosition";
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("projectId", projectId);
        multiValueMap.add("position", "0");

        this.mockMvc.perform(post(url)
                        .params(multiValueMap)
                        .header(HttpHeaders.AUTHORIZATION, SecurityUtils.mockedAuthorizationHeaderWithAnonymousUser)
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

}