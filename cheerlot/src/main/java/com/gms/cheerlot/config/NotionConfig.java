package com.gms.cheerlot.config;

import notion.api.v1.NotionClient;
import notion.api.v1.http.OkHttp5Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotionConfig {

    @Value("${notion.api.key}")
    private String notionApiKey;

    @Bean
    public NotionClient notionClient() {
        NotionClient client = new NotionClient(notionApiKey);
        client.setHttpClient(new OkHttp5Client(30000, 30000, 30000));
        return client;
    }
}
