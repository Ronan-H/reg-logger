package ronan_tommey.reg_logger.image_processing;

import java.util.LinkedList;
import java.util.List;

public class FrameTimeManager {
    public static final long EXPECTED_FRAME_TIME = PiCamFrameStreamer.NS_BETWEEN_FRAMES;

    private List<Long> delays;
    private int bufferSize;

    public FrameTimeManager(int bufferSize) {
        this.bufferSize = bufferSize;

        delays = new LinkedList<Long>();
    }

    public void addFrameTime(long delay) {
        delays.add(delay);

        while (delays.size() > bufferSize) {
            delays.remove(0);
        }
    }

    public long getTimeDuration(int numFrames) {
        if (delays.size() == 0) {
            // no frame times in buffer; return expected frame time
            return EXPECTED_FRAME_TIME;
        }

        long total = 0;
        long avg;

        int startFrame = Math.max(delays.size() - numFrames, 0);
        int numUsed = delays.size() - startFrame;

        for (int i = startFrame; i < delays.size(); ++i) {
            total += delays.get(i);
        }

        avg = Math.round((double)total / numUsed);

        return avg;
    }
}
