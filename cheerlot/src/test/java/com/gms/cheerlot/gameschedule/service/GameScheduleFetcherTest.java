package com.gms.cheerlot.gameschedule.service;

import com.gms.cheerlot.cache.CacheDataService;
import com.gms.cheerlot.gameschedule.client.GameScheduleClient;
import com.gms.cheerlot.gameschedule.domain.GameSchedule;
import com.gms.cheerlot.gameschedule.dto.CrawlerGameScheduleResponse;
import com.gms.cheerlot.gameschedule.dto.CrawlerGameScheduleResponse.DailySchedule;
import com.gms.cheerlot.gameschedule.dto.CrawlerGameScheduleResponse.GameEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameScheduleFetcherTest {

    @Mock
    private GameScheduleClient gameScheduleClient;

    @Mock
    private CacheDataService cacheDataService;

    @InjectMocks
    private GameScheduleFetcher gameScheduleFetcher;

    @Captor
    private ArgumentCaptor<List<GameSchedule>> schedulesCaptor;

    @Test
    @DisplayName("크롤링 서버에서 3일치 일정을 받아 각 날짜별로 캐싱한다")
    void onApplicationReady_cachesAllDays() {
        // given
        LocalDate today = LocalDate.of(2026, 3, 27);
        LocalDate yesterday = LocalDate.of(2026, 3, 26);
        LocalDate twoDaysAgo = LocalDate.of(2026, 3, 25);

        CrawlerGameScheduleResponse response = new CrawlerGameScheduleResponse(List.of(
                new DailySchedule(today, List.of(
                        new GameEntry("lg", "kt", "이민호", "쿠에바스"),
                        new GameEntry("ob", "ss", null, null)
                )),
                new DailySchedule(yesterday, List.of()),
                new DailySchedule(twoDaysAgo, List.of(
                        new GameEntry("ht", "lg", "양현종", "이민호")
                ))
        ));

        when(gameScheduleClient.fetchRecentGameSchedules()).thenReturn(Optional.of(response));

        // when
        gameScheduleFetcher.onApplicationReady();

        // then
        verify(cacheDataService, times(3)).saveGameSchedules(any(LocalDate.class), any());

        verify(cacheDataService).saveGameSchedules(eq(today), schedulesCaptor.capture());
        assertThat(schedulesCaptor.getValue()).hasSize(2);

        verify(cacheDataService).saveGameSchedules(eq(yesterday), schedulesCaptor.capture());
        assertThat(schedulesCaptor.getValue()).isEmpty();

        verify(cacheDataService).saveGameSchedules(eq(twoDaysAgo), schedulesCaptor.capture());
        assertThat(schedulesCaptor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("크롤링 서버 응답 실패 시 캐시를 업데이트하지 않는다")
    void onApplicationReady_crawlerFailure() {
        // given
        when(gameScheduleClient.fetchRecentGameSchedules()).thenReturn(Optional.empty());

        // when
        gameScheduleFetcher.onApplicationReady();

        // then
        verify(cacheDataService, never()).saveGameSchedules(any(), any());
    }

    @Test
    @DisplayName("크롤링 서버 응답의 경기 정보가 GameSchedule 도메인으로 올바르게 변환된다")
    void onApplicationReady_correctMapping() {
        // given
        LocalDate date = LocalDate.of(2026, 3, 27);
        CrawlerGameScheduleResponse response = new CrawlerGameScheduleResponse(List.of(
                new DailySchedule(date, List.of(
                        new GameEntry("lg", "kt", "이민호", "쿠에바스")
                ))
        ));

        when(gameScheduleClient.fetchRecentGameSchedules()).thenReturn(Optional.of(response));

        // when
        gameScheduleFetcher.onApplicationReady();

        // then
        verify(cacheDataService).saveGameSchedules(eq(date), schedulesCaptor.capture());
        GameSchedule saved = schedulesCaptor.getValue().get(0);
        assertThat(saved.getDate()).isEqualTo(date);
        assertThat(saved.getHomeTeamCode()).isEqualTo("lg");
        assertThat(saved.getAwayTeamCode()).isEqualTo("kt");
        assertThat(saved.getHomeStarterPitcherName()).isEqualTo("이민호");
        assertThat(saved.getAwayStarterPitcherName()).isEqualTo("쿠에바스");
    }
}
