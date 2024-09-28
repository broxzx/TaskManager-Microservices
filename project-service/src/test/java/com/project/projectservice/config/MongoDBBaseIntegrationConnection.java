package com.project.projectservice.config;

import com.project.projectservice.ProjectServiceApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = ProjectServiceApplication.class)
@Testcontainers
public class MongoDBBaseIntegrationConnection {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7-jammy").withExposedPorts(27017);

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        mongoDBContainer.start();
        dynamicPropertyRegistry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        dynamicPropertyRegistry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    }
}
