package logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class AppLog {
    private static final String APP_DIRECTORY = "torus-election-gui";
    private static final Path LOG_DIRECTORY = resolveLogDirectory();
    private static final Path LOG_FILE = LOG_DIRECTORY.resolve("torus-election-gui.log");

    private AppLog() {
    }

    public static synchronized void append(String text) {
        try {
            Files.createDirectories(LOG_DIRECTORY);
            Files.writeString(
                    LOG_FILE,
                    text,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ex) {
            System.err.println("Failed to write application log: " + ex.getMessage());
        }
    }

    public static Path getLogDirectory() {
        return LOG_DIRECTORY;
    }

    public static Path getLogFile() {
        return LOG_FILE;
    }

    private static Path resolveLogDirectory() {
        if (isWindows()) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isBlank()) {
                return Paths.get(localAppData, APP_DIRECTORY, "logs");
            }
            return Paths.get(System.getProperty("user.home"), "AppData", "Local", APP_DIRECTORY, "logs");
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return Paths.get(xdgDataHome, APP_DIRECTORY, "logs");
        }
        return Paths.get(System.getProperty("user.home"), ".local", "share", APP_DIRECTORY, "logs");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
