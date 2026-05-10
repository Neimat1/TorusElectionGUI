package algorithm;

import model.AnimationStep;
import model.ProcessNode;
import network.TorusNetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TorusElectionAlgorithm {
    private final TorusNetwork network;
    private final List<AnimationStep> animationSteps = new ArrayList<>();
    private final List<String> executionLog = new ArrayList<>();
    private final List<AnimationStep> animationStepsView = Collections.unmodifiableList(animationSteps);
    private final List<String> executionLogView = Collections.unmodifiableList(executionLog);
    private int rounds;
    private int messages;

    public TorusElectionAlgorithm(TorusNetwork network) {
        this.network = network;
    }

    public void electLeader() {
        animationSteps.clear();
        executionLog.clear();
        rounds = 0;
        messages = 0;

        executionLog.add("Initialization complete. Total processes: " + network.getAllNodes().size());
        executionLog.add("Starting leader election.");

        List<ProcessNode> nodes = network.getAllNodes();
        for (ProcessNode node : nodes) {
            node.resetElectionState();
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            rounds++;

            for (ProcessNode current : nodes) {
                for (ProcessNode neighbor : network.getNeighbors(current)) {
                    messages++;

                    int receivedId = neighbor.getMaxKnownId();
                    boolean updated = receivedId > current.getMaxKnownId();

                    animationSteps.add(new AnimationStep(
                            neighbor.getPosition(),
                            current.getPosition(),
                            neighbor.getId(),
                            current.getId(),
                            receivedId,
                            rounds,
                            updated
                    ));

                    if (updated) {
                        current.updateMaxKnownId(receivedId);
                        changed = true;
                    }
                }
            }

            executionLog.add("Round " + rounds + " completed.");
        }

        int leaderId = findMaximumId();
        for (ProcessNode node : nodes) {
            if (node.getId() == leaderId) {
                node.setLeader(true);
                executionLog.add("Election completed. Leader is Process ID: " + node.getId()
                        + " at position " + node.getPosition());
            }
        }
    }

    private int findMaximumId() {
        int max = Integer.MIN_VALUE;
        for (ProcessNode node : network.getAllNodes()) {
            max = Math.max(max, node.getId());
        }
        return max;
    }

    public List<AnimationStep> getAnimationSteps() {
        return animationStepsView;
    }

    public List<String> getExecutionLog() {
        return executionLogView;
    }

    public int getRounds() { return rounds; }
    public int getMessages() { return messages; }
}
