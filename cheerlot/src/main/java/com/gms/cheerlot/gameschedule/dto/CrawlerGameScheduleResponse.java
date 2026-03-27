package com.gms.cheerlot.gameschedule.dto;

import java.time.LocalDate;
import java.util.List;

public record CrawlerGameScheduleResponse(
        List<DailySchedule> schedules
) {
    public record DailySchedule(
            LocalDate date,
            List<GameEntry> games
    ) {}

    public record GameEntry(
            String homeTeamCode,
            String awayTeamCode,
            String homeStarterPitcherName,
            String awayStarterPitcherName
    ) {}
}
