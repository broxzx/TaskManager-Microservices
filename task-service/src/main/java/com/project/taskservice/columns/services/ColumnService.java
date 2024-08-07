package com.project.taskservice.columns.services;

import com.project.taskservice.columns.data.Column;
import com.project.taskservice.columns.data.request.ColumnRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ColumnService {

    private final ColumnRepository columnRepository;
    private final ModelMapper modelMapper;

    public List<Column> getAllProjectColumns(String projectId) {
        return columnRepository.findByProjectId(projectId);
    }

    public void createColumn(ColumnRequest columnRequest) {
        Column mappedColumn = modelMapper.map(columnRequest, Column.class);

        List<Column> columnsToUpdatePosition = new ArrayList<>(columnRepository.findByProjectId(mappedColumn.getProjectId())
                .stream()
                .filter(column -> column.getPosition() >= mappedColumn.getPosition())
                .peek(column -> column.setPosition(column.getPosition() + 1))
                .toList());

        columnsToUpdatePosition.add(mappedColumn);
        columnRepository.saveAll(columnsToUpdatePosition);
    }

    public Column updateColumn(String columnId, ColumnRequest columnRequest) {
        Column columnToUpdate = getColumnById(columnId);

        if (columnRequest.getColumnName() != null && !columnRequest.getColumnName().equals(columnToUpdate.getColumnName())) {
            columnToUpdate.setColumnName(columnRequest.getColumnName());
        }

        List<Column> columnsToChangePosition = new ArrayList<>();
        if (columnRequest.getPosition() != null) {
            moveColumnsPosition(columnRequest, columnToUpdate, columnsToChangePosition);

            columnToUpdate.setPosition(columnRequest.getPosition());
            columnsToChangePosition.add(columnToUpdate);
            columnRepository.saveAll(columnsToChangePosition);
        }

        return columnToUpdate;
    }

    public void deleteColumn(String projectId, String columnId) {
        columnRepository.findByProjectId(projectId)
                .stream()
                .filter(column -> column.getId().equals(columnId))
                .findAny()
                .map(Column::getProjectId)
                .ifPresent(columnRepository::deleteById);
    }

    private void moveColumnsPosition(ColumnRequest columnRequest, Column columnToUpdate, List<Column> columnsToChangePosition) {
        columnRepository.findByProjectId(columnToUpdate.getProjectId())
                .stream()
                .filter(column -> columnRequest.getPosition() <= column.getPosition())
                .peek(column -> column.setPosition(column.getPosition() + 1))
                .forEach(columnsToChangePosition::add);

        columnRepository.findByProjectId(columnToUpdate.getProjectId())
                .stream()
                .filter(column -> columnRequest.getPosition() > column.getPosition())
                .peek(column -> column.setPosition(column.getPosition() - 1))
                .forEach(columnsToChangePosition::add);
    }

    private Column getColumnById(String columnId) {
        return columnRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column with id '%s' not found".formatted(columnId)));
    }
}
