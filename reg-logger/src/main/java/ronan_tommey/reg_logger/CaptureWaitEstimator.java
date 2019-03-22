package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.*;

public class CaptureWaitEstimator {
    private int numEstimateFrames;
    private CarDataSeries carDataSeries;
    private FrameTimeManager frameTimeManager;
    private boolean allowCapture;

    public CaptureWaitEstimator(int numEstimateFrames, int capturePoint) {
        this.numEstimateFrames = numEstimateFrames;
        carDataSeries = new CarDataSeries();
        // TODO: don't hard code buffer size
        frameTimeManager = new FrameTimeManager(350);
        allowCapture = true;
    }

    public void addNextFrameData(CarData carData, long delta) {
        carDataSeries.addNextCarData(carData);
        frameTimeManager.addFrameTime(delta);

        if(carData == null) {
            allowCapture = true;
            if(carDataSeries.size() > 0) {
                carDataSeries = new CarDataSeries();
            }
        }
        else {
            carDataSeries.addNextCarData(carData);
        }
    }

    public void onCapture(){
        allowCapture = false;
    }

    public boolean estimateReady(){
        return carDataSeries.size() >= numEstimateFrames && allowCapture;
    }

    public long getWaitEstimate(){
        long temp = 0;
        return temp;
    }

    public int regDistToCapturePoint(){
        return 0;
    }
}
