package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /**
     * The width of the window of this game.
     */
    private int width;
    /**
     * The height of the window of this game.
     */
    private int height;
    /**
     * The current round the user is on.
     */
    private int round;
    /**
     * The Random object used to randomly generate Strings.
     */
    private Random rand;
    /**
     * Whether or not the game is over.
     */
    private boolean gameOver;
    /**
     * Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'.
     */
    private boolean playerTurn;
    /**
     * The characters we generate random Strings from.
     */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /**
     * Encouraging phrases. Used in the last section of the spec, 'Helpful UI'.
     */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
            "You got this!", "You're a star!", "Go Bears!",
            "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        this.rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int r = RandomUtils.uniform(this.rand, 0, CHARACTERS.length);
            result.append(CHARACTERS[r]);
        }
        return result.toString();
    }

    public void drawFrame(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(this.width / 2, this.height / 2, s);
        if (!gameOver) {
            StdDraw.textLeft(0, this.height - 1, "Round: " + this.round);
            String state = this.playerTurn ? "Type!" : "Watch!";
            StdDraw.text(this.width / 2, this.height - 1, state);
            int r = RandomUtils.uniform(this.rand, 0, ENCOURAGEMENT.length);
            StdDraw.textRight(this.width, this.height - 1, ENCOURAGEMENT[r]);
            StdDraw.line(0, this.height - 2, this.width, this.height - 2);
        }
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        for (int i = 0; i < letters.length(); i++) {
            drawFrame(String.valueOf(letters.charAt(i)));
            StdDraw.pause(1000);
            drawFrame("");
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        StringBuilder result = new StringBuilder();
        drawFrame(result.toString());
        while (result.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                result.append(c);
                drawFrame(result.toString());
            }
        }
        StdDraw.pause(500);
        return result.toString();
    }

    public void startGame() {
        this.gameOver = false;
        this.playerTurn = false;
        this.round = 1;
        while (!this.gameOver) {
            drawFrame("Round: " + this.round);
            StdDraw.pause(500);
            String chars = generateRandomString(this.round);
            flashSequence(chars);
            this.playerTurn = true;
            String userInput = solicitNCharsInput(this.round);
            this.playerTurn = false;
            if (chars.equalsIgnoreCase(userInput)) {
                this.round++;
            } else {
                this.gameOver = true;
                drawFrame("Game Over! You made it to round: " + this.round);
            }
        }
    }

}
