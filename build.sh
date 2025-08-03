#!/bin/bash

# Build script for Render deployment
echo "Starting build process..."

# Check if Maven is available
if command -v mvn &> /dev/null; then
    echo "Using system Maven..."
    mvn clean package -DskipTests
elif [ -f "./mvnw" ]; then
    echo "Using Maven wrapper..."
    chmod +x ./mvnw
    ./mvnw clean package -DskipTests
else
    echo "Error: Neither Maven nor Maven wrapper found!"
    exit 1
fi

echo "Build completed successfully!"
