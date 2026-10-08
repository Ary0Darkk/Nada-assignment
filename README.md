# Nada Network / People & Connections API

This is a simple HTTP API that stores people and their connections and answers how two people are connected up to 3 hops.

## Requirements Used
- Core Java 17+
- In-memory data store
- Built using Maven
- Uses standard built-in `com.sun.net.httpserver.HttpServer` (zero HTTP framework dependencies)
- Gson for JSON processing
- JUnit 5 for testing

## How to run the server
1. Ensure you have Java 17+ and Maven installed.
2. Build the project:
   ```bash
   mvn clean package
   ```
3. Run the application:
   ```bash
   mvn exec:java
   ```
   By default, the server runs on port **8080**.

## How to run the tests
```bash
mvn test
```
The tests cover typical API requirements, edge cases, the 3-hop shortest path logic, and unicode verification for scripts like Hindi.

## What I would do next with more time
1. Add structured logging instead of simple stdout to help in production environments.
2. Separate the `DataStore` interface and provide a scalable implementation using a persistent graph database (like Neo4j) or Postgres if data sizes outgrow memory limits.
3. Add API documentation using OpenAPI / Swagger to ease onboarding for other developers.
4. Set up CI/CD pipeline (e.g., GitHub Actions) for automated testing and deployment.
5. Create better modular separation for error handling (e.g. specialized exception types returning corresponding HTTP status codes).

## AI Tools Used
- Chatgpt and Gemini - free version used.
