package validation;

import model.ElectionRequest;

public interface ElectionRequestValidator {
    void validate(ElectionRequest request);
}
