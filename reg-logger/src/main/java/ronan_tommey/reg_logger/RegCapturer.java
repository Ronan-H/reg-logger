package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.image_processing.PiCamFrameListener;

import java.awt.image.BufferedImage;

public class RegCapturer implements PiCamFrameListener{
    public void onFrameRead(BufferedImage image, long delta) {
    }
}
