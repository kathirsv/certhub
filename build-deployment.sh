#!/bin/bash

# CertHub Deployment Package Builder
# This script builds the application and creates a deployment ZIP file for AWS Amplify

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BUILD_DIR="target/deployment"
DIST_FILE="certhub-deployment.zip"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  CertHub Deployment Package Builder${NC}"
echo -e "${BLUE}========================================${NC}"

# Check required tools
check_tools() {
    echo -e "${YELLOW}Checking required tools...${NC}"
    
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}Maven is not installed. Please install it and try again.${NC}"
        exit 1
    fi
    
    if ! command -v zip &> /dev/null; then
        echo -e "${RED}zip is not installed. Please install it and try again.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ All required tools are available${NC}"
}

# Clean previous builds
clean_build() {
    echo -e "${YELLOW}Cleaning previous builds...${NC}"
    rm -rf "$BUILD_DIR"
    rm -f "$DIST_FILE"
    echo -e "${GREEN}✓ Clean completed${NC}"
}

# Build the application
build_application() {
    echo -e "${YELLOW}Building the application...${NC}"
    
    # Build with Maven
    mvn clean package -DskipTests -Dquarkus.package.type=uber-jar
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Build failed. Please check the errors above.${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Application built successfully${NC}"
}

# Create deployment structure
create_deployment_structure() {
    echo -e "${YELLOW}Creating deployment structure...${NC}"
    
    # Create deployment directory
    mkdir -p "$BUILD_DIR"
    
    # Copy the uber jar
    cp target/*-runner.jar "$BUILD_DIR/app.jar"
    
    # Create application.properties for production
    cat > "$BUILD_DIR/application.properties" << EOF
# Production configuration
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=\${DB_USERNAME}
quarkus.datasource.password=\${DB_PASSWORD}
quarkus.datasource.jdbc.url=\${DB_URL}
quarkus.hibernate-orm.database.generation=update

# Authentication configuration
app.admin.username=\${ADMIN_USERNAME:admin}
app.admin.password=\${ADMIN_PASSWORD:admin123}

# File upload configuration
quarkus.http.body.handle-file-uploads=true
quarkus.http.limits.max-body-size=16M

# CORS configuration
quarkus.http.cors=true
quarkus.http.cors.origins=*

# OpenAPI configuration
quarkus.smallrye-openapi.info-title=CertHub API
quarkus.smallrye-openapi.info-version=1.0.0
quarkus.smallrye-openapi.info-description=Certificate management system

# Static resources
quarkus.http.static-resources."/"=web/
quarkus.http.static-resources.path="/"

# AWS S3 configuration
aws.s3.bucket-name=\${S3_BUCKET_NAME:certhub-certificates}
aws.region=\${AWS_REGION:us-east-1}

# reCAPTCHA configuration
recaptcha.site-key=\${RECAPTCHA_SITE_KEY:6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI}
recaptcha.secret-key=\${RECAPTCHA_SECRET_KEY:6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe}

# Logging
quarkus.log.level=INFO
quarkus.log.console.enable=true
EOF
    
    # Create startup script
    cat > "$BUILD_DIR/start.sh" << 'EOF'
#!/bin/bash
echo "Starting CertHub..."
java -jar app.jar
EOF
    
    chmod +x "$BUILD_DIR/start.sh"
    
    # Create Amplify buildspec
    cat > "$BUILD_DIR/amplify.yml" << 'EOF'
version: 1
frontend:
  phases:
    build:
      commands:
        - echo "Build completed"
  artifacts:
    files:
      - '**/*'
EOF
    
    # Create package.json for Amplify to recognize it as a web app
    cat > "$BUILD_DIR/package.json" << 'EOF'
{
  "name": "certhub",
  "version": "1.0.0",
  "description": "Certificate management system",
  "main": "app.jar",
  "scripts": {
    "start": "./start.sh"
  },
  "dependencies": {},
  "engines": {
    "node": ">=14.0.0"
  }
}
EOF
    
    # Create deployment README
    cat > "$BUILD_DIR/README.md" << 'EOF'
# CertHub Deployment Package

This package contains the CertHub application ready for deployment to AWS Amplify.

## Contents

- `app.jar` - The main application JAR file
- `application.properties` - Production configuration
- `start.sh` - Startup script
- `amplify.yml` - AWS Amplify build configuration
- `package.json` - Node.js package configuration

## Environment Variables

Set these environment variables in your AWS Amplify environment:

- `ADMIN_USERNAME` - Admin username (default: admin)
- `ADMIN_PASSWORD` - Admin password (default: admin123)
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `DB_URL` - Database connection URL
- `S3_BUCKET_NAME` - S3 bucket name for file storage
- `AWS_REGION` - AWS region (default: us-east-1)
- `RECAPTCHA_SITE_KEY` - reCAPTCHA site key
- `RECAPTCHA_SECRET_KEY` - reCAPTCHA secret key

## Deployment

1. Upload this ZIP file to AWS Amplify
2. Configure the environment variables
3. Deploy the application

## Running Locally

To run the application locally:

```bash
./start.sh
```

The application will start on port 8080.
EOF
    
    echo -e "${GREEN}✓ Deployment structure created${NC}"
}

# Create deployment package
create_deployment_package() {
    echo -e "${YELLOW}Creating deployment package...${NC}"
    
    cd "$BUILD_DIR"
    zip -r "../$DIST_FILE" . -x "*.DS_Store"
    cd ..
    
    echo -e "${GREEN}✓ Deployment package created: $DIST_FILE${NC}"
}

# Show deployment info
show_deployment_info() {
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Deployment Package Ready!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Package: $DIST_FILE${NC}"
    echo -e "${GREEN}Size: $(du -h "$DIST_FILE" | cut -f1)${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo -e "${YELLOW}Next Steps:${NC}"
    echo -e "${YELLOW}1. Upload $DIST_FILE to AWS Amplify${NC}"
    echo -e "${YELLOW}2. Configure environment variables${NC}"
    echo -e "${YELLOW}3. Deploy the application${NC}"
    echo -e "${GREEN}========================================${NC}"
}

# Cleanup
cleanup() {
    echo -e "${YELLOW}Cleaning up...${NC}"
    rm -rf "$BUILD_DIR"
    echo -e "${GREEN}✓ Cleanup completed${NC}"
}

# Main deployment flow
main() {
    check_tools
    clean_build
    build_application
    create_deployment_structure
    create_deployment_package
    show_deployment_info
    cleanup
}

# Run main function
main "$@"