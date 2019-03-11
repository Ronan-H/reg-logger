package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.car_data.CarData;
import ronan_tommey.reg_logger.car_data.CarDataSeries;
import ronan_tommey.reg_logger.car_data.FrameTimeManager;

public class CaptureWaitEstimator {
    private int numEstimateFrames;
    private CarDataSeries carDataSeries;
    private FrameTimeManager frameTimeManager;

    public CaptureWaitEstimator(int numEstimateFrames) {
        this.numEstimateFrames = numEstimateFrames;
        carDataSeries = new CarDataSeries();
        // TODO: don't hard code buffer size
        frameTimeManager = new FrameTimeManager(200);
    }

    public void addNextFrameData(CarData carData, long delta) {
        carDataSeries.addNextCarData(carData);
        frameTimeManager.addFrameTime(delta);
    }

    public boolean estimateReady(){
        return carDataSeries.size() >= numEstimateFrames;
    }

    public long getWaitEstimate(){
        long temp = 0;
        return temp;
    }

}
