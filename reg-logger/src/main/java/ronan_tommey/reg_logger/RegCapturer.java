package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;
import ronan_tommey.reg_logger.image_processing.FrameUtils;
import ronan_tommey.reg_logger.image_processing.MovementHighlighter;
import ronan_tommey.reg_logger.image_processing.PiCamFrameListener;
import ronan_tommey.reg_logger.image_processing.PiCamFrameStreamer;
import ronan_tommey.reg_logger.reg_logging.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;

public class RegCapturer implements PiCamFrameListener, Runnable {
    private static final long minEstimateBeforeCapture = -200 * 1000000;
    private static final long maxEstimateBeforeCapture = 200 * 1000000;
    private static final int numIgnoreFirst = 25 * 5;

    private static final int DSLR_CAPTURE_LATENCY = 530 * 1000000;
    private static final int LAN_LATENCY = 10 * 1000000;
    private static final int TOTAL_CAPTURE_LATENCY = DSLR_CAPTURE_LATENCY + LAN_LATENCY;

    private static final int CAPTURE_POINT = 303;

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
        waitEstimator = new CaptureWaitEstimator(4, CAPTURE_POINT, capWidth, TOTAL_CAPTURE_LATENCY);
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
        FrameUtils.removeNoise(movingPixels, frame.getWidth(), 150);

        int movingPixelCount = FrameUtils.countMoving(movingPixels);

        if (movingPixelCount > 0) {
            //writeDebugFrames(frame, movementImage, movingPixels);
        }

        CarData data = CarDataUtils.generateCarData(movingPixels, frame.getWidth());

        waitEstimator.addNextFrameData(data, movingPixels, delta);

        if (data != null) {
            if (waitEstimator.isCaptureAllowed()) {
                System.out.println("Reg pos x: " + data.getRegPosEstimate());
            }

            if(waitEstimator.estimateReady()) {
                CarEstimate carEstimate = waitEstimator.generateCarEstimate();
                long waitEstimate = waitEstimator.getWaitEstimate();
                System.out.println("\tWait estimate: " + waitEstimate);
                if(waitEstimate > minEstimateBeforeCapture && waitEstimate < maxEstimateBeforeCapture)
                {
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

    private void writeDebugFrames(BufferedImage frame, BufferedImage movementImage, boolean[] movingPixels) {
        try {
            int movingPixelCount = FrameUtils.countMoving(movingPixels);

            System.out.println("Moving pixel count: " + movingPixelCount);

            FrameUtils.saveImage(frame, String.format("./test-images/scaled/scaled_%d.png", frameCounter));
            FrameUtils.saveImage(movementImage, String.format("./test-images/movement/movement_%d.png", frameCounter));
            FrameUtils.saveBoolArrayAsImage(movingPixels, frame.getWidth(), String.format("./test-images/noise-removed/noise-rem_%d.png", frameCounter));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void onFrameRead(BufferedImage image, long delta) {
        nextFrame = image;
        nextFrameDelta = delta;

        notifyAll();
    }
}
