package ronan_tommey.reg_logger.car_data;

import java.util.ArrayList;
import java.util.List;

public class CarDataSeries {
    private int imageWidth;
    private List<CarData> carDataList;

    public CarDataSeries(int imageWidth) {
        this.imageWidth = imageWidth;
        carDataList = new ArrayList<>();
    }

    public void addNextCarData(CarData carData){
        carDataList.add(carData);
    }

    public CarData getFirst() {
        return carDataList.get(0);
    }

    public CarData getLast() {
        return carDataList.get(carDataList.size() - 1);
    }

    public CarData getNthFromEnd(int n) {
        return carDataList.get(carDataList.size() - n - 1);
    }

    public boolean isGoingRight() {
        return getFirst().getLeftX() < (imageWidth / 2);
    }

    public int size() {
        return carDataList.size();
    }
}
