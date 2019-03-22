package ronan_tommey.reg_logger.car_data;

import ronan_tommey.reg_logger.image_processing.FrameUtils;

import java.awt.image.BufferedImage;

public class CarDataUtils {

    /**
     * Generated a CarData object from a black and white image
     * showing a car.
     * @param movingPixels Image to create a CarData object from
     * @return The generated CarData object
     */
    public static CarData generateCarData(boolean[] movingPixels, int imageWidth){
        if(FrameUtils.countMoving(movingPixels) == 0)
        {
            return null;
        }

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
                x = i % imageWidth;
                y = i / imageWidth;

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
        double leftEndDistance = last.getLeftX() - first.getLeftX();

        // figure out car's direction based on speed (positive/negative)
        // use whichever value is bigger, so if the car is emerging from the side of the screen,
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

        // ensure the distance from the front of the car is being used instead of the back
        distance = (goingRight ? rightEndDistance : leftEndDistance);

        // calculate the speed of the car, in pixels per frame
        double pixelSpeed = distance / (carDataSeries.size() - 1);

        // TODO: calculate kmph speed

        CarEstimate carEstimate = new CarEstimate(goingRight, pixelSpeed, 0);

        return carEstimate;
    }
}
