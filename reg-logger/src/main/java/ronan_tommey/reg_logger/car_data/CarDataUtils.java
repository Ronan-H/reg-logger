package ronan_tommey.reg_logger.car_data;

import ronan_tommey.reg_logger.Image_Processing.FrameTimeManager;

import java.awt.image.BufferedImage;
import java.util.List;

public class CarDataUtils {

    public static CarData generateCarData(BufferedImage image){

        CarData temp = new CarData();
        return  temp;
    }

    public static CarEstimate generateCarEstimate(List<CarData> carData, FrameTimeManager FrameTimeMan){

        CarEstimate temp = new CarEstimate();
        return temp;
    }

}
