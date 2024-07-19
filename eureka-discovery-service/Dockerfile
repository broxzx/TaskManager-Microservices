FROM maven:3.8.5-openjdk-17 as build
WORKDIR /build

COPY ../pom.xml parent-pom.xml

RUN mvn dependency:go-offline -f parent-pom.xml
RUN #mvn dependency:go-offline -f pom.xml

COPY src ./src
RUN mvn clean package -DskipTests


FROM amazoncorretto:17
ARG PROFILE=dev
ARG APP_VERSION=1.0.0

WORKDIR /app
COPY --from=build /build/target/eureka-discovery-service-*.jar /app/

EXPOSE 8761

ENV JAR_VERSION=${APP_VERSION}
ENV ACTIVE_PROFILE=${PROFILE}

CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} eureka-discovery-service-${JAR_VERSION}-SNAPSHOT.jar
