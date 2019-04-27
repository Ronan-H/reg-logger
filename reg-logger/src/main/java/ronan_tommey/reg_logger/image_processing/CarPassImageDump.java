package ronan_tommey.reg_logger.image_processing;

import ronan_tommey.reg_logger.RegCapturer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class used for storing many images of a car pass (including raw,
 * movement highlighted, noise removed, etc), and dumping them out to
 * disk all at once. This is to prevent overhead as the car is passing,
 * when dropping frames due to falling behind could be an issue.
 */
public class CarPassImageDump {
    private String rootDir;
    private int numFrames;
    private ArrayList<Long> frameNums;
    private ArrayList<BufferedImage> rawImages;
    private ArrayList<BufferedImage> movementImages;
    private ArrayList<boolean[]> noiseRemImages;

    /**
     * @param rootDir Path of root directory to save images to
     */
    public CarPassImageDump(String rootDir) {
        this.rootDir = rootDir;

        // use the reset methods to initialize the lists
        reset();
    }

    /**
     * Resets all image lists
     */
    private void reset() {
        frameNums = new ArrayList<>();
        rawImages = new ArrayList<>();
        movementImages = new ArrayList<>();
        noiseRemImages = new ArrayList<>();

        numFrames = 0;
    }

    /**
     * Adds all the different frame image stages to the lists. These images
     * were created using a single raw frame of a car pass.
     * @param frame Raw image, taken straight from the camera with no changes
     * @param movementImage Frame indicating any movement detected from the raw frame against the background image
     * @param movingPixels Boolean array representation of the movementImage, with noise removed also
     * @param frameCounter Which frame these images were created from
     */
    public void addFrames(BufferedImage frame, BufferedImage movementImage, boolean[] movingPixels, long frameCounter) {
        // add each image to their corresponding list
        frameNums.add(frameCounter);
        rawImages.add(frame);
        movementImages.add(movementImage);
        noiseRemImages.add(movingPixels);

        numFrames++;
    }

    /**
     * Dumps all frame images to disk
     * @throws IOException Relating to saving the images to disk
     */
    public void dumpFrames() throws IOException {
        if (numFrames == 0) {
            // no images to dump
            return;
        }

        System.out.println("Dumping pass frames...");

        for (int i = 0; i < numFrames; i++) {
            long frameNum = frameNums.get(i);

            // save each image in its own folder, using the frame number as part of the file name
            FrameUtils.saveImage(rawImages.get(i), String.format("%s/raw/raw_%d.png", rootDir, frameNum));
            FrameUtils.saveImage(movementImages.get(i), String.format("%s/movement/movement_%d.png", rootDir, frameNum));
            FrameUtils.saveBoolArrayAsImage(noiseRemImages.get(i), RegCapturer.CAP_WIDTH, String.format("%s/noise-removed/noise-rem_%d.png", rootDir, frameNum));
        }

        System.out.println("Finished dumping pass frames.");

        // reset image lists for next pass
        reset();
    }

}
