package com.gms.cheerlot.lineup.controller;

import com.gms.cheerlot.lineup.dto.TeamResponse;
import com.gms.cheerlot.lineup.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{teamCode}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable String teamCode) {
        TeamResponse response = teamService.getTeam(teamCode);
        return ResponseEntity.ok(response);
    }
}
