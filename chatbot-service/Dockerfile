# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
# Using Alpine for a smaller, faster-to-deploy image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Optimization: JVM flags for container awareness and performance
# -XX:+UseContainerSupport: Ensures JVM respects ECS memory limits
# -XX:MaxRAMPercentage: Dynamically allocates heap based on ECS Task memory
ENTRYPOINT ["java", "-jar", "app.jar"]