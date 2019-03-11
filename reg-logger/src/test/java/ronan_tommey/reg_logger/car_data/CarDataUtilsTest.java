package ronan_tommey.reg_logger.car_data;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CarDataUtilsTest {

    @Test
    public void generateCarData() throws IOException {

        BufferedImage image = Utils.readTestImage("./src/test/input/car-data-utils/generate-car-data/pi-cam-image-1.png");
        CarData tester = CarDataUtils.generateCarData(image);
        CarData expected = new CarData(145,229,40,71);


        assertTrue(expected.equals(tester));
    }

    //Need expected CarEstimate
    @Test
    @Disabled
    public void generateCarEstimate() {
        CarDataSeries seriesTest = new CarDataSeries();
        FrameTimeManager FTMTest = new FrameTimeManager(10);
        CarEstimate carTester = CarDataUtils.generateCarEstimate(seriesTest,FTMTest);


        //assertEquals(,carTester);
    }
}