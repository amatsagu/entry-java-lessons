package amatsagu.merito;

import java.util.Random;

public class Game {
    public Integer answer, guesses = 0;
    public Boolean finished = false;
    
    public Game(int minRange, int maxRange)
    {
        var random = new Random(System.nanoTime());
        this.answer = random.nextInt(maxRange - minRange + 1) + minRange;
    }

    public Boolean guess(Integer guess) {
        if (this.finished) {
            return true;
        }

        var correct = guess.equals(this.answer);

        if (!correct) {
            this.guesses++;
        } else {
            this.finished = true;
        }

        return correct;
    }
}
