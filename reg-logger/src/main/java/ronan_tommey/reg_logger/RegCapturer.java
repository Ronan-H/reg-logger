package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;
import ronan_tommey.reg_logger.image_processing.FrameUtils;
import ronan_tommey.reg_logger.image_processing.MovementHighlighter;
import ronan_tommey.reg_logger.image_processing.PiCamFrameListener;
import ronan_tommey.reg_logger.image_processing.PiCamFrameStreamer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class RegCapturer implements PiCamFrameListener, Runnable {
    private PiCamFrameStreamer piCamFrameStreamer;
    private MovementHighlighter movementHighlighter;

    private BufferedImage nextFrame;
    private long nextFrameDelta;

    private long frameCounter = 0;
    private boolean running;
    private CaptureWaitEstimator estimator;
    private static final long maxEstimateBeforeCapture = 100*1000000;

    private static final int numIgnoreFirst = 25 * 5;

    public RegCapturer(int capWidth, int capHeight) {
        piCamFrameStreamer = new PiCamFrameStreamer(capWidth, capHeight, this);

        movementHighlighter = new MovementHighlighter(capWidth, capHeight);

        estimator = new CaptureWaitEstimator(4, 304);
    }

    public void run() {
        running = true;

        new Thread(piCamFrameStreamer).start();

        while (running) {
            while (nextFrame == null) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            processFrame(nextFrame, nextFrameDelta);
            frameCounter++;
            nextFrame = null;
        }
    }

    private void processFrame(BufferedImage frame, long delta) {
        if (frameCounter < numIgnoreFirst) {
            // ignore frame
            return;
        }

        BufferedImage movementImage = movementHighlighter.getHighlightedImage(frame);

        // boolean array representation of the passed in image, where "true"
        // means the car covers that pixel, and "false" if it doesn't. array is
        // one dimension, with increasing indexes going left to right first, then
        // down, on the passed in image (ie. x = i % width, y = i / width)
        boolean[] movingPixels = FrameUtils.convertImageToBooleanArray(movementImage);
        FrameUtils.removeNoise(movingPixels, frame.getWidth(), 125);

        CarData data = CarDataUtils.generateCarData(movingPixels, frame.getWidth());

        estimator.addNextFrameData(data,delta);

        if(estimator.estimateReady())
        {
            long estimate = estimator.getWaitEstimate();
            if(estimate < maxEstimateBeforeCapture)
            {
                // TODO: 22/03/2019 Lock in capture estimate
                estimator.onCapture();
            }
        }


    }

    public synchronized void onFrameRead(BufferedImage image, long delta) {
        nextFrame = image;
        nextFrameDelta = delta;

        notifyAll();
    }
}
