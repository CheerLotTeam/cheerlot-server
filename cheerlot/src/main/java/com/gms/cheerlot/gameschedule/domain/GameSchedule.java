package com.gms.cheerlot.gameschedule.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameSchedule {
    private LocalDate date;
    private String homeTeamCode;
    private String awayTeamCode;
    private String homeStarterPitcherName;
    private String awayStarterPitcherName;
}
