package com.project.projectservice.project;

import com.project.projectservice.ProjectServiceApplication;
import com.project.projectservice.config.TestBeanConfiguration;
import com.project.projectservice.feings.UserFeign;
import com.project.projectservice.project.data.dto.ProjectQueryResponseDto;
import com.project.projectservice.project.service.ProjectRepository;
import com.project.projectservice.project.service.ProjectService;
import com.project.projectservice.tags.services.TagService;
import com.project.projectservice.utils.JwtUtils;
import com.project.projectservice.utils.MongoQueryUtils;
import com.project.projectservice.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

        when(jwtUtils.extractTokenFromAuthorizationHeader(SecurityUtils.mockedAuthorizationHeader))
                .thenReturn(SecurityUtils.mockedToken);
        when(userFeign.getUserIdByToken(SecurityUtils.mockedToken)).thenReturn(userId);
        when(mongoQueryUtils.createQueryToGetAllUserProjects(userId)).thenReturn(mockProjects);

        List<ProjectQueryResponseDto> result = projectService.getUserProjects(SecurityUtils.mockedAuthorizationHeader);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).isEqualTo(mockProjects);

        verify(userFeign).getUserIdByToken(SecurityUtils.mockedToken);
        verify(mongoQueryUtils).createQueryToGetAllUserProjects(userId);
    }

    @Test
    void givenValidProjectRequestDto_whenUserCreateProject_thenSuccess() {
        String userId = UUID.randomUUID().toString();

        when(jwtUtils.extractTokenFromAuthorizationHeader(SecurityUtils.mockedAuthorizationHeader))
                .thenReturn(SecurityUtils.mockedToken);
        when(userFeign.getUserIdByToken(SecurityUtils.mockedToken)).thenReturn(userId);
        when(projectRepository.countByOwnerId(userId)).thenReturn(0);


    }

}
