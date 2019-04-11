package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;
import ronan_tommey.reg_logger.image_processing.*;
import ronan_tommey.reg_logger.reg_logging.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;

public class RegCapturer implements PiCamFrameListener, Runnable {
    public static final int CAP_WIDTH = 300;
    public static final int CAP_HEIGHT = 64;

    public static final int FPS = 25;
    public static final long NS_BETWEEN_FRAMES = 1000000000 / FPS;

    private static final long minEstimateBeforeCapture = -200 * 1000000;
    private static final long maxEstimateBeforeCapture = 150 * 1000000;
    private static final int numIgnoreFirst = 3 * FPS;

    private static final int DSLR_CAPTURE_LATENCY = 650 * 1000000;
    private static final int LAN_LATENCY = 5 * 1000000;
    public static final int TOTAL_CAPTURE_LATENCY = DSLR_CAPTURE_LATENCY + LAN_LATENCY;

    public static final int CAPTURE_POINT = 250;

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
    private CarPassImageDump carPassImageDump;
    private boolean running;

    public RegCapturer() {
        piCamFrameStreamer = new PiCamFrameStreamer(CAP_WIDTH, CAP_HEIGHT, FPS, this);
        movementHighlighter = new MovementHighlighter(CAP_WIDTH, CAP_HEIGHT);
        carPassImageDump = new CarPassImageDump("./test-images");
        waitEstimator = new CaptureWaitEstimator(4, CAPTURE_POINT, CAP_WIDTH, TOTAL_CAPTURE_LATENCY, carPassImageDump);
        remoteCamera = new RemoteCamera(REMOTE_CAMERA_PORT);
        carPassLogger = new CarPassFileSystem("./car-pass-log");
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

        int movingPixelCount = FrameUtils.countMoving(movingPixels);

        if (movingPixelCount > 0) {
            carPassImageDump.addFrames(frame, movementImage, movingPixels, frameCounter);
        }

        CarData data = CarDataUtils.generateCarData(movingPixels, frame.getWidth());

        waitEstimator.addNextFrameData(data, movingPixels, delta);

        if (data != null) {
            if(waitEstimator.estimateReady()) {
                CarEstimate carEstimate = waitEstimator.generateCarEstimate();
                long waitEstimate = waitEstimator.getWaitEstimate();
                if(waitEstimate > minEstimateBeforeCapture && waitEstimate < maxEstimateBeforeCapture)
                {
                    System.out.println("\tWait estimate: " + waitEstimate);
                    System.out.println("\tLocking in estimate...\n");

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
    }

    public synchronized void onFrameRead(BufferedImage image, long delta) {
        if (nextFrame != null) {
            return;
        }

        nextFrame = image;
        nextFrameDelta = delta;

        notifyAll();
    }
}
