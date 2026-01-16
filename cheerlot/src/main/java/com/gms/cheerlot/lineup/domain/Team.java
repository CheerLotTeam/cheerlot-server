package com.gms.cheerlot.lineup.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class Team {
    private String teamCode;
    private String teamName;
    private boolean hasTodayGame;
    private String opponentTeamCode;
    private String starterPitcherName;
    private LocalDate lastGameDate;
    private boolean isSeasonEnded;
    private LocalDateTime updatedAt;
}
