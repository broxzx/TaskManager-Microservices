package com.project.taskservice.columns;

import com.project.taskservice.columns.data.Column;
import com.project.taskservice.columns.data.dto.ColumnRequest;
import com.project.taskservice.columns.services.ColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/columns")
@RequiredArgsConstructor
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
    public ResponseEntity<Column> updateProject(@PathVariable String columnId, @RequestBody ColumnRequest columnRequest,
                                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return ResponseEntity.ok(columnsService.updateColumn(columnId, columnRequest, authorizationHeader));
    }

    @DeleteMapping("/{projectId}/{columnId}")
    public void deleteColumn(@PathVariable("projectId") String projectId, @PathVariable("columnId") String columnId,
                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        columnsService.deleteColumn(projectId, columnId, authorizationHeader);
    }

}
