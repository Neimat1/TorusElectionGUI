package service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import service.LeaderElectionAlgorithm;
import service.LeaderElectionAlgorithmFactory;
import service.TorusNetworkService;

@ApplicationScoped
public class DefaultLeaderElectionAlgorithmFactory implements LeaderElectionAlgorithmFactory {
    @Override
    public LeaderElectionAlgorithm createAlgorithm(TorusNetworkService network) {
        return new TorusLeaderElectionAlgorithm(network);
    }
}
