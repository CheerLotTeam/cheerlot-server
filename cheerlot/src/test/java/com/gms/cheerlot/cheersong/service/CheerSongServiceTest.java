package com.gms.cheerlot.cheersong.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheerSongServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("파일명으로 오디오 파일을 조회한다")
    void getAudioFile() throws IOException {
        // given
        Path audioFile = tempDir.resolve("lg10.mp3");
        Files.createFile(audioFile);

        CheerSongService cheerSongService = new CheerSongService(tempDir.toString());

        // when
        Resource resource = cheerSongService.getAudioFile("lg10.mp3");

        // then
        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo("lg10.mp3");
    }

    @Test
    @DisplayName("존재하지 않는 파일을 조회하면 예외가 발생한다")
    void getAudioFile_notFound() {
        // given
        CheerSongService cheerSongService = new CheerSongService(tempDir.toString());

        // when & then
        assertThatThrownBy(() -> cheerSongService.getAudioFile("unknown.mp3"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일을 찾을 수 없습니다");
    }
}
