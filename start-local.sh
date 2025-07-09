#!/bin/bash

echo "Starting CertHub Local Development Environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven is not installed. Please install Maven and try again.${NC}"
    exit 1
fi

# Clean and build the application
echo -e "${YELLOW}Building the application...${NC}"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed. Please check the errors above.${NC}"
    exit 1
fi

# Create necessary directories
mkdir -p data/minio

# Start MinIO for local S3 simulation
echo -e "${YELLOW}Starting MinIO...${NC}"
docker-compose up -d minio

# Wait for MinIO to be ready
echo -e "${YELLOW}Waiting for MinIO to be ready...${NC}"
sleep 10

# Start the application
echo -e "${YELLOW}Starting CertHub application...${NC}"
ADMIN_USERNAME=${ADMIN_USERNAME:-admin} ADMIN_PASSWORD=${ADMIN_PASSWORD:-admin123} docker-compose up -d certhub

# Wait for application to start
echo -e "${YELLOW}Waiting for application to start...${NC}"
sleep 15

# Check if application is running
if curl -s http://localhost:8080/q/health > /dev/null; then
    echo -e "${GREEN}✓ CertHub is running successfully!${NC}"
    echo -e "${GREEN}✓ Application URL: http://localhost:8080${NC}"
    echo -e "${GREEN}✓ Health Check: http://localhost:8080/q/health${NC}"
    echo -e "${GREEN}✓ OpenAPI Documentation: http://localhost:8080/q/swagger-ui${NC}"
    echo -e "${GREEN}✓ MinIO Console: http://localhost:9001 (minioadmin/minioadmin)${NC}"
    echo ""
    echo -e "${YELLOW}To stop the application, run: docker-compose down${NC}"
    echo -e "${YELLOW}To view logs, run: docker-compose logs -f certhub${NC}"
else
    echo -e "${RED}✗ Application failed to start. Check logs with: docker-compose logs certhub${NC}"
    exit 1
fi

# Show logs
echo -e "${YELLOW}Showing application logs (press Ctrl+C to stop watching logs):${NC}"
docker-compose logs -f certhub