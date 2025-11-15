# URL Shortener

A minimal Spring Boot service to shorten long URLs and redirect them efficiently using PostgreSQL and Redis.

---

## Tech Stack

- **Java 17**, Spring Boot (no Spring Data)
- **PostgreSQL** for persistence  
- **Redis (Jedis)** for caching and maintaining key pool
- **HikariCP** for DB connection pooling

---

## Features

- Converts long URLs into short keys  
- 302 redirects for short URLs  
- Redis caching for performance  
- Retry logic for key collisions  
- Clean separation: Controller → Service → Repository

---

## Usage

1. Configure `application.properties` with DB and Redis.
2. Start PostgreSQL and Redis.
3. Run the app:

   ```bash
   mvn spring-boot:run
   ```

4. Use `POST /api/shorten` and `GET /{shortKey}` to shorten and resolve URLs.

---

## Next Steps

- Add logging
- Add authentication to APIs
- Docker support  
