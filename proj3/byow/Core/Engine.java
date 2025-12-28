package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;

    private class Rectangle {
        int x;
        int y;
        int width;
        int height;

        Rectangle(Random rand) {
            // create a room
            this.x = RandomUtils.uniform(rand, 1, WIDTH - 16);
            this.y = RandomUtils.uniform(rand, 1, HEIGHT - 8);
            this.width = RandomUtils.uniform(rand, 3, 16);
            this.height = RandomUtils.uniform(rand, 3, 8);
        }

        Rectangle(int x, int y, int width, int height) {
            // create a hallWay
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        // fill world with nothing
        initializeWorld(world);
        Random rand = new Random(Integer.parseInt(input.substring(1, input.length() - 1)));
        // fill world with Rooms and HallWays
        fillRoomAndHallWay(world, rand);
        // fill world with Walls
        fillWalls(world);
        return world;
    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.ter.initialize(WIDTH, HEIGHT);
        TETile[][] world = engine.interactWithInputString("N13212342S");
        engine.ter.renderFrame(world);
    }

    private void initializeWorld(TETile[][] world) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    private void fillRoomAndHallWay(TETile[][] world, Random rand) {
        Rectangle prevRoom = new Rectangle(rand);
        fillRectangle(world, prevRoom);
         int numRooms = RandomUtils.uniform(rand, 5, 15);
        while (numRooms-- > 0) {
            Rectangle nextRoom = new Rectangle(rand);
            fillRectangle(world, nextRoom);
            fillHallWay(world, rand, prevRoom, nextRoom);
            prevRoom = nextRoom;
        }
    }

    private void fillRectangle(TETile[][] world, Rectangle rectangle) {
        for (int x = rectangle.x; x < rectangle.x + rectangle.width; x++) {
            for (int y = rectangle.y; y < rectangle.y + rectangle.height; y++) {
                world[x][y] = Tileset.FLOOR;
            }
        }
    }

    private void fillHallWay(TETile[][] world, Random rand, Rectangle roomA, Rectangle roomB) {
        int xA = RandomUtils.uniform(rand, roomA.x, roomA.x + roomA.width);
        int yA = RandomUtils.uniform(rand, roomA.y, roomA.y + roomA.height);
        int xB = RandomUtils.uniform(rand, roomB.x, roomB.x + roomB.width);
        int yB = RandomUtils.uniform(rand, roomB.y, roomB.y + roomB.height);
        if (RandomUtils.uniform(rand, 2) == 0) {
            fillHorizontalHall(world, rand, xA, xB, yA);
            fillVerticalHall(world, rand, xB, yA, yB);
        } else {
            fillHorizontalHall(world, rand, xA, xB, yB);
            fillVerticalHall(world, rand, xA, yA, yB);
        }
    }

    private void fillHorizontalHall(TETile[][] world, Random rand, int xA, int xB, int y) {
        int h = RandomUtils.uniform(rand, 1, 3);
        Rectangle hall = new Rectangle(Math.min(xA, xB), y, Math.abs(xB - xA) + 1, h);
        fillRectangle(world, hall);
    }

    private void fillVerticalHall(TETile[][] world, Random rand, int x, int yA, int yB) {
        int w = RandomUtils.uniform(rand, 1, 3);
        Rectangle hall = new Rectangle(x, Math.min(yA, yB), w, Math.abs(yB - yA) + 1);
        fillRectangle(world, hall);
    }

    private void fillWalls(TETile[][] world) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    fillWallsAroundFloor(world, x, y);
                }
            }
        }
    }

    private void fillWallsAroundFloor(TETile[][] world, int x, int y) {
        int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
        int[] dy = {1, 1, 0, -1, -1, -1, 0, 1};
        for (int d = 0; d < 8; d++) {
            int nx = x + dx[d];
            int ny = y + dy[d];
            if (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT) {
                continue;
            }
            if (world[nx][ny] == Tileset.NOTHING) {
                world[nx][ny] = Tileset.WALL;
            }
        }
    }
}
