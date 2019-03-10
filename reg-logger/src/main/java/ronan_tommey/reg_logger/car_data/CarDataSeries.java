package ronan_tommey.reg_logger.car_data;

import java.util.ArrayList;
import java.util.List;

public class CarDataSeries {
    private List<CarData> carDataList;

    public CarDataSeries() {
        carDataList = new ArrayList<CarData>();
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

    public int size() {
        return carDataList.size();
    }
}
