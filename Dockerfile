FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY backend/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms256m", "-Xmx384m", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-XX:+UseStringDeduplication", "-jar", "app.jar"]
