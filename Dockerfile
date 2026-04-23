# Phase 1: Build (this is where the jar file is created)
FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /app
COPY . .
# If using Maven (make sure the mvnw file is present in the project)
RUN ./mvnw clean package -DskipTests

# Phase 2: Run (only the jar runs in this layer)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the jar from the build phase
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Define the startup command here
ENTRYPOINT ["java", "-jar", "app.jar"]