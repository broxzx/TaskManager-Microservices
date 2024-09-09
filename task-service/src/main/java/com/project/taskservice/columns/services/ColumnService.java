package com.project.taskservice.columns.services;

import com.project.taskservice.columns.data.Column;
import com.project.taskservice.columns.data.dto.ColumnRequest;
import com.project.taskservice.feigns.UserFeign;
import com.project.taskservice.utils.JwtUtils;
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
    private final UserFeign userFeign;
    private final JwtUtils jwtUtils;

    public List<Column> getAllProjectColumns(String projectId, String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));
        //todo: project members also should be able to see columns
        return columnRepository.findByProjectIdAndCreatedByIdOrderByPosition(projectId, userId);
    }

    public void createColumn(ColumnRequest columnRequest, String authorizationHeader) {
        Column mappedColumn = modelMapper.map(columnRequest, Column.class);
        String userId = userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));

        List<Column> columnsToUpdatePosition = new ArrayList<>(columnRepository.findByProjectIdAndCreatedByIdOrderByPosition(mappedColumn.getProjectId(), userId)
                .stream()
                .filter(column -> column.getPosition() >= mappedColumn.getPosition())
                .peek(column -> column.setPosition(column.getPosition() + 1))
                .toList());

        mappedColumn.setCreatedById(userId);
        columnsToUpdatePosition.add(mappedColumn);
        columnRepository.saveAll(columnsToUpdatePosition);
    }

    public Column updateColumn(String columnId, ColumnRequest columnRequest, String authorizationHeader) {
        Column columnToUpdate = getColumnById(columnId);
        String userId = userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));

        if (columnRequest.getColumnName() != null && !columnRequest.getColumnName().equals(columnToUpdate.getColumnName())) {
            columnToUpdate.setColumnName(columnRequest.getColumnName());
        }

        List<Column> columnsToChangePosition = new ArrayList<>();
        if (columnRequest.getPosition() != null) {
            moveColumnsPosition(columnRequest, columnToUpdate, columnsToChangePosition, userId);

            columnToUpdate.setPosition(columnRequest.getPosition());
            columnsToChangePosition.add(columnToUpdate);
            columnRepository.saveAll(columnsToChangePosition);
        }

        return columnToUpdate;
    }

    public void deleteColumn(String columnId, String authorizationHeader) {
        String userId = userFeign.getUserIdByToken(jwtUtils.getTokenFromAuthorizationHeader(authorizationHeader));
        columnRepository.findByIdAndCreatedById(columnId, userId)
                .map(Column::getId)
                .ifPresent(columnRepository::deleteById);
    }

    private void moveColumnsPosition(ColumnRequest columnRequest, Column columnToUpdate, List<Column> columnsToChangePosition,
                                     String userId) {
        columnRepository.findByProjectIdAndCreatedByIdOrderByPosition(columnToUpdate.getProjectId(), userId)
                .stream()
                .filter(column -> columnRequest.getPosition() <= column.getPosition())
                .peek(column -> column.setPosition(column.getPosition() + 1))
                .forEach(columnsToChangePosition::add);

        columnRepository.findByProjectIdAndCreatedByIdOrderByPosition(columnToUpdate.getProjectId(), userId)
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
