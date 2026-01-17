package com.gms.cheerlot.cheersong.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CheerSongService {

    private final String audioPath;

    public CheerSongService(@Value("${cheersong.audio.path}") String audioPath) {
        this.audioPath = audioPath;
    }

    public Resource getAudioFile(String fileName) {
        try {
            Path filePath = Paths.get(audioPath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            }
            throw new IllegalArgumentException("파일을 찾을 수 없습니다 : " + fileName);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다 : " + fileName, e);
        }
    }
}
