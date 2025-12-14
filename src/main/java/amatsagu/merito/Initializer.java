package amatsagu.merito;

import java.util.Random;
import java.util.Scanner;

public class Initializer {
    public static void main(String[] args) {
        Initializer app = new Initializer();
        app.runGame();
    }

    private void runGame() {
        try (Scanner scan = new Scanner(System.in)) {
            var player = promptForPlayer(scan);
            var stats = new Stats(player.nickname);
            var computerStats = new Stats("Computer");

            System.out.println("Welcome " + player.nickname + "!");
            if (stats.singleEasyBest > 0) System.out.println("Best Easy: " + stats.singleEasyBest);
            if (stats.singleNormalBest > 0) System.out.println("Best Normal: " + stats.singleNormalBest);
            if (stats.singleHardBest > 0) System.out.println("Best Hard: " + stats.singleHardBest);
            System.out.println("Multiplayer Wins: " + stats.multiWins + " | Losses: " + stats.multiLosses);

            int difficulty = chooseDifficulty(scan);
            int gameMode = chooseGameMode(scan);

            switch (gameMode) {
                case 1:
                    handleSinglePlayer(scan, stats, difficulty);
                    break;
                case 2:
                    handleMultiplayerSession(scan, stats, computerStats, difficulty);
                    break;
                case 3:
                    handleReverseSession(scan, difficulty);
                    break;
            }

            stats.save();
            computerStats.save();

        } catch (Exception e) {
            System.err.println("caught exception error in main game loop: " + e);
            e.printStackTrace();
        }
    }

    private int chooseGameMode(Scanner scan) {
        while (true) {
            System.out.println("Choose game mode:");
            System.out.println("1. Single Player (You guess)");
            System.out.println("2. Multiplayer (You vs Computer)");
            System.out.println("3. Reverse (Computer guesses)");
            System.out.print("Enter your choice: ");
            String input = scan.nextLine().trim();
            if (input.equals("1") || input.equals("2") || input.equals("3")) {
                return Integer.parseInt(input);
            } else {
                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
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
                case "1": return 100;
                case "2": return 10000;
                case "3": return 1000000;
                default: System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
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

    private void handleSinglePlayer(Scanner scan, Stats stats, int difficulty) {
        var game = new Game(difficulty);
        while (!game.finished) {
            System.out.print("Guess an integer value from 0 to " + difficulty + ": ");
            Integer playerValue = getIntInput(scan);
            if (playerValue == null) continue;

            if (!game.guess(playerValue)) {
                System.out.print("Incorrect. ");
                if (playerValue > game.answer) System.out.println("Try lower.");
                else System.out.println("Try higher.");
            }
        }
        System.out.println("Congratulations! You've won in " + game.guesses + " guesses.");
        updateSinglePlayerStats(stats, difficulty, game.guesses);
    }

    private void updateSinglePlayerStats(Stats stats, int difficulty, int score) {
        if (difficulty == 100) {
            if (stats.singleEasyBest == 0 || score < stats.singleEasyBest) stats.singleEasyBest = score;
        } else if (difficulty == 10000) {
            if (stats.singleNormalBest == 0 || score < stats.singleNormalBest) stats.singleNormalBest = score;
        } else if (difficulty == 1000000) {
            if (stats.singleHardBest == 0 || score < stats.singleHardBest) stats.singleHardBest = score;
        }
    }

    private void handleMultiplayerSession(Scanner scan, Stats playerStats, Stats computerStats, int difficulty) {
        System.out.println("Multiplayer: You vs Computer.");
        
        boolean playerTurn = new Random().nextBoolean();
        System.out.println("Coin toss result: " + (playerTurn ? "You start!" : "Computer starts!"));

        int playerGuesses = 0;
        int computerGuesses = 0;

        Game game = new Game(difficulty);
        ComputerPlayer computer = new ComputerPlayer(difficulty);

        while (!game.finished) {
            if (playerTurn) {
                System.out.print("Your turn. Guess (0-" + difficulty + "): ");
                Integer playerValue = getIntInput(scan);
                if (playerValue == null) continue;

                playerGuesses++;
                if (game.guess(playerValue)) {
                    System.out.println("You won in " + playerGuesses + " guesses!");
                    playerStats.multiWins++;
                    computerStats.multiLosses++;
                    return;
                } else {
                    System.out.print("Incorrect. ");
                    if (playerValue > game.answer) System.out.println("Lower.");
                    else System.out.println("Higher.");
                }
            } else {
                System.out.println("Computer's turn.");
                int computerGuess = computer.makeGuess();
                System.out.println("Computer guesses: " + computerGuess);
                computerGuesses++;

                if (game.guess(computerGuess)) {
                    System.out.println("Computer won in " + computerGuesses + " guesses.");
                    computerStats.multiWins++;
                    playerStats.multiLosses++;
                    return;
                } else {
                    System.out.print("Computer incorrect. ");
                    if (computerGuess > game.answer) {
                        System.out.println("Answer is lower.");
                        computer.adjustRange(false, computerGuess);
                    } else {
                        System.out.println("Answer is higher.");
                        computer.adjustRange(true, computerGuess);
                    }
                }
            }
            playerTurn = !playerTurn;
        }
    }

    private void handleReverseSession(Scanner scan, int difficulty) {
        System.out.println("Reverse Mode: Enter a number for the computer to guess (0-" + difficulty + ").");
        Integer target = null;
        while (target == null || target < 0 || target > difficulty) {
            System.out.print("Enter secret number: ");
            target = getIntInput(scan);
        }

        Game game = new Game(difficulty);
        game.answer = target;
        game.guesses = 0;

        ComputerPlayer computer = new ComputerPlayer(difficulty);
        int attempts = 0;

        while (!game.finished) {
            attempts++;
            int guess = computer.makeGuess();
            System.out.println("Computer guesses: " + guess);

            if (game.guess(guess)) {
                System.out.println("Computer found the number " + target + " in " + attempts + " guesses!");
            } else {
                if (guess > target) {
                    System.out.println("Too high.");
                    computer.adjustRange(false, guess);
                } else {
                    System.out.println("Too low.");
                    computer.adjustRange(true, guess);
                }
            }
        }
    }

    private Integer getIntInput(Scanner scan) {
        try {
            String line = scan.nextLine().trim();
            if (line.isEmpty()) return null;
            return Integer.valueOf(line);
        } catch (Exception e) {
            System.out.println("Invalid input. Enter an integer.");
            return null;
        }
    }
}