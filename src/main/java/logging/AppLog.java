package logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class AppLog {
    private static final String APP_DIRECTORY = "torus-election-gui";
    private static final String LOG_FILE_NAME = "torus-election-gui.log";
    private static Path logDirectory;
    private static Path logFile;
    private static boolean logDirectoryReady;

    private AppLog() {
    }

    public static synchronized void append(String text) {
        append(getLogDirectory(), text, true);
    }

    static synchronized void append(Path logDirectory, String text) {
        append(logDirectory, text, false);
    }

    private static void append(Path logDirectory, String text, boolean useReadyFlag) {
        try {
            if (!useReadyFlag || !logDirectoryReady) {
                Files.createDirectories(logDirectory);
                if (useReadyFlag) {
                    logDirectoryReady = true;
                }
            }
            Files.writeString(
                    logDirectory.resolve(LOG_FILE_NAME),
                    text,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ex) {
            if (useReadyFlag) {
                logDirectoryReady = false;
            }
            System.err.println("Failed to write application log: " + ex.getMessage());
        }
    }

    public static synchronized Path getLogDirectory() {
        if (logDirectory == null) {
            logDirectory = resolveLogDirectory();
            logFile = logDirectory.resolve(LOG_FILE_NAME);
        }
        return logDirectory;
    }

    public static synchronized Path getLogFile() {
        if (logFile == null) {
            getLogDirectory();
        }
        return logFile;
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
