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
public class URLController {

    @Value("${spring.application.base-url}")
    private String baseURL;
    private ShortenerService shortener;

    public URLController(ShortenerService shortener) {
        this.shortener = shortener;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<String> shortenURL (@RequestBody Map<String,String> requestBody) {
        String longURL = requestBody.get("url");
        if (longURL == null) {
            return ResponseEntity.badRequest().body("JSON body of the request doesn't exist or couldn't be parsed.");
        }
        String shortURL = shortener.shorten(longURL);
        return ResponseEntity.ok(shortURL);
    }

    @GetMapping("/{urlHash}")
    public ResponseEntity<String> getURL (@PathVariable String urlHash) {
        String longURL = shortener.retrieve(urlHash);
        if (longURL == null) {
            return new ResponseEntity<String>("This short URL doesn't exist.", HttpStatus.NOT_FOUND);
        }
        HttpHeaders header = new HttpHeaders();
        header.add("Location", longURL);
        return new ResponseEntity<>(header, HttpStatus.FOUND);
    }

    @DeleteMapping("/api/delete")
    public ResponseEntity<String> deleteURL (@RequestBody Map<String,String> requestBody) {
        String shortURL = requestBody.get("url");
        if (shortURL == null) {
            return ResponseEntity.badRequest().body("JSON body of the request doesn't exist or couldn't be parsed.");
        }
        String urlKey = shortURL.substring(shortURL.lastIndexOf('/') + 1);
        Boolean status = shortener.delete(urlKey);
        if (status != true) {
            return new ResponseEntity<String>("URL couldn't be deleted", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok("URL deleted successfully.");
    }
}
