package service;

import model.AnimationStep;

import java.util.List;

public interface LeaderElectionAlgorithm {
    void electLeader();

    List<AnimationStep> getAnimationSteps();

    List<String> getExecutionLog();

    int getRounds();

    int getMessages();
}
