package com.gms.cheerlot.cheersong.controller;

import com.gms.cheerlot.cheersong.service.CheerSongService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cheersongs")
@RequiredArgsConstructor
public class CheerSongController {

    private final CheerSongService cheerSongService;

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getCheerSongAudio(@PathVariable String fileName) {
        Resource audioFile = cheerSongService.getAudioFile(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(audioFile);
    }
}
