package ronan_tommey.reg_logger.car_data;

import org.junit.Ignore;
import ronan_tommey.reg_logger.image_processing.FrameTimeManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CarDataUtilsTest {

    @org.junit.jupiter.api.Test
    public void generateCarData() throws IOException {

        BufferedImage image = ImageIO.read(new File("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png"));
        CarData tester = CarDataUtils.generateCarData(image);
        CarData expected = new CarData(145,226,40,71);


        assertEquals(expected,tester);
    }

    //Need expected CarEstimate
    @Ignore
    @org.junit.jupiter.api.Test
    public void generateCarEstimate() {
        CarDataSeries seriesTest = new CarDataSeries();
        FrameTimeManager FTMTest = new FrameTimeManager(10);
        CarEstimate carTester = CarDataUtils.generateCarEstimate(seriesTest,FTMTest);


        //assertEquals(,carTester);
    }
}