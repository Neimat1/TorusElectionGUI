package service.impl;

import model.ElectionRequest;
import model.ElectionResult;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import service.ElectionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TorusElectionServiceTest {
    private final ElectionService service = new TorusElectionService();

    @Test
    void runElectionReturnsLeaderAndAnimationData() {
        ElectionResult result = service.runElection(new ElectionRequest(2, 2, new int[]{1, 4, 3, 2}));

        assertEquals("Completed", result.status());
        assertEquals(4, result.leaderId());
        assertEquals(0, result.leaderPosition().row());
        assertEquals(1, result.leaderPosition().col());
        assertEquals(4, result.nodes().size());
        assertEquals(4, result.nodes().stream().filter(node -> node.leader()).findFirst().orElseThrow().id());
    }

    @Test
    void runElectionRejectsDuplicateIds() {
        ElectionRequest request = new ElectionRequest(2, 2, new int[]{1, 2, 2, 4});

        assertThrows(BadRequestException.class, () -> service.runElection(request));
    }
}
