package com.gms.cheerlot.cheersong.repository;

import com.gms.cheerlot.cheersong.domain.CheerSong;
import notion.api.v1.NotionClient;
import notion.api.v1.model.databases.QueryResults;
import notion.api.v1.model.pages.Page;
import notion.api.v1.model.pages.PageProperty;
import notion.api.v1.request.databases.QueryDatabaseRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheerSongRepositoryTest {

    @Mock
    private NotionClient notionClient;

    private CheerSongRepository cheerSongRepository;

    private static final String DATABASE_ID = "test-database-id";

    @BeforeEach
    void setUp() {
        cheerSongRepository = new CheerSongRepository(notionClient, DATABASE_ID);
    }

    @Test
    @DisplayName("모든 응원가를 조회한다")
    void findAll() {
        // given
        Page page = createMockPage(1L, "lg51", "홍창기 응원가", "홍창기 홍창기~", "hong.mp3");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        List<CheerSong> songs = cheerSongRepository.findAll();

        // then
        assertThat(songs).hasSize(1);
        assertThat(songs.get(0).getTitle()).isEqualTo("홍창기 응원가");
    }

    @Test
    @DisplayName("선수 코드로 응원가를 조회한다")
    void findByPlayerCode() {
        // given
        Page page = createMockPage(1L, "lg51", "홍창기 응원가", "홍창기 홍창기~", "hong.mp3");
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of(page));

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        List<CheerSong> songs = cheerSongRepository.findByPlayerCode("lg51");

        // then
        assertThat(songs).hasSize(1);
        assertThat(songs.get(0).getPlayerCode()).isEqualTo("lg51");
    }

    @Test
    @DisplayName("존재하지 않는 선수 코드로 조회하면 빈 리스트를 반환한다")
    void findByPlayerCode_notFound() {
        // given
        QueryResults queryResults = mock(QueryResults.class);
        when(queryResults.getResults()).thenReturn(List.of());

        when(notionClient.queryDatabase(any(QueryDatabaseRequest.class)))
                .thenReturn(queryResults);

        // when
        List<CheerSong> songs = cheerSongRepository.findByPlayerCode("unknown");

        // then
        assertThat(songs).isEmpty();
    }

    private Page createMockPage(Long id, String playerCode, String title,
                                 String lyrics, String audioFileName) {
        Page page = mock(Page.class);

        Map<String, PageProperty> properties = new HashMap<>();
        properties.put("id", createNumberProperty(id));
        properties.put("player_code", createRichTextProperty(playerCode));
        properties.put("title", createTitleProperty(title));
        properties.put("lyrics", createRichTextProperty(lyrics));
        properties.put("audio_file_name", createRichTextProperty(audioFileName));

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
}
