package com.gms.cheerlot.lineup.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Player {
    private String playerCode;
    private String teamCode;
    private String name;
    private Integer backNumber;
    private String position;
    private String batThrow;
    private Integer battingOrder;
    private boolean starter;
}
