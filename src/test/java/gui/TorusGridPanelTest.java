package gui;

import model.AnimationStep;
import model.Position;
import network.TorusNetwork;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TorusGridPanelTest {
    @Test
    void paintsEmptyStateNetworkAndActiveStep() {
        TorusGridPanel panel = new TorusGridPanel();
        panel.setSize(700, 560);

        assertDoesNotThrow(() -> paint(panel));

        TorusNetwork network = new TorusNetwork(2, 2, new int[]{1, 4, 3, 2});
        panel.setNetwork(network);
        assertDoesNotThrow(() -> paint(panel));

        panel.setActiveStep(new AnimationStep(new Position(0, 1), new Position(0, 0), 4, 1, 4, 1, true));
        assertDoesNotThrow(() -> paint(panel));

        panel.clearAnimation();
        assertDoesNotThrow(() -> paint(panel));
    }

    private static void paint(TorusGridPanel panel) {
        BufferedImage image = new BufferedImage(800, 620, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
        }
    }
}
