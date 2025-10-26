package amatsagu.merito;

// import java.util.ArrayList;
import java.util.Random;

public class Game {
    public Integer answer, guesses = 0;
    public Boolean finished = false;
    // private ArrayList<player> players = new ArrayList<player>();
    
    public Game(int maxRange)
    {
        var random = new Random(System.nanoTime());
        this.answer = random.nextInt(maxRange + 1);
    }

    public Boolean guess(Integer guess) {
        if (this.finished) {
            return true;
        }

        var correct = guess == this.answer;

        if (!correct) {
            this.guesses++;
        } else {
            this.finished = true;
        }

        return correct;
    }
}
