# User Management Application

This is a Spring Boot application that provides a RESTful API for managing users.
It allows creating, retrieving, updating, and deleting users, as well as searching users based on date ranges.

## Features

- Create a new user
- Get all users with pagination
- Get an user by ID
- Update an existing user
- Partially update an existing user
- Delete an user by ID
- Search users based on a date range with pagination

## Technologies

- Java 17
- Spring Boot 2.7.0
- Spring Data JPA
- Spring Security
- MySQL
- Flyway
- Swagger OpenAPI
- Slf4j
- JWT Authentication

## Getting Started

### Prerequisites

- Java 17
- Any IDE
- Docker Compose



### Running the application and database

1. Clone the repository:
`
git clone https://github.com/kavasak119999/SpringREST.git
`

2. Build and start the Docker container:
`
docker-compose -f docker-compose.yml up
`
3. Run the application in your IDE


*The application uses port 8080*


### Swagger API Documentation
You can access the Swagger UI for API documentation at [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)




### Authentication
The application uses JWT authentication.
To authenticate, send a POST request to /api/auth/authenticate with the following request body:
```
{
  "email": "user@example.com",
  "password": "password"
}
```
The response will contain an access token and a refresh token.


### Use the access token in the Authorization header for subsequent requests:
`
Authorization: Bearer <access_token>
`

You can use tools like Postman or curl to interact with the API endpoints.
Refer to the Swagger API documentation for detailed information on the available endpoints and their request/response formats.
