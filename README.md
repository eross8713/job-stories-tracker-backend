# Job Stories Tracker - Backend

This is the backend for the Job Stories Tracker project (Spring Boot + Postgres).

Quick start

1) Start Postgres (Docker):

```bash
cd docker
docker compose up -d
```

This will start a Postgres container named `jobstories-postgres` with DB `jobstories` and user `jobstories` (password `jobstories`). If you changed the host port to `5433`, connect to that port instead of `5432`.

2) Run the backend:

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

3) Run tests:

```bash
cd backend
./mvnw test
```

Notes

- For local development we use `spring.jpa.hibernate.ddl-auto=update` so Hibernate will create tables automatically.
- If you prefer to run Postgres locally (Homebrew) instead of Docker, update `backend/src/main/resources/application.properties` with the correct JDBC URL and credentials.
