package com.iamsb97.urlshortener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class UrlShortenerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("urlshortener")
            .withUsername("urluser")
            .withPassword("urlpassword")
            .withInitScript("schema.sql");

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JedisPool jedisPool;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Test
    void shortenPersistsAndRedirects() throws Exception {
        String longUrl = "https://example.com/" + UUID.randomUUID();

        JsonNode shortened = shorten(longUrl);
        String shortKey = shortened.get("shortKey").asText();

        assertEquals("http://localhost:8080/" + shortKey, shortened.get("shortUrl").asText());

        HttpResponse<String> redirect = send(HttpRequest.newBuilder(urlFor("/" + shortKey)).GET().build());

        assertEquals(302, redirect.statusCode());
        assertEquals(longUrl, redirect.headers().firstValue("Location").orElseThrow());
    }

    @Test
    void shortenReturnsSameKeyForTheSameUrl() throws Exception {
        String longUrl = "https://example.com/" + UUID.randomUUID();

        String firstKey = shorten(longUrl).get("shortKey").asText();
        String secondKey = shorten(longUrl).get("shortKey").asText();

        assertEquals(firstKey, secondKey);
    }

    @Test
    void cacheMissLoadsUrlFromDatabaseAndRepopulatesCache() throws Exception {
        String longUrl = "https://example.com/" + UUID.randomUUID();
        String shortKey = shorten(longUrl).get("shortKey").asText();

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(shortKey, longUrl);
        }

        HttpResponse<String> redirect = send(HttpRequest.newBuilder(urlFor("/" + shortKey)).GET().build());

        assertEquals(302, redirect.statusCode());
        try (Jedis jedis = jedisPool.getResource()) {
            assertEquals(longUrl, jedis.get(shortKey));
        }
    }

    @Test
    void deleteRemovesShortUrl() throws Exception {
        String longUrl = "https://example.com/" + UUID.randomUUID();
        String shortKey = shorten(longUrl).get("shortKey").asText();

        HttpResponse<String> deleteResponse = send(HttpRequest.newBuilder(urlFor("/" + shortKey)).DELETE().build());
        HttpResponse<String> retrieveResponse = send(HttpRequest.newBuilder(urlFor("/" + shortKey)).GET().build());

        assertEquals(204, deleteResponse.statusCode());
        assertEquals(404, retrieveResponse.statusCode());
    }

    private JsonNode shorten(String longUrl) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(Map.of("longUrl", longUrl));
        HttpRequest request = HttpRequest.newBuilder(urlFor("/api/shorten"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = send(request);

        assertEquals(200, response.statusCode());
        return objectMapper.readTree(response.body());
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI urlFor(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
