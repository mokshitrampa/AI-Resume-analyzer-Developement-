# Multi-stage Docker build for Java 21 Spring Boot Backend
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/ai-resume-analyzer-backend-*.jar app.jar
RUN mkdir -p uploads/resumes data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
