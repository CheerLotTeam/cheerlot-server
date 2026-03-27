package com.gms.cheerlot.gameschedule.service;

import com.gms.cheerlot.cache.CacheDataService;
import com.gms.cheerlot.gameschedule.domain.GameSchedule;
import com.gms.cheerlot.gameschedule.dto.GameScheduleResponse;
import com.gms.cheerlot.gameschedule.dto.TeamGameStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameScheduleServiceTest {

    @Mock
    private CacheDataService cacheDataService;

    @InjectMocks
    private GameScheduleService gameScheduleService;

    @Test
    @DisplayName("홈팀으로 경기가 있는 경우 상대팀과 홈 여부를 반환한다")
    void getRecentGames_homeTeam() {
        // given
        LocalDate today = LocalDate.now();
        GameSchedule game = GameSchedule.builder()
                .date(today)
                .homeTeamCode("lg")
                .awayTeamCode("kt")
                .homeStarterPitcherName("이민호")
                .awayStarterPitcherName("쿠에바스")
                .build();

        when(cacheDataService.getGameSchedules(today)).thenReturn(List.of(game));
        when(cacheDataService.getGameSchedules(today.minusDays(1))).thenReturn(List.of());
        when(cacheDataService.getGameSchedules(today.minusDays(2))).thenReturn(List.of());

        // when
        GameScheduleResponse response = gameScheduleService.getRecentGames("lg");

        // then
        assertThat(response.teamCode()).isEqualTo("lg");
        assertThat(response.recentGames()).hasSize(3);

        TeamGameStatus todayStatus = response.recentGames().get(0);
        assertThat(todayStatus.hasGame()).isTrue();
        assertThat(todayStatus.opponentTeamCode()).isEqualTo("kt");
        assertThat(todayStatus.isHome()).isTrue();
        assertThat(todayStatus.starterPitcherName()).isEqualTo("이민호");
        assertThat(todayStatus.opponentStarterPitcherName()).isEqualTo("쿠에바스");
    }

    @Test
    @DisplayName("원정팀으로 경기가 있는 경우 상대팀과 홈 여부를 반환한다")
    void getRecentGames_awayTeam() {
        // given
        LocalDate today = LocalDate.now();
        GameSchedule game = GameSchedule.builder()
                .date(today)
                .homeTeamCode("ob")
                .awayTeamCode("lg")
                .homeStarterPitcherName("곽빈")
                .awayStarterPitcherName("이민호")
                .build();

        when(cacheDataService.getGameSchedules(today)).thenReturn(List.of(game));
        when(cacheDataService.getGameSchedules(today.minusDays(1))).thenReturn(List.of());
        when(cacheDataService.getGameSchedules(today.minusDays(2))).thenReturn(List.of());

        // when
        GameScheduleResponse response = gameScheduleService.getRecentGames("lg");

        // then
        TeamGameStatus todayStatus = response.recentGames().get(0);
        assertThat(todayStatus.hasGame()).isTrue();
        assertThat(todayStatus.opponentTeamCode()).isEqualTo("ob");
        assertThat(todayStatus.isHome()).isFalse();
        assertThat(todayStatus.starterPitcherName()).isEqualTo("이민호");
        assertThat(todayStatus.opponentStarterPitcherName()).isEqualTo("곽빈");
    }

    @Test
    @DisplayName("경기가 없는 날은 hasGame이 false이다")
    void getRecentGames_noGame() {
        // given
        when(cacheDataService.getGameSchedules(any(LocalDate.class))).thenReturn(List.of());

        // when
        GameScheduleResponse response = gameScheduleService.getRecentGames("lg");

        // then
        assertThat(response.recentGames()).hasSize(3);
        response.recentGames().forEach(status -> {
            assertThat(status.hasGame()).isFalse();
            assertThat(status.opponentTeamCode()).isNull();
            assertThat(status.isHome()).isNull();
        });
    }

    @Test
    @DisplayName("3일 중 일부만 경기가 있는 경우를 정상 처리한다")
    void getRecentGames_partialGames() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate twoDaysAgo = today.minusDays(2);

        GameSchedule todayGame = GameSchedule.builder()
                .date(today)
                .homeTeamCode("lg")
                .awayTeamCode("kt")
                .build();
        GameSchedule twoDaysAgoGame = GameSchedule.builder()
                .date(twoDaysAgo)
                .homeTeamCode("ht")
                .awayTeamCode("lg")
                .build();

        when(cacheDataService.getGameSchedules(today)).thenReturn(List.of(todayGame));
        when(cacheDataService.getGameSchedules(today.minusDays(1))).thenReturn(List.of());
        when(cacheDataService.getGameSchedules(twoDaysAgo)).thenReturn(List.of(twoDaysAgoGame));

        // when
        GameScheduleResponse response = gameScheduleService.getRecentGames("lg");

        // then
        assertThat(response.recentGames().get(0).hasGame()).isTrue();
        assertThat(response.recentGames().get(1).hasGame()).isFalse();
        assertThat(response.recentGames().get(2).hasGame()).isTrue();
        assertThat(response.recentGames().get(2).opponentTeamCode()).isEqualTo("ht");
    }
}
