package algorithm;

import model.AnimationStep;
import model.ProcessNode;
import network.TorusNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TorusElectionAlgorithmTest {
    @Test
    void electsNodeWithLargestOriginalId() {
        TorusNetwork network = new TorusNetwork(2, 2, new int[]{4, 12, 7, 3});
        TorusElectionAlgorithm algorithm = new TorusElectionAlgorithm(network);

        algorithm.electLeader();

        ProcessNode leader = network.getAllNodes().stream()
                .filter(ProcessNode::isLeader)
                .findFirst()
                .orElseThrow();

        assertEquals(12, leader.getId());
        assertEquals(12, leader.getMaxKnownId());
        assertTrue(network.getAllNodes().stream().allMatch(node -> node.getMaxKnownId() == 12));
        assertEquals(2, algorithm.getRounds());
        assertEquals(16, algorithm.getMessages());
    }

    @Test
    void recordsAnimationStepsForEveryTransmission() {
        TorusNetwork network = new TorusNetwork(2, 2, new int[]{4, 12, 7, 3});
        TorusElectionAlgorithm algorithm = new TorusElectionAlgorithm(network);

        algorithm.electLeader();

        List<AnimationStep> steps = algorithm.getAnimationSteps();

        assertEquals(algorithm.getMessages(), steps.size());
        assertEquals(1, steps.get(0).getRound());
        assertEquals(network.getNode(0, 1).getPosition(), steps.get(0).getSenderPosition());
        assertEquals(network.getNode(0, 0).getPosition(), steps.get(0).getReceiverPosition());
        assertEquals(12, steps.get(0).getTransmittedValue());
        assertTrue(steps.stream().anyMatch(AnimationStep::isUpdated));
        assertTrue(steps.stream().anyMatch(step -> !step.isUpdated()));
    }

    @Test
    void clearsPreviousRunDataAndResetsNodeState() {
        TorusNetwork network = new TorusNetwork(2, 2, new int[]{4, 12, 7, 3});
        TorusElectionAlgorithm algorithm = new TorusElectionAlgorithm(network);

        algorithm.electLeader();
        int firstStepCount = algorithm.getAnimationSteps().size();
        int firstLogCount = algorithm.getExecutionLog().size();

        network.getNode(0, 0).setLeader(true);
        network.getNode(0, 0).updateMaxKnownId(100);
        algorithm.electLeader();

        assertEquals(firstStepCount, algorithm.getAnimationSteps().size());
        assertEquals(firstLogCount, algorithm.getExecutionLog().size());
        assertFalse(network.getNode(0, 0).isLeader());
        assertEquals(12, network.getNode(0, 0).getMaxKnownId());
    }

    @Test
    void exposesUnmodifiableLiveViews() {
        TorusNetwork network = new TorusNetwork(2, 2, new int[]{4, 12, 7, 3});
        TorusElectionAlgorithm algorithm = new TorusElectionAlgorithm(network);

        List<AnimationStep> steps = algorithm.getAnimationSteps();
        List<String> log = algorithm.getExecutionLog();

        assertSame(steps, algorithm.getAnimationSteps());
        assertSame(log, algorithm.getExecutionLog());
        assertThrows(UnsupportedOperationException.class, () -> steps.clear());
        assertThrows(UnsupportedOperationException.class, () -> log.clear());

        algorithm.electLeader();

        assertEquals(algorithm.getMessages(), steps.size());
        assertTrue(log.get(0).contains("Initialization complete"));
        assertTrue(log.get(log.size() - 1).contains("Leader is Process ID: 12"));
    }
}
