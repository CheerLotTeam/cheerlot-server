package com.gms.cheerlot.lineup.service;

import com.gms.cheerlot.lineup.domain.Team;
import com.gms.cheerlot.lineup.dto.TeamResponse;
import com.gms.cheerlot.lineup.repository.TeamRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    @DisplayName("팀 코드로 팀 정보를 조회한다")
    void getTeam() {
        // given
        Team team = Team.builder()
                .teamCode("lg")
                .teamName("LG 트윈스")
                .hasTodayGame(true)
                .opponentTeamCode("kt")
                .starterPitcherName("이민호")
                .lastGameDate(LocalDate.of(2026, 10, 12))
                .isSeasonEnded(false)
                .build();

        when(teamRepository.findByTeamCode("lg")).thenReturn(Optional.of(team));

        // when
        TeamResponse response = teamService.getTeam("lg");

        // then
        assertThat(response.teamCode()).isEqualTo("lg");
        assertThat(response.hasTodayGame()).isTrue();
        assertThat(response.opponentTeamCode()).isEqualTo("kt");
        assertThat(response.starterPitcherName()).isEqualTo("이민호");
    }

    @Test
    @DisplayName("존재하지 않는 팀 코드로 조회하면 예외가 발생한다")
    void getTeam_notFound() {
        // given
        when(teamRepository.findByTeamCode("unknown")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.getTeam("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("팀을 찾을 수 없습니다");
    }
}
