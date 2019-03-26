package ronan_tommey.reg_logger.car_data;

import org.junit.jupiter.api.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CarEstimateTest {

    @Test
    void isGoingRight() throws IOException {
        BufferedImage[] frames = Utils.readFrameImagesFolder("./src/test/input/car-data-utils/frames-for-car-going-right");

        CarDataSeries car = Utils.getCarDataSeriesForImageFrames(frames);

        assertTrue(car.isGoingRight());
    }

    @Test
    void isGoingLeft() throws IOException {
        BufferedImage[] frames = Utils.readFrameImagesFolder("./src/test/input/car-data-utils/frames-for-car-going-left");

        CarDataSeries car = Utils.getCarDataSeriesForImageFrames(frames);

        assertFalse(car.isGoingRight());
    }

    @Test
    void pixelSpeed4FramesGoingRight() throws IOException {
        BufferedImage[] frames = Utils.readFrameImagesFolder("./src/test/input/frames-for-testing/four-frames-going-right");

        //Values of car position (Car Going right)
        int firstX = 98;
        int secondX = 112;

        int distanceInFrames = secondX - firstX;

        double speed = (double)distanceInFrames/(frames.length-1);

        CarEstimate car = Utils.getCarEstimateForImageFrames(frames, 4);

        assertEquals(speed, car.getPixelSpeed(),0.1);
    }

    @Test
    void pixelSpeed2FramesGoingRight() throws IOException {
        BufferedImage[] frames = Utils.readFrameImagesFolder("./src/test/input/frames-for-testing/two-frames-going-right");

        //Values of car position (Car Going right)
        int firstX = 98;
        int secondX = 112;

        int distanceInFrames = secondX - firstX;

        double speed = (double)distanceInFrames/(frames.length-1);

        CarEstimate car = Utils.getCarEstimateForImageFrames(frames, 2);

        assertEquals(speed, car.getPixelSpeed(),0.1);
    }

    @Test
    void pixelSpeed4FramesGoingLeft() throws IOException {
        BufferedImage[] frames = Utils.readFrameImagesFolder("./src/test/input/frames-for-testing/four-frames-going-left");

        //Variables
        int firstX = 106;
        int secondX = 84;

        int distanceInFrames = secondX - firstX;

        double speed = (double)distanceInFrames/(3);

        CarEstimate car = Utils.getCarEstimateForImageFrames(frames, 4);

        assertEquals(speed, car.getPixelSpeed(), 0.1);
    }

    @Disabled
    @Test
    void getKmphSpeed() {
        fail();
    }
}