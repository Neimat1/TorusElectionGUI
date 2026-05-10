package gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TorusElectionGUITest {
    @Test
    void roundsLabelClarifiesConvergenceCheckPass() {
        assertEquals("Rounds Checked:", TorusElectionGUI.ROUNDS_LABEL_TEXT);
    }
}
