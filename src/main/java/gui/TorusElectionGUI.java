package gui;

import algorithm.TorusElectionAlgorithm;
import logging.AppLog;
import model.AnimationStep;
import model.ProcessNode;
import network.TorusNetwork;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class TorusElectionGUI extends JFrame {
    private static final int ANIMATION_STEP_DELAY_MS = 1000;
    private static final Color SURFACE = Color.WHITE;
    private static final Color PAGE_BACKGROUND = new Color(245, 248, 252);
    private static final Color PANEL_BORDER = new Color(215, 225, 235);
    private static final Color BUTTON_BACKGROUND = new Color(243, 247, 251);
    private static final Color BUTTON_BORDER = new Color(196, 210, 226);
    private static final Color BUTTON_TEXT = new Color(19, 32, 51);
    private static final Color LOG_CARET = new Color(127, 199, 255);
    private static final Color LEGEND_TEXT = new Color(70, 88, 120);
    private static final Color SIDEBAR_BACKGROUND = new Color(15, 35, 65);
    private static final Color RUNNING_STATUS_COLOR = new Color(220, 120, 0);
    private static final Color COMPLETED_STATUS_COLOR = new Color(0, 130, 0);
    private static final Color PROCESS_FILL = new Color(230, 245, 255);
    private static final Color PROCESS_BORDER = new Color(0, 120, 255);
    private static final Color SENDER_FILL = new Color(255, 220, 120);
    private static final Color SENDER_BORDER = new Color(230, 140, 0);
    private static final Color RECEIVER_FILL = new Color(255, 160, 160);
    private static final Color RECEIVER_BORDER = new Color(200, 60, 60);
    private static final Color LEADER_FILL = new Color(170, 235, 170);
    private static final Color LEADER_BORDER = new Color(0, 140, 0);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a");

    private final JSpinner rowsSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 8, 1));
    private final JSpinner colsSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 8, 1));
    private final JTextField totalProcessesField = new JTextField("16");
    private final JTextArea idsArea = new JTextArea(
            "12 5 33 8\n" +
                    "17 40 2 29\n" +
                    "11 6 55 21\n" +
                    "9 14 31 25"
    );

    private final JLabel statusValue = new JLabel("Not Started");
    private final JLabel leaderIdValue = new JLabel("-");
    private final JLabel leaderPositionValue = new JLabel("-");
    private final JLabel roundsValue = new JLabel("-");
    private final JLabel messagesValue = new JLabel("-");
    private final JLabel startTimeValue = new JLabel("-");
    private final JLabel endTimeValue = new JLabel("-");

    private final JTextArea logArea = new JTextArea();
    private final TorusGridPanel gridPanel = new TorusGridPanel();
    private TorusNetwork currentNetwork;
    private volatile boolean animationRunning = false;
    private Thread animationThread;

    public TorusElectionGUI() {
        setTitle("Torus Network Leader Election Prototype");
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createSideBar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        rowsSpinner.addChangeListener(e -> updateTotalProcesses());
        colsSpinner.addChangeListener(e -> updateTotalProcesses());

        setVisible(true);
    }

    private JPanel createSideBar() {
        JPanel sideBar = new JPanel();
        sideBar.setPreferredSize(new Dimension(190, 800));
        sideBar.setBackground(SIDEBAR_BACKGROUND);
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("<html><br><b>Torus Election</b><br>Distributed Systems</html>");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.PLAIN, 15));
        title.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 15));

        sideBar.add(title);
        sideBar.add(menuItem("Home"));
        sideBar.add(menuItem("Input"));
        sideBar.add(menuItem("Run Election"));
        sideBar.add(menuItem("Visualization"));
        sideBar.add(menuItem("Results"));
        sideBar.add(menuItem("About"));
        return sideBar;
    }

    private JLabel menuItem(String text) {
        JLabel item = new JLabel("   " + text);
        item.setForeground(Color.WHITE);
        item.setFont(new Font("Arial", Font.PLAIN, 14));
        item.setPreferredSize(new Dimension(180, 45));
        item.setMaximumSize(new Dimension(180, 45));
        return item;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        main.setBackground(PAGE_BACKGROUND);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 8, 8));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(230, 560));
        left.add(createConfigPanel());
        left.add(createIdsPanel());

        JPanel right = new JPanel(new GridLayout(3, 1, 8, 8));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(280, 560));
        right.add(createStatusPanel());
        right.add(createLegendPanel());
        right.add(createControlsPanel());

        top.add(left, BorderLayout.WEST);
        top.add(createVisualizationPanel(), BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        main.add(top, BorderLayout.CENTER);
        main.add(createLogPanel(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel createConfigPanel() {
        JPanel panel = smoothCard("Network Configuration");

        JPanel gridPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        gridPanel.setOpaque(false);

        totalProcessesField.setEditable(false);
        totalProcessesField.setForeground(BUTTON_TEXT);
        totalProcessesField.setBackground(BUTTON_BACKGROUND);
        totalProcessesField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BORDER),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        gridPanel.add(new JLabel("Rows:"));
        gridPanel.add(rowsSpinner);
        gridPanel.add(new JLabel("Columns:"));
        gridPanel.add(colsSpinner);
        gridPanel.add(new JLabel("Total Processes:"));
        gridPanel.add(totalProcessesField);
        panel.add(gridPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createIdsPanel() {
        JPanel panel = smoothCard("Enter Process IDs");

        idsArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        idsArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        idsArea.setForeground(BUTTON_TEXT);
        idsArea.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JScrollPane scrollPane = new JScrollPane(idsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BUTTON_BORDER));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel hint = new JLabel("<html>Enter IDs row-wise, separated by spaces or new lines.</html>");
        hint.setForeground(Color.GRAY);
        panel.add(hint, BorderLayout.SOUTH);

        return panel;
    }
    private JPanel createVisualizationPanel() {
        JPanel panel = smoothCard("Torus Network Visualization");
        JPanel boardPanel = new JPanel(new BorderLayout());

        boardPanel.add(gridPanel, BorderLayout.CENTER);
        panel.add(boardPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusPanel() {


        JPanel panel = smoothCard("Election Status");

        JPanel gridPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        gridPanel.setOpaque(false);
        gridPanel.add(new JLabel("Status:")); gridPanel.add(statusValue);
        gridPanel.add(new JLabel("Leader ID:")); gridPanel.add(leaderIdValue);
        gridPanel.add(new JLabel("Leader Position:")); gridPanel.add(leaderPositionValue);
        gridPanel.add(new JLabel("Rounds:")); gridPanel.add(roundsValue);
        gridPanel.add(new JLabel("Messages Exchanged:")); gridPanel.add(messagesValue);
        gridPanel.add(new JLabel("Start Time:")); gridPanel.add(startTimeValue);
        gridPanel.add(new JLabel("End Time:")); gridPanel.add(endTimeValue);
        panel.add(gridPanel);
        return panel;
    }




    private JPanel createLegendPanel() {
        JPanel panel = smoothCard("Legend");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalStrut(10));
        panel.add(legendRow(PROCESS_FILL, PROCESS_BORDER, "Process"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(legendRow(SENDER_FILL, SENDER_BORDER, "Sender"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(legendRow(RECEIVER_FILL, RECEIVER_BORDER, "Receiver"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(legendRow(LEADER_FILL, LEADER_BORDER, "Leader"));
        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = smoothCard("Controls");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        JButton runButton = new JButton("Run Election");
        JButton animateButton = new JButton("Auto Animate");
        JButton resetButton = new JButton("Reset");

        styleButton(runButton);
        styleButton(animateButton);
        styleButton(resetButton);

        runButton.addActionListener(e -> runElectionOnly());
        animateButton.addActionListener(e -> runElectionWithAnimation());
        resetButton.addActionListener(e -> reset());

        buttonPanel.add(Box.createVerticalStrut(14));
        buttonPanel.add(runButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(animateButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(resetButton);

        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

    private void styleButton(JButton button) {
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(230, 40));
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(BUTTON_TEXT);
        button.setBackground(BUTTON_BACKGROUND);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_BORDER),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setMargin(new Insets(4, 12, 4, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel createLogPanel() {
        JPanel panel = smoothCard("Execution Log");
        panel.setPreferredSize(new Dimension(1000, 170));


        logArea.setEditable(false);

        logArea.setCaretColor(LOG_CARET);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
       // logArea.setMargin(new Insets(12, 14, 12, 14));

        logArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        logArea.setBackground(BUTTON_BACKGROUND);
        logArea.setForeground(BUTTON_TEXT);
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BUTTON_BORDER));
        scrollPane.getViewport().setBackground(BUTTON_BACKGROUND);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel smoothCard(String titlePanel) {
        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));


        JLabel title = new JLabel(titlePanel);
        title.setAlignmentX(LEFT_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(Color.BLACK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
        return panel;
    }

    private JPanel legendRow(Color fill, Color border, String text) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(220, 22));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        label.setForeground(LEGEND_TEXT);

        row.add(new RoundedSwatch(fill, border));
        row.add(Box.createHorizontalStrut(10));
        row.add(label);
        return row;
    }

    private static class RoundedSwatch extends JPanel {
        private final Color fill;
        private final Color border;

        RoundedSwatch(Color fill, Color border) {
            this.fill = fill;
            this.border = border;
            setOpaque(false);
            setPreferredSize(new Dimension(18, 18));
            setMaximumSize(new Dimension(18, 18));
            setMinimumSize(new Dimension(18, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 5, 5);
            g2.setColor(border);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 5, 5);
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private static final int ARC_SIZE = 15;
        private static final int BORDER_WIDTH = 1;
        private static final BasicStroke BORDER_STROKE = new BasicStroke(BORDER_WIDTH);

        RoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw rounded filled background
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC_SIZE, ARC_SIZE);

            // Draw rounded border
            g2.setColor(PANEL_BORDER);
            g2.setStroke(BORDER_STROKE);
            g2.drawRoundRect(BORDER_WIDTH / 2, BORDER_WIDTH / 2,
                    getWidth() - BORDER_WIDTH - 1, getHeight() - BORDER_WIDTH - 1,
                    ARC_SIZE, ARC_SIZE);

            g2.dispose();
        }

        @Override
        public void paintBorder(Graphics g) {
            // Override to prevent default border painting
        }
    }

    private void updateTotalProcesses() {
        int rows = (Integer) rowsSpinner.getValue();
        int cols = (Integer) colsSpinner.getValue();
        totalProcessesField.setText(String.valueOf(rows * cols));
    }

    private void runElectionOnly() {
        if (animationRunning) return;
        try {
            resetStatusOnly();
            appendLog("[" + currentTime() + "] New non-animated election run.\n");
            int rows = (Integer) rowsSpinner.getValue();
            int cols = (Integer) colsSpinner.getValue();
            int[] ids = parseIds(rows, cols);

            startTimeValue.setText(currentTime());
            currentNetwork = new TorusNetwork(rows, cols, ids);
            TorusElectionAlgorithm election = new TorusElectionAlgorithm(currentNetwork);
            election.electLeader();

            gridPanel.setNetwork(currentNetwork);
            gridPanel.clearAnimation();
            updateStatus(currentNetwork, election);
            writeLog(election);
            appendLog("\n[" + currentTime() + "] Election executed without animation.\n");
        } catch (Exception ex) {
            appendLog("[" + currentTime() + "] Input error: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runElectionWithAnimation() {
        if (animationRunning) return;
        try {
            resetStatusOnly();
            appendLog("[" + currentTime() + "] New animated election run.\n");
            int rows = (Integer) rowsSpinner.getValue();
            int cols = (Integer) colsSpinner.getValue();
            int[] ids = parseIds(rows, cols);

            startTimeValue.setText(currentTime());
            statusValue.setText("Running Animation...");
            statusValue.setForeground(RUNNING_STATUS_COLOR);

            currentNetwork = new TorusNetwork(rows, cols, ids);
            TorusElectionAlgorithm election = new TorusElectionAlgorithm(currentNetwork);
            election.electLeader();

            gridPanel.setNetwork(currentNetwork);
            animateElection(election);
        } catch (Exception ex) {
            appendLog("[" + currentTime() + "] Input error: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int[] parseIds(int rows, int cols) {
        String text = idsArea.getText().trim();
        if (text.isEmpty()) throw new IllegalArgumentException("Please enter process IDs.");
        String[] parts = text.split("\\s+");
        if (parts.length != rows * cols) {
            throw new IllegalArgumentException("Number of IDs must equal rows x columns. Expected " + (rows * cols) + " IDs.");
        }
        int[] ids = new int[parts.length];
        Set<Integer> unique = new HashSet<>();
        for (int i = 0; i < parts.length; i++) {
            ids[i] = Integer.parseInt(parts[i]);
            if (!unique.add(ids[i])) throw new IllegalArgumentException("Process IDs must be unique.");
        }
        return ids;
    }

    private void animateElection(TorusElectionAlgorithm election) {
        animationRunning = true;
        appendLog("[" + currentTime() + "] Animation started.\n");

        animationThread = new Thread(() -> {
            try {
                for (AnimationStep step : election.getAnimationSteps()) {
                    if (!animationRunning) {
                        return;
                    }
                    SwingUtilities.invokeLater(() -> {
                        if (!animationRunning) {
                            return;
                        }
                        gridPanel.setActiveStep(step);
                        appendLog("[Round " + step.getRound() + "] P" + step.getSenderId()
                                + " -> P" + step.getReceiverId()
                                + " sends max=" + step.getTransmittedValue()
                                + (step.isUpdated() ? "  UPDATED\n" : "\n"));
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                    });
                    Thread.sleep(ANIMATION_STEP_DELAY_MS);
                }

                SwingUtilities.invokeLater(() -> {
                    if (!animationRunning) {
                        return;
                    }
                    gridPanel.clearAnimation();
                    updateStatus(currentNetwork, election);
                    writeFinalLog(election);
                    appendLog("\n[" + currentTime() + "] Animation completed successfully.\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                    animationRunning = false;
                    animationThread = null;
                });
            } catch (InterruptedException ex) {
                SwingUtilities.invokeLater(() -> {
                    gridPanel.clearAnimation();
                    appendLog("[" + currentTime() + "] Animation terminated.\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                    animationRunning = false;
                    animationThread = null;
                });
                Thread.currentThread().interrupt();
            }
        });
        animationThread.setDaemon(true);
        animationThread.start();
    }

    private void updateStatus(TorusNetwork network, TorusElectionAlgorithm election) {
        statusValue.setText("Completed");
        statusValue.setForeground(COMPLETED_STATUS_COLOR);
        for (ProcessNode node : network.getAllNodes()) {
            if (node.isLeader()) {
                leaderIdValue.setText(String.valueOf(node.getId()));
                leaderIdValue.setForeground(COMPLETED_STATUS_COLOR);
                leaderPositionValue.setText(node.getPosition().toString());
                leaderPositionValue.setForeground(COMPLETED_STATUS_COLOR);
            }
        }
        roundsValue.setText(String.valueOf(election.getRounds()));
        messagesValue.setText(String.valueOf(election.getMessages()));
        endTimeValue.setText(currentTime());
    }

    private void writeLog(TorusElectionAlgorithm election) {
        for (String line : election.getExecutionLog()) {
            appendLog("[" + currentTime() + "] " + line + "\n");
        }
    }

    private void writeFinalLog(TorusElectionAlgorithm election) {
        appendLog("\nFinal summary:\n");
        for (String line : election.getExecutionLog()) {
            appendLog("- " + line + "\n");
        }
    }

    private void resetStatusOnly() {
        statusValue.setText("Not Started");
        statusValue.setForeground(Color.BLACK);
        leaderIdValue.setText("-");
        leaderIdValue.setForeground(Color.BLACK);
        leaderPositionValue.setText("-");
        leaderPositionValue.setForeground(Color.BLACK);
        roundsValue.setText("-");
        messagesValue.setText("-");
        startTimeValue.setText("-");
        endTimeValue.setText("-");
        logArea.setText("");
    }

    private void reset() {
        if (animationThread != null && animationThread.isAlive()) {
            animationRunning = false;
            animationThread.interrupt();
        }
        currentNetwork = null;
        gridPanel.setNetwork(null);
        gridPanel.clearAnimation();
        resetStatusOnly();
    }

    private void appendLog(String text) {
        logArea.append(text);
        AppLog.append(text);
    }

    private String currentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }
}
