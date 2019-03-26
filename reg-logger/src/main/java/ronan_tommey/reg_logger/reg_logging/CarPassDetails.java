package ronan_tommey.reg_logger.reg_logging;

public class CarPassDetails {
    private String regText;
    private long timestamp;
    private String direction;
    private double pixelSpeed;
    private double kmphSpeed;

    public CarPassDetails(String regText, long timestamp, String direction, double pixelSpeed, double kmphSpeed) {
        this.regText = regText;
        this.timestamp = timestamp;
        this.direction = direction;
        this.pixelSpeed = pixelSpeed;
        this.kmphSpeed = kmphSpeed;
    }

    public CarPassDetails(long timestamp, String direction, double pixelSpeed, double kmphSpeed) {
        this(null, timestamp, direction, pixelSpeed, kmphSpeed);
    }

    public void setRegText(String regText) {
        this.regText = regText;
    }

    public String getRegText() {
        return regText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDirection() {
        return direction;
    }

    public double getPixelSpeed() {
        return pixelSpeed;
    }

    public double getKmphSpeed() {
        return kmphSpeed;
    }
}
