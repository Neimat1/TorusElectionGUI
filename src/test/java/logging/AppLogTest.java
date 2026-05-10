package logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppLogTest {
    @TempDir
    Path tempDir;

    @Test
    void appendsTextToProvidedLogFile() throws Exception {
        Path logDirectory = tempDir.resolve("logs");
        Path logFile = logDirectory.resolve("torus-election-gui.log");

        AppLog.append(logDirectory, "first\n");
        AppLog.append(logDirectory, "second\n");

        assertTrue(Files.exists(logDirectory));
        assertTrue(Files.exists(logFile));
        assertEquals("first\nsecond\n", Files.readString(logFile));
    }
}
