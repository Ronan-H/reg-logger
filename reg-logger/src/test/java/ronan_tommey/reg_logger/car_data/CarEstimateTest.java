package ronan_tommey.reg_logger.car_data;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CarEstimateTest {

    @Test
    void isGoingRight() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder("./src/test/input/car-data-utils/frames-for-car");

        CarEstimate car = Utils.TestCarDirectionRight(frames);

        assertTrue(car.isGoingRight());
    }

    @Test
    void isGoingLeft() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder("./src/test/input/car-data-utils/frames-for-car");

        CarEstimate car = Utils.TestCarDirectionRight(frames);

        assertFalse(car.isGoingRight());
    }

    @Test
    void getPixelSpeed() throws IOException {
        BufferedImage[] frames = Utils.readTestImageFolder("./src/test/input/four-frames");

        //Values of car position (Car Going right)
        int firstX = 98;
        int secondX = 113;

        int distanceInFrames = secondX - firstX;

        double speed = distanceInFrames/(frames.length-1);

        CarEstimate car = Utils.TestCarDirectionRight(frames);

        assertEquals(speed, car.getPixelSpeed(),0.1);
    }

    @Ignore
    @Test
    void getKmphSpeed() {
        fail();
    }
}