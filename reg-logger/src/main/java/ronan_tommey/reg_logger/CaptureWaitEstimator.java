package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;
import ronan_tommey.reg_logger.image_processing.CarPassImageDump;

import java.io.IOException;

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

    public CaptureWaitEstimator(int numEstimateFrames, int capturePoint, int imageWidth, long captureLatency, CarPassImageDump carPassImageDump) {
        this.numEstimateFrames = numEstimateFrames;
        this.capturePoint = capturePoint;
        this.imageWidth = imageWidth;
        this.captureLatency = captureLatency;
        this.carPassImageDump = carPassImageDump;
        carDataSeries = new CarDataSeries(imageWidth);
        // TODO: don't hard code buffer size
        frameTimeManager = new FrameTimeManager(350);
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

    public void onCapture(){
        allowCapture = false;
    }

    public boolean estimateReady(){
        return carDataSeries.size() >= numEstimateFrames
                && allowCapture
                && (!carDataSeries.isGoingRight() ||
                carDataSeries.getLast().getLeftX() - carDataSeries.getFirst().getLeftX() > minDistBeforeInFrame);
    }

    public CarEstimate generateCarEstimate(){
        lastCarEstimate = CarDataUtils.generateCarEstimate(carDataSeries, numEstimateFrames, frameTimeManager, true);
        return lastCarEstimate;
    }

    public long getWaitEstimate(){
        int regPosEstimate = carDataSeries.getLast().getRegPosEstimate();
        int distToCapturePoint = capturePoint - regPosEstimate;
        double framesTillCapture = distToCapturePoint / lastCarEstimate.getPixelSpeed();
        long avgFrameDelta = frameTimeManager.getAverageFrameDelta(numEstimateFrames);

        return (long)(framesTillCapture * avgFrameDelta) - captureLatency;
    }
}
