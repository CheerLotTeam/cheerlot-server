package com.gms.cheerlot.gameschedule.dto;

import java.util.List;

public record GameScheduleResponse(
        String teamCode,
        List<TeamGameStatus> recentGames
) {}
