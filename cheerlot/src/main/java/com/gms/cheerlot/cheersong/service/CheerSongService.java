package com.gms.cheerlot.cheersong.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CheerSongService {

    private final String audioBaseUrl;

    public CheerSongService(@Value("${cheersong.audio.base-url}") String audioBaseUrl) {
        this.audioBaseUrl = audioBaseUrl;
    }

    public String getAudioUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        return audioBaseUrl + "/" + fileName;
    }
}
