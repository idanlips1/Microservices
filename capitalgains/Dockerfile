FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/capitalgains-0.0.1-SNAPSHOT.jar capital-gains.jar

EXPOSE 8080

CMD ["java", "-jar", "capital-gains.jar"] 