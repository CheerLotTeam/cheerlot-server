package com.gms.cheerlot.lineup.dto;

import com.gms.cheerlot.cheersong.dto.CheerSongInfo;

import java.util.List;

public record PlayerResponse(
        String playerCode,
        String name,
        String teamCode,
        String position,
        String batThrow,
        Integer backNumber,
        Integer battingOrder,
        List<CheerSongInfo> cheerSongs
) {}
