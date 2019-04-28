package ronan_tommey.reg_logger.car_data;

/**
 * Estimates the speed of the car in kmph and pixel speed from the series of frames
 */
public class CarEstimate {
    private double pixelSpeed;
    private double kmphSpeed;

    /**
     * @param pixelSpeed Speed of the car in pixels per frame
     * @param kmphSpeed Speed of the car in kilometers per hour
     */
    public CarEstimate(double pixelSpeed, double kmphSpeed) {
        this.pixelSpeed = pixelSpeed;
        this.kmphSpeed = kmphSpeed;
    }

    public double getPixelSpeed() {
        return pixelSpeed;
    }

    public double getKmphSpeed() {
        return kmphSpeed;
    }
}
