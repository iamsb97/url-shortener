package com.iamsb97.urlshortener.controller;

import com.iamsb97.urlshortener.dto.ShortenUrlRequestDto;
import com.iamsb97.urlshortener.dto.ShortenUrlResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.iamsb97.urlshortener.service.ShortenerService;

@RestController
public class UrlController {

    @Value("${spring.application.base-url}")
    private String baseUrl;
    private final ShortenerService shortener;

    public UrlController(ShortenerService shortener) {
        this.shortener = shortener;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl (@RequestBody ShortenUrlRequestDto requestBody) {
        String longUrl = requestBody.getLongUrl();
        if (longUrl == null) {
            return ResponseEntity.badRequest().body("JSON body of the request doesn't exist or couldn't be parsed.");
        }
        ShortenUrlResponseDto shortUrlResponse = shortener.shorten(longUrl);
        return ResponseEntity.ok(shortUrlResponse);
    }

    @GetMapping("/{urlHash}")
    public ResponseEntity<String> getUrl (@PathVariable String urlHash) {
        String longUrl = shortener.retrieve(urlHash);
        if (longUrl == null) {
            return new ResponseEntity<>("This short URL doesn't exist.", HttpStatus.NOT_FOUND);
        }
        HttpHeaders header = new HttpHeaders();
        header.add("Location", longUrl);
        return new ResponseEntity<>(header, HttpStatus.FOUND);
    }

    @DeleteMapping("/{urlHash}")
    public ResponseEntity<?> deleteUrl (@PathVariable String urlHash) {
        Boolean status = shortener.delete(urlHash);
        if (status != true) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
