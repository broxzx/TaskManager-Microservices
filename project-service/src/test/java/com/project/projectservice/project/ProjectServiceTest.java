package com.project.projectservice.project;

import com.project.projectservice.ProjectServiceApplication;
import com.project.projectservice.config.TestBeanConfiguration;
import com.project.projectservice.exceptions.DefaultException;
import com.project.projectservice.exceptions.EntityNotFoundException;
import com.project.projectservice.exceptions.ForbiddenException;
import com.project.projectservice.feings.UserFeign;
import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.data.dto.ProjectQueryResponseDto;
import com.project.projectservice.project.data.dto.ProjectRequestDto;
import com.project.projectservice.project.service.ProjectRepository;
import com.project.projectservice.project.service.ProjectService;
import com.project.projectservice.tags.services.TagService;
import com.project.projectservice.utils.JwtUtils;
import com.project.projectservice.utils.MongoQueryUtils;
import com.project.projectservice.utils.ProjectUtils;
import com.project.projectservice.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ProjectServiceApplication.class})
@Import(value = {TestBeanConfiguration.class})
public class ProjectServiceTest {

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserFeign userFeign;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private ModelMapper modelMapper;

    @MockBean
    private TagService tagService;

    @MockBean
    private MongoQueryUtils mongoQueryUtils;

    @Autowired
    private ProjectService projectService;

    @Test
    void givenAuthorizationHeader_whenUserGetProjects_thenSuccess() {
        String userId = UUID.randomUUID().toString();
        List<ProjectQueryResponseDto> mockProjects = List.of(new ProjectQueryResponseDto());

        checkUserIdAccessibility(userId);
        when(mongoQueryUtils.createQueryToGetAllUserProjects(userId)).thenReturn(mockProjects);

        List<ProjectQueryResponseDto> result = projectService.getUserProjects(SecurityUtils.mockedAuthorizationHeader);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(mockProjects);

        verify(userFeign).getUserIdByToken(SecurityUtils.mockedToken);
        verify(mongoQueryUtils).createQueryToGetAllUserProjects(userId);
    }

    @Test
    void givenInvalidAuthorizationHeader_whenGetUserProject_thenThrowException() {
        when(jwtUtils.extractTokenFromAuthorizationHeader(SecurityUtils.mockedAuthorizationHeader))
                .thenReturn(null);

        assertThrows(DefaultException.class, () -> {
            projectService.getUserProjects(SecurityUtils.mockedAuthorizationHeader);
        });
    }

    @Test
    void givenValidProjectRequestDto_whenUserCreateProject_thenSuccess() {
        final String userId = generateRandomId();
        final String projectId = generateRandomId();

        final ProjectRequestDto projectRequestDto = ProjectUtils.buildTestProjectRequestDto();
        final Project mappedProject = ProjectUtils.buildProjectBasedOnProjectRequestDto(projectRequestDto);
        final Project createdProject = ProjectUtils.buildProjectBasedOnMappedProject(projectId, mappedProject);


        checkUserIdAccessibility(userId);
        when(projectRepository.countByOwnerId(userId)).thenReturn(0);
        when(modelMapper.map(projectRequestDto, Project.class)).thenReturn(mappedProject);
        when(projectRepository.save(mappedProject)).thenReturn(createdProject);

        Project project = projectService.createProject(projectRequestDto, SecurityUtils.mockedAuthorizationHeader);

        assertThat(project).isNotNull();
        assertProjectsAreEqual(createdProject, project);
        assertThat(mappedProject.getOwnerId()).isEqualTo(userId);
        assertThat(mappedProject.getPosition()).isEqualTo(project.getPosition() + 1);
    }

    @Test
    void givenInvalidAuthorizationHeader_whenCreateProject_thenThrowException() {
        when(jwtUtils.extractTokenFromAuthorizationHeader(SecurityUtils.mockedAuthorizationHeader))
                .thenReturn(null);

        assertThrows(DefaultException.class, () ->
                projectService.createProject(ProjectUtils.buildTestProjectRequestDto(), SecurityUtils.mockedAuthorizationHeader)
        );
    }

    @Test
    void givenValidProjectIdAndAuthorizationHeader_whenDeleteProjectByIdAndUserProjectsIsEmpty_thenSuccess() {
        final String userId = generateRandomId();
        final String projectId = generateRandomId();

        checkUserIdAccessibility(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.ofNullable(ProjectUtils.buildPersistedProject(projectId, userId)));
        when(projectRepository.findByOwnerId(userId)).thenReturn(new ArrayList<>());

        projectService.deleteProjectById(projectId, SecurityUtils.mockedAuthorizationHeader);
    }

    @Test
    void givenValidProjectIdAndAuthorizationHeader_whenDeleteProjectByIdAndUserProjectIsNotEmpty_thenSuccess() {
        final String userId = generateRandomId();
        final String projectId = generateRandomId();
        Project project1 = ProjectUtils.buildPersistedProject(projectId, userId);
        Project project2 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);
        Project project3 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);

        project2.setPosition(2);
        project3.setPosition(3);

        checkUserIdAccessibility(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        when(projectRepository.findByOwnerId(userId)).thenReturn(List.of(project1, project2, project3));

        projectService.deleteProjectById(projectId, SecurityUtils.mockedAuthorizationHeader);

        assertThat(project2.getPosition()).isEqualTo(1);
        assertThat(project3.getPosition()).isEqualTo(2);
    }

    @Test
    void givenValidProjectIdAndAuthorizationHeader_whenDeleteProjectByIdAndProjectNotExists_thenThrowException() {
        final String userId = generateRandomId();
        final String projectId = generateRandomId();
        checkUserIdAccessibility(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            projectService.deleteProjectById(projectId, SecurityUtils.mockedAuthorizationHeader);
        });
    }

    @Test
    void givenValidProjectIdAndAuthorizationHeader_whenDeleteProjectByIdAndUserNotHaveAccess_thenThrowException() {
        final String userId = generateRandomId();
        final String projectId = generateRandomId();
        Project persistedProject = ProjectUtils.buildPersistedProject(projectId, userId);
        persistedProject.getMemberIds().remove(userId);
        persistedProject.setOwnerId(generateRandomId());

        checkUserIdAccessibility(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(persistedProject));

        assertThrows(ForbiddenException.class, () -> {
            projectService.deleteProjectById(projectId, SecurityUtils.mockedAuthorizationHeader);
        });
    }

    @Test
    void givenProjectIdAndInvalidAuthorizationHeader_whenDeleteProjectById_thenThrowException() {
        final String projectId = generateRandomId();

        when(jwtUtils.extractTokenFromAuthorizationHeader(SecurityUtils.mockedAuthorizationHeader)).thenReturn(null);

        assertThrows(DefaultException.class, () -> {
            projectService.deleteProjectById(projectId, SecurityUtils.mockedAuthorizationHeader);
        });
    }

    //from: project1 -> project2 -> project3
    //to: project2 -> project3 -> project1
    @Test
    void givenProjectIdAndNewPositionAndAuthorizationHeader_whenUpdateProjectPositionToBottom_thenSuccess() {
        final String userId = generateRandomId();
        final String projectId = generateRandomId();
        Project project1 = ProjectUtils.buildPersistedProject(projectId, userId);
        Project project2 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);
        Project project3 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);

        project2.setPosition(2);
        project3.setPosition(3);

        checkUserIdAccessibility(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        when(projectRepository.findByOwnerId(userId)).thenReturn(List.of(project1, project2, project3));

        projectService.updateProjectPosition(projectId, 3, SecurityUtils.mockedAuthorizationHeader);

        assertThat(project2.getPosition()).isEqualTo(1);
        assertThat(project3.getPosition()).isEqualTo(2);
        assertThat(project1.getPosition()).isEqualTo(3);
    }

    //from: project2 -> project3 -> project1
    //to: project1 -> project2 -> project3
    @Test
    void givenProjectIdAndNewPositionAndAuthorizationHeader_whenUpdateProjectPositionToTop_thenSuccess() {
        final String userId = generateRandomId();
        final String projectId = generateRandomId();
        Project project1 = ProjectUtils.buildPersistedProject(projectId, userId);
        Project project2 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);
        Project project3 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);

        project1.setPosition(3);
        project2.setPosition(1);
        project3.setPosition(2);

        checkUserIdAccessibility(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        when(projectRepository.findByOwnerId(userId)).thenReturn(List.of(project1, project2, project3));

        projectService.updateProjectPosition(projectId, 1, SecurityUtils.mockedAuthorizationHeader);

        assertThat(project1.getPosition()).isEqualTo(1);
        assertThat(project2.getPosition()).isEqualTo(2);
        assertThat(project3.getPosition()).isEqualTo(3);
    }

    //from: project1 -> project2 -> project3 -> project4
    //to: project2 -> project1 -> project3 -> project4
    @Test
    void givenProjectIdAndNewPositionAndAuthorizationHeader_whenUpdateProjectPositionToMiddle_thenSuccess() {
        final String userId = generateRandomId();
        final String projectId = generateRandomId();
        Project project1 = ProjectUtils.buildPersistedProject(projectId, userId);
        Project project2 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);
        Project project3 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);
        Project project4 = ProjectUtils.buildPersistedProject(generateRandomId(), userId);

        project1.setPosition(1);
        project2.setPosition(2);
        project3.setPosition(3);
        project4.setPosition(4);

        checkUserIdAccessibility(userId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        when(projectRepository.findByOwnerId(userId)).thenReturn(List.of(project1, project2, project3, project4));

        projectService.updateProjectPosition(projectId, 2, SecurityUtils.mockedAuthorizationHeader);

        assertThat(project1.getPosition()).isEqualTo(2);
        assertThat(project2.getPosition()).isEqualTo(1);
        assertThat(project3.getPosition()).isEqualTo(3);
        assertThat(project4.getPosition()).isEqualTo(4);
    }

    @Test
    void givenProjectIdAndNewPositionAndInvalidAuthorizationHeader_whenUpdateProject_thenThrowException() {
        when(jwtUtils.extractTokenFromAuthorizationHeader(SecurityUtils.mockedAuthorizationHeader)).thenReturn(null);

        assertThrows(DefaultException.class, () -> {
            projectService.updateProjectPosition(generateRandomId(), 1, SecurityUtils.mockedAuthorizationHeader);
        });
    }

    private void assertProjectsAreEqual(Project expected, Project actual) {
        assertThat(actual.getCreatedAt()).isEqualToIgnoringNanos(expected.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualToIgnoringNanos(expected.getUpdatedAt());
        assertThat(actual.getStartDate()).isEqualToIgnoringNanos(expected.getStartDate());
        assertThat(actual.getEndDate()).isEqualToIgnoringNanos(expected.getEndDate());

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt", "startDate", "endDate")
                .isEqualTo(expected);
    }

    private String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    private void checkUserIdAccessibility(String userId) {
        when(jwtUtils.extractTokenFromAuthorizationHeader(SecurityUtils.mockedAuthorizationHeader)).thenReturn(SecurityUtils.mockedToken);
        when(userFeign.getUserIdByToken(SecurityUtils.mockedToken)).thenReturn(userId);
    }

}
