package com.gms.cheerlot.gameschedule.service;

import com.gms.cheerlot.cache.CacheDataService;
import com.gms.cheerlot.gameschedule.domain.GameSchedule;
import com.gms.cheerlot.gameschedule.dto.GameScheduleResponse;
import com.gms.cheerlot.gameschedule.dto.TeamGameStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameScheduleService {

    private final CacheDataService cacheDataService;

    public GameScheduleResponse getRecentGames(String teamCode) {
        LocalDate today = LocalDate.now();
        List<TeamGameStatus> recentGames = List.of(
                getTeamGameStatus(teamCode, today),
                getTeamGameStatus(teamCode, today.plusDays(1)),
                getTeamGameStatus(teamCode, today.plusDays(2))
        );
        return new GameScheduleResponse(teamCode, recentGames);
    }

    private TeamGameStatus getTeamGameStatus(String teamCode, LocalDate date) {
        List<GameSchedule> schedules = cacheDataService.getGameSchedules(date);

        return schedules.stream()
                .filter(g -> g.getHomeTeamCode().equals(teamCode) || g.getAwayTeamCode().equals(teamCode))
                .findFirst()
                .map(game -> {
                    boolean isHome = game.getHomeTeamCode().equals(teamCode);
                    return new TeamGameStatus(
                            date,
                            true,
                            isHome ? game.getAwayTeamCode() : game.getHomeTeamCode(),
                            isHome,
                            isHome ? game.getHomeStarterPitcherName() : game.getAwayStarterPitcherName(),
                            isHome ? game.getAwayStarterPitcherName() : game.getHomeStarterPitcherName()
                    );
                })
                .orElse(TeamGameStatus.noGame(date));
    }
}
