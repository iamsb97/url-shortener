package com.iamsb97.urlshortener.controller;

import com.iamsb97.urlshortener.dto.ShortenUrlRequestDto;
import com.iamsb97.urlshortener.dto.ShortenUrlResponseDto;
import com.iamsb97.urlshortener.repository.UrlRepository;
import com.iamsb97.urlshortener.service.CacheService;
import com.iamsb97.urlshortener.service.ShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class UrlControllerTest {

    private static final String LONG_URL = "https://example.com/path";
    private static final String SHORT_KEY = "abc123";

    @Test
    void shortenReturnsServiceResponse() {
        ShortenUrlResponseDto expectedResponse = new ShortenUrlResponseDto(
                "http://localhost:8080/" + SHORT_KEY, SHORT_KEY);
        UrlController controller = new UrlController(new StubShortenerService(expectedResponse, null, false));

        ResponseEntity<?> response = controller.shortenUrl(new ShortenUrlRequestDto(LONG_URL));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());
    }

    @Test
    void getUrlRedirectsToResolvedUrl() {
        UrlController controller = new UrlController(new StubShortenerService(null, LONG_URL, false));

        ResponseEntity<String> response = controller.getUrl(SHORT_KEY);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(LONG_URL, response.getHeaders().getFirst("Location"));
    }

    @Test
    void getUrlReturnsNotFoundForMissingKey() {
        UrlController controller = new UrlController(new StubShortenerService(null, null, false));

        ResponseEntity<String> response = controller.getUrl(SHORT_KEY);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("This short URL doesn't exist.", response.getBody());
    }

    @Test
    void deleteUrlReturnsNoContentWhenDeleted() {
        UrlController controller = new UrlController(new StubShortenerService(null, null, true));

        ResponseEntity<?> response = controller.deleteUrl(SHORT_KEY);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteUrlReturnsNotFoundWhenMissing() {
        UrlController controller = new UrlController(new StubShortenerService(null, null, false));

        ResponseEntity<?> response = controller.deleteUrl(SHORT_KEY);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private static class StubShortenerService extends ShortenerService {

        private final ShortenUrlResponseDto shortenResponse;
        private final String retrieveResponse;
        private final boolean deleteResponse;

        StubShortenerService(ShortenUrlResponseDto shortenResponse, String retrieveResponse, boolean deleteResponse) {
            super((CacheService) null, (UrlRepository) null, null);
            this.shortenResponse = shortenResponse;
            this.retrieveResponse = retrieveResponse;
            this.deleteResponse = deleteResponse;
        }

        @Override
        public ShortenUrlResponseDto shorten(String url) {
            return shortenResponse;
        }

        @Override
        public String retrieve(String shortUrl) {
            return retrieveResponse;
        }

        @Override
        public Boolean delete(String shortUrl) {
            return deleteResponse;
        }
    }
}
