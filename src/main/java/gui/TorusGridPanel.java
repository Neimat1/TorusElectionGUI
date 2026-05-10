package gui;

import model.AnimationStep;
import model.Position;
import model.ProcessNode;
import network.TorusNetwork;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class TorusGridPanel extends JPanel {
    private static final Color BACKGROUND = Color.WHITE;
    private static final Color EMPTY_TEXT = Color.GRAY;
    private static final Color LINK_COLOR = new Color(0, 95, 220);
    private static final Color PROCESS_FILL = new Color(230, 245, 255);
    private static final Color PROCESS_BORDER = new Color(0, 120, 255);
    private static final Color SENDER_FILL = new Color(255, 220, 120);
    private static final Color SENDER_BORDER = new Color(230, 140, 0);
    private static final Color RECEIVER_UPDATED_FILL = new Color(255, 160, 160);
    private static final Color RECEIVER_FILL = new Color(255, 210, 210);
    private static final Color RECEIVER_BORDER = new Color(200, 60, 60);
    private static final Color LEADER_FILL = new Color(170, 235, 170);
    private static final Color LEADER_BORDER = new Color(0, 140, 0);
    private static final Color MESSAGE_FILL = new Color(220, 30, 30);
    private static final Color FOOTER_LINE = new Color(210, 225, 240);
    private static final Color FOOTER_TEXT = new Color(0, 70, 160);
    private static final Font EMPTY_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font NODE_ID_FONT = new Font("Arial", Font.BOLD, 22);
    private static final Font NODE_MAX_FONT = new Font("Arial", Font.PLAIN, 10);
    private static final Font MESSAGE_FONT = new Font("Arial", Font.BOLD, 11);
    private static final Font FOOTER_FONT = new Font("Arial", Font.BOLD, 13);
    private static final BasicStroke SOLID_STROKE = new BasicStroke(2);
    private static final BasicStroke THIN_STROKE = new BasicStroke(1);
    private static final BasicStroke WRAP_STROKE = new BasicStroke(
            2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 6}, 0
    );

    private TorusNetwork network;
    private AnimationStep activeStep;
    private final int nodeSize = 64;
    private final int footerHeight = 62;

    public TorusGridPanel() {
        setBackground(BACKGROUND);
    }

    public void setNetwork(TorusNetwork network) {
        this.network = network;
        repaint();
    }

    public void setActiveStep(AnimationStep step) {
        this.activeStep = step;
        repaint();
    }

    public void clearAnimation() {
        this.activeStep = null;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(620, 500);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (network == null) {
            drawEmpty(g2);
            return;
        }

        Layout layout = createLayout();
        drawLinks(g2, layout);
        drawWrapLinks(g2, layout);
        drawNodes(g2, layout);
        drawAnimatedMessage(g2, layout);
        drawFooter(g2);
    }

    private void drawEmpty(Graphics2D g2) {
        g2.setColor(EMPTY_TEXT);
        g2.setFont(EMPTY_FONT);
        g2.drawString("Click Run Election ", 60, 240);
        g2.drawString("or Auto Animate " ,60, 260);
        g2.drawString("to visualize the Torus Network", 60, 280);
    }

    private Layout createLayout() {
        int rows = network.getRows();
        int cols = network.getCols();
        int startX = 80;
        int startY = 60;
        int gapX = Math.max(95, (getWidth() - 170) / Math.max(cols - 1, 1));
        int availableHeight = getHeight() - startY - footerHeight - nodeSize - 35;
        int gapY = Math.max(70, availableHeight / Math.max(rows - 1, 1));
        return new Layout(rows, cols, startX, startY, gapX, gapY);
    }

    private void drawLinks(Graphics2D g2, Layout layout) {
        g2.setColor(LINK_COLOR);
        g2.setStroke(SOLID_STROKE);

        for (int r = 0; r < layout.rows; r++) {
            for (int c = 0; c < layout.cols; c++) {
                int x = layout.x(c);
                int y = layout.y(r);

                if (c < layout.cols - 1) {
                    drawArrowLine(g2, x + nodeSize, y + nodeSize / 2, layout.x(c + 1), y + nodeSize / 2);
                    drawArrowLine(g2, layout.x(c + 1), y + nodeSize / 2, x + nodeSize, y + nodeSize / 2);
                }

                if (r < layout.rows - 1) {
                    drawArrowLine(g2, x + nodeSize / 2, y + nodeSize, x + nodeSize / 2, layout.y(r + 1));
                    drawArrowLine(g2, x + nodeSize / 2, layout.y(r + 1), x + nodeSize / 2, y + nodeSize);
                }
            }
        }
    }

    private void drawWrapLinks(Graphics2D g2, Layout layout) {
        g2.setColor(LINK_COLOR);
        g2.setStroke(WRAP_STROKE);

        int leftX = layout.x(0);
        int rightX = layout.x(layout.cols - 1);
        int topY = layout.y(0);
        int bottomY = layout.y(layout.rows - 1);

        for (int r = 0; r < layout.rows; r++) {
            int y = layout.y(r) + nodeSize / 2;
            g2.drawArc(leftX - 45, y - 30, rightX - leftX + nodeSize + 90, 60, 180, 180);
        }

        for (int c = 0; c < layout.cols; c++) {
            int x = layout.x(c) + nodeSize / 2;
            g2.drawArc(x - 30, topY - 40, 60, bottomY - topY + nodeSize + 80, 90, 180);
        }

        g2.setStroke(SOLID_STROKE);
    }

    private void drawNodes(Graphics2D g2, Layout layout) {
        for (int r = 0; r < layout.rows; r++) {
            for (int c = 0; c < layout.cols; c++) {
                ProcessNode node = network.getNode(r, c);
                int x = layout.x(c);
                int y = layout.y(r);

                Color fill = PROCESS_FILL;
                Color border = PROCESS_BORDER;

                if (node.isLeader()) {
                    fill = LEADER_FILL;
                    border = LEADER_BORDER;
                }
                if (activeStep != null && node.getPosition().equals(activeStep.getSenderPosition())) {
                    fill = SENDER_FILL;
                    border = SENDER_BORDER;
                } else if (activeStep != null && node.getPosition().equals(activeStep.getReceiverPosition())) {
                    fill = activeStep.isUpdated() ? RECEIVER_UPDATED_FILL : RECEIVER_FILL;
                    border = RECEIVER_BORDER;
                }

                g2.setColor(fill);
                g2.fillRoundRect(x, y, nodeSize, nodeSize, 10, 10);
                g2.setColor(border);
                g2.drawRoundRect(x, y, nodeSize, nodeSize, 10, 10);

                g2.setColor(Color.BLACK);
                g2.setFont(NODE_ID_FONT);
                String text = String.valueOf(node.getId());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, x + (nodeSize - fm.stringWidth(text)) / 2, y + 38);

                g2.setFont(NODE_MAX_FONT);
                String maxText = "max=" + node.getMaxKnownId();
                g2.drawString(maxText, x + (nodeSize - g2.getFontMetrics().stringWidth(maxText)) / 2, y + 55);
            }
        }
    }

    private void drawAnimatedMessage(Graphics2D g2, Layout layout) {
        if (activeStep == null) return;

        Position s = activeStep.getSenderPosition();
        Position r = activeStep.getReceiverPosition();

        int sx = layout.x(s.getCol()) + nodeSize / 2;
        int sy = layout.y(s.getRow()) + nodeSize / 2;
        int rx = layout.x(r.getCol()) + nodeSize / 2;
        int ry = layout.y(r.getRow()) + nodeSize / 2;

        int mx = (sx + rx) / 2;
        int my = (sy + ry) / 2;

        g2.setColor(MESSAGE_FILL);
        g2.fillOval(mx - 13, my - 13, 26, 26);
        g2.setColor(Color.WHITE);
        g2.setFont(MESSAGE_FONT);
        String value = String.valueOf(activeStep.getTransmittedValue());
        g2.drawString(value, mx - g2.getFontMetrics().stringWidth(value) / 2, my + 4);
    }

    private void drawFooter(Graphics2D g2) {
        int centerX = getWidth() / 2;
        int footerTop = getHeight() - footerHeight;
        int separatorY = footerTop + 7;

        g2.setColor(BACKGROUND);
        g2.fillRect(0, footerTop, getWidth(), footerHeight);

        g2.setStroke(THIN_STROKE);
        g2.setColor(FOOTER_LINE);
        g2.drawLine(centerX - 180, separatorY, centerX - 35, separatorY);
        g2.drawLine(centerX + 35, separatorY, centerX + 180, separatorY);

        g2.setStroke(SOLID_STROKE);
        g2.setColor(LINK_COLOR);
        g2.drawLine(centerX - 26, separatorY, centerX - 8, separatorY);
        g2.drawLine(centerX + 8, separatorY, centerX + 26, separatorY);

        g2.setColor(FOOTER_TEXT);
        g2.setFont(FOOTER_FONT);
        g2.drawString("Last row connects to first row", centerX - 95, footerTop + 31);
        g2.drawString("Last column connects to first column", centerX - 115, footerTop + 51);
    }

    private void drawArrowLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.drawLine(x1, y1, x2, y2);
        int arrowSize = 7;
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int xArrow1 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int yArrow1 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int xArrow2 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int yArrow2 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));
        g2.drawLine(x2, y2, xArrow1, yArrow1);
        g2.drawLine(x2, y2, xArrow2, yArrow2);
    }

    private static class Layout {
        final int rows, cols, startX, startY, gapX, gapY;
        Layout(int rows, int cols, int startX, int startY, int gapX, int gapY) {
            this.rows = rows; this.cols = cols; this.startX = startX; this.startY = startY; this.gapX = gapX; this.gapY = gapY;
        }
        int x(int col) { return startX + col * gapX; }
        int y(int row) { return startY + row * gapY; }
    }
}
