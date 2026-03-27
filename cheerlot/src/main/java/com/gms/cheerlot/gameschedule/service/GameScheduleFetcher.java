package com.gms.cheerlot.gameschedule.service;

import com.gms.cheerlot.cache.CacheDataService;
import com.gms.cheerlot.gameschedule.client.GameScheduleClient;
import com.gms.cheerlot.gameschedule.domain.GameSchedule;
import com.gms.cheerlot.gameschedule.dto.CrawlerGameScheduleResponse;
import com.gms.cheerlot.gameschedule.dto.CrawlerGameScheduleResponse.DailySchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameScheduleFetcher {

    private final GameScheduleClient gameScheduleClient;
    private final CacheDataService cacheDataService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("서버 시작 - 경기 일정 캐시 초기화");
        fetchAndCacheAll();
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void scheduledFetch() {
        log.info("스케줄 실행 - 경기 일정 갱신");
        fetchAndCacheAll();
    }

    private void fetchAndCacheAll() {
        gameScheduleClient.fetchRecentGameSchedules()
                .ifPresentOrElse(
                        this::cacheAllSchedules,
                        () -> log.warn("크롤링 서버 응답 없음 - 기존 캐시 유지")
                );
    }

    private void cacheAllSchedules(CrawlerGameScheduleResponse response) {
        for (DailySchedule daily : response.schedules()) {
            List<GameSchedule> schedules = toGameSchedules(daily);
            cacheDataService.saveGameSchedules(daily.date(), schedules);
            log.info("경기 일정 캐싱: date={}, games={}", daily.date(), schedules.size());
        }
        log.info("경기 일정 캐시 갱신 완료: {}일치", response.schedules().size());
    }

    private List<GameSchedule> toGameSchedules(DailySchedule daily) {
        return daily.games().stream()
                .map(game -> GameSchedule.builder()
                        .date(daily.date())
                        .homeTeamCode(game.homeTeamCode())
                        .awayTeamCode(game.awayTeamCode())
                        .homeStarterPitcherName(game.homeStarterPitcherName())
                        .awayStarterPitcherName(game.awayStarterPitcherName())
                        .build())
                .toList();
    }
}
