package service;

import model.ElectionRequest;
import model.ElectionResult;

public interface ElectionService {
    ElectionResult runElection(ElectionRequest request);
}
