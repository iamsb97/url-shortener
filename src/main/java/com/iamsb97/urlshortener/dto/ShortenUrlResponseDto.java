package com.iamsb97.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

@Getter
@Setter
@RequiredArgsConstructor
public class ShortenUrlResponseDto {
    @NonNull String shortUrl;
    @NonNull String shortKey;
    Long expiresAt;
}
