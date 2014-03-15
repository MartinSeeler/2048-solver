import java.awt.*;

/**
 * @author Martin Seeler <mseeler@avantgarde-labs.de>
 * @since 12.03.14 - 20:51
 */
public class Main {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public static void main(String[] args) throws InterruptedException, AWTException {
        final int squareWidth = 120;
        final int squareHeight = 120;
        final int startX = 490;
        final int startY = 380;
        final int squareSize = 4;

        final int[][] model = new int[4][4];

        final Color colorEmpty = new Color(193, 179, 163);
        final Color color2 = new Color(234, 222, 209);
        final Color color4 = new Color(233, 218, 187);
        final Color color8 = new Color(239, 162, 98);
        final Color color16 = new Color(243, 129, 76);
        final Color color32 = new Color(244, 101, 72);
        final Color color64 = new Color(244, 69, 38);
        final Color color128 = new Color(233, 200, 89);
        final Color color256 = new Color(233, 196, 70);

        // sleep at begining
        Thread.sleep(2000L);
        final PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Robot robot = new Robot();

        for (int x = 0; x < squareSize; x++) {
            for (int y = 0; y < squareSize; y++) {
                Thread.sleep(10);
                final int cX = startX + (squareWidth * x);
                final int cY = startY + (squareHeight * y);
                robot.mouseMove(cX, cY);
                final Color pixelColor = robot.getPixelColor(cX, cY);
                if (pixelColor.equals(colorEmpty)) {
                    model[y][x] = 0;
                }
                if (pixelColor.equals(color2)) {
                    model[y][x] = 2;
                }
                if (pixelColor.equals(color4)) {
                    model[y][x] = 4;
                }
                if (pixelColor.equals(color8)) {
                    model[y][x] = 8;
                }
                if (pixelColor.equals(color16)) {
                    model[y][x] = 16;
                }
                if (pixelColor.equals(color32)) {
                    model[y][x] = 32;
                }
                if (pixelColor.equals(color64)) {
                    model[y][x] = 64;
                }
                if (pixelColor.equals(color128)) {
                    model[y][x] = 128;
                }
                if (pixelColor.equals(color256)) {
                    model[y][x] = 256;
                }
            }
        }

        System.out.println("Result:");

        for (int x = 0; x < squareSize; x++) {
            for (int y = 0; y < squareSize; y++) {
                System.out.print(model[x][y] + "\t");
            }
            System.out.print('\n');
        }

    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}
