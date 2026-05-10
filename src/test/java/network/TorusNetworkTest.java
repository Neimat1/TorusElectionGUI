package network;

import model.ProcessNode;
import model.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TorusNetworkTest {
    @Test
    void rejectsInvalidDimensionsAndIdCounts() {
        assertThrows(IllegalArgumentException.class, () -> new TorusNetwork(1, 2, new int[]{1, 2}));
        assertThrows(IllegalArgumentException.class, () -> new TorusNetwork(2, 1, new int[]{1, 2}));
        assertThrows(IllegalArgumentException.class, () -> new TorusNetwork(2, 2, new int[]{1, 2, 3}));
    }

    @Test
    void createsNodesInRowMajorOrder() {
        TorusNetwork network = new TorusNetwork(2, 3, new int[]{10, 11, 12, 20, 21, 22});

        assertEquals(2, network.getRows());
        assertEquals(3, network.getCols());
        assertEquals(6, network.getAllNodes().size());
        assertEquals(10, network.getNode(0, 0).getId());
        assertEquals(12, network.getNode(new Position(0, 2)).getId());
        assertEquals(22, network.getNode(1, 2).getId());
    }

    @Test
    void allNodesViewIsUnmodifiable() {
        TorusNetwork network = new TorusNetwork(2, 2, new int[]{1, 2, 3, 4});

        assertThrows(UnsupportedOperationException.class, () -> network.getAllNodes().add(network.getNode(0, 0)));
    }

    @Test
    void resolvesNeighborsWithTorusWraparound() {
        TorusNetwork network = new TorusNetwork(3, 3, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        ProcessNode center = network.getNode(1, 1);

        List<ProcessNode> neighbors = network.getNeighbors(center);

        assertEquals(List.of(
                network.getNode(1, 2),
                network.getNode(1, 0),
                network.getNode(2, 1),
                network.getNode(0, 1)
        ), neighbors);

        List<ProcessNode> cornerNeighbors = network.getNeighbors(network.getNode(0, 0));
        assertEquals(network.getNode(0, 1), cornerNeighbors.get(0));
        assertEquals(network.getNode(0, 2), cornerNeighbors.get(1));
        assertEquals(network.getNode(1, 0), cornerNeighbors.get(2));
        assertEquals(network.getNode(2, 0), cornerNeighbors.get(3));
    }

    @Test
    void deduplicatesOverlappingWraparoundNeighbors() {
        TorusNetwork network = new TorusNetwork(2, 2, new int[]{1, 2, 3, 4});

        List<ProcessNode> neighbors = network.getNeighbors(network.getNode(0, 0));

        assertEquals(List.of(
                network.getNode(0, 1),
                network.getNode(1, 0)
        ), neighbors);
    }

    @Test
    void neighborViewIsCachedAndUnmodifiable() {
        TorusNetwork network = new TorusNetwork(2, 2, new int[]{1, 2, 3, 4});
        ProcessNode node = network.getNode(0, 0);

        assertSame(network.getNeighbors(node), network.getNeighbors(node));
        assertThrows(UnsupportedOperationException.class, () -> network.getNeighbors(node).clear());
    }

    @Test
    void rejectsNodesFromOtherNetworks() {
        TorusNetwork network = new TorusNetwork(2, 2, new int[]{1, 2, 3, 4});
        ProcessNode outsider = new ProcessNode(99, new Position(0, 0));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> network.getNeighbors(outsider));

        assertTrue(ex.getMessage().contains("Node does not belong"));
    }
}
