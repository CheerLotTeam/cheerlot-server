package com.gms.cheerlot.lineup.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Team {
    private String teamCode;
    private String teamName;
    @Deprecated
    private boolean hasTodayGame;
    @Deprecated
    private String opponentTeamCode;
    @Deprecated
    private String starterPitcherName;
    private LocalDate lastGameDate;
    private boolean seasonEnded;
    private int playersVersion;
    private int lineupVersion;
    private LocalDateTime updatedAt;
}
