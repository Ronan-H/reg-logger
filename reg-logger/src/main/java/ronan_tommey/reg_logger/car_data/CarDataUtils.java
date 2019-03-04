package ronan_tommey.reg_logger.car_data;

import ronan_tommey.reg_logger.image_processing.FrameTimeManager;
import ronan_tommey.reg_logger.image_processing.FrameUtils;

import java.awt.image.BufferedImage;
import java.util.List;

public class CarDataUtils {

    /**
     * Generated a CarData object from a black and white image
     * showing a car.
     * @param image Image to create a CarData object from
     * @return The generated CarData object
     */
    public static CarData generateCarData(BufferedImage image){
        // boolean array representation of the passed in image, where "true"
        // means the car covers that pixel, and "false" if it doesn't. array is
        // one dimension, with increasing indexes going left to right first, then
        // down, on the passed in image (ie. x = i % width, y = i / width)
        boolean[] movingPixels = FrameUtils.convertImageToBooleanArray(image);

        int imgWidth = image.getWidth();

        // variables to set and later load into a new CarData object
        int leftX = Integer.MAX_VALUE;
        int rightX = -1;
        int topY = Integer.MAX_VALUE;
        int bottomY = -1;

        // i values converted to image (x, y) for each pixel
        int x, y;

        for (int i = 0; i < movingPixels.length; ++i) {
            if (movingPixels[i]) {
                // pixels moving; update CarData variables
                x = i % imgWidth;
                y = i / imgWidth;

                if (x < leftX) {
                    leftX = x;
                }

                if (x > rightX) {
                    rightX = x;
                }

                if (y < topY) {
                    topY = y;
                }

                if (y > bottomY) {
                    bottomY = y;
                }
            }
        }

        return new CarData(leftX, rightX, topY, bottomY);
    }

    public static CarEstimate generateCarEstimate(CarDataSeries carDataSeries, FrameTimeManager frameTimeManager){
        return null;
    }
}
