package com.iamsb97.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
@RequiredArgsConstructor
public class ShortenUrlRequestDto {
    @NonNull String longUrl;
    @Nullable Long ttlSeconds;
}
