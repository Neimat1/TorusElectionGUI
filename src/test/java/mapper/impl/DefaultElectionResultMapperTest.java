package mapper.impl;

import mapper.ElectionResultMapper;
import model.ElectionRequest;
import model.ElectionResult;
import org.junit.jupiter.api.Test;
import service.LeaderElectionAlgorithm;
import service.TorusNetworkService;
import service.impl.TorusLeaderElectionAlgorithm;
import service.impl.TorusNetwork;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultElectionResultMapperTest {
    private final ElectionResultMapper mapper = new DefaultElectionResultMapper();

    @Test
    void mapsElectionDomainStateToResult() {
        ElectionRequest request = new ElectionRequest(2, 2, new int[]{1, 4, 3, 2});
        TorusNetworkService network = new TorusNetwork(2, 2, request.ids());
        LeaderElectionAlgorithm algorithm = new TorusLeaderElectionAlgorithm(network);
        algorithm.electLeader();

        ElectionResult result = mapper.map(request, network, algorithm, "09:00:00 AM", "09:00:01 AM");

        assertEquals("Completed", result.status());
        assertEquals(4, result.leaderId());
        assertEquals(0, result.leaderPosition().row());
        assertEquals(1, result.leaderPosition().col());
        assertEquals(4, result.nodes().size());
        assertEquals(algorithm.getMessages(), result.steps().size());
        assertTrue(result.log().get(0).contains("Initialization complete"));
    }
}
