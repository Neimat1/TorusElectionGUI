package service.impl;

import model.AnimationStep;
import model.ProcessNode;
import service.LeaderElectionAlgorithm;
import service.TorusNetworkService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TorusLeaderElectionAlgorithm implements LeaderElectionAlgorithm {
    private final TorusNetworkService network;
    private final List<AnimationStep> animationSteps = new ArrayList<>();
    private final List<String> executionLog = new ArrayList<>();
    private final List<AnimationStep> animationStepsView = Collections.unmodifiableList(animationSteps);
    private final List<String> executionLogView = Collections.unmodifiableList(executionLog);
    private int rounds;
    private int messages;

    public TorusLeaderElectionAlgorithm(TorusNetworkService network) {
        this.network = network;
    }

    @Override
    public void electLeader() {
        animationSteps.clear();
        executionLog.clear();
        rounds = 0;
        messages = 0;

        List<ProcessNode> nodes = network.getAllNodes();
        executionLog.add("Initialization complete. Total processes: " + nodes.size());
        executionLog.add("Starting leader election.");

        for (ProcessNode node : nodes) {
            node.resetElectionState();
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            rounds++;
            int roundMessages = 0;
            int updatedNodesInRound = 0;

            for (ProcessNode current : nodes) {
                for (ProcessNode neighbor : network.getNeighbors(current)) {
                    messages++;
                    roundMessages++;

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
                        updatedNodesInRound++;
                        changed = true;
                    }
                }
            }

            if (changed) {
                executionLog.add("Round " + rounds + " completed. Status: " + updatedNodesInRound
                        + " nodes updated, " + roundMessages + " messages exchanged.");
            } else {
                executionLog.add("Round " + rounds + " completed (convergence check). Status: No updates detected - election converged.");
            }
        }

        int leaderId = findMaximumId(nodes);
        for (ProcessNode node : nodes) {
            if (node.getId() == leaderId) {
                node.setLeader(true);
                executionLog.add("Election completed. Leader is Process ID: " + node.getId()
                        + " at position " + node.getPosition() + ". Total rounds: " + rounds
                        + ", Total messages: " + messages);
            }
        }
    }

    private int findMaximumId(List<ProcessNode> nodes) {
        int max = Integer.MIN_VALUE;
        for (ProcessNode node : nodes) {
            max = Math.max(max, node.getId());
        }
        return max;
    }

    @Override
    public List<AnimationStep> getAnimationSteps() {
        return animationStepsView;
    }

    @Override
    public List<String> getExecutionLog() {
        return executionLogView;
    }

    @Override
    public int getRounds() {
        return rounds;
    }

    @Override
    public int getMessages() {
        return messages;
    }
}
