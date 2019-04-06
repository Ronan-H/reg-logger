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
        if(FrameUtils.countMoving(movingPixels) == 0) {
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

        // construct CarData object from values
        return new CarData(leftX, rightX, topY, bottomY);
    }

    /**
     * Generates a CarEstimate object based on a series of CarData objects (CarDataSeries),
     * and the time delta values between them (FrameTimeManager).
     * @param carDataSeries The series of CarData objects to base the estimate on
     * @param estimateFrames The number of frames to use for the estimate (eg. 4 meaning the last 4 frames only)
     * @param frameTimeManager The FrameTimeManager class that stored the frame time delta values for theassociated carDataSeries
     * @param useHeuristic If set to true, uses a heuristic algorithm to determine the location of the left side of the car,
     *                     accounting for shadows. If set to false, uses the car's leftmost/rightmost pixel.
     * @return The generated CarEstimate object
     */
    public static CarEstimate generateCarEstimate(CarDataSeries carDataSeries, int estimateFrames, FrameTimeManager frameTimeManager, boolean useHeuristic) {
        // CarData objects to base the speed estimate on
        CarData first = carDataSeries.getNthFromEnd(estimateFrames - 1);
        CarData last = carDataSeries.getLast();

        double distance;

        // calculate the distance the car travelled
        if (useHeuristic) {
            // use the heuristic shadow removing algorithm to calculate the cars distance
            distance = last.getRegPosEstimate() - first.getRegPosEstimate();
        }
        else {
            // use the very front of the car when calculating the distance
            if (carDataSeries.isGoingRight()) {
                distance = last.getRightX() - first.getRightX();
            }
            else {
                distance = last.getLeftX() - first.getLeftX();
            }
        }

        // calculate the speed of the car, in pixels per frame
        double pixelSpeed = distance / (estimateFrames  - 1);

        // TODO: calculate kmph speed

        return new CarEstimate(pixelSpeed, 0);
    }

    /**
     * Updates a CarData object to include an estimate of where the registration plate is
     * (by removing any trailing shadow)
     * @param carData The CarData object to update
     * @param movingPixels The array indicating movement for the frame at a given index
     * @param imageWidth The width of the frame
     */
    public static void addRegPosEstimate(CarData carData, boolean[] movingPixels, int imageWidth) {
        final double shadowCuttoffRatio = 0.4;

        int carHeight = carData.getBottomY() - carData.getTopY();

        int colHeight;

        for (int scanX = carData.getLeftX(); scanX < carData.getRightX(); ++scanX) {
            for (int scanY = carData.getTopY(); scanY < carData.getBottomY(); ++scanY) {
                if (movingPixels[scanY * imageWidth + scanX]) {
                    colHeight = carHeight - (scanY - carData.getTopY());

                    if ((double) colHeight / carHeight > shadowCuttoffRatio) {
                        carData.setRegPosEstimate(scanX);
                        return;
                    }
                }
            }
        }
    }
}
