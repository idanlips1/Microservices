#!/bin/bash

echo "Building Capital Gains Service..."
cd capitalgains
./mvnw clean package -DskipTests

echo "Building Stock API Service..."
cd ../stockapi_updated
./mvnw clean package -DskipTests

echo "Starting all services with Docker Compose..."
cd ..
docker-compose up --build 