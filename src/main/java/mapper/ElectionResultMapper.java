package mapper;

import model.ElectionRequest;
import model.ElectionResult;
import service.LeaderElectionAlgorithm;
import service.TorusNetworkService;

public interface ElectionResultMapper {
    ElectionResult map(ElectionRequest request, TorusNetworkService network, LeaderElectionAlgorithm election,
                       String startTime, String endTime);
}
