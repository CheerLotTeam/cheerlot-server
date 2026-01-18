package com.gms.cheerlot.cheersong.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheerSongServiceTest {

    @Test
    @DisplayName("파일명으로 오디오 URL을 생성한다")
    void getAudioUrl() {
        // given
        CheerSongService cheerSongService = new CheerSongService("https://example.r2.dev");

        // when
        String audioUrl = cheerSongService.getAudioUrl("lg10.mp3");

        // then
        assertThat(audioUrl).isEqualTo("https://example.r2.dev/lg10.mp3");
    }

    @Test
    @DisplayName("파일명이 null이면 null을 반환한다")
    void getAudioUrl_nullFileName() {
        // given
        CheerSongService cheerSongService = new CheerSongService("https://example.r2.dev");

        // when
        String audioUrl = cheerSongService.getAudioUrl(null);

        // then
        assertThat(audioUrl).isNull();
    }

    @Test
    @DisplayName("파일명이 빈 문자열이면 null을 반환한다")
    void getAudioUrl_blankFileName() {
        // given
        CheerSongService cheerSongService = new CheerSongService("https://example.r2.dev");

        // when
        String audioUrl = cheerSongService.getAudioUrl("  ");

        // then
        assertThat(audioUrl).isNull();
    }
}
