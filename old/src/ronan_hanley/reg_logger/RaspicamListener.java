package ronan_hanley.reg_logger;

import java.awt.image.BufferedImage;

public interface RaspicamListener {
	
	public void nextImageRetrieved(BufferedImage image, long frameDelay);
	
}
