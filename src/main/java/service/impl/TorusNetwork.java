package service.impl;

import model.Position;
import model.ProcessNode;
import service.TorusNetworkService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TorusNetwork implements TorusNetworkService {
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
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                nodes[row][col] = new ProcessNode(ids[index++], new Position(row, col));
                mutableNodes.add(nodes[row][col]);
            }
        }
        this.allNodes = Collections.unmodifiableList(mutableNodes);

        for (ProcessNode node : allNodes) {
            neighborsByNode.put(node, Collections.unmodifiableList(resolveNeighbors(node)));
        }
    }

    @Override
    public ProcessNode getNode(int row, int col) {
        return nodes[row][col];
    }

    @Override
    public ProcessNode getNode(Position position) {
        return nodes[position.getRow()][position.getCol()];
    }

    @Override
    public List<ProcessNode> getAllNodes() {
        return allNodes;
    }

    @Override
    public List<ProcessNode> getNeighbors(ProcessNode node) {
        List<ProcessNode> neighbors = neighborsByNode.get(node);
        if (neighbors == null) {
            throw new IllegalArgumentException("Node does not belong to this network.");
        }
        return neighbors;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getCols() {
        return cols;
    }

    private List<ProcessNode> resolveNeighbors(ProcessNode node) {
        Set<ProcessNode> neighbors = new LinkedHashSet<>();
        int row = node.getPosition().getRow();
        int col = node.getPosition().getCol();

        neighbors.add(nodes[row][(col + 1) % cols]);
        neighbors.add(nodes[row][(col - 1 + cols) % cols]);
        neighbors.add(nodes[(row + 1) % rows][col]);
        neighbors.add(nodes[(row - 1 + rows) % rows][col]);

        return new ArrayList<>(neighbors);
    }
}
