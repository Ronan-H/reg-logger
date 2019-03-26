package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;

public class CaptureWaitEstimator {
    // minimum distance the car has to be from where it started
    // before an estimate can be generated, when coming from the left
    // (the car must have fully emerged from the edge of the frame before
    // the length of the car can be found)
    private static final int minDistBeforeInFrame = 20;

    private int numEstimateFrames;
    private int capturePoint;
    private int imageWidth;
    private long captureLatency;
    private CarDataSeries carDataSeries;
    private boolean[] lastMovingPixels;
    private CarEstimate lastCarEstimate;
    private FrameTimeManager frameTimeManager;
    private boolean allowCapture;

    public CaptureWaitEstimator(int numEstimateFrames, int capturePoint, int imageWidth, long captureLatency) {
        this.numEstimateFrames = numEstimateFrames;
        this.capturePoint = capturePoint;
        this.imageWidth = imageWidth;
        this.captureLatency = captureLatency;
        carDataSeries = new CarDataSeries(imageWidth);
        // TODO: don't hard code buffer size
        frameTimeManager = new FrameTimeManager(350);
        allowCapture = true;
    }

    public boolean isGoingRight() {
        return carDataSeries.isGoingRight();
    }

    public void addNextFrameData(CarData carData, boolean[] movingPixels, long delta) {
        frameTimeManager.addFrameDelta(delta);

        if(carData == null) {
            allowCapture = true;
            if(carDataSeries.size() > 0) {
                carDataSeries = new CarDataSeries(imageWidth);
            }
        }
        else {
            carDataSeries.addNextCarData(carData);
        }

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
        lastCarEstimate = CarDataUtils.generateCarEstimate(carDataSeries, numEstimateFrames, frameTimeManager);
        return lastCarEstimate;
    }

    public long getWaitEstimate(){
        int regPosEstimate = CarDataUtils.getRegPosEstimate(lastCarEstimate, carDataSeries.getLast(), lastMovingPixels, imageWidth);
        int distToCapturePoint = capturePoint - regPosEstimate;
        double framesTillCapture = distToCapturePoint / lastCarEstimate.getPixelSpeed();
        long avgFrameDelta = frameTimeManager.getAverageFrameDelta(numEstimateFrames);

        return (long)(framesTillCapture * avgFrameDelta) - captureLatency;
    }
}
