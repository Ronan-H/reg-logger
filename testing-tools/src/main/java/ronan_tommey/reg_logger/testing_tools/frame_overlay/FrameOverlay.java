package ronan_tommey.reg_logger.testing_tools.frame_overlay;

import ronan_tommey.reg_logger.car_data.CarData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FrameOverlay {
    private CarData carData;
    private BufferedImage image;

    public FrameOverlay(CarData carData, BufferedImage image) {
        this.carData = carData;
        this.image = image;
    }

    public void drawOverlay() {
        Graphics g = image.getGraphics();

        // draw red line for leftX
        g.setColor(getTranparentColor(Color.RED));
        g.drawLine(carData.getLeftX(), 0, carData.getLeftX(), image.getHeight());

        // draw green line for rightX
        g.setColor(getTranparentColor(Color.GREEN));
        g.drawLine(carData.getRightX(), 0, carData.getRightX(), image.getHeight());

        // draw cyan line for topY
        g.setColor(getTranparentColor(Color.CYAN));
        g.drawLine(0, carData.getTopY(), image.getWidth(), carData.getTopY());

        // draw magenta line for topY
        g.setColor(getTranparentColor(Color.MAGENTA));
        g.drawLine(0, carData.getBottomY(), image.getWidth(), carData.getBottomY());

        g.dispose();
    }

    public void saveToFile(String outputDir, int fileNum) {
        String path = String.format("%sframe-overlay-%d.png", outputDir, fileNum);
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
