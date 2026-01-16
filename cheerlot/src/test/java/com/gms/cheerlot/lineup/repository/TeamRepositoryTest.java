package com.gms.cheerlot.lineup.repository;

import com.gms.cheerlot.lineup.domain.Team;
import notion.api.v1.NotionClient;
import notion.api.v1.model.databases.QueryResults;
import notion.api.v1.model.pages.Page;
import notion.api.v1.model.pages.PageProperty;
import notion.api.v1.request.databases.QueryDatabaseRequest;
import notion.api.v1.request.pages.UpdatePageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeamRepositoryTest {

    @Mock
    private NotionClient notionClient;

    private TeamRepository teamRepository;

    private static final String DATABASE_ID = "test-database-id";

    @BeforeEach
    void setUp() {
        teamRepository = new TeamRepository(notionClient, DATABASE_ID);
    }

    @Test
    @DisplayName("모든 팀을 조회한다")
    void findAll() {
        // given
        Page page = createMockPage("test-page-id", "lg", "LG 트윈스");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        List<Team> teams = teamRepository.findAll();

        // then
        assertThat(teams).hasSize(1);
        assertThat(teams.get(0).getTeamCode()).isEqualTo("lg");
        assertThat(teams.get(0).getTeamName()).isEqualTo("LG 트윈스");
    }

    @Test
    @DisplayName("팀 코드로 팀을 조회한다")
    void findByTeamCode() {
        // given
        Page page = createMockPage("test-page-id", "lg", "LG 트윈스");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        Optional<Team> team = teamRepository.findByTeamCode("lg");

        // then
        assertThat(team).isPresent();
        assertThat(team.get().getTeamCode()).isEqualTo("lg");
    }

    @Test
    @DisplayName("존재하지 않는 팀 코드로 조회하면 빈 Optional을 반환한다")
    void findByTeamCode_notFound() {
        // given
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of());

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        Optional<Team> team = teamRepository.findByTeamCode("unknown");

        // then
        assertThat(team).isEmpty();
    }

    @Test
    @DisplayName("팀의 오늘 경기 여부를 업데이트한다")
    void updateHasTodayGame() {
        // given
        Page page = createMockPage("test-page-id", "lg", "LG 트윈스");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        teamRepository.updateHasTodayGame("lg", true);

        // then
        verify(notionClient).updatePage(any(UpdatePageRequest.class));
    }

    @Test
    @DisplayName("팀의 상대팀 코드를 업데이트한다")
    void updateOpponentTeamCode() {
        // given
        Page page = createMockPage("test-page-id", "lg", "LG 트윈스");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        teamRepository.updateOpponentTeamCode("lg", "doosan");

        // then
        verify(notionClient).updatePage(any(UpdatePageRequest.class));
    }

    @Test
    @DisplayName("팀의 선발투수 이름을 업데이트한다")
    void updateStarterPitcherName() {
        // given
        Page page = createMockPage("test-page-id", "lg", "LG 트윈스");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        teamRepository.updateStarterPitcherName("lg", "임찬규");

        // then
        verify(notionClient).updatePage(any(UpdatePageRequest.class));
    }

    @Test
    @DisplayName("팀의 갱신 시각을 업데이트한다")
    void updateUpdatedAt() {
        // given
        Page page = createMockPage("test-page-id", "lg", "LG 트윈스");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        teamRepository.updateUpdatedAt("lg", LocalDateTime.now());

        // then
        verify(notionClient).updatePage(any(UpdatePageRequest.class));
    }

    @Test
    @DisplayName("팀의 마지막 경기일을 업데이트한다")
    void updateLastGameDate() {
        // given
        Page page = createMockPage("test-page-id", "lg", "LG 트윈스");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        teamRepository.updateLastGameDate("lg", LocalDate.now());

        // then
        verify(notionClient).updatePage(any(UpdatePageRequest.class));
    }

    private Page createMockPage(String pageId, String teamCode, String teamName) {
        Page page = mock(Page.class);
        when(page.getId()).thenReturn(pageId);

        Map<String, PageProperty> properties = new HashMap<>();
        properties.put("team_code", createTitleProperty(teamCode));
        properties.put("team_name", createRichTextProperty(teamName));
        properties.put("has_today_game", createCheckboxProperty(false));
        properties.put("is_season_ended", createCheckboxProperty(false));

        when(page.getProperties()).thenReturn(properties);
        return page;
    }

    private PageProperty createTitleProperty(String value) {
        PageProperty.RichText richText = mock(PageProperty.RichText.class);
        when(richText.getPlainText()).thenReturn(value);

        PageProperty prop = mock(PageProperty.class);
        when(prop.getTitle()).thenReturn(List.of(richText));
        return prop;
    }

    private PageProperty createRichTextProperty(String value) {
        PageProperty.RichText richText = mock(PageProperty.RichText.class);
        when(richText.getPlainText()).thenReturn(value);

        PageProperty prop = mock(PageProperty.class);
        when(prop.getRichText()).thenReturn(List.of(richText));
        return prop;
    }

    private PageProperty createCheckboxProperty(boolean value) {
        PageProperty prop = mock(PageProperty.class);
        when(prop.getCheckbox()).thenReturn(value);
        return prop;
    }
}
