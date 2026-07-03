package mapper.impl;

import jakarta.enterprise.context.ApplicationScoped;
import mapper.ElectionResultMapper;
import model.AnimationStep;
import model.ElectionRequest;
import model.ElectionResult;
import model.NodeState;
import model.PositionState;
import model.ProcessNode;
import model.StepState;
import service.LeaderElectionAlgorithm;
import service.TorusNetworkService;

import java.util.List;

@ApplicationScoped
public class DefaultElectionResultMapper implements ElectionResultMapper {
    @Override
    public ElectionResult map(ElectionRequest request, TorusNetworkService network, LeaderElectionAlgorithm election,
                              String startTime, String endTime) {
        ProcessNode leader = findLeader(network);
        return new ElectionResult(
                "Completed",
                request.rows(),
                request.cols(),
                leader.getId(),
                new PositionState(leader.getPosition().getRow(), leader.getPosition().getCol()),
                election.getRounds(),
                election.getMessages(),
                startTime,
                endTime,
                toNodeStates(network),
                toStepStates(election),
                election.getExecutionLog());
    }

    private static ProcessNode findLeader(TorusNetworkService network) {
        return network.getAllNodes().stream()
                .filter(ProcessNode::isLeader)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Election completed without a leader."));
    }

    private static List<NodeState> toNodeStates(TorusNetworkService network) {
        return network.getAllNodes().stream()
                .map(node -> new NodeState(
                        node.getId(),
                        node.getPosition().getRow(),
                        node.getPosition().getCol(),
                        node.getMaxKnownId(),
                        node.isLeader()))
                .toList();
    }

    private static List<StepState> toStepStates(LeaderElectionAlgorithm election) {
        return election.getAnimationSteps().stream()
                .map(DefaultElectionResultMapper::toStepState)
                .toList();
    }

    private static StepState toStepState(AnimationStep step) {
        return new StepState(
                new PositionState(step.getSenderPosition().getRow(), step.getSenderPosition().getCol()),
                new PositionState(step.getReceiverPosition().getRow(), step.getReceiverPosition().getCol()),
                step.getSenderId(),
                step.getReceiverId(),
                step.getTransmittedValue(),
                step.getRound(),
                step.isUpdated());
    }
}
