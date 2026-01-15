package com.gms.cheerlot.lineup.repository;

import com.gms.cheerlot.lineup.domain.Player;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerRepositoryTest {

    @Mock
    private NotionClient notionClient;

    private PlayerRepository playerRepository;

    private static final String DATABASE_ID = "test-database-id";

    @BeforeEach
    void setUp() {
        playerRepository = new PlayerRepository(notionClient, DATABASE_ID);
    }

    @Test
    @DisplayName("모든 선수를 조회한다")
    void findAll() {
        // given
        Page page = createMockPage("test-page-id", "lg51", "lg", "홍창기", 51, true);
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        List<Player> players = playerRepository.findAll();

        // then
        assertThat(players).hasSize(1);
        assertThat(players.get(0).getPlayerCode()).isEqualTo("lg51");
        assertThat(players.get(0).getName()).isEqualTo("홍창기");
    }

    @Test
    @DisplayName("팀 코드로 선수를 조회한다")
    void findByTeamCode() {
        // given
        Page page = createMockPage("test-page-id", "lg51", "lg", "홍창기", 51, true);
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        List<Player> players = playerRepository.findByTeamCode("lg");

        // then
        assertThat(players).hasSize(1);
        assertThat(players.get(0).getTeamCode()).isEqualTo("lg");
    }

    @Test
    @DisplayName("팀 코드로 선발 선수를 조회한다")
    void findStartersByTeamCode() {
        // given
        Page starterPage = createMockPage("test-page-id-1", "lg51", "lg", "홍창기", 51, true);
        Page benchPage = createMockPage("test-page-id-2", "lg99", "lg", "박동원", 99, false);
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(starterPage, benchPage));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        List<Player> starters = playerRepository.findStartersByTeamCode("lg");

        // then
        assertThat(starters).hasSize(1);
        assertThat(starters.get(0).getName()).isEqualTo("홍창기");
        assertThat(starters.get(0).isStarter()).isTrue();
    }

    @Test
    @DisplayName("선수 코드로 선수를 조회한다")
    void findByPlayerCode() {
        // given
        Page page = createMockPage("test-page-id", "lg51", "lg", "홍창기", 51, true);
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        Optional<Player> player = playerRepository.findByPlayerCode("lg51");

        // then
        assertThat(player).isPresent();
        assertThat(player.get().getPlayerCode()).isEqualTo("lg51");
    }

    @Test
    @DisplayName("존재하지 않는 선수 코드로 조회하면 빈 Optional을 반환한다")
    void findByPlayerCode_notFound() {
        // given
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of());

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        Optional<Player> player = playerRepository.findByPlayerCode("unknown");

        // then
        assertThat(player).isEmpty();
    }

    @Test
    @DisplayName("선수의 타순을 업데이트한다")
    void updateBattingOrder() {
        // given
        Page page = createMockPage("test-page-id", "lg51", "lg", "홍창기", 51, true);
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        playerRepository.updateBattingOrder("lg51", 1);

        // then
        verify(notionClient).updatePage(any(UpdatePageRequest.class));
    }

    @Test
    @DisplayName("선수의 선발 여부를 업데이트한다")
    void updateIsStarter() {
        // given
        Page page = createMockPage("test-page-id", "lg51", "lg", "홍창기", 51, false);
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        playerRepository.updateIsStarter("lg51", true);

        // then
        verify(notionClient).updatePage(any(UpdatePageRequest.class));
    }

    private Page createMockPage(String pageId, String playerCode, String teamCode,
                                 String name, int backNumber, boolean isStarter) {
        Page page = mock(Page.class);
        when(page.getId()).thenReturn(pageId);

        Map<String, PageProperty> properties = new HashMap<>();
        properties.put("player_code", createTitleProperty(playerCode));
        properties.put("team_code", createRichTextProperty(teamCode));
        properties.put("name", createRichTextProperty(name));
        properties.put("back_number", createNumberProperty(backNumber));
        properties.put("position", createRichTextProperty("외야수"));
        properties.put("bat_throw", createRichTextProperty("우타우투"));
        properties.put("is_starter", createCheckboxProperty(isStarter));

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

    private PageProperty createNumberProperty(Number value) {
        PageProperty prop = mock(PageProperty.class);
        when(prop.getNumber()).thenReturn(value);
        return prop;
    }

    private PageProperty createCheckboxProperty(boolean value) {
        PageProperty prop = mock(PageProperty.class);
        when(prop.getCheckbox()).thenReturn(value);
        return prop;
    }
}
