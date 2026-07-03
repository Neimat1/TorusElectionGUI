package model;

public record NodeState(int id, int row, int col, int maxKnownId, boolean leader) {}
