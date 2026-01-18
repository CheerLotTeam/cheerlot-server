package com.gms.cheerlot.lineup.service;

import com.gms.cheerlot.cheersong.domain.CheerSong;
import com.gms.cheerlot.cheersong.repository.CheerSongRepository;
import com.gms.cheerlot.cheersong.service.CheerSongService;
import com.gms.cheerlot.lineup.domain.Player;
import com.gms.cheerlot.lineup.dto.PlayerListResponse;
import com.gms.cheerlot.lineup.dto.PlayerResponse;
import com.gms.cheerlot.lineup.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private CheerSongRepository cheerSongRepository;

    @Mock
    private CheerSongService cheerSongService;

    @InjectMocks
    private PlayerService playerService;

    @Test
    @DisplayName("팀 코드로 전체 선수 목록을 조회한다")
    void getPlayers() {
        // given
        Player player = createPlayer("lg10", "lg", "이민호", 10, 1, false);
        when(playerRepository.findByTeamCode("lg")).thenReturn(List.of(player));
        when(cheerSongRepository.findByPlayerCode("lg10")).thenReturn(List.of());

        // when
        PlayerListResponse response = playerService.getPlayers("lg");

        // then
        assertThat(response.teamCode()).isEqualTo("lg");
        assertThat(response.role()).isNull();
        assertThat(response.players()).hasSize(1);
        assertThat(response.players().get(0).name()).isEqualTo("이민호");
    }

    @Test
    @DisplayName("선발 라인업을 타순 순서로 조회한다")
    void getStarterLineup() {
        // given
        Player player1 = createPlayer("lg7", "lg", "김철수", 7, 2, true);
        Player player2 = createPlayer("lg10", "lg", "이민호", 10, 1, true);

        when(playerRepository.findStartersByTeamCode("lg")).thenReturn(List.of(player1, player2));
        when(cheerSongRepository.findByPlayerCode("lg7")).thenReturn(List.of());
        when(cheerSongRepository.findByPlayerCode("lg10")).thenReturn(List.of());

        // when
        PlayerListResponse response = playerService.getStarterLineup("lg");

        // then
        assertThat(response.teamCode()).isEqualTo("lg");
        assertThat(response.role()).isEqualTo("starter");
        assertThat(response.players()).hasSize(2);
        assertThat(response.players().get(0).battingOrder()).isEqualTo(1);
        assertThat(response.players().get(1).battingOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("선수 코드로 선수 정보를 조회한다")
    void getPlayer() {
        // given
        Player player = createPlayer("lg10", "lg", "이민호", 10, 1, true);
        CheerSong cheerSong = CheerSong.builder()
                .title("이민호 응원가")
                .lyrics("이민호 이민호~")
                .audioFileName("lg10.mp3")
                .build();

        when(playerRepository.findByPlayerCode("lg10")).thenReturn(Optional.of(player));
        when(cheerSongRepository.findByPlayerCode("lg10")).thenReturn(List.of(cheerSong));
        when(cheerSongService.getAudioUrl("lg10.mp3")).thenReturn("https://example.r2.dev/lg10.mp3");

        // when
        PlayerResponse response = playerService.getPlayer("lg10");

        // then
        assertThat(response.playerCode()).isEqualTo("lg10");
        assertThat(response.name()).isEqualTo("이민호");
        assertThat(response.cheerSongs()).hasSize(1);
        assertThat(response.cheerSongs().get(0).title()).isEqualTo("이민호 응원가");
        assertThat(response.cheerSongs().get(0).audioUrl()).isEqualTo("https://example.r2.dev/lg10.mp3");
    }

    @Test
    @DisplayName("존재하지 않는 선수 코드로 조회하면 예외가 발생한다")
    void getPlayer_notFound() {
        // given
        when(playerRepository.findByPlayerCode("unknown")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> playerService.getPlayer("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("선수를 찾을 수 없습니다");
    }

    private Player createPlayer(String playerCode, String teamCode, String name,
                                  Integer backNumber, Integer battingOrder, boolean isStarter) {
        return Player.builder()
                .playerCode(playerCode)
                .teamCode(teamCode)
                .name(name)
                .backNumber(backNumber)
                .position("외야수")
                .batThrow("우타좌투")
                .battingOrder(battingOrder)
                .isStarter(isStarter)
                .build();
    }
}
