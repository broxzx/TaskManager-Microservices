package com.project.projectservice.project;

import com.github.loki4j.client.http.HttpHeader;
import com.project.projectservice.config.SecurityBeans;
import com.project.projectservice.config.SecurityTestBeans;
import com.project.projectservice.exceptions.EntityNotFoundException;
import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.ProjectAccessDto;
import com.project.projectservice.project.data.dto.ProjectQueryResponseDto;
import com.project.projectservice.project.data.dto.ProjectRequestDto;
import com.project.projectservice.project.service.ProjectService;
import com.project.projectservice.utils.ProjectUtils;
import com.project.projectservice.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.project.projectservice.utils.ProjectUtils.generateRandomId;
import static org.hamcrest.Matchers.*;
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

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenAuthorizationHeader_whenGetUserProjects_thenSuccess(String authorizationHeader, String grantedAuthority) throws Exception {
        final String url = "/projects/getUserProjects";
        List<ProjectQueryResponseDto> projects = List.of(
                ProjectUtils.buildProjectQueryResponseDto(),
                ProjectUtils.buildProjectQueryResponseDto()
        );

        when(projectService.getUserProjects(any(String.class))).thenReturn(projects);

        mockMvc.perform(get(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))))
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


    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenAuthorizationHeader_whenUserCreateProject_thenSuccess(String authorizationHeader,
                                                                    String grantedAuthority) throws Exception {
        final String url = "/projects/createProject";
        ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        Project project = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        String projectRequestDtoJson = ProjectUtils.buildProjectRequestJson(project);

        when(projectService.createProject(projectRequestDto, authorizationHeader))
                .thenReturn(project);

        this.mockMvc
                .perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequestDtoJson)
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value(project.getName()),
                        jsonPath("$.description").value(project.getDescription()),
                        jsonPath("$.memberIds").isNotEmpty(),
                        jsonPath("$.status").value(project.getStatus())
                );
    }

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenAuthorizationHeader_whenUserCreateProjectWithInvalidFields_thenThrowException(String authorizationHeader,
                                                                                            String grantedAuthority) throws Exception {
        final String url = "/projects/createProject";

        this.mockMvc
                .perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .content(ProjectUtils.buildProjectRequestJsonWithInvalidFields())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))
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

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenProjectIdAndAuthorizationHeaderAndProjectRequestDto_whenUpdateProject_thenSuccess(String authorizationHeader,
                                                                                                String grantedAuthority) throws Exception {
        String randomId = generateRandomId();
        final String url = "/projects/updateProject/%s".formatted(randomId);
        ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        Project project = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        String projectRequestDtoJson = ProjectUtils.buildProjectRequestJson(project);

        when(projectService.updateProject(randomId, projectRequestDto, authorizationHeader))
                .thenReturn(project);

        project.setName("new test name");
        project.setDescription("new test name");
        project.setStatus("New Status");
        project.setStartDate(LocalDateTime.of(2024, 12, 12, 13, 14, 15));


        this.mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequestDtoJson)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))
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

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenInvalidProjectIdAndAuthorizationHeaderAndProjectRequestDto_whenUpdateProject_thenFailure(String authorizationHeader,
                                                                                                       String grantedAuthority) throws Exception {
        final String randomId = generateRandomId();
        final String url = "/projects/updateProject/%s".formatted(randomId);
        ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        Project project = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        String projectRequestDtoJson = ProjectUtils.buildProjectRequestJson(project);

        when(projectService.updateProject(randomId, projectRequestDto, authorizationHeader))
                .thenReturn(null);

        this.mockMvc.perform(put(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectRequestDtoJson)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority)))
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

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenProjectIdAndAuthorizationHeader_whenDeleteProject_thenSuccess(String authorizationHeader,
                                                                            String grantedAuthority) throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/deleteProject/%s".formatted(projectId);

        doNothing().when(projectService)
                .deleteProjectById(projectId, authorizationHeader);

        this.mockMvc.perform(delete(url)
                        .header(HttpHeader.AUTHORIZATION, authorizationHeader)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))
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

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenProjectIdAndPosition_whenChangeProjectPosition_thenSuccess(String authorizationHeader,
                                                                         String grantedAuthority) throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/changeProjectPosition";
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("projectId", projectId);
        multiValueMap.add("position", "0");

        when(projectService.updateProjectPosition(projectId, 0, authorizationHeader))
                .thenReturn(List.of(
                        ProjectUtils.buildPersistedProject(projectId, generateRandomId()),
                        ProjectUtils.buildPersistedProject(generateRandomId(), generateRandomId()),
                        ProjectUtils.buildPersistedProject(generateRandomId(), generateRandomId())
                ));

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .params(multiValueMap)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(List.of(new SimpleGrantedAuthority(grantedAuthority)))
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

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenInvalidProjectIdAndPosition_whenChangeProjectPosition_thenFailure(String authorizationHeader,
                                                                                String grantedAuthority) throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/changeProjectPosition";
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("projectId", projectId);
        multiValueMap.add("position", "0");

        when(projectService.updateProjectPosition(projectId, 0, authorizationHeader))
                .thenThrow(EntityNotFoundException.class);

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .params(multiValueMap)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(List.of(new SimpleGrantedAuthority(grantedAuthority)))
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

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenProjectIdAndMemberIds_whenAddMembersToProject_thenSuccess(String authorizationHeader,
                                                                        String grantedAuthority) throws Exception {
        List<String> ids = List.of(generateRandomId(), generateRandomId(), generateRandomId());
        final String projectId = generateRandomId();
        Project project = ProjectUtils.buildPersistedProject(projectId, generateRandomId());
        final String jsonBody = "[\"%s\", \"%s\", \"%s\"]".formatted(ids.get(0), ids.get(1), ids.get(2));
        final String url = "/projects/%s/addMembers".formatted(projectId);


        when(projectService.addMembersToProject(projectId, ids, authorizationHeader))
                .thenReturn(project);

        project.setMemberIds(new HashSet<>(ids));

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))
                        ))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.memberIds").exists(),
                        jsonPath("$.memberIds").isArray(),
                        jsonPath("$.memberIds", hasSize(ids.size())),
                        jsonPath("$.memberIds", containsInAnyOrder(ids.toArray()))
                );
    }

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenProjectIdAndMemberIds_whenDeleteMembersFromProject_thenSuccess(String authorizationHeader,
                                                                             String grantedAuthority) throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/%s/deleteMembers".formatted(projectId);
        Project project = ProjectUtils.buildPersistedProject(projectId, generateRandomId());
        List<String> memberIds = List.of(generateRandomId(), generateRandomId());
        final String jsonBody = "[\"%s\", \"%s\"]".formatted(memberIds.get(0), memberIds.get(1));

        project.setMemberIds(new HashSet<>(memberIds));

        when(projectService.deleteMembersFromProject(projectId, memberIds, authorizationHeader))
                .thenReturn(project);

        project.getMemberIds().removeAll(memberIds);

        this.mockMvc.perform(delete(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))
                        ))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.memberIds").isArray(),
                        jsonPath("$.memberIds", not(hasItems(memberIds.toArray())))
                );
    }

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenProjectIdAndStatus_whenChangeProjectStatus_thenSuccess(String authorizationHeader,
                                                                     String grantedAuthority) throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/%s/statuses".formatted(projectId);
        Project project = ProjectUtils.buildPersistedProject(projectId, generateRandomId());

        when(projectService.changeProjectStatus(projectId, "new status", authorizationHeader))
                .thenReturn(project);

        project.setStatus("new status");

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("status", "new status")
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))
                        ))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.status").value("new status")
                );
    }

    @ParameterizedTest
    @MethodSource(value = {"testMethodWithDifferentUsers"})
    void givenProjectId_whenGetProjectAccess_thenSuccess(String authorizationHeader,
                                                         String grantedAuthority) throws Exception {
        final String projectId = generateRandomId();
        final String url = "/projects/%s/access".formatted(projectId);
        List<String> randomIds = List.of(generateRandomId(), generateRandomId(), generateRandomId());

        when(projectService.getProjectAccess(projectId, authorizationHeader))
                .thenReturn(new ProjectAccessDto(randomIds.get(0), Set.of(randomIds.get(1), randomIds.get(2))));

        this.mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(grantedAuthority))
                        ))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.ownerId").value(randomIds.get(0)),
                        jsonPath("$.memberIds").isArray(),
                        jsonPath("$.memberIds", containsInAnyOrder(randomIds.get(1), randomIds.get(2)))
                );
    }

    private static Stream<Arguments> testMethodWithDifferentUsers() {
        return Stream.of(
                Arguments.of("ROLE_USER", SecurityUtils.mockedAuthorizationHeaderWithUserRole),
                Arguments.of("ROLE_ADMIN", SecurityUtils.mockedAuthorizationHeaderWithAdminRole));
    }

}