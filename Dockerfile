# Build stage
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Build argument for LLM profile (default to gemini)
ARG LLM_PROFILE=gemini

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B -P ${LLM_PROFILE}

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -B -P ${LLM_PROFILE}

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Port configuration (defaults to 8080)
ARG PORT=8080
ENV PORT=${PORT}

# Expose the application port
EXPOSE ${PORT}

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
