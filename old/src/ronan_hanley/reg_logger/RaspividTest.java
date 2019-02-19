package ronan_hanley.reg_logger;

import java.awt.image.BufferedImage;

public class RaspividTest implements RaspicamListener {
	private int imgCounter = 0;
	private long lastTime = System.nanoTime();
	
	public void go() {
		RaspividReader mjpeg = new RaspividReader(800, 300, this);
		new Thread(mjpeg).start();
	}
	
	@Override
	public void nextImageRetrieved(BufferedImage image, long captureTime) {
		/*
		try {
			ImageIO.write(image, "JPEG", new File(String.format("./MJPEG/MJPEG_2_%d.jpg", imgCounter)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
		imgCounter++;
		
		if (imgCounter % 25 == 0) {
			System.out.println("NS Gap: " + (System.nanoTime() - lastTime));
		}
		
		lastTime = captureTime;
	}
	
	public static void main(String[] args) {
		new RaspividTest().go();
	}
}
