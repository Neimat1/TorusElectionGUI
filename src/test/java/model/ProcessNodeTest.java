package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessNodeTest {
    @Test
    void initializesFromIdAndPosition() {
        Position position = new Position(0, 1);
        ProcessNode node = new ProcessNode(12, position);

        assertEquals(12, node.getId());
        assertEquals(12, node.getMaxKnownId());
        assertSame(position, node.getPosition());
        assertFalse(node.isLeader());
    }

    @Test
    void onlyRaisesKnownMaximum() {
        ProcessNode node = new ProcessNode(12, new Position(0, 0));

        node.updateMaxKnownId(7);
        assertEquals(12, node.getMaxKnownId());

        node.updateMaxKnownId(33);
        assertEquals(33, node.getMaxKnownId());
    }

    @Test
    void resetRestoresElectionState() {
        ProcessNode node = new ProcessNode(12, new Position(0, 0));
        node.updateMaxKnownId(33);
        node.setLeader(true);

        node.resetElectionState();

        assertEquals(12, node.getMaxKnownId());
        assertFalse(node.isLeader());
    }

    @Test
    void leaderFlagCanBeSet() {
        ProcessNode node = new ProcessNode(12, new Position(0, 0));

        node.setLeader(true);

        assertTrue(node.isLeader());
    }
}
