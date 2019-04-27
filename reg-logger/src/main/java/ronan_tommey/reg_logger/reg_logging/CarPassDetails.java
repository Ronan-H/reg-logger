package ronan_tommey.reg_logger.reg_logging;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;

/**
 * Has all of the information about the car from a series of frames in this class
 */
public class CarPassDetails {
    private BufferedImage capturedImage;
    private String regText;
    private long timestamp;
    private String direction;
    private double pixelSpeed;
    private double kmphSpeed;

    /**
     * @param capturedImage Image of car registration
     * @param regText Car registration text in string
     * @param timestamp TimeStamp of when the captureedImage was captured
     * @param direction Direction car was travelling in
     * @param pixelSpeed Speed of car in pixel per frame
     * @param kmphSpeed Speed of car in kilometer per hour
     */
    public CarPassDetails(BufferedImage capturedImage, String regText, long timestamp, String direction, double pixelSpeed, double kmphSpeed) {
        this.capturedImage = capturedImage;
        this.regText = regText;
        this.timestamp = timestamp;
        this.direction = direction;
        this.pixelSpeed = pixelSpeed;
        this.kmphSpeed = kmphSpeed;
    }

    public CarPassDetails(long timestamp, String direction, double pixelSpeed, double kmphSpeed) {
        this(null, null, timestamp, direction, pixelSpeed, kmphSpeed);
    }

    public void setCapturedImage(BufferedImage capturedImage) {
        this.capturedImage = capturedImage;
    }

    public void setRegText(String regText) {
        this.regText = regText;
    }

    public BufferedImage getCapturedImage() {
        return capturedImage;
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
