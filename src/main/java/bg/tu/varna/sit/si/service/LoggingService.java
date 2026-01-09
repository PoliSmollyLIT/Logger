package bg.tu.varna.sit.si.service;

import bg.tu.varna.sit.si.model.LogMessage;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class LoggingService {

    @Inject
    ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME = "logs";

    public void addLogs(List<LogMessage> logMessages) {
        for (LogMessage logMessage : logMessages) {
            if (logMessage != null && logMessage.getLevel() != null && logMessage.getMessage() != null) {
                logMessage.setTimestamp(LocalDateTime.now());
                try {
                    elasticsearchClient.index(i -> i
                            .index(INDEX_NAME)
                            .document(logMessage)
                    );
                } catch (IOException e) {
                    throw new RuntimeException("Failed to index log message", e);
                }
            }
        }
    }

    public List<LogMessage> searchLogs(String query, String service, String level, String startTime, String endTime) {
        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            if (query != null && !query.isBlank()) {
                boolQuery.must(m -> m.match(t -> t.field("message").query(query)));
            }
            if (service != null && !service.isBlank()) {
                boolQuery.must(m -> m.match(t -> t.field("service").query(service)));
            }
            if (level != null && !level.isBlank()) {
                boolQuery.must(m -> m.match(t -> t.field("level").query(level)));
            }
            if (startTime != null && !startTime.isBlank()) {
                boolQuery.filter(f -> f.range(r -> r.field("timestamp").gte(co.elastic.clients.json.JsonData.of(startTime))));
            }
            if (endTime != null && !endTime.isBlank()) {
                boolQuery.filter(f -> f.range(r -> r.field("timestamp").lte(co.elastic.clients.json.JsonData.of(endTime))));
            }

            SearchResponse<LogMessage> response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q.bool(boolQuery.build()))
                            .size(1000),
                    LogMessage.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Failed to search logs", e);
        }
    }

    public List<LogMessage> getLogSummary() {
        try {
            SearchResponse<LogMessage> response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q.matchAll(m -> m))
                            .size(10000),
                    LogMessage.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to get log summary", e);
        }
    }
}
