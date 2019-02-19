package ronan_hanley.reg_logger;

import java.awt.image.BufferedImage;

public class DummyCameraManager extends CameraManager {

    @Override
    public BufferedImage captureImage(long captureTime) {
        return null;
    }

    @Override
    public void captureImageToFile(long captureTime, String path) {}

}
