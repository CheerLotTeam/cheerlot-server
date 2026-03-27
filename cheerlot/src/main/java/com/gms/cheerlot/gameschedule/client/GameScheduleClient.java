package com.gms.cheerlot.gameschedule.client;

import com.gms.cheerlot.gameschedule.dto.CrawlerGameScheduleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@Component
public class GameScheduleClient {

    private final RestClient restClient;

    public GameScheduleClient(@Value("${crawler.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Optional<CrawlerGameScheduleResponse> fetchRecentGameSchedules() {
        try {
            CrawlerGameScheduleResponse response = restClient.get()
                    .uri("/api/games/recent")
                    .retrieve()
                    .body(CrawlerGameScheduleResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.error("크롤링 서버 경기 일정 조회 실패", e);
            return Optional.empty();
        }
    }
}
