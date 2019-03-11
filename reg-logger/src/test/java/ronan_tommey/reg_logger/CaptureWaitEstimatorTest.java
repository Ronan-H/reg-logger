package ronan_tommey.reg_logger;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class CaptureWaitEstimatorTest {

    @Test
    void addNextFrameData() {
    }

    @Test
    void estimateReady() {
        CaptureWaitEstimator estimator = new CaptureWaitEstimator(4);
        assertFalse(estimator.estimateReady());

        for(int i = 0; i < 3; i++)
        {
            estimator.addNextFrameData(null,0);
            assertFalse(estimator.estimateReady());
        }

        estimator.addNextFrameData(null,0);
        assertTrue(estimator.estimateReady());
    }

    @Test
    void getWaitEstimate() {
    }
}