package ronan_tommey.reg_logger.car_data;

import ronan_tommey.reg_logger.RegCapturer;
import ronan_tommey.reg_logger.image_processing.PiCamFrameStreamer;

import java.util.LinkedList;
import java.util.List;

/**
 * Gets time between each frame and uses them for calculations
 */
public class FrameTimeManager {
    public static final long EXPECTED_FRAME_TIME = RegCapturer.NS_BETWEEN_FRAMES;

    private List<Long> delays;
    private int bufferSize;

    /**
     * @param bufferSize Max amount of deltas between frames list will hold
     */
    public FrameTimeManager(int bufferSize) {
        this.bufferSize = bufferSize;

        delays = new LinkedList<Long>();
    }

    /**
     * Adds delta to the list delays and ensures delays list is less than bufferSize
     * @param delta Difference in time between each frame
     */
    public void addFrameDelta(long delta) {
        delays.add(delta);

        //removes least recent delta from list if bufferSize is greater than delays
        while (delays.size() > bufferSize) {
            delays.remove(0);
        }
    }

    /**
     *Calculates the average time difference between each frame in delays list
     */
    public long getAverageFrameDelta(int numFrames) {
        if (delays.size() == 0) {
            // no frame times in buffer; return expected frame time
            return EXPECTED_FRAME_TIME;
        }

        //Compute the average of the last few frames
        long total = 0;
        long avg;

        int startFrame = Math.max(delays.size() - numFrames, 0);
        int numUsed = delays.size() - startFrame;

        for (int i = startFrame; i < delays.size(); ++i) {
            total += delays.get(i);
        }

        avg = total / numUsed;

        return avg;
    }
}
