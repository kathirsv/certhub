# CertHub - Certificate Management System

CertHub is a simple web application for storing, managing, and sharing certificates. Built with Quarkus (Java) and featuring a responsive Material Design UI with 'CH' logo.

## Features

- **Single User Application**: Configured via VM arguments for credentials
- **Certificate Upload**: Upload PDF/JPEG certificates up to 15MB
- **Secure Storage**: Files stored in AWS S3 with restricted access
- **Shareable Links**: Generate public links for certificate viewing
- **reCAPTCHA Protection**: Prevents automated access
- **Material Design UI**: Responsive, mobile-friendly interface
- **Session-based Authentication**: Simple HTTP session authentication

## Quick Start

### Local Development

1. **Prerequisites**:
   - Java 17+
   - Maven 3.6+
   - Docker and Docker Compose
   - AWS CLI (for deployment)

2. **Start Local Environment**:
   ```bash
   ./start-local.sh
   ```
   
   This will:
   - Build the application
   - Start LocalStack (for S3 simulation)
   - Create necessary S3 buckets
   - Start the application on http://localhost:8080

3. **Stop Local Environment**:
   ```bash
   ./stop-local.sh
   ```

### Manual Development

1. **Build the application**:
   ```bash
   mvn clean package
   ```

2. **Run in development mode**:
   ```bash
   mvn quarkus:dev -DADMIN_USERNAME=admin -DADMIN_PASSWORD=your-password
   ```

3. **Run tests**:
   ```bash
   mvn test
   ```

## AWS Deployment

### Build Deployment Package

```bash
./build-deployment.sh
```

This creates `certhub-deployment.zip` ready for manual upload to AWS Amplify.

### Environment Variables

Set these in your AWS Amplify environment:

```bash
ADMIN_USERNAME=your-admin-username
ADMIN_PASSWORD=your-admin-password
AWS_REGION=us-east-1
RECAPTCHA_SITE_KEY=your-site-key
RECAPTCHA_SECRET_KEY=your-secret-key
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password
DB_URL=jdbc:postgresql://your-db-host:5432/certhub
S3_BUCKET_NAME=your-s3-bucket-name
```

### Manual Deployment Steps

1. Run `./build-deployment.sh` to create the deployment package
2. Upload `certhub-deployment.zip` to AWS Amplify
3. Configure the environment variables
4. Deploy the application

## Configuration

### Application Properties

Key configuration properties:

```properties
# Database
quarkus.datasource.db-kind=h2
quarkus.datasource.username=sa
quarkus.datasource.password=
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb

# Authentication
app.admin.username=${ADMIN_USERNAME:admin}
app.admin.password=${ADMIN_PASSWORD:admin123}

# File upload
quarkus.http.limits.max-body-size=16M

# AWS S3
aws.s3.bucket-name=${S3_BUCKET_NAME:certhub-certificates}
aws.region=${AWS_REGION:us-east-1}

# reCAPTCHA
recaptcha.site-key=${RECAPTCHA_SITE_KEY:test-key}
recaptcha.secret-key=${RECAPTCHA_SECRET_KEY:test-secret}
```

## API Endpoints

### Authentication

- `GET /api/auth/status` - Check authentication status
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Certificate Management (Authenticated)

- `GET /api/certificates` - List all certificates
- `POST /api/certificates/upload` - Upload certificate
- `GET /api/certificates/{id}` - Get certificate details
- `PUT /api/certificates/{id}` - Update certificate
- `DELETE /api/certificates/{id}` - Delete certificate

### Public Access

- `GET /api/public/certificate/{shareableId}` - View certificate (requires reCAPTCHA)
- `GET /api/public/certificate/{shareableId}/download` - Download certificate (requires reCAPTCHA)

## Security Features

- **Session Authentication**: All user endpoints require valid HTTP sessions
- **reCAPTCHA Protection**: Login and public certificate viewing require reCAPTCHA
- **S3 Access Control**: Files stored in S3 with restricted access policies
- **VM Arguments**: Admin credentials configured via environment variables
- **HTTPS Only**: Production deployment uses HTTPS

## Technology Stack

- **Backend**: Quarkus (Java 17)
- **Frontend**: HTML5, CSS3, JavaScript, Bootstrap 5
- **Database**: H2 (local), PostgreSQL (production)
- **File Storage**: AWS S3
- **Authentication**: HTTP Sessions with Servlet Filters
- **Styling**: Material Design with Bootstrap
- **Deployment**: AWS Amplify
- **Testing**: JUnit 5, RestAssured

## File Structure

```
certhub/
├── src/
│   ├── main/
│   │   ├── java/com/certhub/
│   │   │   ├── entity/          # Database entities
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── resource/        # REST endpoints
│   │   │   └── service/         # Business logic
│   │   ├── resources/
│   │   │   ├── META-INF/
│   │   │   │   └── resources/   # JWT keys
│   │   │   ├── application.properties
│   │   │   └── import.sql
│   │   └── web/                 # Frontend files
│   │       ├── css/
│   │       ├── js/
│   │       └── index.html
│   └── test/                    # Test files
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── start-local.sh
├── stop-local.sh
└── deploy-aws.sh
```

## Testing

The application includes comprehensive tests:

- **Unit Tests**: Entity and service testing
- **Integration Tests**: REST endpoint testing
- **Security Tests**: Authentication and authorization testing

Run tests with:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes and add tests
4. Run tests to ensure they pass
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue in the repository.