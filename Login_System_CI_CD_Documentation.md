# Login System CI/CD --- Complete End-to-End Documentation

## 1. Overview

This document explains the complete CI/CD flow implemented for the
**Project Documentation Login System**, starting from developer code
changes and ending with automatic deployment to AWS EC2.

### Technology Stack

-   **Backend:** Java 17, Spring Boot, Maven
-   **Frontend:** React, Vite, Node.js 20
-   **Database:** PostgreSQL 17
-   **Code Quality:** SonarCloud
-   **Containerization:** Docker
-   **Container Registry:** GitHub Container Registry (GHCR)
-   **CI/CD:** GitHub Actions
-   **Server:** AWS EC2 Ubuntu
-   **Deployment:** Docker Compose + SSH
-   **Reverse Proxy:** Nginx inside the frontend container

------------------------------------------------------------------------

# 2. Final CI/CD Architecture

``` text
Developer
   |
   | git push origin main
   v
GitHub Repository
   |
   v
GitHub Actions
   |
   +-----------------------------+
   |                             |
   v                             v
Backend CI                   Frontend CI
   |                             |
   +-- Maven tests               +-- npm ci
   +-- Maven build              +-- ESLint
   +-- SonarCloud               +-- npm build
   |                             |
   +-------------+---------------+
                 |
                 v
          Docker Build & Push
                 |
          +------+------+
          |             |
          v             v
     Backend Image   Frontend Image
          |             |
          +------+------+
                 |
                 v
              GHCR
                 |
                 v
       Continuous Deployment
                 |
          SSH to AWS EC2
                 |
                 v
        docker compose pull
                 |
                 v
        docker compose up -d
                 |
                 v
           Live Application
```

------------------------------------------------------------------------

# 3. Repository Structure

The project is organized approximately as follows:

``` text
Project-Documentation-login-system/
│
├── .github/
│   └── workflows/
│       └── login-system-ci.yml
│
├── auth-backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
├── auth-frontend/
│   └── auth-frontend/
│       ├── package.json
│       ├── package-lock.json
│       ├── Dockerfile
│       ├── nginx.conf
│       └── src/
│
├── docker-compose.yml
└── README.md
```

------------------------------------------------------------------------

# 4. Development Stage

The CI/CD process starts with normal application development.

A developer makes changes to:

-   Java/Spring Boot backend
-   React frontend
-   Database migration/configuration
-   Tests
-   Application configuration

The developer first validates the application locally.

Typical local checks:

``` bash
# Backend
cd auth-backend
mvn clean test
mvn clean package
```

Frontend:

``` bash
cd auth-frontend/auth-frontend
npm ci
npm run lint
npm run build
```

The goal is to catch errors before code reaches the CI pipeline.

------------------------------------------------------------------------

# 5. Git Workflow

After local development and testing:

``` bash
git status
```

Review the changed files.

Then:

``` bash
git add .
```

Commit the changes:

``` bash
git commit -m "Implement login system changes"
```

Push to the main branch:

``` bash
git push origin main
```

A push to `main` automatically starts GitHub Actions.

Pull requests targeting `main` also start the CI jobs, but deployment is
intentionally disabled for pull requests.

------------------------------------------------------------------------

# 6. GitHub Actions Trigger

The workflow starts with:

``` yaml
name: Login System CI

on:
  push:
    branches:
      - main

  pull_request:
    branches:
      - main
```

This means:

### Push to main

``` text
git push origin main
        |
        v
GitHub Actions starts
        |
        v
CI + Docker + Deployment
```

### Pull Request to main

``` text
Pull Request
     |
     v
CI validation
     |
     v
No deployment
```

Deployment is restricted with:

``` yaml
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
```

This prevents a pull request from deploying unapproved code to EC2.

------------------------------------------------------------------------

# 7. Backend CI

The backend CI job is responsible for:

1.  Checking out source code
2.  Setting up Java 17
3.  Running backend tests
4.  Building the backend
5.  Running SonarCloud analysis

Job:

``` yaml
backend-ci:
  name: Backend Build, Test and SonarCloud
  runs-on: ubuntu-latest
```

The working directory is:

``` yaml
defaults:
  run:
    working-directory: auth-backend
```

Therefore backend Maven commands automatically execute inside:

``` text
auth-backend/
```

------------------------------------------------------------------------

# 8. Checkout Backend Code

GitHub Actions uses:

``` yaml
- name: Checkout repository
  uses: actions/checkout@v4
  with:
    fetch-depth: 0
```

`actions/checkout` downloads the repository into the GitHub Actions
runner.

`fetch-depth: 0` downloads the complete Git history.

This is useful for SonarCloud analysis because code-quality analysis can
use Git history and branch information.

------------------------------------------------------------------------

# 9. Java 17 Setup

The backend requires Java 17.

The workflow uses:

``` yaml
- name: Set up Java 17
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: '17'
    cache: maven
```

This gives the GitHub Actions runner:

``` text
Java 17
Maven
Maven dependency caching
```

Maven caching helps reduce dependency download time between workflow
runs.

------------------------------------------------------------------------

# 10. Backend Unit/Integration Tests

The workflow executes:

``` bash
mvn clean test
```

This performs:

``` text
clean
  |
  v
Remove previous build output
  |
  v
test
  |
  v
Compile application
  |
  v
Run tests
```

If tests fail, the workflow stops.

Example:

``` text
Backend tests
      |
      +---- PASS ----> Continue
      |
      +---- FAIL ----> Stop pipeline
```

This is an important CI principle:

> Do not build and release code that does not pass automated tests.

------------------------------------------------------------------------

# 11. Backend Build

The workflow then runs:

``` bash
mvn clean package -DskipTests
```

The tests were already executed in the previous step, so this step
packages the application without running the tests again.

The result is a Spring Boot JAR under:

``` text
auth-backend/target/
```

For example:

``` text
target/
└── application.jar
```

The exact JAR name depends on the Maven project configuration.

------------------------------------------------------------------------

# 12. SonarCloud Integration

SonarCloud is used for static code analysis and quality checking.

The workflow uses:

``` yaml
- name: SonarCloud analysis
  env:
    SONAR_TOKEN: ${{ secrets.LOGIN_SYSTEM_SONAR_TOKEN }}
  run: >
    mvn clean verify sonar:sonar
    -Dsonar.projectKey=Sanket-devloper_Project-Documentation-login-system
    -Dsonar.organization=sanket-devloper
    -Dsonar.token=${SONAR_TOKEN}
    -Dsonar.qualitygate.wait=true
```

### Important SonarCloud Configuration

The following information is configured in the workflow:

``` text
Project Key:
Sanket-devloper_Project-Documentation-login-system

Organization:
sanket-devloper
```

The authentication token is NOT hardcoded.

It is stored in GitHub Secrets:

``` text
LOGIN_SYSTEM_SONAR_TOKEN
```

------------------------------------------------------------------------

# 13. Why GitHub Secrets Are Used

Never put sensitive credentials directly into:

-   Java code
-   YAML files
-   Dockerfiles
-   GitHub repository source code
-   README files

Instead:

``` yaml
${{ secrets.LOGIN_SYSTEM_SONAR_TOKEN }}
```

GitHub injects the secret during workflow execution.

The same principle is used for EC2 deployment credentials:

``` text
EC2_HOST
EC2_USERNAME
EC2_SSH_KEY
```

------------------------------------------------------------------------

# 14. SonarCloud Quality Gate

The workflow uses:

``` text
-Dsonar.qualitygate.wait=true
```

This tells Maven/SonarCloud to wait for the quality gate result.

Conceptually:

``` text
Code
 |
 v
SonarCloud
 |
 +--> Bugs
 +--> Vulnerabilities
 +--> Code Smells
 +--> Coverage
 +--> Quality Gate
 |
 +--> PASS --> Continue
 |
 +--> FAIL --> Stop
```

This makes code quality part of the release pipeline.

------------------------------------------------------------------------

# 15. Frontend CI

The frontend CI job validates the React application.

``` yaml
frontend-ci:
  name: Frontend Build and Lint
  runs-on: ubuntu-latest
```

Its working directory is:

``` text
auth-frontend/auth-frontend
```

------------------------------------------------------------------------

# 16. Node.js Setup

The workflow uses:

``` yaml
- name: Set up Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: npm
    cache-dependency-path: auth-frontend/auth-frontend/package-lock.json
```

The runner uses:

``` text
Node.js 20
npm
```

The package-lock file is used for npm dependency caching.

------------------------------------------------------------------------

# 17. Frontend Dependency Installation

The workflow runs:

``` bash
npm ci
```

`npm ci` is preferred in CI because it installs dependencies based on
the existing `package-lock.json`.

This makes CI dependency installation more predictable.

------------------------------------------------------------------------

# 18. ESLint

The workflow runs:

``` bash
npm run lint
```

ESLint checks the frontend source code for configured coding problems.

If linting fails:

``` text
Frontend lint
     |
     +---- FAIL
            |
            v
       Pipeline stops
```

------------------------------------------------------------------------

# 19. Frontend Production Build

The workflow runs:

``` bash
npm run build
```

For the Vite application this produces a production build, normally
under:

``` text
dist/
```

The production build is later copied into the Nginx Docker image.

------------------------------------------------------------------------

# 20. Docker Build and Publish

Docker publishing starts only after both CI jobs succeed.

``` yaml
needs:
  - backend-ci
  - frontend-ci
```

Therefore:

``` text
Backend CI ------+
                 |
                 +----> Docker Publish
                 |
Frontend CI -----+
```

If either CI job fails, Docker publishing does not start.

------------------------------------------------------------------------

# 21. Why Docker Is Used

Docker packages applications and their runtime environment into images.

Instead of installing the complete application stack manually on EC2, we
deploy containers.

The final EC2 environment contains:

``` text
PostgreSQL container
Backend container
Frontend/Nginx container
```

This makes deployment repeatable.

------------------------------------------------------------------------

# 22. Backend Dockerfile

The backend Dockerfile is:

``` dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Explanation

``` dockerfile
FROM eclipse-temurin:17-jre
```

Uses Java 17 runtime.

``` dockerfile
WORKDIR /app
```

Sets the working directory.

``` dockerfile
COPY target/*.jar app.jar
```

Copies the Maven-built JAR into the image.

``` dockerfile
EXPOSE 8080
```

Documents that the backend listens on port 8080.

``` dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Starts the Spring Boot application.

------------------------------------------------------------------------

# 23. Why the Backend JAR Must Be Built Before Docker

The Dockerfile contains:

``` dockerfile
COPY target/*.jar app.jar
```

A fresh GitHub Actions runner does not automatically have the
Maven-generated JAR.

Therefore the Docker job explicitly performs:

``` yaml
- name: Set up Java 17
  uses: actions/setup-java@v4
```

and:

``` yaml
- name: Build backend JAR
  working-directory: auth-backend
  run: mvn clean package -DskipTests
```

Then:

``` text
Maven
  |
  v
target/*.jar
  |
  v
docker build
  |
  v
Backend Docker Image
```

------------------------------------------------------------------------

# 24. Frontend Dockerfile

The frontend uses a multi-stage Docker build:

``` dockerfile
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### Stage 1 --- Build

``` text
Node.js 20
   |
   +-- npm ci
   |
   +-- npm run build
   |
   v
dist/
```

### Stage 2 --- Runtime

``` text
Nginx
   |
   +-- Copy dist/
   +-- Copy nginx.conf
   |
   v
Production frontend container
```

The final image does not need Node.js to serve the static production
files.

------------------------------------------------------------------------

# 25. Frontend Nginx Configuration

The frontend Nginx configuration is:

``` nginx
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    location /api/ {
        proxy_pass http://backend:8080/api/;

        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

This provides two important behaviors.

### Frontend requests

``` text
/
 |
 v
React static files
```

### API requests

``` text
/api/...
    |
    v
Nginx
    |
    v
backend:8080
```

The frontend container can reach the backend using the Docker Compose
service name:

``` text
backend
```

This is possible because Docker Compose creates a shared network for the
services.

------------------------------------------------------------------------

# 26. Frontend API Configuration

Development environment:

``` env
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_USE_MOCK_AUTH=false
```

Production environment:

``` env
VITE_API_BASE_URL=/api/v1
VITE_USE_MOCK_AUTH=false
```

Production uses:

``` text
/api/v1
```

instead of hardcoding an EC2 IP address.

The browser sends:

``` text
http://EC2-IP/api/v1/...
```

Nginx receives the request and proxies it to:

``` text
backend:8080/api/...
```

------------------------------------------------------------------------

# 27. GitHub Container Registry (GHCR)

The Docker images are published to GitHub Container Registry.

Image names:

``` text
ghcr.io/sanket-devloper/project-documentation-login-system-backend:latest

ghcr.io/sanket-devloper/project-documentation-login-system-frontend:latest
```

The workflow needs permission to publish packages:

``` yaml
permissions:
  contents: read
  packages: write
```

------------------------------------------------------------------------

# 28. GHCR Login from GitHub Actions

The workflow uses:

``` yaml
- name: Log in to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```

GitHub's automatically provided `GITHUB_TOKEN` is used by the workflow
to publish the images.

No personal GitHub password is stored in the workflow.

------------------------------------------------------------------------

# 29. Lowercase GHCR Image Owner

Container registry image names use a lowercase owner.

The workflow converts the repository owner to lowercase:

``` yaml
- name: Set lowercase image owner
  run: |
    echo "IMAGE_OWNER=$(echo '${{ github.repository_owner }}' | tr '[:upper:]' '[:lower:]')" >> $GITHUB_ENV
```

The resulting variable is:

``` text
IMAGE_OWNER
```

It is then used:

``` text
ghcr.io/${IMAGE_OWNER}/...
```

------------------------------------------------------------------------

# 30. Building Backend Docker Image

The workflow executes:

``` bash
docker build \
  -t ghcr.io/${IMAGE_OWNER}/project-documentation-login-system-backend:latest \
  ./auth-backend
```

This uses:

``` text
auth-backend/Dockerfile
```

and produces the backend image.

------------------------------------------------------------------------

# 31. Building Frontend Docker Image

The workflow executes:

``` bash
docker build \
  -t ghcr.io/${IMAGE_OWNER}/project-documentation-login-system-frontend:latest \
  ./auth-frontend/auth-frontend
```

This uses:

``` text
auth-frontend/auth-frontend/Dockerfile
```

------------------------------------------------------------------------

# 32. Push Docker Images

Backend:

``` bash
docker push \
  ghcr.io/${IMAGE_OWNER}/project-documentation-login-system-backend:latest
```

Frontend:

``` bash
docker push \
  ghcr.io/${IMAGE_OWNER}/project-documentation-login-system-frontend:latest
```

The flow becomes:

``` text
Source Code
    |
    v
Docker Build
    |
    v
Docker Image
    |
    v
GHCR
```

------------------------------------------------------------------------

# 33. AWS EC2 Manual Setup

Before Continuous Deployment can work, the EC2 server must be prepared.

The EC2 instance used for this project is:

``` text
Ubuntu
t3.micro
```

The server needs:

-   Docker
-   Docker Compose
-   SSH access
-   Internet access for pulling GHCR images
-   Correct Security Group rules

------------------------------------------------------------------------

# 34. EC2 Security Group

The Security Group controls incoming network traffic.

The required application ports for this setup are:

``` text
HTTP 80
SSH  22
```

HTTP:

``` text
TCP 80
Source: 0.0.0.0/0
```

This allows users to access the application from the internet.

During initial CD troubleshooting, SSH was changed to:

``` text
TCP 22
Source: 0.0.0.0/0
```

This allowed GitHub Actions runners to reach the EC2 server.

### Security Warning

Opening SSH to:

``` text
0.0.0.0/0
```

is not ideal for permanent production use.

After the pipeline is stable, SSH access should be restricted using a
more secure architecture, such as a controlled administration path,
VPN/bastion, or another AWS-supported access method.

------------------------------------------------------------------------

# 35. EC2 SSH Access

The EC2 user is:

``` text
ubuntu
```

The GitHub Actions deployment connects using an SSH private key.

The private key must never be committed to Git.

The public key was added to the EC2 user's:

``` text
~/.ssh/authorized_keys
```

SSH permissions were configured with:

``` bash
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

------------------------------------------------------------------------

# 36. Testing EC2 SSH from Windows

Before configuring GitHub Actions, SSH was tested manually.

PowerShell:

``` powershell
ssh -i "$env:USERPROFILE\.ssh\github-actions-deploy" ubuntu@YOUR_EC2_PUBLIC_IP
```

A successful login confirms that:

-   EC2 is reachable
-   SSH is running
-   the private key is valid
-   the public key is installed correctly
-   the username is correct

------------------------------------------------------------------------

# 37. Testing Port 22

From Windows PowerShell:

``` powershell
Test-NetConnection YOUR_EC2_PUBLIC_IP -Port 22
```

Expected:

``` text
TcpTestSucceeded : True
```

If this is false, check:

-   EC2 Security Group
-   Public IP
-   EC2 instance state
-   SSH service
-   Network ACLs
-   local/network firewall

------------------------------------------------------------------------

# 38. Docker Installation on EC2

Docker must be installed on the EC2 server.

After installation verify:

``` bash
docker --version
```

Verify Compose:

``` bash
docker compose version
```

Also verify Docker can run without requiring `sudo` for the deployment
user:

``` bash
docker ps
```

If the `ubuntu` user cannot access Docker, add it to the Docker group
and reconnect to SSH.

------------------------------------------------------------------------

# 39. PostgreSQL Container

PostgreSQL is deployed as a Docker container.

The Compose configuration uses:

``` yaml
postgres:
  image: postgres:17-alpine
  environment:
    POSTGRES_DB: authdb
    POSTGRES_USER: authuser
    POSTGRES_PASSWORD: authpass
```

The database uses a persistent Docker volume:

``` yaml
volumes:
  - auth_postgres_data:/var/lib/postgresql/data
```

This is important because the database data should survive container
recreation.

------------------------------------------------------------------------

# 40. PostgreSQL Health Check

The database uses:

``` yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U authuser -d authdb"]
  interval: 5s
  timeout: 5s
  retries: 10
```

The health check verifies that PostgreSQL is ready.

The backend depends on PostgreSQL being healthy:

``` yaml
depends_on:
  postgres:
    condition: service_healthy
```

Conceptually:

``` text
PostgreSQL starts
       |
       v
Health check
       |
       +---- Not ready ----> Wait
       |
       +---- Healthy ------> Backend starts
```

------------------------------------------------------------------------

# 41. Backend Production Container

The backend service uses the GHCR image:

``` yaml
backend:
  image: ghcr.io/sanket-devloper/project-documentation-login-system-backend:latest
```

Environment variables are supplied by Docker Compose:

``` yaml
environment:
  SERVER_PORT: 8080
  DB_URL: jdbc:postgresql://postgres:5432/authdb
  DB_USERNAME: authuser
  DB_PASSWORD: authpass
  SESSION_COOKIE_SECURE: "false"
  FRONTEND_URL: http://YOUR_EC2_PUBLIC_IP
```

The critical point is:

``` text
DB_URL = jdbc:postgresql://postgres:5432/authdb
```

Inside Docker Compose, `postgres` resolves to the PostgreSQL container.

Do NOT use:

``` text
localhost
```

for the database connection from the backend container.

Inside a backend container, `localhost` means the backend container
itself, not PostgreSQL.

------------------------------------------------------------------------

# 42. Spring Boot Environment Configuration

The application uses environment variables with local defaults.

Example:

``` yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/authdb}
    username: ${DB_USERNAME:authuser}
    password: ${DB_PASSWORD:authpass}

app:
  frontend-url: ${FRONTEND_URL:http://localhost:5173}
```

This allows the same application to work in different environments.

### Local

``` text
localhost
```

### Docker/EC2

``` text
postgres
```

and the EC2 public application URL.

------------------------------------------------------------------------

# 43. Frontend Production Container

The frontend service uses:

``` yaml
frontend:
  image: ghcr.io/sanket-devloper/project-documentation-login-system-frontend:latest
```

It exposes port 80:

``` yaml
ports:
  - "80:80"
```

Therefore:

``` text
Internet
   |
   v
EC2 Port 80
   |
   v
Frontend/Nginx Container Port 80
```

------------------------------------------------------------------------

# 44. Production Docker Compose

The production Compose file is:

``` yaml
services:
  postgres:
    image: postgres:17-alpine
    environment:
      POSTGRES_DB: authdb
      POSTGRES_USER: authuser
      POSTGRES_PASSWORD: authpass
    volumes:
      - auth_postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U authuser -d authdb"]
      interval: 5s
      timeout: 5s
      retries: 10

  backend:
    image: ghcr.io/sanket-devloper/project-documentation-login-system-backend:latest
    environment:
      SERVER_PORT: 8080
      DB_URL: jdbc:postgresql://postgres:5432/authdb
      DB_USERNAME: authuser
      DB_PASSWORD: authpass
      SESSION_COOKIE_SECURE: "false"
      FRONTEND_URL: http://YOUR_EC2_PUBLIC_IP
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    image: ghcr.io/sanket-devloper/project-documentation-login-system-frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  auth_postgres_data:
```

Replace:

``` text
YOUR_EC2_PUBLIC_IP
```

with the actual EC2 public IP.

------------------------------------------------------------------------

# 45. EC2 Deployment Directory

The deployment workflow uses:

``` bash
cd ~/login-system
```

Therefore the EC2 server contains:

``` text
/home/ubuntu/login-system/
```

with the production Compose configuration:

``` text
docker-compose.yml
```

The deployment does not need to build the application on EC2.

The Docker images have already been built by GitHub Actions.

------------------------------------------------------------------------

# 46. GHCR Authentication on EC2

The EC2 server must be able to pull private GHCR images.

A GitHub Personal Access Token was created with package read permission.

The server can authenticate with:

``` bash
docker login ghcr.io
```

Use:

``` text
Username: GitHub username
Password: GitHub Personal Access Token
```

The token should have the minimum permission required to download
packages, such as:

``` text
read:packages
```

Do not put the token in:

-   GitHub repository source
-   Dockerfile
-   docker-compose.yml
-   README
-   workflow YAML

------------------------------------------------------------------------

# 47. Manual Docker Pull Test

Before CD was enabled, the EC2 server was tested manually with Docker
pulls.

Example:

``` bash
docker pull ghcr.io/sanket-devloper/project-documentation-login-system-backend:latest
```

and:

``` bash
docker pull ghcr.io/sanket-devloper/project-documentation-login-system-frontend:latest
```

Successful pulls confirm:

``` text
EC2
 |
 +-- Internet access
 |
 +-- GHCR authentication
 |
 +-- Package permission
 |
 v
Docker images available
```

------------------------------------------------------------------------

# 48. First Manual Deployment

The first deployment can be performed manually:

``` bash
cd ~/login-system
```

Then:

``` bash
docker compose pull
```

Then:

``` bash
docker compose up -d
```

Check:

``` bash
docker compose ps
```

Expected services:

``` text
postgres    Up (healthy)
backend     Up
frontend    Up
```

------------------------------------------------------------------------

# 49. Application Verification

Open the EC2 public IP in a browser:

``` text
http://YOUR_EC2_PUBLIC_IP
```

The request flow is:

``` text
Browser
   |
   v
EC2 :80
   |
   v
Frontend/Nginx
   |
   +------ Static React application
   |
   +------ /api/*
             |
             v
         Backend :8080
             |
             v
        PostgreSQL :5432
```

------------------------------------------------------------------------

# 50. Continuous Deployment Configuration

The deployment job is:

``` yaml
deploy:
  name: Deploy to AWS EC2
  runs-on: ubuntu-latest

  if: github.event_name == 'push' && github.ref == 'refs/heads/main'

  needs:
    - docker-publish
```

This gives deployment two important conditions.

### Condition 1 --- Docker publishing must succeed

``` yaml
needs:
  - docker-publish
```

### Condition 2 --- Only push to main can deploy

``` yaml
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
```

Therefore:

``` text
Pull Request
   |
   v
CI
   |
   X
No deployment
```

while:

``` text
Push to main
   |
   v
CI
   |
   v
Docker
   |
   v
Deployment
```

------------------------------------------------------------------------

# 51. GitHub Repository Secrets

The deployment uses these GitHub Secrets:

``` text
LOGIN_SYSTEM_SONAR_TOKEN
EC2_HOST
EC2_USERNAME
EC2_SSH_KEY
```

### LOGIN_SYSTEM_SONAR_TOKEN

Used for SonarCloud authentication.

### EC2_HOST

Contains the EC2 public host/IP.

### EC2_USERNAME

Contains:

``` text
ubuntu
```

### EC2_SSH_KEY

Contains the private SSH key used by GitHub Actions to connect to EC2.

Never print or commit this private key.

------------------------------------------------------------------------

# 52. SSH Deployment Action

The workflow uses:

``` yaml
uses: appleboy/ssh-action@v1.2.2
```

Configuration:

``` yaml
with:
  host: ${{ secrets.EC2_HOST }}
  username: ${{ secrets.EC2_USERNAME }}
  key: ${{ secrets.EC2_SSH_KEY }}
```

The GitHub Actions runner connects to EC2 using SSH.

------------------------------------------------------------------------

# 53. Deployment Script

The deployment script is:

``` bash
cd ~/login-system

echo "Pulling latest Docker images..."
docker compose pull

echo "Restarting application..."
docker compose up -d

echo "Removing unused Docker images..."
docker image prune -f

echo "Deployment completed successfully!"

docker compose ps
```

------------------------------------------------------------------------

# 54. What `docker compose pull` Does

This command:

``` bash
docker compose pull
```

checks the image definitions in `docker-compose.yml` and pulls the
latest versions from GHCR.

For this project:

``` text
GHCR
 |
 +--> backend:latest
 |
 +--> frontend:latest
```

are downloaded to EC2.

------------------------------------------------------------------------

# 55. What `docker compose up -d` Does

The command:

``` bash
docker compose up -d
```

creates/recreates containers as necessary using the updated images.

`-d` means detached mode.

The terminal does not stay attached to the application processes.

------------------------------------------------------------------------

# 56. Why Database Data Is Not Lost

The PostgreSQL container uses:

``` yaml
volumes:
  - auth_postgres_data:/var/lib/postgresql/data
```

The Docker volume persists independently of the PostgreSQL container.

Therefore:

``` text
Container recreated
       |
       v
PostgreSQL starts
       |
       v
Existing volume mounted
       |
       v
Existing database data remains
```

### Important

Never run this casually on production:

``` bash
docker compose down -v
```

The `-v` option can remove the database volume and therefore delete the
persisted database data.

------------------------------------------------------------------------

# 57. Docker Image Cleanup

The deployment runs:

``` bash
docker image prune -f
```

This removes unused Docker images.

It helps prevent old unused images from consuming disk space.

It does not remove the currently used images.

------------------------------------------------------------------------

# 58. Deployment Verification

The workflow finishes with:

``` bash
docker compose ps
```

This allows the deployment log to show the current container state.

Successful deployment should contain:

``` text
Deployment completed successfully!
```

and healthy/running services.

------------------------------------------------------------------------

# 59. Complete Release Flow

The complete release process is:

``` text
1. Developer writes code
          |
          v
2. Developer runs local tests
          |
          v
3. Developer commits code
          |
          v
4. git push origin main
          |
          v
5. GitHub Actions starts
          |
          +---------------------+
          |                     |
          v                     v
   Backend CI              Frontend CI
          |                     |
          v                     v
   Maven test               npm ci
          |                     |
          v                     v
   Maven build              ESLint
          |                     |
          v                     v
   SonarCloud               npm build
          |                     |
          +----------+----------+
                     |
                     v
             Docker Publish
                     |
             +-------+-------+
             |               |
             v               v
       Backend Image    Frontend Image
             |               |
             +-------+-------+
                     |
                     v
                    GHCR
                     |
                     v
                 Deploy Job
                     |
                     v
                 SSH → EC2
                     |
                     v
            docker compose pull
                     |
                     v
            docker compose up -d
                     |
                     v
             Application LIVE
```

------------------------------------------------------------------------

# 60. Failure Handling in the Pipeline

The pipeline intentionally stops when an important stage fails.

### Backend test failure

``` text
Backend test FAIL
       |
       v
Pipeline stops
```

### Frontend lint failure

``` text
ESLint FAIL
    |
    v
Pipeline stops
```

### SonarCloud quality gate failure

``` text
Quality Gate FAIL
       |
       v
Pipeline stops
```

### Docker build failure

``` text
Docker build FAIL
       |
       v
No deployment
```

### Docker push failure

``` text
GHCR push FAIL
       |
       v
No deployment
```

### SSH deployment failure

``` text
SSH/EC2 deployment FAIL
       |
       v
GitHub Actions reports deployment failure
```

This protects the production server from many bad releases.

------------------------------------------------------------------------

# 61. Real Example of a Release

Suppose a developer changes:

``` text
LoginController.java
```

and updates a React login component.

The developer executes:

``` bash
git add .
git commit -m "Improve login validation"
git push origin main
```

Then:

``` text
GitHub receives commit
        |
        v
Backend tests
        |
        v
Backend build
        |
        v
SonarCloud
        |
        v
Frontend lint
        |
        v
Frontend build
        |
        v
Docker backend build
        |
        v
Docker frontend build
        |
        v
Push images to GHCR
        |
        v
SSH to EC2
        |
        v
Pull new images
        |
        v
Restart containers
        |
        v
New login system version LIVE
```

No manual Docker build is required on the EC2 server after CD is
configured.

------------------------------------------------------------------------

# 62. Local Docker vs CI Docker vs Production Docker

There are three different responsibilities.

## Local development

Developer runs and tests the application locally.

``` text
Developer machine
 |
 +-- Java
 +-- Node.js
 +-- PostgreSQL/Docker
```

## CI

GitHub Actions validates and packages the application.

``` text
GitHub Runner
 |
 +-- Java 17
 +-- Maven
 +-- Node 20
 +-- Docker
 +-- SonarCloud
```

## Production

EC2 runs the already-built containers.

``` text
AWS EC2
 |
 +-- PostgreSQL container
 +-- Backend container
 +-- Frontend/Nginx container
```

The production server does not need to run Maven or npm builds.

------------------------------------------------------------------------

# 63. Why Build in CI Instead of EC2

The recommended flow is:

``` text
Source Code
     |
     v
CI validates code
     |
     v
Docker image created
     |
     v
Image stored in registry
     |
     v
EC2 pulls tested image
```

Instead of:

``` text
EC2
 |
 +-- git pull
 +-- mvn build
 +-- npm build
 +-- docker build
```

Keeping builds in CI gives a cleaner separation:

``` text
CI = Build and Validate

CD = Deploy
```

------------------------------------------------------------------------

# 64. Important Environment Difference

Local backend database:

``` text
jdbc:postgresql://localhost:5432/authdb
```

Docker backend database:

``` text
jdbc:postgresql://postgres:5432/authdb
```

Why?

Docker Compose provides service discovery by service name.

The service:

``` yaml
postgres:
```

is reachable from the backend using:

``` text
postgres
```

rather than:

``` text
localhost
```

------------------------------------------------------------------------

# 65. Current Session Configuration

The backend uses session-based authentication.

The session cookie configuration includes:

``` yaml
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: ${SESSION_COOKIE_SECURE:false}
        same-site: lax
```

For the current HTTP-based EC2 deployment:

``` text
SESSION_COOKIE_SECURE=false
```

was used.

For a proper HTTPS production deployment, this should be revisited and
normally changed to:

``` text
SESSION_COOKIE_SECURE=true
```

after TLS/HTTPS is configured.

------------------------------------------------------------------------

# 66. Current Security Considerations

The current setup is suitable for learning and an initial deployment,
but several areas should be improved before calling it hardened
production infrastructure.

### 1. SSH

Current temporary configuration:

``` text
22 -> 0.0.0.0/0
```

Should eventually be restricted.

### 2. Database credentials

Current Compose file contains database credentials directly:

``` text
authuser
authpass
```

For production, move sensitive values into a secure secret-management
mechanism or protected environment configuration.

### 3. HTTPS

Current application is accessed through HTTP.

Production should use:

``` text
HTTPS :443
```

with TLS.

### 4. Session cookie

After HTTPS is enabled:

``` text
SESSION_COOKIE_SECURE=true
```

should be used.

### 5. Docker image tags

Current deployment uses:

``` text
:latest
```

A stronger production strategy is to use immutable tags such as:

``` text
:1.0.0
```

or Git commit SHA tags.

### 6. Rollback

A future improvement should provide a quick rollback to the previous
image version.

------------------------------------------------------------------------

# 67. Important Commands Reference

## Git

``` bash
git status
git add .
git commit -m "message"
git push origin main
```

## Backend

``` bash
cd auth-backend
mvn clean test
mvn clean package
```

## Frontend

``` bash
cd auth-frontend/auth-frontend
npm ci
npm run lint
npm run build
```

## Docker

``` bash
docker --version
docker compose version
docker ps
docker compose ps
docker compose pull
docker compose up -d
docker image prune -f
```

## EC2 deployment directory

``` bash
cd ~/login-system
```

## SSH test

``` powershell
ssh -i "$env:USERPROFILE\.ssh\github-actions-deploy" ubuntu@YOUR_EC2_PUBLIC_IP
```

## Port test

``` powershell
Test-NetConnection YOUR_EC2_PUBLIC_IP -Port 22
```

------------------------------------------------------------------------

# 68. Troubleshooting Guide

## Problem: `mvn` not found

Check Java/Maven setup.

In GitHub Actions, `setup-java` provides the Java environment and Maven
is available on the Ubuntu runner.

Locally verify:

``` bash
java -version
mvn -version
```

------------------------------------------------------------------------

## Problem: Docker build says `target/*.jar` does not exist

The backend JAR was not built before Docker build.

Run:

``` bash
mvn clean package -DskipTests
```

before:

``` bash
docker build ...
```

The CI workflow already performs this in the Docker publishing job.

------------------------------------------------------------------------

## Problem: GHCR rejects image name because of uppercase letters

Use the lowercase owner conversion:

``` bash
IMAGE_OWNER=$(echo '${{ github.repository_owner }}' | tr '[:upper:]' '[:lower:]')
```

------------------------------------------------------------------------

## Problem: EC2 cannot pull GHCR image

Check:

``` bash
docker login ghcr.io
```

Then test:

``` bash
docker pull ghcr.io/sanket-devloper/project-documentation-login-system-backend:latest
```

Verify that the GitHub token has package read permission.

------------------------------------------------------------------------

## Problem: GitHub Actions SSH timeout

Typical error:

``` text
dial tcp ***:22: i/o timeout
```

Check the EC2 Security Group.

SSH must be allowed to port:

``` text
22/TCP
```

Also verify:

``` text
EC2 instance is Running
EC2 public IP is correct
EC2_HOST secret is correct
```

------------------------------------------------------------------------

## Problem: `docker compose` backend cannot connect to PostgreSQL

Check the backend DB URL.

Inside Docker Compose it should use:

``` text
jdbc:postgresql://postgres:5432/authdb
```

not:

``` text
jdbc:postgresql://localhost:5432/authdb
```

------------------------------------------------------------------------

## Problem: Application works locally but not through EC2

Check:

``` bash
docker compose ps
```

Then inspect logs:

``` bash
docker compose logs backend
```

``` bash
docker compose logs frontend
```

``` bash
docker compose logs postgres
```

Also verify EC2 Security Group port 80.

------------------------------------------------------------------------

# 69. Do Not Delete the Database Volume Accidentally

Avoid:

``` bash
docker compose down -v
```

unless intentionally destroying the database volume.

Prefer:

``` bash
docker compose up -d
```

for normal deployments.

------------------------------------------------------------------------

# 70. Final GitHub Actions Workflow

The final workflow is:

``` yaml
name: Login System CI

on:
  push:
    branches:
      - main

  pull_request:
    branches:
      - main

jobs:

  # ==========================================
  # BACKEND CI
  # ==========================================

  backend-ci:
    name: Backend Build, Test and SonarCloud
    runs-on: ubuntu-latest

    defaults:
      run:
        working-directory: auth-backend

    steps:

      - name: Checkout repository
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: maven

      - name: Run backend tests
        run: mvn clean test

      - name: Build backend
        run: mvn clean package -DskipTests

      - name: SonarCloud analysis
        env:
          SONAR_TOKEN: ${{ secrets.LOGIN_SYSTEM_SONAR_TOKEN }}
        run: >
          mvn clean verify sonar:sonar
          -Dsonar.projectKey=Sanket-devloper_Project-Documentation-login-system
          -Dsonar.organization=sanket-devloper
          -Dsonar.token=${SONAR_TOKEN}
          -Dsonar.qualitygate.wait=true


  # ==========================================
  # FRONTEND CI
  # ==========================================

  frontend-ci:
    name: Frontend Build and Lint
    runs-on: ubuntu-latest

    defaults:
      run:
        working-directory: auth-frontend/auth-frontend

    steps:

      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: auth-frontend/auth-frontend/package-lock.json

      - name: Install dependencies
        run: npm ci

      - name: Run ESLint
        run: npm run lint

      - name: Build frontend
        run: npm run build


  # ==========================================
  # DOCKER BUILD & PUSH
  # ==========================================

  docker-publish:
    name: Build and Push Docker Images
    runs-on: ubuntu-latest

    needs:
      - backend-ci
      - frontend-ci

    permissions:
      contents: read
      packages: write

    steps:

      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: maven

      - name: Build backend JAR
        working-directory: auth-backend
        run: mvn clean package -DskipTests

      - name: Set lowercase image owner
        run: |
          echo "IMAGE_OWNER=$(echo '${{ github.repository_owner }}' | tr '[:upper:]' '[:lower:]')" >> $GITHUB_ENV

      - name: Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build backend Docker image
        run: |
          docker build \
            -t ghcr.io/${IMAGE_OWNER}/project-documentation-login-system-backend:latest \
            ./auth-backend

      - name: Build frontend Docker image
        run: |
          docker build \
            -t ghcr.io/${IMAGE_OWNER}/project-documentation-login-system-frontend:latest \
            ./auth-frontend/auth-frontend

      - name: Push backend Docker image
        run: |
          docker push \
            ghcr.io/${IMAGE_OWNER}/project-documentation-login-system-backend:latest

      - name: Push frontend Docker image
        run: |
          docker push \
            ghcr.io/${IMAGE_OWNER}/project-documentation-login-system-frontend:latest


  # ==========================================
  # CONTINUOUS DEPLOYMENT
  # ==========================================

  deploy:
    name: Deploy to AWS EC2
    runs-on: ubuntu-latest

    if: github.event_name == 'push' && github.ref == 'refs/heads/main'

    needs:
      - docker-publish

    steps:

      - name: Deploy application to EC2
        uses: appleboy/ssh-action@v1.2.2
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USERNAME }}
          key: ${{ secrets.EC2_SSH_KEY }}

          script: |
            cd ~/login-system

            echo "Pulling latest Docker images..."
            docker compose pull

            echo "Restarting application..."
            docker compose up -d

            echo "Removing unused Docker images..."
            docker image prune -f

            echo "Deployment completed successfully!"

            docker compose ps
```

------------------------------------------------------------------------

# 71. Final Checklist

## Application

-   [x] Backend builds successfully
-   [x] Backend tests execute
-   [x] Frontend builds successfully
-   [x] ESLint executes
-   [x] Production frontend environment configured
-   [x] Backend environment variables configured

## SonarCloud

-   [x] SonarCloud project created/configured
-   [x] Project key configured
-   [x] Organization configured
-   [x] Sonar token stored as GitHub Secret
-   [x] Quality gate checked by CI

## Docker

-   [x] Backend Dockerfile created
-   [x] Frontend Dockerfile created
-   [x] Nginx configuration created
-   [x] Docker images build successfully
-   [x] Backend image pushed to GHCR
-   [x] Frontend image pushed to GHCR

## AWS

-   [x] EC2 instance created
-   [x] Ubuntu server configured
-   [x] Docker installed
-   [x] Docker Compose available
-   [x] Security Group configured
-   [x] Port 80 available
-   [x] SSH access configured
-   [x] Production Compose file created
-   [x] PostgreSQL volume configured
-   [x] GHCR authentication configured
-   [x] Manual Docker pull tested
-   [x] Manual application deployment tested

## GitHub Actions

-   [x] GitHub Actions workflow created
-   [x] Backend CI configured
-   [x] Frontend CI configured
-   [x] Docker publishing configured
-   [x] EC2 secrets configured
-   [x] SSH deployment configured
-   [x] Deployment restricted to pushes to main
-   [x] Automatic EC2 deployment tested successfully

------------------------------------------------------------------------

# 72. Current Status

The Login System now has a working end-to-end CI/CD pipeline:

``` text
                     LOGIN SYSTEM
                          |
                          v
                   Developer Code
                          |
                          v
                    Git Push / PR
                          |
                          v
                 +------------------+
                 |  GitHub Actions  |
                 +------------------+
                    /            \
                   /              \
                  v                v
           Backend CI         Frontend CI
              |                    |
              v                    v
          Maven Test          npm ci
              |                    |
              v                    v
          Maven Build           ESLint
              |                    |
              v                    v
          SonarCloud           npm build
              |                    |
              +---------+----------+
                        |
                        v
                  Docker Build
                        |
                +-------+-------+
                |               |
                v               v
           Backend Image   Frontend Image
                |               |
                +-------+-------+
                        |
                        v
                       GHCR
                        |
                        v
                 SSH to AWS EC2
                        |
                        v
              docker compose pull
                        |
                        v
              docker compose up -d
                        |
                        v
                  LIVE SYSTEM
                        |
              +---------+---------+
              |         |         |
              v         v         v
           Nginx     Spring     PostgreSQL
          Frontend   Backend      Database
```

## One-line explanation for a technical discussion

> "We implemented a GitHub Actions CI/CD pipeline where every change is
> tested and quality-checked with Maven, ESLint and SonarCloud, then
> packaged into Docker images, published to GitHub Container Registry,
> and automatically deployed to an AWS EC2 server using SSH and Docker
> Compose."

------------------------------------------------------------------------

# 73. Recommended Next Improvements

The current pipeline is functional. The next production-level
improvements should be:

1.  **Replace `latest` Docker tags with immutable Git SHA/version
    tags.**
2.  **Add deployment health checks.**
3.  **Add automatic rollback if the new deployment fails.**
4.  **Configure a domain name.**
5.  **Configure HTTPS/SSL.**
6.  **Move database credentials to secure secrets.**
7.  **Harden SSH access instead of `0.0.0.0/0`.**
8.  **Add Docker container health checks for backend/frontend.**
9.  **Add deployment notifications.**
10. **Add a staging environment before production.**

The current implementation should be considered the **working baseline
CI/CD pipeline**, while the above items are the next step toward a
hardened production deployment.
