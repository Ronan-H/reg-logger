package ronan_tommey.reg_logger.car_data;

import org.junit.jupiter.api.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CarEstimateTest {

    @Test
    void isGoingRight() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder("./src/test/input/car-data-utils/frames-for-car-going-right");

        CarEstimate car = Utils.getCarEstimateForImageFrames(frames);

        assertTrue(car.isGoingRight());
    }

    @Test
    void isGoingLeft() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder("./src/test/input/car-data-utils/frames-for-car-going-left");

        CarEstimate car = Utils.getCarEstimateForImageFrames(frames);

        assertFalse(car.isGoingRight());
    }

    @Test
    void pixelSpeed4FramesGoingRight() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder("./src/test/input/frames-for-testing/four-frames-going-right");

        //Values of car position (Car Going right)
        int firstX = 98;
        int secondX = 113;

        int distanceInFrames = secondX - firstX;

        double speed = distanceInFrames/(frames.length-1);

        CarEstimate car = Utils.getCarEstimateForImageFrames(frames);

        assertEquals(speed, car.getPixelSpeed(),0.1);
    }

    @Test
    void pixelSpeed2FramesGoingRight() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder("./src/test/input/frames-for-testing/two-frames-going-right");

        //Values of car position (Car Going right)
        int firstX = 98;
        int secondX = 113;

        int distanceInFrames = secondX - firstX;

        double speed = distanceInFrames/(frames.length-1);

        CarEstimate car = Utils.getCarEstimateForImageFrames(frames);

        assertEquals(speed, car.getPixelSpeed(),0.1);
    }

    @Test
    void pixelSpeed4FramesGoingLeft() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder("./src/test/input/frames-for-testing/four-frames-going-left");

        //Variables
        int firstX = 194;
        int secondX = 173;

        int distanceInFrames = secondX - firstX;

        double speed = distanceInFrames/(frames.length-1);

        CarEstimate car = Utils.getCarEstimateForImageFrames(frames);

        assertEquals(speed, car.getPixelSpeed(), 0.1);
    }

    @Disabled
    @Test
    void getKmphSpeed() {
        fail();
    }
}