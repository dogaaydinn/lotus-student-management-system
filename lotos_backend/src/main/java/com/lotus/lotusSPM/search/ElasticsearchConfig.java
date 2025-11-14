package com.lotus.lotusSPM.search;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch Configuration for Advanced Search Capabilities.
 *
 * Enterprise Pattern: Full-Text Search / Search Engine
 *
 * Capabilities:
 * - Full-text search with relevance scoring
 * - Fuzzy matching (typo tolerance)
 * - Faceted search (filters and aggregations)
 * - Auto-complete and suggestions
 * - Geospatial search
 * - Real-time indexing
 * - Scalable horizontal search
 * - Analytics and aggregations
 *
 * Use Cases:
 * - Student search (name, email, faculty, department)
 * - Opportunity search (title, company, skills)
 * - Document search (content indexing)
 * - Message search (full-text search in messages)
 * - Advanced filtering and faceting
 * - Search analytics (popular searches)
 *
 * Architecture:
 * - Index: Database-like collection
 * - Document: JSON record
 * - Mapping: Schema definition
 * - Analyzer: Text processing pipeline
 * - Query DSL: Powerful query language
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.lotus.lotusSPM.search.repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${elasticsearch.host:localhost}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port:9200}")
    private int elasticsearchPort;

    @Value("${elasticsearch.username:}")
    private String username;

    @Value("${elasticsearch.password:}")
    private String password;

    /**
     * Configure Elasticsearch client.
     */
    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder =
            ClientConfiguration.builder()
                .connectedTo(elasticsearchHost + ":" + elasticsearchPort);

        // Add authentication if credentials provided
        if (!username.isEmpty() && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }

        ClientConfiguration clientConfiguration = builder
            .withConnectTimeout(10000)
            .withSocketTimeout(30000)
            .build();

        return RestClients.create(clientConfiguration).rest();
    }

    /**
     * Elasticsearch operations template.
     */
    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }
}
