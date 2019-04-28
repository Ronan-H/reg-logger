package ronan_tommey.reg_logger.image_processing;

import java.awt.image.BufferedImage;

/**
 * Interface used to listen to PiCamFrameStreamer for new images, using the
 * observer design pattern
 */
public interface PiCamFrameListener {
   /**
    * Should be called by PiCamFrameStreamer when a new image is read in
    * @param image The image that was read
    * @param delta The time since the previous image was read, in nanoseconds
    */
   void onFrameRead(BufferedImage image, long delta);
}
