package bg.tu.varna.sit.si.controller;

import bg.tu.varna.sit.si.model.LogMessage;
import bg.tu.varna.sit.si.service.LoggingService;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.orm.Search;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoggingController {

    private static final Logger LOG = Logger.getLogger(LoggingController.class);

    @Inject
    LoggingService loggingService;

    @Inject
    EntityManager entityManager;

    @POST
    @Path("/logs")
    public Response ingest(List<LogMessage> logMessages) {
        for (LogMessage logMessage : logMessages) {
            if (logMessage != null && logMessage.getLevel() != null && logMessage.getMessage() != null) {
                logMessage.setTimestamp(LocalDateTime.now());
                loggingService.addLog(logMessage);
            }
        }
        return Response.ok().build();
    }

    @GET
    @Path("/ingest/health")
    public Response healthCheck() {
        return Response.ok().entity("{\"status\": \"UP\"}").build();
    }

    @GET
    @Path("/logs/search")
    public List<LogMessage> searchLogs(@QueryParam("query") String query,
                                       @QueryParam("service") String service,
                                       @QueryParam("level") String level,
                                       @QueryParam("start_time") String startTimeStr,
                                       @QueryParam("end_time") String endTimeStr) {

        SearchSession searchSession = Search.session(entityManager);
        SearchPredicateFactory f = searchSession.scope(LogMessage.class).predicate();
        List<SearchPredicate> predicates = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            predicates.add(f.match().field("message").matching(query).toPredicate());
        }
        if (service != null && !service.isBlank()) {
            predicates.add(f.match().field("service").matching(service).toPredicate());
        }
        if (level != null && !level.isBlank()) {
            predicates.add(f.match().field("level").matching(level).toPredicate());
        }
        if (startTimeStr != null && !startTimeStr.isBlank()) {
            predicates.add(f.range().field("timestamp").greaterThan(LocalDateTime.parse(startTimeStr)).toPredicate());
        }
        if (endTimeStr != null && !endTimeStr.isBlank()) {
            predicates.add(f.range().field("timestamp").lessThan(LocalDateTime.parse(endTimeStr)).toPredicate());
        }

        SearchPredicate finalPredicate;
        if (predicates.isEmpty()) {
            finalPredicate = f.matchAll().toPredicate();
        } else {
            finalPredicate = f.bool(b -> {
                for (SearchPredicate p : predicates) {
                    b.must(p);
                }
            }).toPredicate();
        }

        SearchQuery<LogMessage> searchQuery = searchSession.search(LogMessage.class)
                .where(finalPredicate)
                .toQuery();

        return searchQuery.fetchHits(1000);
    }

    @GET
    @Path("/logs/summary")
    public Response getLogSummary() {
        SearchSession searchSession = Search.session(entityManager);
        SearchPredicateFactory f = searchSession.scope(LogMessage.class).predicate();
        SearchPredicate predicate = f.matchAll().toPredicate();

        long total = searchSession.search(LogMessage.class)
                .where(predicate)
                .fetchTotalHitCount();

        return Response.ok().entity("{\"total_logs\": " + total + "}").build();
    }
}
