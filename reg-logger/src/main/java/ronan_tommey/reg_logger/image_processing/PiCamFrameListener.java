package ronan_tommey.reg_logger.image_processing;

import java.awt.image.BufferedImage;

public interface PiCamFrameListener {

   void onFrameRead(BufferedImage image, long delta);
}
