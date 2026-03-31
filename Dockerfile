# Use Java
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy everything
COPY . .

# Give permission
RUN chmod +x mvnw

# Build jar
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run exact jar (IMPORTANT)
CMD ["java", "-jar", "target/cryptotrade-backend-0.0.1-SNAPSHOT.jar"]
