package ronan_hanley.reg_logger;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CameraViewFinder implements RaspicamListener {
	
	public void go() {
		Dimension viewSize = new Dimension(640, 480);
		double downsizeScale = 0.5;
		Dimension scaledViewSize = new Dimension((int)(viewSize.width * downsizeScale), (int)(viewSize.height * downsizeScale));
		
		RaspividReader raspiReader = new RaspividReader(600, 150, this);
		new Thread(raspiReader).start();
		
		int numImages = Integer.MAX_VALUE;
		
		/*
		System.out.print("Doing inital sleep...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done.");
		*/
		
		BufferedImage image;
		for (int i = 0; i < numImages; ++i) {
			image = raspiReader.getLastImage();
			if (image != null) {
				System.out.printf("Taking image %d...%n", i);
				//image = scaleImage(image, scaledViewSize);
				
				try {
					ImageIO.write(image, "JPEG", new File("/var/www/html/view.jpg"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println("Sleeping...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("Image was null.");
			}
		}
	}
	
	
	public static void main(String[] args) {
		new CameraViewFinder().go();
	}
	
	public static BufferedImage scaleImage(BufferedImage before, Dimension newSize) {
		int newWidth = newSize.width;
		int newHeight = newSize.height;
		
		BufferedImage scaled = new BufferedImage(newWidth, newHeight, before.getType());

		Graphics2D g = (Graphics2D)scaled.createGraphics();
		
		g.setRenderingHint(
	             RenderingHints.KEY_ANTIALIASING,
	             RenderingHints.VALUE_ANTIALIAS_OFF);
		
		g.setRenderingHint(
	             RenderingHints.KEY_INTERPOLATION,
	             RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		
		g.drawImage(before, 0, 0, newWidth, newHeight, null);
		g.dispose();
		
		return scaled;
	}

	@Override
	public void nextImageRetrieved(BufferedImage image, long captureTime) {
		// TODO Auto-generated method stub
		
	}
	
}
