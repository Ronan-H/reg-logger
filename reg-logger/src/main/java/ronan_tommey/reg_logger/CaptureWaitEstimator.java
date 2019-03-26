package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;

public class CaptureWaitEstimator {
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
        carDataSeries = new CarDataSeries();
        // TODO: don't hard code buffer size
        frameTimeManager = new FrameTimeManager(350);
        allowCapture = true;
    }

    public void addNextFrameData(CarData carData, boolean[] movingPixels, long delta) {
        frameTimeManager.addFrameDelta(delta);

        if(carData == null) {
            allowCapture = true;
            if(carDataSeries.size() > 0) {
                carDataSeries = new CarDataSeries();
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
        return carDataSeries.size() >= numEstimateFrames && allowCapture;
    }

    public CarEstimate generateCarEstimate(){
        CarEstimate carEstimate = CarDataUtils.generateCarEstimate(carDataSeries, frameTimeManager);
        return carEstimate;
    }

    public long getWaitEstimate(){
        int regPosEstimate = CarDataUtils.getRegPosEstimate(lastCarEstimate, carDataSeries.getLast(), lastMovingPixels, imageWidth);
        int distToCapturePoint = capturePoint - regPosEstimate;
        double framesTillCapture = distToCapturePoint / lastCarEstimate.getPixelSpeed();
        long avgFrameDelta = frameTimeManager.getAverageFrameDelta(numEstimateFrames);

        return (long)(framesTillCapture * avgFrameDelta) - captureLatency;
    }
}
