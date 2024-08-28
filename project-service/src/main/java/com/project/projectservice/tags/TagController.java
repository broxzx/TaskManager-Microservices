package com.project.projectservice.tags;

import com.project.projectservice.tags.data.Tag;
import com.project.projectservice.tags.data.dto.TagRequest;
import com.project.projectservice.tags.services.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;


    @GetMapping
    public ResponseEntity<List<Tag>> getAllProjectTags(@RequestParam("projectId") String projectId,
                                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(tagService.getTagsByProjectId(projectId, authorizationHeader));
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody TagRequest tag) {
        return ResponseEntity.ok(tagService.createTag(tag));
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<Tag> getTagById(@PathVariable String tagId) {
        return ResponseEntity.ok(tagService.getTagById(tagId));
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<Tag> updateTag(@PathVariable String tagId,
                                         @RequestParam String newTagName,
                                         @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(tagService.changeTagName(tagId, newTagName, authorizationHeader));
    }

    @DeleteMapping("/{tagId}/{projectId}")
    public void deleteTagFromProject(@PathVariable("projectId") String projectId, @PathVariable("tagId") String tagId) {
        tagService.deleteTagFromProject(tagId, projectId);
    }

    @DeleteMapping("/{tagId}")
    public void deleteTagFromAccount(@PathVariable String tagId) {
        tagService.deleteTag(tagId);
    }

}
