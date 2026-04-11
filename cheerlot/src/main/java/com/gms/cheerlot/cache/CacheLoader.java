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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<Player> oldPlayers = loadOldPlayers();

        List<Player> newPlayers = playerRepository.findAll();
        List<Team> teams = teamRepository.findAll();
        List<CheerSong> cheerSongs = cheerSongRepository.findAll();

        boolean versionChanged = incrementVersionsIfChanged(teams, oldPlayers, newPlayers);

        List<Team> teamsToCache = versionChanged ? teamRepository.findAll() : teams;

        redisTemplate.delete(List.of(KEY_TEAMS, KEY_PLAYERS, KEY_CHEERSONGS));

        saveToRedis(KEY_TEAMS, teamsToCache);
        saveToRedis(KEY_PLAYERS, newPlayers);
        saveToRedis(KEY_CHEERSONGS, cheerSongs);

        return new ResetResult(teamsToCache.size(), newPlayers.size(), cheerSongs.size());
    }

    private List<Player> loadOldPlayers() {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PLAYERS);
            if (json == null) {
                return List.of();
            }
            return redisObjectMapper.readValue(json,
                    redisObjectMapper.getTypeFactory().constructCollectionType(List.class, Player.class));
        } catch (Exception e) {
            log.warn("이전 캐시 선수 데이터 로드 실패, 빈 리스트로 대체", e);
            return List.of();
        }
    }

    private boolean incrementVersionsIfChanged(List<Team> teams, List<Player> oldPlayers, List<Player> newPlayers) {
        Map<String, List<Player>> oldByTeam = oldPlayers.stream()
                .collect(Collectors.groupingBy(Player::getTeamCode));
        Map<String, List<Player>> newByTeam = newPlayers.stream()
                .collect(Collectors.groupingBy(Player::getTeamCode));

        boolean changed = false;

        for (Team team : teams) {
            String teamCode = team.getTeamCode();
            List<Player> oldTeamPlayers = oldByTeam.getOrDefault(teamCode, List.of());
            List<Player> newTeamPlayers = newByTeam.getOrDefault(teamCode, List.of());

            if (!isSamePlayers(oldTeamPlayers, newTeamPlayers)) {
                teamRepository.incrementPlayersVersion(teamCode);
                log.info("팀 {} playersVersion 증가", teamCode);
                changed = true;
            }

            if (!isSameLineup(oldTeamPlayers, newTeamPlayers)) {
                teamRepository.incrementLineupVersion(teamCode);
                log.info("팀 {} lineupVersion 증가", teamCode);
                changed = true;
            }
        }

        return changed;
    }

    private boolean isSamePlayers(List<Player> oldPlayers, List<Player> newPlayers) {
        if (oldPlayers.size() != newPlayers.size()) {
            return false;
        }

        Set<String> oldCodes = oldPlayers.stream()
                .map(Player::getPlayerCode)
                .collect(Collectors.toSet());
        Set<String> newCodes = newPlayers.stream()
                .map(Player::getPlayerCode)
                .collect(Collectors.toSet());

        if (!oldCodes.equals(newCodes)) {
            return false;
        }

        Map<String, Player> oldMap = oldPlayers.stream()
                .collect(Collectors.toMap(Player::getPlayerCode, p -> p, (a, b) -> a));

        return newPlayers.stream().allMatch(np -> {
            Player op = oldMap.get(np.getPlayerCode());
            return op != null
                    && Objects.equals(op.getName(), np.getName())
                    && Objects.equals(op.getBackNumber(), np.getBackNumber())
                    && Objects.equals(op.getPosition(), np.getPosition())
                    && Objects.equals(op.getBatThrow(), np.getBatThrow());
        });
    }

    private boolean isSameLineup(List<Player> oldPlayers, List<Player> newPlayers) {
        List<Player> oldStarters = oldPlayers.stream()
                .filter(Player::isStarter)
                .toList();
        List<Player> newStarters = newPlayers.stream()
                .filter(Player::isStarter)
                .toList();

        if (oldStarters.size() != newStarters.size()) {
            return false;
        }

        Map<String, Integer> oldLineup = oldStarters.stream()
                .collect(Collectors.toMap(Player::getPlayerCode, Player::getBattingOrder, (a, b) -> a));
        Map<String, Integer> newLineup = newStarters.stream()
                .collect(Collectors.toMap(Player::getPlayerCode, Player::getBattingOrder, (a, b) -> a));

        return oldLineup.equals(newLineup);
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
