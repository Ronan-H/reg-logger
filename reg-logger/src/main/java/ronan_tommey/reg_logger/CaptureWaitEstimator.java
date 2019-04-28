package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;
import ronan_tommey.reg_logger.image_processing.CarPassImageDump;

import java.io.IOException;

/**
 * Uses a history of CarData objects and frame delta values
 * to produce an estimate for how long a passing car will take
 * to reach the specified capture point
 */
public class CaptureWaitEstimator {
    // minimum distance the car has to be from where it started
    // before an estimate can be generated, when coming from the left
    // (the car must have fully emerged from the edge of the frame before
    // the length of the car can be found)
    private static final int minDistBeforeInFrame = 15;

    private int numEstimateFrames;
    private int capturePoint;
    private int imageWidth;
    private long captureLatency;
    private CarDataSeries carDataSeries;
    private boolean[] lastMovingPixels;
    private CarEstimate lastCarEstimate;
    private FrameTimeManager frameTimeManager;
    private CarPassImageDump carPassImageDump;
    private boolean allowCapture;

    /**
     * @param numEstimateFrames Number of frames to use to produce the estimate (last n frames)
     * @param capturePoint The x position of the frame where the reg of the car
     *                     should be when the image should be captured
     * @param captureLatency The total time it takes to actually capture an image of the passing car's reg plate
     * @param carPassImageDump Associated CarPassImageDump object, to trigger the dumping of frames. Can be null.
     */
    public CaptureWaitEstimator(int numEstimateFrames, int capturePoint, int imageWidth, long captureLatency, FrameTimeManager frameTimeManager, CarPassImageDump carPassImageDump) {
        this.numEstimateFrames = numEstimateFrames;
        this.capturePoint = capturePoint;
        this.imageWidth = imageWidth;
        this.captureLatency = captureLatency;
        this.frameTimeManager = frameTimeManager;
        this.carPassImageDump = carPassImageDump;

        carDataSeries = new CarDataSeries(imageWidth);
        allowCapture = true;
    }

    /**
     * Delegate method; returns true if the currently passing car is going right,
     * false otherwise.
     *
     * @return True if the currently passing car is going right, false otherwise
     */
    public boolean isGoingRight() {
        return carDataSeries.isGoingRight();
    }

    /**
     * Indicates whether or not a picture can currently be taken. This is to prevent
     * multiple pictures being captured of the same car in one pass.
     *
     * @return True if capture is allowed
     */
    public boolean isCaptureAllowed() {
        return allowCapture;
    }

    /**
     * Updates this object with data from the next frame of streamed video.
     *
     * @param carData Calculated CarData object of this frame
     * @param movingPixels Boolean array representing which pixels of the frame are detected as moving
     * @param delta Amount of time that has passed since the last frame read in, in nanoseconds
     */
    public void addNextFrameData(CarData carData, boolean[] movingPixels, long delta) {
        frameTimeManager.addFrameDelta(delta);

        // null CarData represents no car being detected in the frame
        if(carData == null) {
            // ensure capture is allowed for the next car that passes
            // (capture is allowed again once a passing car leaves the frame)
            allowCapture = true;

            if(carDataSeries.size() > 0) {
                // reset CarDataSeries object; should only contain frame data of one pass
                carDataSeries = new CarDataSeries(imageWidth);

                if (carPassImageDump != null) {
                    try {
                        carPassImageDump.dumpFrames();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            // add generated reg pos estimate field to carData
            CarDataUtils.addRegPosEstimate(carData, movingPixels, imageWidth);

            carDataSeries.addNextCarData(carData);
        }

        // store only the current frame's movingPixels array, for use in estimating the car's
        // distance to the capture point (uses a heuristic algorithm)
        this.lastMovingPixels = movingPixels;
    }

    /**
     * Method to be called when a wait estimate is locked in, to prevent
     * further images from attempting to be captured
     */
    public void onCapture(){
        allowCapture = false;
    }

    /**
     * @return True if this object is ready to produce a capture wait estimate for a passing car
     */
    public boolean estimateReady(){
        return carDataSeries.size() >= numEstimateFrames // ensure enough frames are stored to generate an estimate
                && allowCapture // no estimate if capture is not allowed
                // ensure car is fully in frame if it's coming from the left
                && (!carDataSeries.isGoingRight() ||
                carDataSeries.getLast().getLeftX() - carDataSeries.getFirst().getLeftX() > minDistBeforeInFrame);
    }

    /**
     * Delegate method; calls a utility method to generate the CarEstimate
     * based on the history of CarData objects and frame deltas
     * @return The generated CarEstimate object
     */
    public CarEstimate generateCarEstimate(){
        lastCarEstimate = CarDataUtils.generateCarEstimate(carDataSeries, numEstimateFrames, frameTimeManager, true);
        return lastCarEstimate;
    }

    /**
     * Generates an estimate of how long it will take the passing car's
     * registration plate to reach the capture point, in nanoseconds. Also
     * takes into accunt the capure latency.
     * @return Capture wait esatimate (nanoseconds)
     */
    public long getWaitEstimate(){
        // get estimate of where the car's registration plate was in the most recent frame
        int regPosEstimate = carDataSeries.getLast().getRegPosEstimate();
        // calculate the car's distance to the capture point based on the reg position estimate
        int distToCapturePoint = capturePoint - regPosEstimate;
        // calculate the number of frames before the car reaches the capture point
        // formula: (time = distance / speed)
        double framesTillCapture = distToCapturePoint / lastCarEstimate.getPixelSpeed();
        // get the average time between frames for the number of frames we're using for the estimatee
        long avgFrameDelta = frameTimeManager.getAverageFrameDelta(numEstimateFrames);

        // multiply frames until capture by average frame delta to get the capture wait time in nanoseconds
        // also account for capture latency by taking it away
        return (long)(framesTillCapture * avgFrameDelta) - captureLatency;
    }
}
