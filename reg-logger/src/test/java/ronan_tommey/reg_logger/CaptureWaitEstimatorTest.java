package ronan_tommey.reg_logger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ronan_tommey.reg_logger.car_data.CarData;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class CaptureWaitEstimatorTest {

    @Disabled
    @Test
    void addNextFrameData() {

    }

    @Test
    void estimateReady() {
        CarData dummy = new CarData(0, 0, 0, 0);

        CaptureWaitEstimator estimator = new CaptureWaitEstimator(4,236);
        assertFalse(estimator.estimateReady());

        for(int i = 0; i < 3; i++)
        {
            estimator.addNextFrameData(dummy,0);
            assertFalse(estimator.estimateReady());
        }

        estimator.addNextFrameData(dummy,0);
        assertTrue(estimator.estimateReady());
    }

    @Disabled
    @Test
    void getWaitEstimate() {

    }

    @Disabled
    @Test
    void regDistToCapturePoint(){

    }
}