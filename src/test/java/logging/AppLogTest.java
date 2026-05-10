package logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppLogTest {
    @TempDir
    Path userHome;

    @Test
    void appendsTextToResolvedLogFile() throws Exception {
        System.setProperty("user.home", userHome.toString());

        AppLog.append("first\n");
        AppLog.append("second\n");

        assertTrue(Files.exists(AppLog.getLogDirectory()));
        assertTrue(Files.exists(AppLog.getLogFile()));
        assertEquals("first\nsecond\n", Files.readString(AppLog.getLogFile()));
    }
}
