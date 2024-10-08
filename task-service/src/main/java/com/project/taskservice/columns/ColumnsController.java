package com.project.taskservice.columns;

import com.project.taskservice.columns.data.Column;
import com.project.taskservice.columns.data.dto.ColumnRequest;
import com.project.taskservice.columns.data.dto.ColumnsTasksResponse;
import com.project.taskservice.columns.services.ColumnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/columns")
@RequiredArgsConstructor
@Slf4j
public class ColumnsController {

    private final ColumnService columnsService;

    @GetMapping("/{projectId}")
    public ResponseEntity<List<Column>> getAllProjectColumns(@PathVariable("projectId") String projectId,
                                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(columnsService.getAllProjectColumns(projectId, authorizationHeader));
    }

    @PostMapping
    public void createColumn(@RequestBody ColumnRequest columnRequest,
                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        columnsService.createColumn(columnRequest, authorizationHeader);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<Column> updateColumn(@PathVariable String columnId, @RequestBody ColumnRequest columnRequest,
                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(columnsService.updateColumn(columnId, columnRequest, authorizationHeader));
    }

    @DeleteMapping("/{columnId}")
    public void deleteColumn(@PathVariable("columnId") String columnId,
                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        columnsService.deleteColumn(columnId, authorizationHeader);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ColumnsTasksResponse>> getColumnsAndTasksByProjectId(@PathVariable String projectId) {
        return ResponseEntity.ok(columnsService.getColumnsAndTasksByProjectId(projectId));
    }

}
