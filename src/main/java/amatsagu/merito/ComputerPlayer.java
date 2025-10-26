package amatsagu.merito;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ComputerPlayer {
    private int lowerBound = 0;
    private int upperBound;
    private final String logicFilePath = "ComputerLogic.txt";

    public ComputerPlayer(int maxRange) {
        this.upperBound = maxRange;
        Path path = Paths.get(logicFilePath);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int makeGuess() {
        int guess = lowerBound + (upperBound - lowerBound) / 2;
        log("Thinking... My range is " + lowerBound + " to " + upperBound + ". I'll guess " + guess);
        return guess;
    }

    public void adjustRange(boolean higher, int lastGuess) {
        if (higher) {
            lowerBound = lastGuess + 1;
            log("The answer is higher. New range: " + lowerBound + " to " + upperBound);
        } else {
            upperBound = lastGuess - 1;
            log("The answer is lower. New range: " + lowerBound + " to " + upperBound);
        }
    }

    private void log(String message) {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get(logicFilePath), StandardOpenOption.APPEND))) {
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
