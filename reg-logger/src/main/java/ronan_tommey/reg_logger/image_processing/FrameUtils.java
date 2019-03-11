package ronan_tommey.reg_logger.image_processing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

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
        FrameNoiseRemover frameNoiseRemover = new FrameNoiseRemover(movingPixels, imageWidth);
        frameNoiseRemover.removeNoise(minClusterSize);
    }
}

class FrameNoiseRemover {
    private boolean[] movingPixels;
    private int imageWidth;

    private boolean[] markedPixels;
    private int numMarked;

    private static final int[][] offsets = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

    public FrameNoiseRemover(boolean[] movingPixels, int imageWidth) {
        this.movingPixels = movingPixels;
        this.imageWidth = imageWidth;
    }

    protected void removeNoise(int minClusterSize) {
        int i;

        markedPixels = new boolean[movingPixels.length];
        for (i = 0; i < movingPixels.length; ++i) {
            markedPixels[i] = movingPixels[i];
        }

        for (i = 0; i < markedPixels.length; ++i) {
            if (markedPixels[i]) {
                int numConnected = findConnected(i);

                if (numConnected < minClusterSize) {
                    removeConnected(i);
                }
            }
        }
    }

    private int findConnected(int index) {
        numMarked = 0;

        recFindConnected(index);

        return numMarked;
    }

    private void recFindConnected(int index) {
        int offX, offY;
        int newX, newY;
        int newIndex;

        int x = index % imageWidth;
        int y = index / imageWidth;

        markedPixels[index] = false;
        ++numMarked;

        // loop over neighbours
        for (int off = 0; off < offsets.length; ++off) {
            offY = offsets[off][0];
            offX = offsets[off][1];

            newX = x + offX;
            newY = y + offY;

            newIndex = (newY * imageWidth) + newX;

            if (newIndex >= 0 && newIndex < markedPixels.length && markedPixels[newIndex]) {
                recFindConnected(newIndex);
            }
        }
    }

    public void removeConnected(int index) {
        int offX, offY;
        int newX, newY;
        int newIndex;

        int x = index % imageWidth;
        int y = index / imageWidth;

        movingPixels[index] = false;

        // loop over neighbours
        for (int off = 0; off < offsets.length; ++off) {
            offY = offsets[off][0];
            offX = offsets[off][1];

            newX = x + offX;
            newY = y + offY;

            newIndex = (newY * imageWidth) + newX;

            if (newIndex >= 0 && newIndex < movingPixels.length && movingPixels[newIndex]) {
                removeConnected(newIndex);
            }
        }
    }
}