package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.image_processing.FrameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class NoiseRemovalTest {

    public static void main(String[] args) throws IOException {
        BufferedImage img = readTestImage("/home/ronan/temp/noise-removal-test.png");
        boolean[] bools = null;

        long start = System.nanoTime();
        int reps = 1000;
        for (int i = 0; i < reps; i++) {
            bools = FrameUtils.convertImageToBooleanArray(img);
            FrameUtils.removeNoise(bools, img.getWidth(), 300);
        }

        long nsTaken = System.nanoTime() - start;
        double msTaken = (double) nsTaken / 1000000 / reps;

        System.out.printf("Noise removal took %.3fms.%n%n", msTaken);

        FrameUtils.saveBoolArrayAsImage(bools, img.getWidth(), "/home/ronan/temp/noise-removal-test-2.png");
    }

    /**
     * Reads an image from a file, and returns a BufferedImage object whose image type
     * matches the images that are created by the PiCamFrameStreamer class.
     *
     * @throws IOException
     */
    public static BufferedImage readTestImage(String path) throws IOException {
        // read in image
        BufferedImage image = ImageIO.read(new File(path));

        // create new BufferedImage with correct image type
        BufferedImage imageCorrectFormat = new BufferedImage(
                image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_RGB
        );

        // draw old image on new BufferedImage with correct image type
        Graphics g = imageCorrectFormat.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return imageCorrectFormat;
    }
}

