package com.gms.cheerlot.lineup.repository;

import com.gms.cheerlot.lineup.domain.Player;
import com.gms.cheerlot.config.NotionPaginationHelper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PlayerRepository {

    private final NotionClient notionClient;
    private final NotionPaginationHelper paginationHelper;
    private final String databaseId;

    public PlayerRepository(NotionClient notionClient, NotionPaginationHelper paginationHelper, @Value("${notion.database.player-id}") String databaseId) {
        this.notionClient = notionClient;
        this.paginationHelper = paginationHelper;
        this.databaseId = databaseId;
    }

    public List<Player> findAll() {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);

        return paginationHelper.queryAll(notionClient, request).stream()
                .map(this::toPlayer)
                .toList();
    }

    public List<Player> findByTeamCode(String teamCode) {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);
        request.setFilter(createTextFilter("team_code", teamCode));

        return paginationHelper.queryAll(notionClient, request).stream()
                .map(this::toPlayer)
                .toList();
    }

    public List<Player> findStartersByTeamCode(String teamCode) {
        return findByTeamCode(teamCode).stream()
                .filter(Player::isStarter)
                .toList();
    }

    public Optional<Player> findByPlayerCode(String playerCode) {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);
        request.setFilter(createTextFilter("player_code", playerCode));

        QueryResults results = notionClient.queryDatabase(request);

        if (results.getResults().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toPlayer(results.getResults().get(0)));
    }

    public void updateBattingOrder(String playerCode, Integer battingOrder) {
        String pageId = findPageIdByPlayerCode(playerCode);
        if (pageId == null) {
            return;
        }

        Map<String, PageProperty> properties = new HashMap<>();
        PageProperty prop = new PageProperty();
        prop.setNumber(battingOrder);
        properties.put("batting_order", prop);

        UpdatePageRequest request = new UpdatePageRequest(pageId, properties);
        notionClient.updatePage(request);
    }

    public void updateIsStarter(String playerCode, boolean isStarter) {
        String pageId = findPageIdByPlayerCode(playerCode);
        if (pageId == null) {
            return;
        }

        Map<String, PageProperty> properties = new HashMap<>();
        PageProperty prop = new PageProperty();
        prop.setCheckbox(isStarter);
        properties.put("is_starter", prop);

        UpdatePageRequest request = new UpdatePageRequest(pageId, properties);
        notionClient.updatePage(request);
    }

    private String findPageIdByPlayerCode(String playerCode) {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);
        request.setFilter(createTextFilter("player_code", playerCode));

        QueryResults results = notionClient.queryDatabase(request);

        if (results.getResults().isEmpty()) {
            return null;
        }
        return results.getResults().get(0).getId();
    }

    private QueryTopLevelFilter createTextFilter(String property, String value) {
        return new PropertyFilter(property, new TextFilter(value, null, null, null));
    }

    private Player toPlayer(Page page) {
        Map<String, PageProperty> props = page.getProperties();

        return Player.builder()
                .playerCode(getText(props, "player_code"))
                .teamCode(getText(props, "team_code"))
                .name(getText(props, "name"))
                .backNumber(getNumber(props, "back_number"))
                .position(getText(props, "position"))
                .batThrow(getText(props, "bat_throw"))
                .battingOrder(getNumber(props, "batting_order"))
                .starter(getCheckbox(props, "is_starter"))
                .build();
    }

    private String getText(Map<String, PageProperty> props, String key) {
        PageProperty prop = props.get(key);
        if (prop == null) {
            return null;
        }

        if (prop.getRichText() != null && !prop.getRichText().isEmpty()) {
            return prop.getRichText().stream()
                    .map(PageProperty.RichText::getPlainText)
                    .collect(java.util.stream.Collectors.joining());
        }
        if (prop.getTitle() != null && !prop.getTitle().isEmpty()) {
            return prop.getTitle().stream()
                    .map(PageProperty.RichText::getPlainText)
                    .collect(java.util.stream.Collectors.joining());
        }
        return null;
    }

    private Integer getNumber(Map<String, PageProperty> props, String key) {
        PageProperty prop = props.get(key);
        if (prop == null || prop.getNumber() == null) {
            return null;
        }
        return prop.getNumber().intValue();
    }

    private boolean getCheckbox(Map<String, PageProperty> props, String key) {
        PageProperty prop = props.get(key);
        return prop != null && Boolean.TRUE.equals(prop.getCheckbox());
    }
}
