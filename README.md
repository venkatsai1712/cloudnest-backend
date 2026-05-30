# CloudNest

CloudNest is a secure and scalable cloud storage application built with Spring Boot and MinIO. It allows users to upload, download, and manage files and folders with ease.

## Features

- **File Management**: Upload, download, and delete files.
- **Folder Structure**: Organize files into folders and subfolders.
- **Security**: Robust authentication and authorization using Spring Security.
- **Cloud Storage**: Seamless integration with MinIO for scalable object storage.
- **Presigned URLs**: Securely upload and download files via presigned URLs.

## Tech Stack

- **Backend**: Java 21, Spring Boot 3+ (Spring Security, Spring Data JPA)
- **Database**: PostgreSQL (for metadata), H2 (for testing)
- **Object Storage**: MinIO
- **Build Tool**: Maven

## Getting Started

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/venkatsai/CloudNest.git
    ```
2.  **Configure Environment**: Update `src/main/resources/application.properties` or set environment variables for database and MinIO credentials.
3.  **Run with Docker**:
    ```bash
    docker-compose up -d
    ```
4.  **Build and Run**:
    ```bash
    ./mvnw spring-boot:run
    ```

## API Documentation

The application exposes RESTful APIs for file and folder operations. Authentication is required for most endpoints.

- `POST /api/auth/signup`: User registration
- `POST /api/auth/signin`: User login
- `POST /api/file/upload`: Upload a file
- `GET /api/files`: List user files
- `POST /api/folder`: Create a new folder
