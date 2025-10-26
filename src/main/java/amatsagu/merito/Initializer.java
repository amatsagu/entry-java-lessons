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

            int difficulty = chooseDifficulty(scan);
            int gameMode = chooseGameMode(scan);

            if (gameMode == 1) {
                var score = handleSession(scan, player, difficulty);
                if (score < currentBest || currentBest == 0) {
                    System.out.println("Congratulations! You've established new personal best guess score of " + score + " guess(es)!");
                    writeGameResult(player, score);
                }
            } else {
                handleMultiplayerSession(scan, player, difficulty);
            }
        } catch (Exception e) {
            System.err.println("caught exception error in main game loop: " + e);
        }
    }

    private int chooseGameMode(Scanner scan) {
        while (true) {
            System.out.println("Choose game mode:");
            System.out.println("1. Single Player");
            System.out.println("2. Multiplayer (vs. Computer)");
            System.out.print("Enter your choice: ");
            String input = scan.nextLine().trim();
            if (input.equals("1") || input.equals("2")) {
                return Integer.parseInt(input);
            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    private int chooseDifficulty(Scanner scan) {
        while (true) {
            System.out.println("Choose difficulty level:");
            System.out.println("1. Easy (0-100)");
            System.out.println("2. Normal (0-10000)");
            System.out.println("3. Hard (0-1000000)");
            System.out.print("Enter your choice: ");
            String input = scan.nextLine().trim();
            switch (input) {
                case "1":
                    return 100;
                case "2":
                    return 10000;
                case "3":
                    return 1000000;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private void handleMultiplayerSession(Scanner scan, Player player, int difficulty) {
        System.out.println("You are playing against the Computer. You will take turns guessing.");
        int playerGuesses = 0;
        int computerGuesses = 0;
        boolean playerTurn = true;

        Game game = new Game(difficulty);
        ComputerPlayer computer = new ComputerPlayer(difficulty);

        while (!game.finished) {
            if (playerTurn) {
                System.out.print("Your turn. Guess an integer value from 0 to " + difficulty + ": ");
                Integer playerValue;
                try {
                    playerValue = Integer.valueOf(scan.nextLine().trim(), 10);
                } catch (Exception e) {
                    System.out.println("Provided value is not a correct integer, try again.");
                    continue;
                }
                playerGuesses++;
                if (game.guess(playerValue)) {
                    System.out.println("Congratulations! You've won in " + playerGuesses + " guesses.");
                    writeGameResult(player, playerGuesses);
                } else {
                    System.out.print("You've guessed incorrectly. ");
                    if (playerValue > game.answer) {
                        System.out.print("Try guessing lower value.\n");
                    } else {
                        System.out.print("Try guessing higher value.\n");
                    }
                }
            } else {
                System.out.println("Computer's turn.");
                int computerGuess = computer.makeGuess();
                System.out.println("Computer guesses: " + computerGuess);
                computerGuesses++;
                if (game.guess(computerGuess)) {
                    System.out.println("Computer won in " + computerGuesses + " guesses.");
                    writeGameResult(new Player("Computer"), computerGuesses);
                } else {
                    System.out.print("Computer guessed incorrectly. ");
                    if (computerGuess > game.answer) {
                        System.out.print("Answer is lower.\n");
                        computer.adjustRange(false, computerGuess);
                    } else {
                        System.out.print("Answer is higher.\n");
                        computer.adjustRange(true, computerGuess);
                    }
                }
            }
            playerTurn = !playerTurn;
        }
    }

    private Player promptForPlayer(Scanner scan) {
        while (true) {
            System.out.print("What is your player name: ");
            var name = scan.nextLine().trim();

            if (name.isBlank() || name.equalsIgnoreCase("Computer")) {
                System.out.println("Provided name is incorrect or reserved, try again.");
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

    private Integer handleSession(Scanner scan, Player player, int difficulty) {
        var game = new Game(difficulty);
        while (!game.finished) {
            System.err.print("Guess an integer value from 0 to " + difficulty + ": ");
            Integer playerValue;

            try {
                playerValue = Integer.valueOf(scan.nextLine().trim(), 10);
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
