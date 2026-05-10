package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnimationStep {
    private final Position senderPosition;
    private final Position receiverPosition;
    private final int senderId;
    private final int receiverId;
    private final int transmittedValue;
    private final int round;
    private final boolean updated;
}
