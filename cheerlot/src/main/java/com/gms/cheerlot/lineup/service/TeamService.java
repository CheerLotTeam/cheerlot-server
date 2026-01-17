package com.gms.cheerlot.lineup.service;

import com.gms.cheerlot.lineup.domain.Team;
import com.gms.cheerlot.lineup.dto.TeamResponse;
import com.gms.cheerlot.lineup.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamResponse getTeam(String teamCode) {
        Team team = teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다: " + teamCode));

        return new TeamResponse(
                team.getTeamCode(),
                team.isSeasonEnded(),
                team.getLastGameDate(),
                team.isHasTodayGame(),
                team.getOpponentTeamCode(),
                team.getStarterPitcherName()
        );
    }
}
