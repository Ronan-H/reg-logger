package ronan_tommey.reg_logger.testing_tools.frame_overlay;

import ronan_tommey.reg_logger.CaptureWaitEstimator;
import ronan_tommey.reg_logger.RegCapturer;
import ronan_tommey.reg_logger.RegLogger;
import ronan_tommey.reg_logger.car_data.*;
import ronan_tommey.reg_logger.image_processing.FrameUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class CarPassSimulator {
    private String inputDir;
    private String outputDir;

    public CarPassSimulator(String inputDir, String outputDir) {
        this.inputDir = inputDir;
        this.outputDir = outputDir;
    }

    public void simulatePass() {
        try {
            BufferedImage[] inputFrames = Utils.readFrameImagesFolder(inputDir);

            CarDataSeries carDataSeries = new CarDataSeries(inputFrames[0].getWidth());
            FrameTimeManager frameTimeManager = new FrameTimeManager(inputFrames.length);
            CaptureWaitEstimator waitEstimator = new CaptureWaitEstimator(
                    4,
                    RegCapturer.CAPTURE_POINT,
                    inputFrames[0].getWidth(),
                    RegCapturer.TOTAL_CAPTURE_LATENCY,
                    null);
            KMPHEstimator kmphEstimator = new KMPHEstimator(inputFrames[0].getWidth(),  20);

            for (int i = 0; i < inputFrames.length; i++) {
                System.out.printf("Frame %d%n", i);

                boolean[] movingPixels = FrameUtils.convertImageToBooleanArray(inputFrames[i]);
                CarData carData = CarDataUtils.generateCarData(movingPixels, inputFrames[i].getWidth());
                carDataSeries.addNextCarData(carData);

                waitEstimator.addNextFrameData(carData, movingPixels, frameTimeManager.getAverageFrameDelta(1));

                if (waitEstimator.estimateReady()) {
                    System.out.println("Estimate ready");

                    CarEstimate carEstimate = waitEstimator.generateCarEstimate();
                    double kmphSpeed = kmphEstimator.getKMPHEstimate(carEstimate.getPixelSpeed(), frameTimeManager);

                    System.out.printf("Going right: %b%nEstimated pixel speed: %.2f%nEstimated speed in KMPH: %.2f%n",
                            carDataSeries.isGoingRight(), carEstimate.getPixelSpeed(), kmphSpeed);

                    long waitEstimate = waitEstimator.getWaitEstimate();

                    System.out.printf("Wait estimate: %d milliseconds%n", (waitEstimate / 1000000));
                }
                else {
                    System.out.println("Estimate not ready");
                }

                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Simulating car pass...\n");

        String usingDir = "./res/car-data-testing/50km-pass-demo/";
        String inputDir = usingDir + "input/";
        String outputDir = usingDir + "output/";
        CarPassSimulator carPassSimulator = new CarPassSimulator(inputDir, outputDir);
        carPassSimulator.simulatePass();

        System.out.println("\nFinished. Exiting...");
    }
}
