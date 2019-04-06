package ronan_tommey.reg_logger.testing_tools.frame_overlay;

import ronan_tommey.reg_logger.reg_logging.CarPassDatabase;
import ronan_tommey.reg_logger.reg_logging.CarPassDetails;

import java.awt.image.BufferedImage;
import java.util.Calendar;

public class CarPassDatabaseTest {
    public static void main(String[] args){
        CarPassDetails CAR =  new CarPassDetails(null, "17-G-2836",Calendar.getInstance().getTimeInMillis(),"right",42,69.69);
        new CarPassDatabase().logPass(CAR);
    }
}