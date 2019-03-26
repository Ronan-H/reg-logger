package ronan_tommey.reg_logger.car_data;

public class CarEstimate {
    private double pixelSpeed;
    private double kmphSpeed;

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
