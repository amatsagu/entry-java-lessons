package amatsagu.merito;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SettingsManager {
    private static final Path SETTINGS_FILE = Paths.get("settings.properties");

    public static void saveSettings(Settings settings) {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(SETTINGS_FILE))) {
            writer.println("gameMode=" + settings.gameMode.name());
            writer.println("minRange=" + settings.minRange);
            writer.println("maxRange=" + settings.maxRange);
            for (int i = 0; i < settings.playerNames.size(); i++) {
                writer.println("player" + (i + 1) + "=" + settings.playerNames.get(i));
            }
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    public static Settings loadSettings() {
        if (!Files.exists(SETTINGS_FILE)) {
            return null;
        }
        Settings settings = new Settings();
        try {
            List<String> lines = Files.readAllLines(SETTINGS_FILE);
            for (String line : lines) {
                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                String key = parts[0];
                String value = parts[1];
                if (key.equals("gameMode")) {
                    settings.gameMode = GameMode.valueOf(value);
                } else if (key.equals("minRange")) {
                    settings.minRange = Integer.parseInt(value);
                } else if (key.equals("maxRange")) {
                    settings.maxRange = Integer.parseInt(value);
                } else if (key.startsWith("player")) {
                    settings.playerNames.add(value);
                }
            }
            return settings;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            return null;
        }
    }
}
