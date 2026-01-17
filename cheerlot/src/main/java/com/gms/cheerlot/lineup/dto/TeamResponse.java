package com.gms.cheerlot.lineup.dto;

import java.time.LocalDate;

public record TeamResponse(
        String teamCode,
        boolean isSeasonEnded,
        LocalDate lastGameDate,
        boolean hasTodayGame,
        String opponentTeamCode,
        String starterPitcherName
) {}
