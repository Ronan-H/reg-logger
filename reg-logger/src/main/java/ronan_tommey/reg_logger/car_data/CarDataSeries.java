package ronan_tommey.reg_logger.car_data;

import java.util.ArrayList;
import java.util.List;

/**
 * CarDataSeries stores the series of frames from when a car passes into a list
 */
public class CarDataSeries {
    private int imageWidth;
    //List of frames of singular car passing
    private List<CarData> carDataList;

    public CarDataSeries(int imageWidth) {
        this.imageWidth = imageWidth;
        carDataList = new ArrayList<>();
    }

    /**
     * Adds the following frame of the car passing into the list
     * @param carData The object with information about the car in the current frame
     */
    public void addNextCarData(CarData carData){
        carDataList.add(carData);
    }

    /**
     *  Gets the first frame from the list of frames
     */
    public CarData getFirst() {
        return carDataList.get(0);
    }

    /**
     *  Gets the last frame from the list of frames
     */
    public CarData getLast() {
        return carDataList.get(carDataList.size() - 1);
    }

    /**
     * Gets the nth frame where n is the passed in integer
     * @param n The number of the frame in the list required
     */
    public CarData getNthFromEnd(int n) {
        return carDataList.get(carDataList.size() - n - 1);
    }

    /**
     * Checks the list of frames to determine which direction the car is travelling towards
     * @return Boolean variable indicating if thew car is travelling right
     */
    public boolean isGoingRight() {
        return getFirst().getLeftX() < (imageWidth / 2);
    }

    public int size() {
        return carDataList.size();
    }
}
