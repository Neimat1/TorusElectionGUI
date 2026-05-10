package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PositionTest {
    @Test
    void storesCoordinatesAndFormatsThem() {
        Position position = new Position(2, 3);

        assertEquals(2, position.getRow());
        assertEquals(3, position.getCol());
        assertEquals("(2, 3)", position.toString());
    }

    @Test
    void comparesByCoordinateValues() {
        Position same = new Position(1, 4);

        assertEquals(same, new Position(1, 4));
        assertEquals(same.hashCode(), new Position(1, 4).hashCode());
        assertNotEquals(same, new Position(4, 1));
    }
}
