package com.project.projectservice.utils;

import com.project.projectservice.project.data.dto.ProjectQueryResponseDto;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MongoQueryUtils {

    private final MongoTemplate mongoTemplate;

    public List<ProjectQueryResponseDto> createQueryToGetAllUserProjects(String userId) {
        List<AggregationOperation> aggregationOperations = buildQueryToGetAllUsersProjectWithTags(userId);
        TypedAggregation<Document> aggregation = buildAggregationToGetProjects(aggregationOperations);

        return mongoTemplate.aggregate(aggregation, "projects", ProjectQueryResponseDto.class).getMappedResults();
    }

    private TypedAggregation<Document> buildAggregationToGetProjects(List<AggregationOperation> aggregationOperations) {
        return TypedAggregation.newAggregation(Document.class, aggregationOperations);
    }

    private List<AggregationOperation> buildQueryToGetAllUsersProjectWithTags(String userId) {
        List<AggregationOperation> aggregationOperations = new ArrayList<>();

        AggregationOperation matchFields = context -> new Document("$match",
                new Document("$or", Arrays.asList(new Document("ownerId", userId),
                        new Document("memberIds",
                                new Document("$in", List.of(userId))))));

        AggregationOperation addFieldsOperation = context -> new Document("$addFields",
                new Document("_id",
                        new Document("$toString", "$_id")));

        AggregationOperation lookUpOperation = context -> new Document("$lookup",
                new Document("from", "tags")
                        .append("localField", "_id")
                        .append("foreignField", "projectId")
                        .append("as", "tags")
                        .append("pipeline", List.of(new Document("$project",
                                new Document("_id", 1L)
                                        .append("name", 1L)))));

        aggregationOperations.add(matchFields);
        aggregationOperations.add(addFieldsOperation);
        aggregationOperations.add(lookUpOperation);

        return aggregationOperations;
    }

}
