package com.gms.cheerlot.lineup.service;

import com.gms.cheerlot.cheersong.dto.CheerSongInfo;
import com.gms.cheerlot.cheersong.repository.CheerSongRepository;
import com.gms.cheerlot.lineup.domain.Player;
import com.gms.cheerlot.lineup.dto.PlayerListResponse;
import com.gms.cheerlot.lineup.dto.PlayerResponse;
import com.gms.cheerlot.lineup.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final CheerSongRepository cheerSongRepository;

    public PlayerListResponse getPlayers(String teamCode) {
        List<PlayerResponse> players = playerRepository.findByTeamCode(teamCode).stream()
                .map(this::toPlayerResponse)
                .toList();

        return new PlayerListResponse(teamCode, null, players);
    }

    public PlayerListResponse getStarterLineup(String teamCode) {
        List<PlayerResponse> players = playerRepository.findStartersByTeamCode(teamCode).stream()
                .sorted(Comparator.comparing(Player::getBattingOrder))
                .map(this::toPlayerResponse)
                .toList();

        return new PlayerListResponse(teamCode, "starter", players);
    }

    public PlayerResponse getPlayer(String playerCode) {
        Player player = playerRepository.findByPlayerCode(playerCode)
                .orElseThrow(() -> new IllegalArgumentException("선수를 찾을 수 없습니다: " + playerCode));

        return toPlayerResponse(player);
    }

    private PlayerResponse toPlayerResponse(Player player) {
        List<CheerSongInfo> cheerSongs = cheerSongRepository.findByPlayerCode(player.getPlayerCode()).stream()
                .map(cs -> new CheerSongInfo(cs.getTitle(), cs.getLyrics(), cs.getAudioFileName()))
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
