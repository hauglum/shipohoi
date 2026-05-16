#!/bin/bash
# Ship O'Hoi Startup Script

APP_NAME="ship-o-hoi"
JAR_FILE="${APP_NAME}-0.0.1-SNAPSHOT.jar"
LOG_FILE="/var/log/ship-o-hoi.log"

# Change to app directory
cd /opt/ship-o-hoi

# Run the application
exec java -jar ${JAR_FILE} \
    --spring.profiles.active=prod \
    --server.port=8080 \
    >> ${LOG_FILE} 2>&1
