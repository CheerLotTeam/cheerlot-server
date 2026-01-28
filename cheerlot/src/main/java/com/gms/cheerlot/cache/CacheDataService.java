package com.gms.cheerlot.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.cheerlot.cheersong.domain.CheerSong;
import com.gms.cheerlot.lineup.domain.Player;
import com.gms.cheerlot.lineup.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheDataService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper redisObjectMapper;

    private static final String KEY_TEAMS = "teams";
    private static final String KEY_PLAYERS = "players";
    private static final String KEY_CHEERSONGS = "cheersongs";

    // ========== Team ==========

    public List<Team> getTeams() {
        return getFromRedis(KEY_TEAMS, new TypeReference<>() {});
    }

    public Optional<Team> getTeamByCode(String teamCode) {
        return getTeams().stream()
                .filter(t -> t.getTeamCode().equals(teamCode))
                .findFirst();
    }

    // ========== Player ==========

    public List<Player> getPlayers() {
        return getFromRedis(KEY_PLAYERS, new TypeReference<>() {});
    }

    public List<Player> getPlayersByTeamCode(String teamCode) {
        return getPlayers().stream()
                .filter(p -> p.getTeamCode().equals(teamCode))
                .toList();
    }

    public List<Player> getStartersByTeamCode(String teamCode) {
        return getPlayers().stream()
                .filter(p -> p.getTeamCode().equals(teamCode))
                .filter(Player::isStarter)
                .toList();
    }

    public Optional<Player> getPlayerByCode(String playerCode) {
        return getPlayers().stream()
                .filter(p -> p.getPlayerCode().equals(playerCode))
                .findFirst();
    }

    // ========== CheerSong ==========

    public List<CheerSong> getCheerSongs() {
        return getFromRedis(KEY_CHEERSONGS, new TypeReference<>() {});
    }

    public List<CheerSong> getCheerSongsByPlayerCode(String playerCode) {
        return getCheerSongs().stream()
                .filter(cs -> cs.getPlayerCode().equals(playerCode))
                .toList();
    }

    // ========== Helper ==========

    private <T> T getFromRedis(String key, TypeReference<T> typeReference) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            throw new IllegalStateException("캐시가 비어있습니다. 서버 시작 초기화되어야 합니다: " + key);
        }

        try {
            return redisObjectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 조회 실패: " + key, e);
        }
    }
}
