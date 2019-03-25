package ronan_tommey.reg_logger.car_data;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class FrameTimeManagerTest {

    @Test
    void testEmptyTimeBuffer() {
        FrameTimeManager frameTimeManager = new FrameTimeManager(10);

        assertEquals(FrameTimeManager.EXPECTED_FRAME_TIME, frameTimeManager.getAverageFrameDelta(5));
    }

    @Test
    void testGetTimeDurationSimple() {
        int bufferSize = 10;
        FrameTimeManager frameTimeManager = new FrameTimeManager(bufferSize);

        for (int i = 0; i < bufferSize; i++) {
            frameTimeManager.addFrameDelta(12345);
        }

        assertEquals(12345, frameTimeManager.getAverageFrameDelta(bufferSize));
    }

    @Test
    void testGetTimeDurationComplex() {
        // total: 225
        // mean: 45
        long[] times = {40, 45, 41, 44, 55};
        long expectedMean = 45;
        int bufferSize = times.length;


        FrameTimeManager frameTimeManager = new FrameTimeManager(bufferSize);

        for (Long time : times) {
            frameTimeManager.addFrameDelta(time);
        }

        assertEquals(expectedMean, frameTimeManager.getAverageFrameDelta(bufferSize));
    }

    @Test
    void testPartiallyFilledBuffer() {
        int bufferSize = 500;
        int fillAmount = 350;
        int usingAmount = 200;

        int time = 55;

        FrameTimeManager frameTimeManager = new FrameTimeManager(bufferSize);

        for (int i = 0; i < fillAmount; i++) {
            frameTimeManager.addFrameDelta(time);
        }

        assertEquals(time, frameTimeManager.getAverageFrameDelta(usingAmount));
    }

    @Test
    void testRequestedDurationBiggerThanBuffer() {
        int bufferSize = 500;
        int fillAmount = 350;
        int usingAmount = 10000;

        int time = 55;

        FrameTimeManager frameTimeManager = new FrameTimeManager(bufferSize);

        for (int i = 0; i < fillAmount; i++) {
            frameTimeManager.addFrameDelta(time);
        }

        assertEquals(time, frameTimeManager.getAverageFrameDelta(usingAmount));
    }
}