package validation.impl;

import jakarta.ws.rs.BadRequestException;
import model.ElectionRequest;
import org.junit.jupiter.api.Test;
import validation.ElectionRequestValidator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultElectionRequestValidatorTest {
    private final ElectionRequestValidator validator = new DefaultElectionRequestValidator();

    @Test
    void acceptsValidElectionInput() {
        assertDoesNotThrow(() -> validator.validate(new ElectionRequest(2, 2, new int[]{1, 2, 3, 4})));
    }

    @Test
    void rejectsInvalidElectionInput() {
        assertThrows(BadRequestException.class, () -> validator.validate(null));
        assertThrows(BadRequestException.class, () -> validator.validate(new ElectionRequest(1, 2, new int[]{1, 2})));
        assertThrows(BadRequestException.class, () -> validator.validate(new ElectionRequest(2, 2, new int[]{1, 2, 3})));
        assertThrows(BadRequestException.class, () -> validator.validate(new ElectionRequest(2, 2, new int[]{1, 2, 2, 4})));
    }
}
