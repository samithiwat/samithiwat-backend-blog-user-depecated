# Base Image
FROM openjdk:17-jdk-alpine

# Working directory
WORKDIR /app

# JAR file
ARG JAR_FILE=target/*.jar

# Add the application's JAR file to the container
COPY ${JAR_FILE} app.jar

# Set port to 3007
EXPOSE 3007

# Run JAR file
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]