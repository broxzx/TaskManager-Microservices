package com.project.projectservice.tags.services;

import com.project.projectservice.exceptions.EntityNotFoundException;
import com.project.projectservice.exceptions.ForbiddenException;
import com.project.projectservice.feings.UserFeign;
import com.project.projectservice.project.data.Project;
import com.project.projectservice.project.service.ProjectRepository;
import com.project.projectservice.tags.data.Tag;
import com.project.projectservice.tags.data.dto.TagRequest;
import com.project.projectservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;
    private final UserFeign userFeign;
    private final ProjectRepository projectRepository;

    public Tag createTag(TagRequest tagRequest) {
        Tag mappedTag = modelMapper.map(tagRequest, Tag.class);
        // for some reason id is being associated with projectId field in tagRequest. So, the following code
        // should resolve this problem
        mappedTag.setId(null);

        log.info(mappedTag.toString());
        return tagRepository.save(mappedTag);
    }

    public List<Tag> getTagsByProjectId(String projectId, String authorizationHeader) {
        String userId = getUserIdByTokenUsingFeign(authorizationHeader);
        checkAccessToProject(projectId, userId);

        return tagRepository.findByProjectId(projectId);
    }

    public Tag changeTagName(String tagId, String newTagName, String authorizationHeader) {
        Tag obtainedTag = getTagById(tagId);
        String userId = getUserIdByTokenUsingFeign(authorizationHeader);

        checkAccessToProject(obtainedTag.getProjectId(), userId);

        obtainedTag.setName(newTagName);
        return tagRepository.save(obtainedTag);
    }

    public void deleteTagFromProject(String tagId, String projectId) {
        List<Tag> tagsToDelete = tagRepository.findByProjectId(projectId)
                .stream()
                .filter(tag -> tag.getId().equals(tagId))
                .toList();

        tagRepository.deleteAll(tagsToDelete);
    }

    public void deleteTag(String tagId) {
        tagRepository.deleteById(tagId);
    }

    public Tag getTagById(String tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("tag with id '%s' not found".formatted(tagId)));
    }

    public void deleteTagsByProjectId(String projectId) {
        tagRepository.deleteByProjectId(projectId);
    }

    private String getUserIdByTokenUsingFeign(String authorizationHeader) {
        return userFeign.getUserIdByToken(jwtUtils.extractTokenFromAuthorizationHeader(authorizationHeader));
    }

    private void checkAccessToProject(String projectId, String userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("projectId '%s' is not found".formatted(projectId)));

        if (!project.getMemberIds().contains(userId) && !project.getOwnerId().equals(userId)) {
            throw new ForbiddenException("you don't have access to project with name '%s'".formatted(project.getName()));
        }
    }

}
