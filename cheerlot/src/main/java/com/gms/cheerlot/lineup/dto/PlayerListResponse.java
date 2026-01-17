package com.gms.cheerlot.lineup.dto;

import java.util.List;

public record PlayerListResponse(
        String teamCode,
        String role,
        List<PlayerResponse> players
) {}
