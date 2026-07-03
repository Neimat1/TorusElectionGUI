package resource;

import model.ElectionRequest;
import model.ElectionResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import service.ElectionService;

@Path("/api/election")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ElectionResource {
    private final ElectionService electionService;

    @Inject
    public ElectionResource(ElectionService electionService) {
        this.electionService = electionService;
    }

    @POST
    public ElectionResult runElection(ElectionRequest request) {
        return electionService.runElection(request);
    }
}
