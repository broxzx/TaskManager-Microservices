package com.project.taskservice.columns.services;

import com.project.taskservice.columns.data.Column;
import com.project.taskservice.columns.data.dto.ColumnRequest;
import com.project.taskservice.columns.data.dto.ColumnsTasksResponse;
import com.project.taskservice.feigns.UserFeign;
import com.project.taskservice.tasks.data.Task;
import com.project.taskservice.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.modelmapper.ModelMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColumnService {

    private final ColumnRepository columnRepository;
    private final ModelMapper modelMapper;
    private final UserFeign userFeign;
    private final JwtUtils jwtUtils;
    private final MongoTemplate mongoTemplate;

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

    public List<ColumnsTasksResponse> getColumnsAndTasksByProjectId(String projectId) {
        TypedAggregation<Document> aggregation = getDocumentTypedAggregation(projectId);

        List<Document> columns = mongoTemplate.aggregate(aggregation, "columns", Document.class).getMappedResults();

        return buildResponseFromTaskColumnsAggregation(columns);
    }

    private List<ColumnsTasksResponse> buildResponseFromTaskColumnsAggregation(List<Document> columns) {
        List<ColumnsTasksResponse> response = new ArrayList<>();

        columns.forEach(doc -> {
            ColumnsTasksResponse columnsTasksResponse = new ColumnsTasksResponse();
            columnsTasksResponse.setId(doc.get("_id").toString());
            columnsTasksResponse.setColumnName(doc.getString("columnName"));
            columnsTasksResponse.setPosition(doc.getInteger("position"));
            columnsTasksResponse.setProjectId(doc.getString("projectId"));
            columnsTasksResponse.setCreatedById(doc.getString("createdById"));
            List<Task> tasks = buildTasksFromDocument(doc);
            columnsTasksResponse.setTasks(tasks);

            response.add(columnsTasksResponse);
        });
        return response;
    }

    private List<Task> buildTasksFromDocument(Document doc) {
        return doc.getList("tasks", Document.class)
                .stream()
                .map(cDoc -> {
                    Task task = modelMapper.map(cDoc, Task.class);
                    task.setId(cDoc.get("_id").toString());
                    return task;
                })
                .toList();
    }

    private static TypedAggregation<Document> getDocumentTypedAggregation(String projectId) {
        List<AggregationOperation> operationList = new ArrayList<>();

        AggregationOperation matchProjectId = context -> new Document("$match",
                new Document("projectId", projectId));

        AggregationOperation convertIdToString = context -> new Document("$addFields",
                new Document("_id",
                        new Document("$toString", "$_id")));

        AggregationOperation lookupToTasks = context -> new Document("$lookup",
                new Document("from", "tasks")
                        .append("localField", "_id")
                        .append("foreignField", "columnId")
                        .append("as", "tasks"));

        AggregationOperation sortColumns = context -> new Document("$sort",
                new Document("position", 1L));

        operationList.add(matchProjectId);
        operationList.add(convertIdToString);
        operationList.add(lookupToTasks);
        operationList.add(sortColumns);

        return new TypedAggregation<>(Document.class, operationList);
    }

}
