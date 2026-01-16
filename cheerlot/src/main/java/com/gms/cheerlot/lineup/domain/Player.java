package com.gms.cheerlot.lineup.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Player {
    private String playerCode;
    private String teamCode;
    private String name;
    private Integer backNumber;
    private String position;
    private String batThrow;
    private Integer battingOrder;
    private boolean isStarter;
}
