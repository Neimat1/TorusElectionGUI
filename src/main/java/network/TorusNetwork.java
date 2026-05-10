package network;

import model.Position;
import model.ProcessNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class TorusNetwork {
    private final int rows;
    private final int cols;
    private final ProcessNode[][] nodes;
    private final List<ProcessNode> allNodes;
    private final Map<ProcessNode, List<ProcessNode>> neighborsByNode;

    public TorusNetwork(int rows, int cols, int[] ids) {
        if (rows < 2 || cols < 2) {
            throw new IllegalArgumentException("Rows and columns must be at least 2.");
        }
        if (ids.length != rows * cols) {
            throw new IllegalArgumentException("Number of IDs must equal rows x columns.");
        }

        this.rows = rows;
        this.cols = cols;
        this.nodes = new ProcessNode[rows][cols];
        this.neighborsByNode = new IdentityHashMap<>(rows * cols);

        List<ProcessNode> mutableNodes = new ArrayList<>(rows * cols);
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                nodes[r][c] = new ProcessNode(ids[index++], new Position(r, c));
                mutableNodes.add(nodes[r][c]);
            }
        }
        this.allNodes = Collections.unmodifiableList(mutableNodes);

        for (ProcessNode node : allNodes) {
            neighborsByNode.put(node, Collections.unmodifiableList(resolveNeighbors(node)));
        }
    }

    public ProcessNode getNode(int row, int col) { return nodes[row][col]; }
    public ProcessNode getNode(Position position) { return nodes[position.getRow()][position.getCol()]; }

    public List<ProcessNode> getAllNodes() {
        return allNodes;
    }

    public List<ProcessNode> getNeighbors(ProcessNode node) {
        List<ProcessNode> neighbors = neighborsByNode.get(node);
        if (neighbors == null) {
            throw new IllegalArgumentException("Node does not belong to this network.");
        }
        return neighbors;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    private List<ProcessNode> resolveNeighbors(ProcessNode node) {
        List<ProcessNode> neighbors = new ArrayList<>();
        int r = node.getPosition().getRow();
        int c = node.getPosition().getCol();

        neighbors.add(nodes[r][(c + 1) % cols]);
        neighbors.add(nodes[r][(c - 1 + cols) % cols]);
        neighbors.add(nodes[(r + 1) % rows][c]);
        neighbors.add(nodes[(r - 1 + rows) % rows][c]);

        return neighbors;
    }
}
