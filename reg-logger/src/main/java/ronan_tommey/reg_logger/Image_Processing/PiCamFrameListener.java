package ronan_tommey.reg_logger.Image_Processing;

import java.awt.image.BufferedImage;

public interface PiCamFrameListener {

    void onFrameRead(BufferedImage image, long delta);
}
