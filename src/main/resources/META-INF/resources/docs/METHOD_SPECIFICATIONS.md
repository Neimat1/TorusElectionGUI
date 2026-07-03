# Method Specifications

- ElectionResource.runElection: REST boundary for POST /api/election.
- ElectionService.runElection: application use case.
- ElectionRequestValidator.validate: validates request shape and unique IDs.
- ElectionResultMapper.map: converts domain state to response records.
- TorusNetworkService: torus topology access.
- LeaderElectionAlgorithm: election execution and metrics.
