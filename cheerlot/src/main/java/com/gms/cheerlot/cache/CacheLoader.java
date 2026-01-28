package com.gms.cheerlot.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gms.cheerlot.cache.dto.ResetResult;
import com.gms.cheerlot.cheersong.domain.CheerSong;
import com.gms.cheerlot.cheersong.repository.CheerSongRepository;
import com.gms.cheerlot.lineup.domain.Player;
import com.gms.cheerlot.lineup.domain.Team;
import com.gms.cheerlot.lineup.repository.PlayerRepository;
import com.gms.cheerlot.lineup.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheLoader {

    private final StringRedisTemplate redisTemplate;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final CheerSongRepository cheerSongRepository;

    private static final String KEY_TEAMS = "teams";
    private static final String KEY_PLAYERS = "players";
    private static final String KEY_CHEERSONGS = "cheersongs";
    private final ObjectMapper redisObjectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("서버 시작 - 캐시 초기화 시작");
        ResetResult result = reset();
        log.info("캐시 초기화 완료 - teams: {}, players: {}, cheersongs: {}", result.teams(), result.players(), result.cheerSongs());
    }

    public ResetResult reset() {
        redisTemplate.delete(List.of(KEY_TEAMS, KEY_PLAYERS, KEY_CHEERSONGS));

        List<Team> teams = teamRepository.findAll();
        List<Player> players = playerRepository.findAll();
        List<CheerSong> cheerSongs = cheerSongRepository.findAll();

        saveToRedis(KEY_TEAMS, teams);
        saveToRedis(KEY_PLAYERS, players);
        saveToRedis(KEY_CHEERSONGS, cheerSongs);

        return new ResetResult(teams.size(), players.size(), cheerSongs.size());
    }

    private <T> void saveToRedis(String key, T data) {
        try {
            String json = redisObjectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 저장 실패: " + key, e);
        }
    }
}
