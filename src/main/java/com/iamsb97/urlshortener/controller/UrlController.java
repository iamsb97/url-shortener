package com.iamsb97.urlshortener.controller;

import java.util.Map;

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
    private ShortenerService shortener;

    public UrlController(ShortenerService shortener) {
        this.shortener = shortener;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<String> shortenUrl (@RequestBody Map<String,String> requestBody) {
        String longUrl = requestBody.get("url");
        if (longUrl == null) {
            return ResponseEntity.badRequest().body("JSON body of the request doesn't exist or couldn't be parsed.");
        }
        String shortUrl = shortener.shorten(longUrl);
        if (shortUrl == null) {
            return ResponseEntity.internalServerError().body("Couldn't shorten the URL at this time. Please try again.");
        }
        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{urlHash}")
    public ResponseEntity<String> getUrl (@PathVariable String urlHash) {
        String longUrl = shortener.retrieve(urlHash);
        if (longUrl == null) {
            return new ResponseEntity<String>("This short URL doesn't exist.", HttpStatus.NOT_FOUND);
        }
        HttpHeaders header = new HttpHeaders();
        header.add("Location", longUrl);
        return new ResponseEntity<>(header, HttpStatus.FOUND);
    }

    @DeleteMapping("/api/delete")
    public ResponseEntity<String> deleteUrl (@RequestBody Map<String,String> requestBody) {
        String shortUrl = requestBody.get("url");
        if (shortUrl == null) {
            return ResponseEntity.badRequest().body("JSON body of the request doesn't exist or couldn't be parsed.");
        }
        String urlKey = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);
        Boolean status = shortener.delete(urlKey);
        if (status != true) {
            return new ResponseEntity<String>("URL couldn't be deleted", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("URL deleted successfully.");
    }
}
