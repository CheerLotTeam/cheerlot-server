package com.gms.cheerlot.gameschedule.service;

import com.gms.cheerlot.cache.CacheDataService;
import com.gms.cheerlot.gameschedule.client.GameScheduleClient;
import com.gms.cheerlot.gameschedule.domain.GameSchedule;
import com.gms.cheerlot.gameschedule.dto.CrawlerGameScheduleResponse;
import com.gms.cheerlot.gameschedule.dto.CrawlerGameScheduleResponse.DailySchedule;
import com.gms.cheerlot.lineup.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameScheduleFetcher {

    private final GameScheduleClient gameScheduleClient;
    private final CacheDataService cacheDataService;
    private final TeamRepository teamRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("서버 시작 - 경기 일정 캐시 초기화");
        fetchAndCacheAll();
    }

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
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
        Set<String> changedTeams = new HashSet<>();

        for (DailySchedule daily : response.schedules()) {
            List<GameSchedule> oldSchedules = cacheDataService.getGameSchedules(daily.date());
            List<GameSchedule> newSchedules = toGameSchedules(daily);

            collectChangedTeams(oldSchedules, newSchedules, changedTeams);

            cacheDataService.saveGameSchedules(daily.date(), newSchedules);
            log.info("경기 일정 캐싱: date={}, games={}", daily.date(), newSchedules.size());
        }

        for (String teamCode : changedTeams) {
            teamRepository.incrementLineupVersion(teamCode);
            log.info("경기 일정 변경으로 팀 {} lineupVersion 증가", teamCode);
        }

        log.info("경기 일정 캐시 갱신 완료: {}일치", response.schedules().size());
    }

    private void collectChangedTeams(List<GameSchedule> oldSchedules, List<GameSchedule> newSchedules, Set<String> changedTeams) {
        if (oldSchedules.size() != newSchedules.size()) {
            newSchedules.forEach(g -> {
                changedTeams.add(g.getHomeTeamCode());
                changedTeams.add(g.getAwayTeamCode());
            });
            return;
        }

        for (int i = 0; i < newSchedules.size(); i++) {
            GameSchedule oldGame = oldSchedules.get(i);
            GameSchedule newGame = newSchedules.get(i);

            if (!Objects.equals(oldGame.getHomeTeamCode(), newGame.getHomeTeamCode())
                    || !Objects.equals(oldGame.getAwayTeamCode(), newGame.getAwayTeamCode())
                    || !Objects.equals(oldGame.getHomeStarterPitcherName(), newGame.getHomeStarterPitcherName())
                    || !Objects.equals(oldGame.getAwayStarterPitcherName(), newGame.getAwayStarterPitcherName())) {
                changedTeams.add(newGame.getHomeTeamCode());
                changedTeams.add(newGame.getAwayTeamCode());
            }
        }
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
