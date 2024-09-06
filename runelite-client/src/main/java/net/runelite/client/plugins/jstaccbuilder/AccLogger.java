package net.runelite.client.plugins.jstaccbuilder;

import lombok.extern.slf4j.Slf4j;
import net.unethicalite.api.game.Game;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class AccLogger {

    private static final String DIRECTORY_PATH = System.getProperty("user.home") + File.separator + ".openosrs" + File.separator + "furyshark";
    private final String scriptName;

    /**
     * Constructor for AccountLogger.
     *
     * @param scriptName the name of the script for which the events are being logged.
     */
    public AccLogger(String scriptName) {
        this.scriptName = scriptName;
    }

    /**
     * Writes an event to the specified file.
     *
     * @param fileName        the name of the file to write to.
     * @param optionalMessage an optional message to include in the log.
     * @return true if the write was successful, false otherwise.
     */
    public boolean writeToFile(String fileName, String optionalMessage) {
        File file = createFile(fileName);
        if (file == null) {
            return false;
        }

        try (FileWriter writer = new FileWriter(file, true)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
            String time = LocalDateTime.now().format(formatter);
            String username = Game.getGameAccount().getUsername();
            String password = Game.getGameAccount().getPassword();
            String line = String.format("[%s] %s:%s finished %s. %s%n", time, username, password, scriptName, optionalMessage);
            writer.write(line);
            return true;
        } catch (IOException e) {
            log.error("Failed to write to file: " + file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Writes an event to the specified file without an optional message.
     *
     * @param fileName the name of the file to write to.
     * @return true if the write was successful, false otherwise.
     */
    public boolean writeToFile(String fileName) {
        return writeToFile(fileName, "");
    }

    /**
     * Creates a file with the specified name in the predefined directory.
     *
     * @param fileName the name of the file to create.
     * @return the created File object, or null if the file could not be created.
     */
    private File createFile(String fileName) {
        Path directoryPath = Paths.get(DIRECTORY_PATH);
        File directory = directoryPath.toFile();
        if (!directory.exists() && !directory.mkdirs()) {
            log.error("Failed to create directory: " + DIRECTORY_PATH);
            return null;
        }

        Path filePath = Paths.get(DIRECTORY_PATH, fileName.endsWith(".txt") ? fileName : fileName + ".txt");
        File file = filePath.toFile();
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    log.error("Failed to create file: " + file.getAbsolutePath());
                    return null;
                }
            } catch (IOException e) {
                log.error("Failed to create file: " + file.getAbsolutePath(), e);
                return null;
            }
        }
        return file;
    }

}
