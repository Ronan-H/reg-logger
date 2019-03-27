package ronan_tommey.reg_logger.testing_tools.frame_overlay;

import ronan_tommey.reg_logger.reg_logging.CarPassDetails;
import ronan_tommey.reg_logger.reg_logging.CarPassFileSystem;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class TestCarrPassFileSystem {

    public void go() throws IOException {
        BufferedImage[] frames = Utils.readFrameImagesFolder("./res/car-data-testing/sample-frames-1/input");

        CarPassFileSystem carPassFileSystem = new CarPassFileSystem("./res/log-system-test");

        CarPassDetails carPassDetails = new CarPassDetails(
                frames[0],
                "09-G-12345",
                Calendar.getInstance().getTimeInMillis(),
                "Right",
                8.2,
                42);

        carPassFileSystem.logPass(carPassDetails);
    }

    public static void main(String[] args) {
        try {
            new TestCarrPassFileSystem().go();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
