package ronan_tommey.reg_logger.car_data;

import ronan_tommey.reg_logger.image_processing.FrameUtils;

import java.awt.image.BufferedImage;

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
        double distance;
        CarData first = carDataSeries.getFirst();
        CarData last = carDataSeries.getLast();
        // calculate distances from both side of the car across all frames
        double rightEndDistance = last.getRightX() - first.getRightX();
        double leftEndDistance = last.getLeftX() - first.getRightX();

        // use whichever value is bigger, so if the car is emergin from the side of the screen,
        // the appropriate value will be used
        if (Math.abs(rightEndDistance) > Math.abs(leftEndDistance)) {
            distance = rightEndDistance;
        }
        else {
            distance = leftEndDistance;
        }

        // positive distance indicates that the car is going right
        // (otherwise, it is going left)
        boolean goingRight = (distance > 0);

        // calculate the speed of the car, in pixels per frame
        double pixelSpeed = distance / carDataSeries.size();

        // TODO: calculate kmph speed
        CarEstimate carEstimate = new CarEstimate(goingRight, pixelSpeed, 0);

        return carEstimate;
    }
}
