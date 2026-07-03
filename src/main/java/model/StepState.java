package model;

public record StepState(PositionState senderPosition, PositionState receiverPosition, int senderId,
                        int receiverId, int transmittedValue, int round, boolean updated) {}
