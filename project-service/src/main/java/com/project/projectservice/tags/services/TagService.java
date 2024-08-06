package com.project.projectservice.tags.services;

import com.project.projectservice.exceptions.EntityNotFoundException;
import com.project.projectservice.tags.data.Tag;
import com.project.projectservice.tags.data.dto.TagRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;

    public Tag createTag(TagRequest tagRequest) {
        Tag mappedTag = modelMapper.map(tagRequest, Tag.class);

        return tagRepository.save(mappedTag);
    }

    public List<Tag> getTagsByProjectId(String projectId) {
       return tagRepository.findByProjectId(projectId);
    }

    public Tag changeTagName(String tagId, String newTagName) {
        Tag obtainedTag = getTagById(tagId);
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

}
