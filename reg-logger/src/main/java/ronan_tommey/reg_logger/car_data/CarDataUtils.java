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

    public static CarEstimate generateCarEstimate(CarDataSeries carDataSeries, int estimateFrames, FrameTimeManager frameTimeManager) {
        double distance;
        // CarData objects to base the speed estimate on
        CarData first = carDataSeries.getNthFromEnd(estimateFrames - 1);
        CarData last = carDataSeries.getLast();

        // use the front of the car when calculating the distance
        if (carDataSeries.isGoingRight()) {
            distance = last.getRightX() - first.getRightX();
        }
        else {
            distance = last.getLeftX() - first.getLeftX();
        }

        // calculate the speed of the car, in pixels per frame
        double pixelSpeed = distance / (estimateFrames  - 1);

        // TODO: calculate kmph speed

        return new CarEstimate(pixelSpeed, 0);
    }

    public static int getRegPosEstimate(CarEstimate carEstimate, CarData carData, boolean[] movingPixels, int imageWidth) {
        final double shadowCuttoffRatio = 0.4;

        int carHeight = carData.getBottomY() - carData.getTopY();

        int colHeight;

        for (int scanX = carData.getLeftX(); scanX < carData.getRightX(); ++scanX) {
            for (int scanY = carData.getTopY(); scanY < carData.getBottomY(); ++scanY) {
                if (movingPixels[scanY * imageWidth + scanX]) {
                    colHeight = carHeight - (scanY - carData.getTopY());

                    if ((double) colHeight / carHeight > shadowCuttoffRatio) {
                        return scanX;
                    }
                }
            }
        }

        System.err.println("Error while estimating car reg pos: var was never set");
        return -1;
    }
}
