package service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.ElectionRequest;
import model.ElectionResult;
import mapper.ElectionResultMapper;
import mapper.impl.DefaultElectionResultMapper;
import validation.ElectionRequestValidator;
import validation.impl.DefaultElectionRequestValidator;
import service.ElectionService;
import service.LeaderElectionAlgorithm;
import service.LeaderElectionAlgorithmFactory;
import service.TorusNetworkFactory;
import service.TorusNetworkService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class TorusElectionService implements ElectionService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private final ElectionRequestValidator requestValidator;
    private final TorusNetworkFactory networkFactory;
    private final LeaderElectionAlgorithmFactory algorithmFactory;
    private final ElectionResultMapper resultMapper;

    @Inject
    public TorusElectionService(ElectionRequestValidator requestValidator,
                                TorusNetworkFactory networkFactory,
                                LeaderElectionAlgorithmFactory algorithmFactory,
                                ElectionResultMapper resultMapper) {
        this.requestValidator = requestValidator;
        this.networkFactory = networkFactory;
        this.algorithmFactory = algorithmFactory;
        this.resultMapper = resultMapper;
    }

    TorusElectionService() {
        this(
                new DefaultElectionRequestValidator(),
                new DefaultTorusNetworkFactory(),
                new DefaultLeaderElectionAlgorithmFactory(),
                new DefaultElectionResultMapper()
        );
    }

    @Override
    public ElectionResult runElection(ElectionRequest request) {
        requestValidator.validate(request);
        String startTime = currentTime();
        TorusNetworkService network = networkFactory.createNetwork(request.rows(), request.cols(), request.ids());
        LeaderElectionAlgorithm election = algorithmFactory.createAlgorithm(network);
        election.electLeader();
        String endTime = currentTime();

        return resultMapper.map(request, network, election, startTime, endTime);
    }

    private static String currentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }
}
