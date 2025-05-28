package com.iamsb97.urlshortener.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ShortenerService {

    public String shorten(String url) {
        return "http://localhost:8080/abcdefg";
    }

    public String retrieve(String string) {
        return "https://example.com";
    }

    public HttpStatus delete(String shortURL) {
        return HttpStatus.OK;
    }

}
