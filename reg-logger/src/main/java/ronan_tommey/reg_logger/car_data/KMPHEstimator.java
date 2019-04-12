package ronan_tommey.reg_logger.car_data;

public class KMPHEstimator {
    private double pixelsPerMeter;

    public KMPHEstimator(int frameWidth, double imageWidthMeters) {
        pixelsPerMeter = frameWidth / imageWidthMeters;
    }

    public double getKMPHEstimate(double pixelsPerFrame, FrameTimeManager frameTimeManager) {
        double pixelsPerSecond = pixelsPerFrame * (1000000000d / frameTimeManager.getAverageFrameDelta(4));
        double metersPerSecond = pixelsPerSecond / pixelsPerMeter;
        return (metersPerSecond * 60 * 60) / 1000;
    }
}
