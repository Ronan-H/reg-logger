package ronan_tommey.reg_logger.car_data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CarDataTest {
    //Expected Result Variables
    int left = 145;
    int right = 229;
    int top = 40;
    int bottom = 71;

    @Test
    void testCarData() throws IOException {

        BufferedImage image = Utils.readTestImage("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png");
        CarData tester = CarDataUtils.generateCarData(image);
        CarData expected = new CarData(left,right,top,bottom);

        assertEquals(expected,tester);
    }

    @Test
    void testGetLeftX() throws IOException {

        BufferedImage image = Utils.readTestImage("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png");
        CarData tester = CarDataUtils.generateCarData(image);
        int testedLeft = tester.getLeftX();

        assertEquals(left, testedLeft);
    }

    @Test
    void testGetRightX() throws IOException {
        BufferedImage image = Utils.readTestImage("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png");
        CarData tester = CarDataUtils.generateCarData(image);
        int testedRight = tester.getRightX();

        assertEquals(right,testedRight);
    }

    @Test
    void testGetTopY() throws IOException {

        BufferedImage image = Utils.readTestImage("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png");
        CarData tester = CarDataUtils.generateCarData(image);
        int testedTop = tester.getTopY();

        assertEquals(testedTop,top);
    }

    @Test
    void testGetBottomY() throws IOException {
        BufferedImage image = Utils.readTestImage("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png");
        CarData tester = CarDataUtils.generateCarData(image);
        int testedBottom = tester.getBottomY();

        assertEquals(bottom,testedBottom);
    }

    @Test
    void testGetWidth() throws IOException {

        BufferedImage image = Utils.readTestImage("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png");
        CarData tester = CarDataUtils.generateCarData(image);

        //Values being tested
        int testedLeft = tester.getLeftX();
        int testedRight = tester.getRightX();
        int testedWidth = testedRight-testedLeft;

        //Expected Width
        int width = right - left;

        assertEquals(testedWidth,width);

    }
}