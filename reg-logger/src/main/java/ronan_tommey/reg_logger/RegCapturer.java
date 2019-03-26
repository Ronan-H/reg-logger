package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;
import ronan_tommey.reg_logger.image_processing.FrameUtils;
import ronan_tommey.reg_logger.image_processing.MovementHighlighter;
import ronan_tommey.reg_logger.image_processing.PiCamFrameListener;
import ronan_tommey.reg_logger.image_processing.PiCamFrameStreamer;
import ronan_tommey.reg_logger.reg_logging.AsyncALPRCaptureLogger;
import ronan_tommey.reg_logger.reg_logging.CarPassDatabase;
import ronan_tommey.reg_logger.reg_logging.CarPassDetails;
import ronan_tommey.reg_logger.reg_logging.CarPassLogger;

import java.awt.image.BufferedImage;
import java.util.Calendar;

public class RegCapturer implements PiCamFrameListener, Runnable {
    private static final long minEstimateBeforeCapture = -100 * 1000000;
    private static final long maxEstimateBeforeCapture = 100 * 1000000;
    private static final int numIgnoreFirst = 25 * 5;

    private static final int DSLR_CAPTURE_LATENCY = 600 * 1000000;
    private static final int LAN_LATENCY = 10 * 1000000;
    private static final int TOTAL_CAPTURE_LATENCY = DSLR_CAPTURE_LATENCY + LAN_LATENCY;

    public static final int REMOTE_CAMERA_PORT = 52197;

    private PiCamFrameStreamer piCamFrameStreamer;
    private MovementHighlighter movementHighlighter;

    private BufferedImage nextFrame;
    private long nextFrameDelta;

    private long frameCounter = 0;
    private CaptureWaitEstimator waitEstimator;
    private RemoteCamera remoteCamera;
    private CarPassLogger carPassLogger;
    private AsyncALPRCaptureLogger captureLogger;
    private boolean running;

    public RegCapturer(int capWidth, int capHeight) {
        piCamFrameStreamer = new PiCamFrameStreamer(capWidth, capHeight, this);
        movementHighlighter = new MovementHighlighter(capWidth, capHeight);
        waitEstimator = new CaptureWaitEstimator(4, 304, capWidth, TOTAL_CAPTURE_LATENCY);
        remoteCamera = new RemoteCamera(REMOTE_CAMERA_PORT);
        carPassLogger = new CarPassDatabase();
        captureLogger = new AsyncALPRCaptureLogger(remoteCamera, carPassLogger);
        new Thread(captureLogger).start();
    }

    @Override
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

        waitEstimator.addNextFrameData(data, movingPixels, delta);

        if(waitEstimator.estimateReady())
        {
            CarEstimate carEstimate = waitEstimator.generateCarEstimate();
            long waitEstimate = waitEstimator.getWaitEstimate();
            if(waitEstimate > minEstimateBeforeCapture && waitEstimate < maxEstimateBeforeCapture)
            {
                CarPassDetails carPassDetails = new CarPassDetails(
                        Calendar.getInstance().getTimeInMillis(),
                        waitEstimator.isGoingRight() ? "Right" : "Left",
                        carEstimate.getPixelSpeed(),
                        carEstimate.getKmphSpeed()
                );

                captureLogger.capturePass(carPassDetails, waitEstimate);

                waitEstimator.onCapture();
            }
        }


    }

    public synchronized void onFrameRead(BufferedImage image, long delta) {
        nextFrame = image;
        nextFrameDelta = delta;

        notifyAll();
    }
}
