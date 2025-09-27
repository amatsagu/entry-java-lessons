package amatsagu.merito;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Initializer {
    public static void main(String[] args) {
        Initializer app = new Initializer();
        app.runGame();
    }

    private void runGame() {
        try (Scanner scan = new Scanner(System.in)) {
            var player = promptForPlayer(scan);
            var currentBest = readBestScore(player); // lowest number of guesses

            if (currentBest != 0) {
                System.out.println("Welcome back! Your current best personal score from last game(s) is " + currentBest + " guess(es).");
            } else {
                System.out.println("Welcome! There's no previous record of your game(s) - starting as new challenger!");
            }

            var score = handleSession(scan, player);
            if (score < currentBest) {
                System.out.println("Congratulations! You've established new personal best guess score of " + score + " guess(es)!");
                writeGameResult(player, score);
            }
        } catch (Exception e) {
            System.err.println("caught exception error in main game loop: " + e);
        }
    }

    private Player promptForPlayer(Scanner scan) {
        while (true) {
            System.out.print("What is your player name: ");
            var name = scan.nextLine().trim();

            if (name.isBlank()) { 
                System.out.println("Provided name is incorrect, try again.");
            } else {
                return new Player(name);
            }
        }
    }

    private Integer readBestScore(Player player) {
        var pathname = Paths.get("./" + player.nickname + ".txt");

        if (Files.exists(pathname)) {
            try {
                String scoreText = Files.readString(pathname).trim();
                return Integer.valueOf(scoreText);
            } catch (IOException | NumberFormatException e) {
                System.err.println("detected corrupted best score file (starting fresh): " + e.getMessage());
            }
        }
    
        return 0;
    }

    // Returns in how many guesses player end up winning.
    private Integer handleSession(Scanner scan, Player player) {
        var game = new Game();
        while (!game.finished) {
            System.err.print("Guess an integer value from 0 to 100: ");
            Integer playerValue;

            try {
                playerValue = Integer.valueOf(scan.nextLine(), 10);
            } catch (Exception e) {
                System.out.println("Provided value is not a correct integer, try again.");
                continue;
            }

            if (!game.guess(playerValue)) {
                System.out.print("You've guessed incorrectly. ");

                if (playerValue > game.answer) {
                    System.out.print("Try guessing lower value.\n");
                } else {
                    System.out.print("Try guessing higher value.\n");
                }
            }
        }

        System.out.println("Congratulations! You've won.");
        return game.guesses;
    }

    private void writeGameResult(Player player, Integer attempts) {
        Path pathname = Paths.get("./" + player.nickname + ".txt");
        File file = new File(pathname.toString());

        if (!file.exists()) {
            try 
            {
                Files.createFile(pathname);
            }
            catch (FileAlreadyExistsException e) 
            {
                System.err.println("file at path = " + pathname + " already exists");
            } 
            catch (IOException e) 
            {
                System.err.println("failed to create file for storing player game result: " + e);
            }
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(attempts);
        } catch (IOException e) {
            System.err.println("received error when tried to write player game result to file: " + e);
        }
    }
}
