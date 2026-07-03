package validation.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import model.ElectionRequest;
import validation.ElectionRequestValidator;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class DefaultElectionRequestValidator implements ElectionRequestValidator {
    @Override
    public void validate(ElectionRequest request) {
        if (request == null) {
            throw new BadRequestException("Election input is required.");
        }
        if (request.rows() < 2 || request.cols() < 2) {
            throw new BadRequestException("Rows and columns must be at least 2.");
        }
        if (request.ids() == null || request.ids().length != request.rows() * request.cols()) {
            throw new BadRequestException("Number of IDs must equal rows x columns.");
        }
        Set<Integer> unique = new HashSet<>();
        for (int id : request.ids()) {
            if (!unique.add(id)) {
                throw new BadRequestException("Process IDs must be unique.");
            }
        }
    }
}
