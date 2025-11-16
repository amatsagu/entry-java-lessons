package amatsagu.merito;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import amatsagu.merito.GameMode;
import amatsagu.merito.Settings;
import amatsagu.merito.SettingsManager;

public class Initializer {
    public static void main(String[] args) {
        Initializer app = new Initializer();
        app.runGame();
    }

    private void runGame() {
        try (Scanner scan = new Scanner(System.in)) {
            boolean playAgain;
            do {
                Settings settings = getGameSettings(scan);
                runGameLoop(scan, settings);
                playAgain = promptForPlayAgain(scan);
            } while (playAgain);
        } catch (Exception e) {
            System.err.println("Caught exception error in main game loop: " + e);
        }
    }

    private boolean promptForPlayAgain(Scanner scan) {
        while (true) {
            System.out.print("Play again? (y/n): ");
            String input = scan.nextLine().trim().toLowerCase();
            if (input.equals("y")) {
                return true;
            } else if (input.equals("n")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }

    private void runGameLoop(Scanner scan, Settings settings) {
        switch (settings.gameMode) {
            case SINGLE_PLAYER:
                Player player = new Player(settings.playerNames.get(0));
                var currentBest = readBestScore(player);
                if (currentBest != 0) {
                    System.out.println("Welcome back! Your current best personal score is " + currentBest + " guesses.");
                }
                var score = handleSession(scan, player, settings);
                if (score < currentBest || currentBest == 0) {
                    System.out.println("Congratulations! You've set a new personal best of " + score + " guesses!");
                    writeGameResult(player, score);
                }
                break;
            case VS_COMPUTER:
                Player vsComputerPlayer = new Player(settings.playerNames.get(0));
                handleVsComputerSession(scan, vsComputerPlayer, settings);
                break;
            case MULTIPLAYER:
                List<Player> players = new ArrayList<>();
                for (String name : settings.playerNames) {
                    players.add(new Player(name));
                }
                handleNewMultiplayerSession(scan, players, settings);
                break;
        }
    }

    private Settings getGameSettings(Scanner scan) {
        Settings settings = SettingsManager.loadSettings();
        if (settings != null) {
            System.out.print("Load previous settings? (y/n): ");
            String input = scan.nextLine().trim().toLowerCase();
            if (input.equals("y")) {
                return settings;
            }
        }

        settings = chooseDifficulty(scan);
        settings.gameMode = chooseGameMode(scan);
        if (settings.gameMode == GameMode.SINGLE_PLAYER || settings.gameMode == GameMode.VS_COMPUTER) {
            settings.playerNames.add(promptForPlayer(scan).nickname);
        } else {
            List<Player> players = promptForPlayers(scan);
            for (Player player : players) {
                settings.playerNames.add(player.nickname);
            }
        }
        SettingsManager.saveSettings(settings);
        return settings;
    }

    private GameMode chooseGameMode(Scanner scan) {
        while (true) {
            System.out.println("Choose game mode:");
            System.out.println("1. Single Player");
            System.out.println("2. Multiplayer (vs. Computer)");
            System.out.println("3. Multiplayer (vs. Player)");
            System.out.print("Enter your choice: ");
            String input = scan.nextLine().trim();
            switch (input) {
                case "1":
                    return GameMode.SINGLE_PLAYER;
                case "2":
                    return GameMode.VS_COMPUTER;
                case "3":
                    return GameMode.MULTIPLAYER;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }



    private Settings chooseDifficulty(Scanner scan) {
        Settings settings = new Settings();
        while (true) {
            System.out.println("Choose difficulty level:");
            System.out.println("1. Easy (0-100)");
            System.out.println("2. Normal (0-10000)");
            System.out.println("3. Hard (0-1000000)");
            System.out.println("4. Advanced (custom range)");
            System.out.print("Enter your choice: ");
            String input = scan.nextLine().trim();
            switch (input) {
                case "1":
                    settings.minRange = 0;
                    settings.maxRange = 100;
                    return settings;
                case "2":
                    settings.minRange = 0;
                    settings.maxRange = 10000;
                    return settings;
                case "3":
                    settings.minRange = 0;
                    settings.maxRange = 1000000;
                    return settings;
                case "4":
                    int min = -1, max = -1;
                    while (min < 0) {
                        System.out.print("Enter minimum value (>= 0): ");
                        try {
                            min = Integer.parseInt(scan.nextLine().trim());
                            if (min < 0) System.out.println("Minimum must be non-negative.");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number.");
                        }
                    }
                    while (max <= min) {
                        System.out.print("Enter maximum value (> " + min + "): ");
                        try {
                            max = Integer.parseInt(scan.nextLine().trim());
                            if (max <= min) System.out.println("Maximum must be greater than minimum.");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number.");
                        }
                    }
                    settings.minRange = min;
                    settings.maxRange = max;
                    return settings;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, 3, or 4.");
            }
        }
    }

    private void handleVsComputerSession(Scanner scan, Player player, Settings settings) {
        System.out.println("You are playing against the Computer. You will take turns guessing.");
        int computerGuesses = 0;
        boolean playerTurn = true;

        Game game = new Game(settings.minRange, settings.maxRange);
        ComputerPlayer computer = new ComputerPlayer(settings.maxRange);

        while (!game.finished) {
            if (playerTurn) {
                System.out.print("Your turn. Guess an integer value from " + settings.minRange + " to " + settings.maxRange + ": ");
                Integer playerValue;
                try {
                    playerValue = Integer.valueOf(scan.nextLine().trim(), 10);
                } catch (Exception e) {
                    System.out.println("Provided value is not a correct integer, try again.");
                    continue;
                }
                player.guesses++;
                if (game.guess(playerValue)) {
                    System.out.println("Congratulations! You've won in " + player.guesses + " guesses.");
                    writeGameResult(player, player.guesses);
                } else {
                    System.out.print("You've guessed incorrectly. ");
                    if (playerValue > game.answer) {
                        System.out.print("Try guessing lower value.\n");
                    } else {
                        System.out.print("Try guessing higher value.\n");
                    }
                }
            } else {
                int computerGuess = computer.makeGuess();
                System.out.println("Computer's turn. Computer guesses: " + computerGuess);
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
                        computer.adjustRange(true, computerGuess);
                    }
                }
            }
            playerTurn = !playerTurn;
        }
    }

    private void handleNewMultiplayerSession(Scanner scan, List<Player> players, Settings settings) {
        Game game = new Game(settings.minRange, settings.maxRange);
        int currentPlayerIndex = 0;

        while (!game.finished) {
            Player currentPlayer = players.get(currentPlayerIndex);
            System.out.print(currentPlayer.nickname + "'s turn. Guess an integer value from " + settings.minRange + " to " + settings.maxRange + ": ");
            Integer playerValue;

            try {
                playerValue = Integer.valueOf(scan.nextLine().trim(), 10);
            } catch (Exception e) {
                System.out.println("Provided value is not a correct integer, try again.");
                continue;
            }

            currentPlayer.guesses++;
            if (game.guess(playerValue)) {
                System.out.println("Congratulations " + currentPlayer.nickname + "! You've won in " + currentPlayer.guesses + " guesses.");
                writeGameResult(currentPlayer, currentPlayer.guesses);
            } else {
                System.out.print("You've guessed incorrectly. ");
                if (playerValue > game.answer) {
                    System.out.print("Try guessing lower value.\n");
                } else {
                    System.out.print("Try guessing higher value.\n");
                }
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        }
    }

    private List<Player> promptForPlayers(Scanner scan) {
        int numPlayers = 0;
        while (numPlayers <= 1) {
            System.out.print("Enter the number of players (> 1): ");
            try {
                numPlayers = Integer.parseInt(scan.nextLine().trim());
                if (numPlayers <= 1) {
                    System.out.println("Multiplayer requires at least 2 players.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            players.add(promptForPlayer(scan, i + 1));
        }
        return players;
    }

    private Player promptForPlayer(Scanner scan, int... playerNumber) {
        String prompt = playerNumber.length > 0 ? "What is your player name for player " + playerNumber[0] + ": " : "What is your player name: ";
        while (true) {
            System.out.print(prompt);
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

    private Integer handleSession(Scanner scan, Player player, Settings settings) {
        var game = new Game(settings.minRange, settings.maxRange);
        while (!game.finished) {
            System.err.print("Guess an integer value from " + settings.minRange + " to " + settings.maxRange + ": ");
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
