package service;

public interface LeaderElectionAlgorithmFactory {
    LeaderElectionAlgorithm createAlgorithm(TorusNetworkService network);
}
