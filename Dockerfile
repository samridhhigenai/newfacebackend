# Use Eclipse Temurin with Maven for building
FROM eclipse-temurin:17-jdk AS build

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the built JAR file from build stage
COPY --from=build /app/target/face-attendance-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
CMD ["java", "-jar", "app.jar"]
