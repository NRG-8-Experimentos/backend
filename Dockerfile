FROM maven:3.9.9-eclipse-temurin-21-alpine as build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:26-ea-21-slim
COPY --from=build /target/synhub-backend-0.0.1-SNAPSHOT.jar synhub-backend.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "synhub-backend.jar"]
