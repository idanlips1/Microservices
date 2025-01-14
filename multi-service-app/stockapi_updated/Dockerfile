# Use an official Java runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the build output to the container
COPY target/stockapi-0.0.1-SNAPSHOT.jar stocks-api.jar

# Expose the application's port
EXPOSE 8000

# Set environment variables
ENV NINJA_API_KEY=GSBDSCF31HehCSxIozHtjw==CY8yGhcKCEU3ZNgG

# Command to run the application
CMD ["java", "-jar", "stocks-api.jar"]

