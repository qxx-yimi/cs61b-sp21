package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 40;
    private static final int HEIGHT = 40;

    private static final long SEED = 28731223;
    private static final Random RANDOM = new Random(SEED);

    /**
     * Picks a RANDOM tile.
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0:
                return Tileset.GRASS;
            case 1:
                return Tileset.FLOWER;
            case 2:
                return Tileset.SAND;
            case 3:
                return Tileset.TREE;
            case 4:
                return Tileset.MOUNTAIN;
            default:
                return Tileset.NOTHING;
        }
    }

    /**
     * Fills the given 2D array of tiles with a single hexagon.
     */
    private static void fillSingleHexagon(TETile[][] tiles, int xPos, int yPos, int size) {
        TETile tile = randomTile();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size + 2 * i; j++) {
                tiles[xPos - i + j][yPos + i] = TETile.colorVariant(tile,
                        10, 10, 10, RANDOM);
                tiles[xPos - i + j][yPos + 2 * size - 1 - i] = TETile.colorVariant(tile,
                        10, 10, 10, RANDOM);
            }
        }
    }

    /**
     * Fills the given 2D array of tiles with a hexagonal tessellation.
     */
    private static void fillHexagonWorld(TETile[][] tiles, int xPos, int yPos, int size) {
        // fill middle column
        for (int i = 0; i < 5; i++) {
            fillSingleHexagon(tiles, xPos, yPos + i * size * 2, size);
        }
        for (int i = 0; i < 2; i++) {
            int dx = (size * 2 - 1) * (i + 1), dy = size * (i + 1);
            for (int j = 0; j < 4 - i; j++) {
                fillSingleHexagon(tiles, xPos - dx, yPos + dy + j * size * 2, size);
                fillSingleHexagon(tiles, xPos + dx, yPos + dy + j * size * 2, size);
            }
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] hexWorld = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                hexWorld[x][y] = Tileset.NOTHING;
            }
        }
        int size = 4;
        fillHexagonWorld(hexWorld, (WIDTH - size) / 2, 0, size);

        ter.renderFrame(hexWorld);
    }
}
