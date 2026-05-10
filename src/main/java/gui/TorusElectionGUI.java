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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class TorusElectionGUI extends JFrame {
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
        sideBar.setBackground(new Color(15, 35, 65));
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
        main.setBackground(new Color(245, 248, 252));

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
        JPanel panel = card("1. Network Configuration");
        panel.setLayout(new GridLayout(4, 2, 8, 8));
        totalProcessesField.setEditable(false);
        panel.add(new JLabel("Rows:"));
        panel.add(rowsSpinner);
        panel.add(new JLabel("Columns:"));
        panel.add(colsSpinner);
        panel.add(new JLabel("Total Processes:"));
        panel.add(totalProcessesField);
        return panel;
    }

    private JPanel createIdsPanel() {
        JPanel panel = card("2. Enter Process IDs");
        panel.setLayout(new BorderLayout(8, 8));
        idsArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        panel.add(new JScrollPane(idsArea), BorderLayout.CENTER);
        JLabel hint = new JLabel("<html>Enter IDs row-wise, separated by spaces or new lines.</html>");
        hint.setForeground(Color.GRAY);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createVisualizationPanel() {
        JPanel panel = card("3. Torus Network Visualization");
        panel.setLayout(new BorderLayout());
        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = card("4. Election Status");
        panel.setLayout(new GridLayout(7, 2, 8, 8));
        panel.add(new JLabel("Status:")); panel.add(statusValue);
        panel.add(new JLabel("Leader ID:")); panel.add(leaderIdValue);
        panel.add(new JLabel("Leader Position:")); panel.add(leaderPositionValue);
        panel.add(new JLabel("Rounds:")); panel.add(roundsValue);
        panel.add(new JLabel("Messages Exchanged:")); panel.add(messagesValue);
        panel.add(new JLabel("Start Time:")); panel.add(startTimeValue);
        panel.add(new JLabel("End Time:")); panel.add(endTimeValue);
        return panel;
    }

    private JPanel createLegendPanel() {
        JPanel panel = card("5. Legend");
        panel.setLayout(new GridLayout(4, 2, 8, 8));
        panel.add(colorBox(new Color(230, 245, 255))); panel.add(new JLabel("Process"));
        panel.add(colorBox(new Color(255, 220, 120))); panel.add(new JLabel("Sender"));
        panel.add(colorBox(new Color(255, 160, 160))); panel.add(new JLabel("Receiver"));
        panel.add(colorBox(new Color(170, 235, 170))); panel.add(new JLabel("Leader"));
        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = card("6. Controls");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton runButton = new JButton("Run Election");
        JButton animateButton = new JButton("Auto Animate");
        JButton resetButton = new JButton("Reset");

        styleButton(runButton);
        styleButton(animateButton);
        styleButton(resetButton);

        runButton.addActionListener(e -> runElectionOnly());
        animateButton.addActionListener(e -> runElectionWithAnimation());
        resetButton.addActionListener(e -> reset());

        panel.add(Box.createVerticalStrut(18));
        panel.add(runButton);
        panel.add(Box.createVerticalStrut(30));
        panel.add(animateButton);
        panel.add(Box.createVerticalStrut(30));
        panel.add(resetButton);
        return panel;
    }

    private void styleButton(JButton button) {
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(230, 44));
        button.setPreferredSize(new Dimension(230, 44));
        button.setFont(new Font("Arial", Font.BOLD, 13));
    }

    private JPanel createLogPanel() {
        JPanel panel = card("7. Execution Log");
        panel.setPreferredSize(new Dimension(1000, 170));
        panel.setLayout(new BorderLayout());
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel card(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 225, 235)),
                BorderFactory.createTitledBorder(title)
        ));
        return panel;
    }

    private JLabel colorBox(Color color) {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBackground(color);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return label;
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
            statusValue.setForeground(new Color(220, 120, 0));

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
                    Thread.sleep(250);
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
        statusValue.setForeground(new Color(0, 130, 0));
        for (ProcessNode node : network.getAllNodes()) {
            if (node.isLeader()) {
                leaderIdValue.setText(String.valueOf(node.getId()));
                leaderIdValue.setForeground(new Color(0, 130, 0));
                leaderPositionValue.setText(node.getPosition().toString());
                leaderPositionValue.setForeground(new Color(0, 130, 0));
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
        return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }
}
