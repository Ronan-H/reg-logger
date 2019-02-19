package ronan_hanley.reg_logger.test;

import ronan_hanley.reg_logger.CarData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CarDataOverlay {
    private CarData carData;
    private BufferedImage image;

    public CarDataOverlay(CarData carData, BufferedImage image) {
        this.carData = carData;
        this.image = image;

        drawOverlay();
    }

    public void drawOverlay() {
        Graphics g = image.getGraphics();
        // draw red line for leftX
        g.setColor(getTranparentColor(Color.RED));
        g.drawLine(carData.getLeftX() - 1, 0, carData.getLeftX() - 1, image.getHeight());

        // draw red dot at center of car
        g.setColor(getTranparentColor(Color.red));
        g.fillRect(carData.getX(), carData.getY(), 1, 1);

        // draw green line for rightX
        g.setColor(getTranparentColor(Color.GREEN));
        g.drawLine(carData.getRightX() + 1, 0, carData.getRightX() + 1, image.getHeight());

        // draw cyan line for topY
        g.setColor(getTranparentColor(Color.CYAN));
        g.drawLine(0, carData.getTopY() - 1, image.getWidth(), carData.getTopY() - 1);

        // draw magenta line for topY
        g.setColor(getTranparentColor(Color.MAGENTA));
        g.drawLine(0, carData.getBottomY() + 1, image.getWidth(), carData.getBottomY() + 1);

        // draw orange line for true front (no shadow)
        g.setColor(getTranparentColor(Color.ORANGE));
        g.drawLine(carData.getNoShadowFront() - 1, 0, carData.getNoShadowFront() - 1, image.getHeight());

        g.dispose();
    }

    public void saveToFile(String filename) {
        String path = String.format("%s%s", "../output/CarDataOverlay/", filename);
        try {
            ImageIO.write(image, "PNG", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Color getTranparentColor(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 180);
    }

}
