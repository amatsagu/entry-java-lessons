package amatsagu.merito;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class Stats {
    public int singleEasyBest = 0;
    public int singleNormalBest = 0;
    public int singleHardBest = 0;
    public int multiWins = 0;
    public int multiLosses = 0;
    private final String nickname;

    public Stats(String nickname) {
        this.nickname = nickname;
        load();
    }

    private void load() {
        Path path = Paths.get("./" + nickname + ".txt");
        if (Files.exists(path)) {
            try {
                // Check if it's the old format (just an integer)
                String content = Files.readString(path).trim();
                if (!content.isEmpty() && !content.contains("=")) {
                    try {
                        // Assume old format was for Easy difficulty
                        singleEasyBest = Integer.parseInt(content);
                        return; // Done loading legacy
                    } catch (NumberFormatException ignored) {}
                }

                try (InputStream in = Files.newInputStream(path)) {
                    Properties props = new Properties();
                    props.load(in);
                    singleEasyBest = Integer.parseInt(props.getProperty("SINGLE_EASY", "0"));
                    singleNormalBest = Integer.parseInt(props.getProperty("SINGLE_NORMAL", "0"));
                    singleHardBest = Integer.parseInt(props.getProperty("SINGLE_HARD", "0"));
                    multiWins = Integer.parseInt(props.getProperty("MULTI_WINS", "0"));
                    multiLosses = Integer.parseInt(props.getProperty("MULTI_LOSSES", "0"));
                }
            } catch (IOException e) {
                System.err.println("Error loading stats for " + nickname + ": " + e.getMessage());
            }
        }
    }

    public void save() {
        Properties props = new Properties();
        props.setProperty("SINGLE_EASY", String.valueOf(singleEasyBest));
        props.setProperty("SINGLE_NORMAL", String.valueOf(singleNormalBest));
        props.setProperty("SINGLE_HARD", String.valueOf(singleHardBest));
        props.setProperty("MULTI_WINS", String.valueOf(multiWins));
        props.setProperty("MULTI_LOSSES", String.valueOf(multiLosses));

        try (OutputStream out = Files.newOutputStream(Paths.get("./" + nickname + ".txt"))) {
            props.store(out, "Player Stats");
        } catch (IOException e) {
            System.err.println("Error saving stats for " + nickname + ": " + e.getMessage());
        }
    }
}
