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
    private FrameTimeManager frameTimeManager;
    private CaptureWaitEstimator waitEstimator;
    private RemoteCamera remoteCamera;
    private CarPassLogger carPassLogger;
    private AsyncALPRCaptureLogger captureLogger;
    private CarPassImageDump carPassImageDump;
    private KMPHEstimator kmphEstimator;
    private boolean running;

    public RegCapturer() {
        frameTimeManager = new FrameTimeManager(350);
        piCamFrameStreamer = new PiCamFrameStreamer(CAP_WIDTH, CAP_HEIGHT, FPS, this);
        movementHighlighter = new MovementHighlighter(CAP_WIDTH, CAP_HEIGHT);
        carPassImageDump = new CarPassImageDump("./test-images");
        waitEstimator = new CaptureWaitEstimator(4, CAPTURE_POINT, CAP_WIDTH, TOTAL_CAPTURE_LATENCY, frameTimeManager, carPassImageDump);
        remoteCamera = new RemoteCamera(REMOTE_CAMERA_PORT);

        carPassLogger = new CarPassFileSystem("./car-pass-log");
        //carPassLogger = new CarPassDatabase();

        captureLogger = new AsyncALPRCaptureLogger(remoteCamera, carPassLogger);
        kmphEstimator = new KMPHEstimator(CAP_WIDTH, 20);
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

    /**
     * Processes a single frame of video streamed from the PiCamFrameStreamer class.
     *
     * @param frame Frame to process
     * @param delta Amount of time (nanoseconds) that has passed from the last frame
     *              being streamed to to this one
     */
    private void processFrame(BufferedImage frame, long delta) {
        // ignore the first few frames to avoid the camera white balancing
        // being detected as movement etc
        if (frameCounter < numIgnoreFirst) {
            // ignore frame
            return;
        }

        // highlight movement in the image
        BufferedImage movementImage = movementHighlighter.getHighlightedImage(frame);

        // convert the black and white image to a boolean array for efficiency
        boolean[] movingPixels = FrameUtils.convertImageToBooleanArray(movementImage);

        // remove noise in the movement array
        FrameUtils.removeNoise(movingPixels, frame.getWidth(), 125);

        // if there's any movement in the image, add the above images to the carPassImageDump
        /// to be saved together whenever the car has left the frame (to prevent slowdowns)
        int movingPixelCount = FrameUtils.countMoving(movingPixels);
        if (movingPixelCount > 0) {
            carPassImageDump.addFrames(frame, movementImage, movingPixels, frameCounter);
        }

        // generate a CarData object based on image movement
        CarData data = CarDataUtils.generateCarData(movingPixels, frame.getWidth());

        // add frame data to class that generates the wait estimates
        waitEstimator.addNextFrameData(data, movingPixels, delta);

        if (data != null) {
            // car detected in frame

            if(waitEstimator.estimateReady()) {
                // waitEstimator has enough information to generate a wait estimate

                // generate estimate of car's speed
                CarEstimate carEstimate = waitEstimator.generateCarEstimate();

                // generate a wait time estimate (nanoseconds) based on the car's position and speed
                long waitEstimate = waitEstimator.getWaitEstimate();

                // wait until as late as possible to "lock in" the estimate to improve accuracy
                if(waitEstimate > minEstimateBeforeCapture && waitEstimate < maxEstimateBeforeCapture)
                {
                    System.out.println("\tWait estimate: " + waitEstimate);
                    System.out.println("\tLocking in estimate...\n");

                    // store details of the car pass in a CarPassDetails object
                    CarPassDetails carPassDetails = new CarPassDetails(
                            Calendar.getInstance().getTimeInMillis(),
                            waitEstimator.isGoingRight() ? "Right" : "Left",
                            carEstimate.getPixelSpeed(),
                            kmphEstimator.getKMPHEstimate(carEstimate.getPixelSpeed(), frameTimeManager)
                    );

                    // hand off the wait estimate to another class to handle sending the
                    // request to take an image, downloading and saving the image etc
                    captureLogger.capturePass(carPassDetails, waitEstimate);

                    // notify the waitEstimator that an image has been taken to prevent any attempts
                    // at taking more pictures of this same car pass
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
