package ronan_tommey.reg_logger.car_data;

/**
 * Estimates the speed of the car in kilometers per hour from a series of frames
 */
public class KMPHEstimator {
    private double pixelsPerMeter;

    public KMPHEstimator(int frameWidth, double imageWidthMeters) {
        pixelsPerMeter = frameWidth / imageWidthMeters;
    }

    /**
     *Estimates the speed in kilometer per hour
     * @param pixelsPerFrame The number of pixels in a frame
     * @param frameTimeManager The difference in time between each frame
     */
    public double getKMPHEstimate(double pixelsPerFrame, FrameTimeManager frameTimeManager) {
        double pixelsPerSecond = pixelsPerFrame * (1000000000d / frameTimeManager.getAverageFrameDelta(4));
        double metersPerSecond = pixelsPerSecond / pixelsPerMeter;
        return (metersPerSecond * 60 * 60) / 1000;
    }
}
