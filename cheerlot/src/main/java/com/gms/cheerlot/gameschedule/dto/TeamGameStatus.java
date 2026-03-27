package com.gms.cheerlot.gameschedule.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TeamGameStatus(
        LocalDate date,
        boolean hasGame,
        String opponentTeamCode,
        Boolean isHome,
        String starterPitcherName,
        String opponentStarterPitcherName
) {
    public static TeamGameStatus noGame(LocalDate date) {
        return new TeamGameStatus(date, false, null, null, null, null);
    }
}
