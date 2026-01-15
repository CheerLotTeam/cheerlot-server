package com.gms.cheerlot.lineup.repository;

import com.gms.cheerlot.lineup.domain.Team;
import notion.api.v1.NotionClient;
import notion.api.v1.model.databases.QueryResults;
import notion.api.v1.model.databases.query.filter.PropertyFilter;
import notion.api.v1.model.databases.query.filter.QueryTopLevelFilter;
import notion.api.v1.model.databases.query.filter.condition.TextFilter;
import notion.api.v1.model.pages.Page;
import notion.api.v1.model.pages.PageProperty;
import notion.api.v1.request.databases.QueryDatabaseRequest;
import notion.api.v1.request.pages.UpdatePageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TeamRepository {

    private final NotionClient notionClient;
    private final String databaseId;

    public TeamRepository(NotionClient notionClient, @Value("${notion.database.team-id}") String databaseId) {
        this.notionClient = notionClient;
        this.databaseId = databaseId;
    }

    public List<Team> findAll() {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);
        QueryResults results = notionClient.queryDatabase(request);

        return results.getResults().stream()
                .map(this::toTeam)
                .toList();
    }

    public Optional<Team> findByTeamCode(String teamCode) {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);
        request.setFilter(createTextFilter("team_code", teamCode));

        QueryResults results = notionClient.queryDatabase(request);

        if (results.getResults().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toTeam(results.getResults().get(0)));
    }

    public void updateHasTodayGame(String teamCode, boolean hasTodayGame) {
        String pageId = findPageIdByTeamCode(teamCode);
        if (pageId == null) {
            return;
        }

        Map<String, PageProperty> properties = new HashMap<>();
        properties.put("has_today_game", createCheckboxProperty(hasTodayGame));

        UpdatePageRequest request = new UpdatePageRequest(pageId, properties);
        notionClient.updatePage(request);
    }

    public void updateOpponentTeamCode(String teamCode, String opponentTeamCode) {
        String pageId = findPageIdByTeamCode(teamCode);
        if (pageId == null) {
            return;
        }

        Map<String, PageProperty> properties = new HashMap<>();
        properties.put("opponent_team_code", createRichTextProperty(opponentTeamCode));

        UpdatePageRequest request = new UpdatePageRequest(pageId, properties);
        notionClient.updatePage(request);
    }

    public void updateStarterPitcherName(String teamCode, String starterPitcherName) {
        String pageId = findPageIdByTeamCode(teamCode);
        if (pageId == null) {
            return;
        }

        Map<String, PageProperty> properties = new HashMap<>();
        properties.put("starter_pitcher_name", createRichTextProperty(starterPitcherName));

        UpdatePageRequest request = new UpdatePageRequest(pageId, properties);
        notionClient.updatePage(request);
    }

    public void updateUpdatedAt(String teamCode, LocalDateTime updatedAt) {
        String pageId = findPageIdByTeamCode(teamCode);
        if (pageId == null) {
            return;
        }

        Map<String, PageProperty> properties = new HashMap<>();
        properties.put("updated_at", createDateProperty(updatedAt));

        UpdatePageRequest request = new UpdatePageRequest(pageId, properties);
        notionClient.updatePage(request);
    }

    public void updateLastGameDate(String teamCode, LocalDate lastGameDate) {
        String pageId = findPageIdByTeamCode(teamCode);
        if (pageId == null) {
            return;
        }

        Map<String, PageProperty> properties = new HashMap<>();
        PageProperty prop = new PageProperty();
        PageProperty.Date date = new PageProperty.Date();
        date.setStart(lastGameDate.toString());
        prop.setDate(date);
        properties.put("last_game_date", prop);

        UpdatePageRequest request = new UpdatePageRequest(pageId, properties);
        notionClient.updatePage(request);
    }

    private PageProperty createCheckboxProperty(boolean value) {
        PageProperty prop = new PageProperty();
        prop.setCheckbox(value);
        return prop;
    }

    private PageProperty createRichTextProperty(String value) {
        PageProperty prop = new PageProperty();
        PageProperty.RichText richText = new PageProperty.RichText();
        PageProperty.RichText.Text text = new PageProperty.RichText.Text();
        text.setContent(value);
        richText.setText(text);
        prop.setRichText(List.of(richText));
        return prop;
    }

    private PageProperty createDateProperty(LocalDateTime dateTime) {
        PageProperty prop = new PageProperty();
        PageProperty.Date date = new PageProperty.Date();
        date.setStart(dateTime.toString());
        prop.setDate(date);
        return prop;
    }

    private String findPageIdByTeamCode(String teamCode) {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);
        request.setFilter(createTextFilter("team_code", teamCode));

        QueryResults results = notionClient.queryDatabase(request);

        if (results.getResults().isEmpty()) {
            return null;
        }
        return results.getResults().get(0).getId();
    }

    private QueryTopLevelFilter createTextFilter(String property, String value) {
        return new PropertyFilter(property, new TextFilter(value, null, null, null));
    }

    private Team toTeam(Page page) {
        Map<String, PageProperty> props = page.getProperties();

        return Team.builder()
                .teamCode(getText(props, "team_code"))
                .teamName(getText(props, "team_name"))
                .hasTodayGame(getCheckbox(props, "has_today_game"))
                .opponentTeamCode(getText(props, "opponent_team_code"))
                .starterPitcherName(getText(props, "starter_pitcher_name"))
                .lastGameDate(getDate(props, "last_game_date"))
                .isSeasonEnded(getCheckbox(props, "is_season_ended"))
                .updatedAt(getDateTime(props, "updated_at"))
                .build();
    }

    private String getText(Map<String, PageProperty> props, String key) {
        PageProperty prop = props.get(key);
        if (prop == null) {
            return null;
        }
        if (prop.getRichText() != null && !prop.getRichText().isEmpty()) {
            return prop.getRichText().get(0).getPlainText();
        }
        if (prop.getTitle() != null && !prop.getTitle().isEmpty()) {
            return prop.getTitle().get(0).getPlainText();
        }
        return null;
    }

    private boolean getCheckbox(Map<String, PageProperty> props, String key) {
        PageProperty prop = props.get(key);
        return prop != null && Boolean.TRUE.equals(prop.getCheckbox());
    }

    private LocalDate getDate(Map<String, PageProperty> props, String key) {
        PageProperty prop = props.get(key);
        if (prop == null || prop.getDate() == null) {
            return null;
        }
        String start = prop.getDate().getStart();
        return start != null ? LocalDate.parse(start) : null;
    }

    private LocalDateTime getDateTime(Map<String, PageProperty> props, String key) {
        PageProperty prop = props.get(key);
        if (prop == null || prop.getDate() == null) {
            return null;
        }
        String start = prop.getDate().getStart();
        if (start == null) {
            return null;
        }
        return OffsetDateTime.parse(start).toLocalDateTime();
    }
}
