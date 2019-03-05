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
    }

    public void addNextFrameData(CarData carData, long delta) {

    }

    public boolean estimateReady(){
        boolean temp = false;
        return temp;
    }

    public long getWaitEstimate(){
        long temp = 0;
        return temp;
    }

}
