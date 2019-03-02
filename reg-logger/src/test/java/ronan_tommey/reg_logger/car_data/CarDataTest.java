package ronan_tommey.reg_logger.car_data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CarDataTest {

    //Expected Result Variables
    int left = 145;
    int right = 226;
    int top = 40;
    int bottom = 71;

    @org.junit.jupiter.api.Test
    void testCarData() throws IOException {

        BufferedImage image = ImageIO.read(new File("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png"));
        CarData tester = CarDataUtils.generateCarData(image);
        CarData expected = new CarData(left,right,top,bottom);

        assertEquals(expected,tester);
    }

    @org.junit.jupiter.api.Test
    void testGetLeftX() throws IOException {

        BufferedImage image = ImageIO.read(new File("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png"));
        CarData tester = CarDataUtils.generateCarData(image);
        int testedLeft = tester.getLeftX();

        assertEquals(left, testedLeft);
    }

    @org.junit.jupiter.api.Test
    void testGetRightX() throws IOException {
        BufferedImage image = ImageIO.read(new File("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png"));
        CarData tester = CarDataUtils.generateCarData(image);
        int testedRight = tester.getRightX();

        assertEquals(right,testedRight);
    }

    @org.junit.jupiter.api.Test
    void testGetTopY() throws IOException {

        BufferedImage image = ImageIO.read(new File("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png"));
        CarData tester = CarDataUtils.generateCarData(image);
        int testedTop = tester.getTopY();

        assertEquals(testedTop,top);
    }

    @org.junit.jupiter.api.Test
    void testGetBottomY() throws IOException {
        BufferedImage image = ImageIO.read(new File("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png"));
        CarData tester = CarDataUtils.generateCarData(image);
        int testedBottom = tester.getBottomY();

        assertEquals(bottom,testedBottom);
    }

    @org.junit.jupiter.api.Test
    void testGetWidth() throws IOException {

        BufferedImage image = ImageIO.read(new File("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png"));
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