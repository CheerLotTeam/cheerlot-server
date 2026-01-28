package com.gms.cheerlot.lineup.service;

import com.gms.cheerlot.cache.CacheDataService;
import com.gms.cheerlot.cheersong.dto.CheerSongInfo;
import com.gms.cheerlot.cheersong.service.CheerSongService;
import com.gms.cheerlot.lineup.domain.Player;
import com.gms.cheerlot.lineup.dto.PlayerListResponse;
import com.gms.cheerlot.lineup.dto.PlayerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final CacheDataService cacheDataService;
    private final CheerSongService cheerSongService;

    public PlayerListResponse getPlayers(String teamCode) {
        List<PlayerResponse> players = cacheDataService.getPlayersByTeamCode(teamCode).stream()
                .map(this::toPlayerResponse)
                .toList();

        return new PlayerListResponse(teamCode, null, players);
    }

    public PlayerListResponse getStarterLineup(String teamCode) {
        List<PlayerResponse> players = cacheDataService.getStartersByTeamCode(teamCode).stream()
                .sorted(Comparator.comparing(Player::getBattingOrder))
                .map(this::toPlayerResponse)
                .toList();

        return new PlayerListResponse(teamCode, "starter", players);
    }

    public PlayerResponse getPlayer(String playerCode) {
        Player player = cacheDataService.getPlayerByCode(playerCode)
                .orElseThrow(() -> new IllegalArgumentException("선수를 찾을 수 없습니다: " + playerCode));

        return toPlayerResponse(player);
    }

    private PlayerResponse toPlayerResponse(Player player) {
        List<CheerSongInfo> cheerSongs = cacheDataService.getCheerSongsByPlayerCode(player.getPlayerCode()).stream()
                .map(cs -> new CheerSongInfo(cs.getTitle(), cs.getLyrics(), cheerSongService.getAudioUrl(cs.getAudioFileName())))
                .toList();

        return new PlayerResponse(
                player.getPlayerCode(),
                player.getName(),
                player.getTeamCode(),
                player.getPosition(),
                player.getBatThrow(),
                player.getBackNumber(),
                player.getBattingOrder(),
                cheerSongs
        );
    }
}
