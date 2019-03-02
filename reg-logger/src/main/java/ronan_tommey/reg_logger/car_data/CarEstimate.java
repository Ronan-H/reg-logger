package ronan_tommey.reg_logger.car_data;

public class CarEstimate {
    private boolean goingRight;
    private double pixelSpeed;
    private double kmphSpeed;

    public CarEstimate(boolean goingRight, double pixelSpeed, double kmphSpeed) {
        this.goingRight = goingRight;
        this.pixelSpeed = pixelSpeed;
        this.kmphSpeed = kmphSpeed;
    }

    public boolean isGoingRight() {
        return goingRight;
    }

    public double getPixelSpeed() {
        return pixelSpeed;
    }

    public double getKmphSpeed() {
        return kmphSpeed;
    }
}
