# Learning Path Generator 🎓

**Author:** Jeevan192  
**Date:** 2025-10-23  
**Status:** ✅ Production Ready

## Overview

An AI-powered Learning Path Generator that creates personalized learning curricula based on:
- Current skill level (Beginner/Intermediate/Advanced)
- Learning interests and goals
- Available weekly hours
- Target career position

## Features

✅ **JWT Authentication** - Secure user registration and login  
✅ **Interactive Quizzes** - 10 programming topics with skill assessment  
✅ **Personalized Learning Paths** - AI-generated curriculum (60-120 hours)  
✅ **PostgreSQL Database** - Persistent data storage  
✅ **Docker Deployment** - One-command setup  
✅ **REST API** - Clean, documented endpoints  

## Tech Stack

- **Backend:** Spring Boot 3.1.4 (Java 17)
- **Security:** JWT, BCrypt password encryption
- **Database:** PostgreSQL 15
- **Containerization:** Docker & Docker Compose
- **Build Tool:** Gradle 8.14.3

## Quick Start

### Prerequisites
- Docker Desktop installed and running
- PowerShell (Windows) or Bash (Linux/Mac)

### Installation

\\\powershell
# Clone repository
git clone https://github.com/Jeevan192/learningpathgenerator.git
cd learningpathgenerator

# Start application
docker-compose up --build -d

# Wait 60 seconds for startup, then test
Start-Sleep -Seconds 60
Invoke-RestMethod -Uri "http://localhost:8080/api/quiz/topics"
\\\

### Access
- **API Base URL:** http://localhost:8080
- **Database:** localhost:5432
  - Database: learningpath
  - Username: admin
  - Password: admin123

## API Documentation

### Authentication

#### Register User
\\\powershell
$body = @{
    username = "jeevan"
    password = "securepass123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
    -Method POST -ContentType "application/json" -Body $body
\\\

#### Login
\\\powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST -ContentType "application/json" -Body $body

$token = $response.token
\\\

### Quiz System

#### Get Available Topics
\\\powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/quiz/topics"
\\\

**Response:** Array of topic IDs
\\\json
["t-java-basics", "t-oop", "t-data-structures", ...]
\\\

#### Get Quiz
\\\powershell
$quiz = Invoke-RestMethod -Uri "http://localhost:8080/api/quiz/t-java-basics"
\\\

#### Submit Quiz
\\\powershell
$submitBody = @{
    answers = @(1, 0, 2, 1, 0)  # Answer indices
} | ConvertTo-Json

$result = Invoke-RestMethod -Uri "http://localhost:8080/api/quiz/t-java-basics/submit" `
    -Method POST -ContentType "application/json" -Body $submitBody
\\\

### Learning Path Generation

\\\powershell
$pathBody = @{
    name = "Jeevan"
    skillLevel = "INTERMEDIATE"
    interests = @("java", "spring", "microservices", "docker")
    target = "Senior Backend Developer"
    weeklyHours = 15
} | ConvertTo-Json

$headers = @{ "Authorization" = "Bearer $token" }

$path = Invoke-RestMethod -Uri "http://localhost:8080/api/learning-path/generate" `
    -Method POST -Headers $headers -ContentType "application/json" -Body $pathBody
\\\

**Response Example:**
\\\json
{
  "title": "Jeevan's Senior Backend Developer Learning Path (INTERMEDIATE)",
  "learnerName": "Jeevan",
  "skillLevel": "INTERMEDIATE",
  "weeklyHours": 15,
  "totalHours": 92,
  "estimatedWeeks": 7,
  "modules": [
    {
      "name": "Spring Boot Advanced",
      "hours": 20,
      "description": "Master Spring Boot microservices",
      "resources": ["https://spring.io/guides/..."]
    },
    ...
  ]
}
\\\

## Available Quiz Topics

1. **t-java-basics** - Java Syntax & Basics
2. **t-oop** - Object-Oriented Programming
3. **t-data-structures** - Data Structures & Algorithms
4. **t-spring** - Spring Framework Basics
5. **t-rest-api** - Building REST APIs
6. **t-database** - Database & SQL
7. **t-testing** - Unit Testing with JUnit
8. **t-git** - Version Control with Git
9. **t-docker** - Docker & Containers
10. **t-microservices** - Microservices Architecture

## Project Structure

\\\
learningpathgenerator/
├── src/main/java/com/example/learningpathgenerator/
│   ├── Controller/          # REST API endpoints
│   ├── Service/             # Business logic
│   ├── Repository/          # Database access
│   ├── Model/               # Entity classes
│   ├── dto/                 # Data Transfer Objects
│   ├── security/            # JWT & Authentication
│   └── config/              # Configuration classes
├── src/main/resources/
│   └── application.properties
├── docker-compose.yml       # Docker configuration
├── Dockerfile               # Application container
└── build.gradle             # Dependencies
\\\

## Docker Management

\\\powershell
# Start
docker-compose up -d

# Stop
docker-compose down

# View logs
docker logs -f learning-path-app

# Rebuild
docker-compose down -v
docker-compose up --build

# Database access
docker exec -it learning-path-db psql -U admin -d learningpath
\\\

## Development

### Local Build (without Docker)
\\\ash
./gradlew build
./gradlew bootRun
\\\

### Environment Variables
\\\properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/learningpath
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin123
JWT_SECRET=mySecretKeyForJWT123456789012345678901234567890
JWT_EXPIRATION_MS=86400000
\\\

## Troubleshooting

### Container won't start
\\\powershell
docker-compose down -v
docker system prune -af
docker-compose up --build
\\\

### Port already in use
\\\powershell
# Change ports in docker-compose.yml
ports:
  - "8081:8080"  # Use 8081 instead of 8080
\\\

### Database connection failed
\\\powershell
# Check if PostgreSQL container is running
docker ps | Select-String "learning-path"

# Check logs
docker logs learning-path-db
\\\

## Future Enhancements

- [ ] React/Vue frontend
- [ ] Progress tracking dashboard
- [ ] Email notifications
- [ ] Social sharing features
- [ ] Export to PDF
- [ ] Calendar integration
- [ ] Cloud deployment (AWS/Azure)
- [ ] CI/CD pipeline

## License

MIT License - See LICENSE file

## Contact

**GitHub:** [@Jeevan192](https://github.com/Jeevan192)  
**Repository:** https://github.com/Jeevan192/learningpathgenerator

---

**Built with ❤️ using Spring Boot and Docker**
