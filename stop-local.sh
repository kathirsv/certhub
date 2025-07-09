#!/bin/bash

echo "Stopping CertHub Local Development Environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Stop all services
echo -e "${YELLOW}Stopping all services...${NC}"
docker-compose down

# Remove containers and volumes (optional)
read -p "Do you want to remove all data (containers, volumes, and local files)? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Removing all data...${NC}"
    docker-compose down -v
    rm -rf data/
    rm -rf localstack/
    echo -e "${GREEN}✓ All data removed successfully!${NC}"
else
    echo -e "${GREEN}✓ Services stopped. Data preserved.${NC}"
fi

echo -e "${GREEN}✓ CertHub local environment stopped.${NC}"