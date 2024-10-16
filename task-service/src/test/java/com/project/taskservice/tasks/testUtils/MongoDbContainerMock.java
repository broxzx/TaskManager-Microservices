package com.project.taskservice.tasks.testUtils;

import com.project.taskservice.TaskServiceApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = {TaskServiceApplication.class})
@Testcontainers
public class MongoDbContainerMock {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7-jammy")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry propertyRegistry) {
        mongoDBContainer.start();
        propertyRegistry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        propertyRegistry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    }

}
