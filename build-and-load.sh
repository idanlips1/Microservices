#!/bin/bash

echo "Building and loading images into KIND cluster..."

# Build stocks service
echo "Building stocks service..."
cd stocks
./mvnw clean package -DskipTests
docker build -t stocks:latest .
echo "Loading stocks image into KIND..."
kind load docker-image stocks:latest

# Build capital gains service
echo "Building capital gains service..."
cd ../capitalgains
./mvnw clean package -DskipTests
docker build -t capitalgains:latest .
echo "Loading capital gains image into KIND..."
kind load docker-image capitalgains:latest

# Build NGINX image
echo "Building NGINX image..."
cd ../nginx
docker build -t nginx-custom:latest .
echo "Loading NGINX image into KIND..."
kind load docker-image nginx-custom:latest

echo "All images built and loaded successfully!" 