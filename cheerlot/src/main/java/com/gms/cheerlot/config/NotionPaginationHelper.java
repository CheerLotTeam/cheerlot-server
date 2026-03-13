package com.gms.cheerlot.config;

import lombok.extern.slf4j.Slf4j;
import notion.api.v1.NotionClient;
import notion.api.v1.model.databases.QueryResults;
import notion.api.v1.model.pages.Page;
import notion.api.v1.request.databases.QueryDatabaseRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class NotionPaginationHelper {

    private static final int MAX_ITERATIONS = 50;

    public List<Page> queryAll(NotionClient client, QueryDatabaseRequest request) {
        List<Page> allPages = new ArrayList<>();
        int iteration = 0;

        do {
            QueryResults results = client.queryDatabase(request);
            allPages.addAll(results.getResults());

            if (!Boolean.TRUE.equals(results.getHasMore())) {
                break;
            }

            request.setStartCursor(results.getNextCursor());
            iteration++;

            if (iteration >= MAX_ITERATIONS) {
                log.warn("Notion 페이지네이션 최대 반복 횟수({}) 도달. 총 {}건 조회됨", MAX_ITERATIONS, allPages.size());
                break;
            }
        } while (true);

        return allPages;
    }
}
