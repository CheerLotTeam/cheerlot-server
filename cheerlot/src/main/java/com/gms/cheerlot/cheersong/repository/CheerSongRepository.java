package com.gms.cheerlot.cheersong.repository;

import com.gms.cheerlot.cheersong.domain.CheerSong;
import notion.api.v1.NotionClient;
import notion.api.v1.model.databases.QueryResults;
import notion.api.v1.model.databases.query.filter.PropertyFilter;
import notion.api.v1.model.databases.query.filter.QueryTopLevelFilter;
import notion.api.v1.model.databases.query.filter.condition.TextFilter;
import notion.api.v1.model.pages.Page;
import notion.api.v1.model.pages.PageProperty;
import notion.api.v1.request.databases.QueryDatabaseRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CheerSongRepository {

    private final NotionClient notionClient;
    private final String databaseId;

    public CheerSongRepository(NotionClient notionClient, @Value("${notion.database.cheersong-id}") String databaseId) {
        this.notionClient = notionClient;
        this.databaseId = databaseId;
    }

    public List<CheerSong> findAll() {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);
        QueryResults results = notionClient.queryDatabase(request);

        return results.getResults().stream()
                .map(this::toCheerSong)
                .toList();
    }

    public List<CheerSong> findByPlayerCode(String playerCode) {
        QueryDatabaseRequest request = new QueryDatabaseRequest(databaseId);
        request.setFilter(createTextFilter("player_code", playerCode));

        QueryResults results = notionClient.queryDatabase(request);

        return results.getResults().stream()
                .map(this::toCheerSong)
                .toList();
    }

    private QueryTopLevelFilter createTextFilter(String property, String value) {
        return new PropertyFilter(property, new TextFilter(value, null, null, null));
    }

    private CheerSong toCheerSong(Page page) {
        Map<String, PageProperty> props = page.getProperties();

        return CheerSong.builder()
                .id(getNumberAsLong(props, "id"))
                .playerCode(getText(props, "player_code"))
                .title(getText(props, "title"))
                .lyrics(getText(props, "lyrics"))
                .audioFileName(getText(props, "audio_file_name"))
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

    private Long getNumberAsLong(Map<String, PageProperty> props, String key) {
        PageProperty prop = props.get(key);
        if (prop == null || prop.getNumber() == null) {
            return null;
        }
        return prop.getNumber().longValue();
    }
}
