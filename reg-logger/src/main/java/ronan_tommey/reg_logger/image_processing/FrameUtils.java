package ronan_tommey.reg_logger.image_processing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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

    public static void removeNoise(boolean[] movingPixels) {
    }
}
