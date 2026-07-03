package model;

import java.util.List;

public record ElectionResult(String status, int rows, int cols, int leaderId, PositionState leaderPosition,
                             int rounds, int messages, String startTime, String endTime,
                             List<NodeState> nodes, List<StepState> steps, List<String> log) {}
