package ronan_tommey.reg_logger.car_data;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CarEstimateTest {

    @Test
    void isGoingRight() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder(".src/test/input/car-data-utils/frames-for-car");

        CarEstimate car = Utils.TestCarDirectionRight(frames);

        assertTrue(car.isGoingRight());
    }

    @Test
    void isGoingLeft() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder(".src/test/input/car-data-utils/frames-for-car");

        CarEstimate car = Utils.TestCarDirectionRight(frames);

        assertFalse(car.isGoingRight());
    }

    @Test
    void getPixelSpeed() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder(".src/test/input/car-data-utils/frames-for-car");

        //Values of car position (Car Going right)
        int firstX = 43;
        int secondX = 223;

        int distanceInFrames = secondX - firstX;

        double speed = distanceInFrames/frames.length;

        assertEquals(speed, 4.19);
    }

    @Test
    void getKmphSpeed() {
    }
}