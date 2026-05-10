package model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ProcessNode {
    private final int id;
    private final Position position;
    private int maxKnownId;
    @Setter
    private boolean leader;

    public ProcessNode(int id, Position position) {
        this.id = id;
        this.position = position;
        this.maxKnownId = id;
        this.leader = false;
    }

    public void updateMaxKnownId(int receivedId) {
        if (receivedId > maxKnownId) {
            maxKnownId = receivedId;
        }
    }

    public void resetElectionState() {
        maxKnownId = id;
        leader = false;
    }
}
