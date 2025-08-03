#!/bin/bash

# Face Attendance Backend Server Startup Script
# Usage: ./start-server.sh [port]

# Default port
PORT=${1:-8081}

# JAR file name
JAR_FILE="face-attendance-backend-1.0.0.jar"

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file '$JAR_FILE' not found!"
    echo "Please run 'mvn clean package -DskipTests' first to build the JAR file."
    exit 1
fi

echo "Starting Face Attendance Backend Server..."
echo "Port: $PORT"
echo "JAR: $JAR_FILE"
echo "Time: $(date)"
echo "----------------------------------------"

# Start the server
java -jar "$JAR_FILE" --server.port="$PORT"
