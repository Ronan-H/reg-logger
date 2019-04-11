package ronan_tommey.reg_logger.image_processing;

import ronan_tommey.reg_logger.RegCapturer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class CarPassImageDump {
    private String rootDir;
    private int numFrames;
    private ArrayList<Long> frameNums;
    private ArrayList<BufferedImage> rawImages;
    private ArrayList<BufferedImage> movementImages;
    private ArrayList<boolean[]> noiseRemImages;

    public CarPassImageDump(String rootDir) {
        this.rootDir = rootDir;

        reset();
    }

    private void reset() {
        frameNums = new ArrayList<>();
        rawImages = new ArrayList<>();
        movementImages = new ArrayList<>();
        noiseRemImages = new ArrayList<>();

        numFrames = 0;
    }

    public void addFrames(BufferedImage frame, BufferedImage movementImage, boolean[] movingPixels, long frameCounter) {
        frameNums.add(frameCounter);
        rawImages.add(frame);
        movementImages.add(movementImage);
        noiseRemImages.add(movingPixels);

        numFrames++;
    }

    public void dumpFrames() throws IOException {
        if (numFrames == 0) {
            return;
        }

        System.out.println("Dumping pass frames...");

        for (int i = 0; i < numFrames; i++) {
            long frameNum = frameNums.get(i);
            FrameUtils.saveImage(rawImages.get(i), String.format("%s/raw/raw_%d.png", rootDir, frameNum));
            FrameUtils.saveImage(movementImages.get(i), String.format("%s/movement/movement_%d.png", rootDir, frameNum));
            FrameUtils.saveBoolArrayAsImage(noiseRemImages.get(i), RegCapturer.CAP_WIDTH, String.format("%s/noise-removed/noise-rem_%d.png", rootDir, frameNum));
        }

        System.out.println("Finished dumping pass frames.");

        reset();
    }

}
