#!/bin/bash
# Deploy script to z1Server

SERVER="z1Server"
SERVER_USER="sindre"
APP_DIR="/opt/ship-o-hoi"
echo "Building application..."
mvn clean package -DskipTests -q

echo "Copying files to ${SERVER}..."
ssh ${SERVER_USER}@${SERVER} "sudo mkdir -p ${APP_DIR}"

# Copy jar to home dir first, then sudo move to app dir
scp target/ship-o-hoi.jar ${SERVER_USER}@${SERVER}:~/ship-o-hoi.jar
ssh ${SERVER_USER}@${SERVER} "sudo mv ~/ship-o-hoi.jar ${APP_DIR}/"

# Copy service file
scp scripts/ship-o-hoi.service ${SERVER_USER}@${SERVER}:~/ship-o-hoi.service
ssh ${SERVER_USER}@${SERVER} "sudo mv ~/ship-o-hoi.service /etc/systemd/system/"

# Restart service
echo "Restarting service..."
ssh ${SERVER_USER}@${SERVER} "sudo systemctl daemon-reload && sudo systemctl restart ship-o-hoi"

echo "Deploy complete!"
ssh ${SERVER_USER}@${SERVER} "sudo systemctl status ship-o-hoi --no-pager"
