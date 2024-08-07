package com.project.taskservice.columns;

import com.project.taskservice.columns.data.Column;
import com.project.taskservice.columns.data.request.ColumnRequest;
import com.project.taskservice.columns.services.ColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/columns")
@RequiredArgsConstructor
public class ColumnsController {

    private final ColumnService columnsService;

    @GetMapping("/{projectId}")
    public ResponseEntity<List<Column>> getAllProjectColumns(@PathVariable("projectId") String projectId) {
        return ResponseEntity.ok(columnsService.getAllProjectColumns(projectId));
    }

    @PostMapping
    public void createProject(@RequestBody ColumnRequest columnRequest) {
        columnsService.createColumn(columnRequest);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<Column> updateProject(@PathVariable String columnId, @RequestBody ColumnRequest columnRequest) {
        return ResponseEntity.ok(columnsService.updateColumn(columnId, columnRequest));
    }

    @DeleteMapping("/{projectId}/{columnId}")
    public void deleteColumn(@PathVariable("projectId") String projectId, @PathVariable("columnId") String columnId) {
        columnsService.deleteColumn(projectId, columnId);
    }

}
