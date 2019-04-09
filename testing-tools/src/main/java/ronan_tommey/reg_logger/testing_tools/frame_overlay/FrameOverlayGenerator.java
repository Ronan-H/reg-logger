package ronan_tommey.reg_logger.testing_tools.frame_overlay;

import ronan_tommey.reg_logger.car_data.CarData;
import ronan_tommey.reg_logger.car_data.CarDataUtils;
import ronan_tommey.reg_logger.image_processing.FrameUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class FrameOverlayGenerator {
    private String inputDir;
    private String outputDir;

    public FrameOverlayGenerator(String inputDir, String outputDir) {
        this.inputDir = inputDir;
        this.outputDir = outputDir;
    }

    public void generateOverlaysForDirectory() {
        try {
            BufferedImage[] inputFrames = Utils.readFrameImagesFolder(inputDir);

            for (int i = 0; i < inputFrames.length; i++) {
                boolean[] movingPixels = FrameUtils.convertImageToBooleanArray(inputFrames[i]);
                CarData carData = CarDataUtils.generateCarData(movingPixels, inputFrames[i].getWidth());
                CarDataUtils.addRegPosEstimate(carData, movingPixels, inputFrames[i].getWidth());
                FrameOverlay frameOverlay = new FrameOverlay(carData, inputFrames[i]);
                frameOverlay.drawOverlay();
                frameOverlay.saveToFile(outputDir, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Generating overlay frames...");

        String usingDir = "./res/car-data-testing/sample-frames-1/";
        String inputDir = usingDir + "input/";
        String outputDir = usingDir + "output/";
        FrameOverlayGenerator frameOverlayGenerator = new FrameOverlayGenerator(inputDir, outputDir);
        frameOverlayGenerator.generateOverlaysForDirectory();

        System.out.println("Finished. Exiting...");
    }

}
