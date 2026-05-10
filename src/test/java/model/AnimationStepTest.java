package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimationStepTest {
    @Test
    void exposesMessageSnapshot() {
        Position sender = new Position(0, 0);
        Position receiver = new Position(0, 1);

        AnimationStep step = new AnimationStep(sender, receiver, 12, 5, 40, 3, true);

        assertEquals(sender, step.getSenderPosition());
        assertEquals(receiver, step.getReceiverPosition());
        assertEquals(12, step.getSenderId());
        assertEquals(5, step.getReceiverId());
        assertEquals(40, step.getTransmittedValue());
        assertEquals(3, step.getRound());
        assertTrue(step.isUpdated());
    }
}
