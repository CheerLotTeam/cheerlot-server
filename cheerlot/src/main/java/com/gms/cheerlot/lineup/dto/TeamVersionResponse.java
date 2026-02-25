package com.gms.cheerlot.lineup.dto;

public record TeamVersionResponse(
        String teamCode,
        int playersVersion,
        int lineupVersion
) {}
