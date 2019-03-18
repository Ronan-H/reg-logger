package ronan_tommey.reg_logger.image_processing;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FrameUtils {
    public static boolean[] convertImageToBooleanArray(BufferedImage frame) {
        int[] pixels = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        boolean[] movingPixels = new boolean[pixels.length];

        for (int i = 0; i < pixels.length; ++i) {
            movingPixels[i] = (pixels[i] != 0);
        }

        return movingPixels;
    }

    public static void removeNoise(boolean[] movingPixels, int imageWidth, int minClusterSize) {
        FrameNoiseRemover frameNoiseRemover = new FrameNoiseRemover(movingPixels, imageWidth, minClusterSize);
        frameNoiseRemover.removeNoise();
    }

    public static int countMoving(boolean[] movingPixels) {
        int count = 0;

        for (int i = 0; i < movingPixels.length; i++) {
            if (movingPixels[i]) {
                count++;
            }
        }

        return count;
    }

    public static BufferedImage scaleImage(BufferedImage before, Dimension newSize) {
        int newWidth = newSize.width;
        int newHeight = newSize.height;

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, before.getType());

        Graphics2D g = scaled.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        g.drawImage(before, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return scaled;
    }

    public static void saveBoolArrayAsImage(boolean[] movingPixels, int width, String outPath) throws IOException {
        // https://stackoverflow.com/questions/20321606/convert-2d-binary-matrix-to-black-white-image-in-java

        int height = movingPixels.length / width;

        final byte BLACK = 0;
        final byte WHITE = (byte) 255;

        byte[] map = {BLACK, WHITE};
        IndexColorModel icm = new IndexColorModel(1, 2, map, map, map);

        int[] data = new int[movingPixels.length];

        for (int i = 0; i < movingPixels.length; i++) {
            data[i] = (movingPixels[i] ? WHITE : BLACK);
        }

        WritableRaster raster = icm.createCompatibleWritableRaster(width, height);
        raster.setPixels(0, 0, width, height, data);

        BufferedImage imgOut = new BufferedImage(icm, raster, false, null);

        ImageIO.write(imgOut, "PNG", new File(outPath));
    }

    public static void saveImage(BufferedImage image, String outPath) throws IOException {
        ImageIO.write(image, "PNG", new File(outPath));
    }
}

class FrameNoiseRemover {
    private boolean[] movingPixels;
    private int imageWidth;
    private int minClusterSize;

    private final int[] offsets;

    FrameNoiseRemover(boolean[] movingPixels, int imageWidth, int minClusterSize) {
        this.movingPixels = movingPixels;
        this.imageWidth = imageWidth;
        this.minClusterSize = minClusterSize;

        offsets = new int[] {-1, +1, -imageWidth, +imageWidth};
    }

    void removeNoise() {
        boolean[] processed = new boolean[movingPixels.length];

        for (int i = 0; i < movingPixels.length; i++) {
            if (movingPixels[i] && !processed[i]) {
                Deque<Integer> visited = removeClusterIfSmall(i);

                for (int j : visited) {
                    processed[j] = true;
                }
            }
        }
    }

    private Deque<Integer> removeClusterIfSmall(int startIndex) {
        if (!movingPixels[startIndex]) {
            return null;
        }

        boolean[] marked = new boolean[movingPixels.length];

        Deque<Integer> unvisited = new ArrayDeque<>();
        Deque<Integer> visited = new ArrayDeque<>();

        unvisited.push(startIndex);
        marked[startIndex] = true;

        int index, newIndex;

        while (!unvisited.isEmpty()) {
            index = unvisited.pop();
            visited.push(index);

            for (int offset : offsets) {
                newIndex = index + offset;

                if (newIndex >= 0 && newIndex < movingPixels.length && movingPixels[newIndex] && !marked[newIndex]) {
                    unvisited.push(newIndex);
                    marked[newIndex] = true;
                }
            }
        }

        if (visited.size() < minClusterSize) {
            for (int removalIndex : visited) {
                movingPixels[removalIndex] = false;
            }
        }

        return visited;
    }
}
