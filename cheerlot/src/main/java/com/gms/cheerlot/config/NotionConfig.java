package com.gms.cheerlot.config;

import notion.api.v1.NotionClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotionConfig {

    @Value("${notion.api.key}")
    private String notionApiKey;

    @Bean
    public NotionClient notionClient() {
        return new NotionClient(notionApiKey);
    }
}
