package bg.tu.varna.sit.si.controller;

import bg.tu.varna.sit.si.model.LogMessage;
import bg.tu.varna.sit.si.service.LoggingService;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoggingController {

    private static final Logger LOG = Logger.getLogger(LoggingController.class);

    @Inject
    LoggingService loggingService;

    @POST
    @Path("/logs")
    public Response addLog(List<LogMessage> logMessages) {
       loggingService.addLogs(logMessages);
        return Response.ok().build();
    }

    @GET
    @Path("/logs/search")
    public List<LogMessage> searchLogs(@QueryParam("query") String query,
                                       @QueryParam("service") String service,
                                       @QueryParam("level") String level,
                                       @QueryParam("start_time") String startTimeStr,
                                       @QueryParam("end_time") String endTimeStr) {

       return loggingService.searchLogs(query, service, level, startTimeStr, endTimeStr);
    }

    @GET
    @Path("/logs/summary")
    public List<LogMessage> getLogSummary() {
        return loggingService.getLogSummary();
    }
}
