package service;

import model.Position;
import model.ProcessNode;

import java.util.List;

public interface TorusNetworkService {
    ProcessNode getNode(int row, int col);

    ProcessNode getNode(Position position);

    List<ProcessNode> getAllNodes();

    List<ProcessNode> getNeighbors(ProcessNode node);

    int getRows();

    int getCols();
}
