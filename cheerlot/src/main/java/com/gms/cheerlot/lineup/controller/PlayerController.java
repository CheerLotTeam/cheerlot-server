package com.gms.cheerlot.lineup.controller;

import com.gms.cheerlot.lineup.dto.PlayerListResponse;
import com.gms.cheerlot.lineup.dto.PlayerResponse;
import com.gms.cheerlot.lineup.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/team/{teamCode}")
    public ResponseEntity<PlayerListResponse> getPlayers(
            @PathVariable String teamCode,
            @RequestParam(required = false) String role) {

        if ("starter".equals(role)) {
            return ResponseEntity.ok(playerService.getStarterLineup(teamCode));
        }
        return ResponseEntity.ok(playerService.getPlayers(teamCode));
    }

    @GetMapping("/{playerCode}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable String playerCode) {
        PlayerResponse response = playerService.getPlayer(playerCode);
        return ResponseEntity.ok(response);
    }
}
