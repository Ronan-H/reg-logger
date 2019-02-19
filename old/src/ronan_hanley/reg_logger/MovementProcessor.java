package ronan_hanley.reg_logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

public class MovementProcessor {
	private int imgWidth, imgHeight;
	private boolean[] movingPixels;
	
	private boolean[] markedPixels;
	private int numMarked;
	
	private int oldCarX = 0;
	private int oldCarY = 0;
	
	private static final int[][] offsets = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
	
	public MovementProcessor(int imgWidth, int imgHeight) {
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
	}
	
	public void feedImage(BufferedImage imageMovement) {
		int[] pixels = ((DataBufferInt) imageMovement.getRaster().getDataBuffer()).getData();
		movingPixels = new boolean[pixels.length];
		
		for (int i = 0; i < pixels.length; ++i) {
			movingPixels[i] = (pixels[i] != 0);
		}
		
		removeNoise(125);
	}
	
	public CarData estimateCar(int frameNum) {
		CarData carData = null;
		
		int leftX = Integer.MAX_VALUE;
		int rightX = Integer.MIN_VALUE;
		int topY = Integer.MAX_VALUE;
		int bottomY = 0;
		int leftY = 0;
		int rightY = 0;
		int noShadowFront;

		// get mean moving pixel location
		int totX = 0;
		int totY = 0;

		int totalMoving = 0;

		int x, y;

		for (int i = 0; i < movingPixels.length; ++i) {
			if (movingPixels[i]) {
				++totalMoving;

				x = i % imgWidth;
				y = i / imgWidth;

				if (x < leftX) {
					leftX = x;
					leftY = y;
				}

				if (x > rightX) {
					rightX = x;
					rightY = y;
				}

				if (y < topY) {
					topY = y;
				}

				if (y > bottomY) {
					bottomY = y;
				}

				totX += x;
				totY += y;
			}
		}

		int carHeight = bottomY - topY;
		noShadowFront = leftX;

		// find "true" front of car (remove the shadow)
		final double maxShadow = 0.4;
		int colHeight;
		int lastTop = 0;

		outerLoop:
		for (int scanX = leftX; scanX < rightX; ++scanX) {
			for (int scanY = topY; scanY < bottomY; ++scanY) {
				if (movingPixels[scanY * imgWidth + scanX]) {
					colHeight = carHeight - (scanY - topY);

					if ((double) colHeight / carHeight > maxShadow) {
						noShadowFront = scanX;
						break outerLoop;
					}
				}
			}
		}

		if (totalMoving >= 250) {
			int avgX = (int)Math.round((double)totX / totalMoving);
			int avgY = (int)Math.round((double)totY / totalMoving);

			int changeX = avgX - oldCarX;
			int changeY = avgY - oldCarY;

			//System.out.printf("Frame %d: Car in frame.%nx: %d%ny: %d%nxChange: %d%nyChange: %d%n%n", frameNum, avgX, avgY, changeX, changeY);

			if (oldCarX != 0 && oldCarY != 0) {
				carData = new CarData(imgWidth, avgX, avgY,
						changeX, changeY,
						totalMoving,
						leftX, leftY,
						rightX, rightY,
						topY, bottomY,
						noShadowFront);
			}

			oldCarX = avgX;
			oldCarY = avgY;

			outputImage(frameNum);
		}
		
		return carData;
	}
	
	private void removeNoise(int minConnected) {
		int i;
		
		markedPixels = new boolean[movingPixels.length];
		for (i = 0; i < movingPixels.length; ++i) {
			markedPixels[i] = movingPixels[i];
		}
		
		for (i = 0; i < markedPixels.length; ++i) {
			if (markedPixels[i]) {
				int numConnected = findConnected(i);
				
				if (numConnected < minConnected) {
					removeConnected(i);
				}
			}
		}
	}
	
	private int findConnected(int index) {
		numMarked = 0;
		
		recFindConnected(index);
		
		return numMarked;
	}
	
	private void recFindConnected(int index) {
		int offX, offY;
		int newX, newY;
		int newIndex;
		
		int x = index % imgWidth;
		int y = index / imgWidth;
		
		markedPixels[index] = false;
		++numMarked;
		
		// loop over neighbours
		for (int off = 0; off < offsets.length; ++off) {
			offY = offsets[off][0];
			offX = offsets[off][1];
			
			newX = x + offX;
			newY = y + offY;
			
			newIndex = (newY * imgWidth) + newX;
			
			if (newIndex >= 0 && newIndex < markedPixels.length && markedPixels[newIndex]) {
				recFindConnected(newIndex);
			}
		}
	}
	
	private void removeConnected(int index) {
		int offX, offY;
		int newX, newY;
		int newIndex;
		
		int x = index % imgWidth;
		int y = index / imgWidth;
		
		movingPixels[index] = false;
		
		// loop over neighbours
		for (int off = 0; off < offsets.length; ++off) {
			offY = offsets[off][0];
			offX = offsets[off][1];
			
			newX = x + offX;
			newY = y + offY;
			
			newIndex = (newY * imgWidth) + newX;
			
			if (newIndex >= 0 && newIndex < movingPixels.length && movingPixels[newIndex]) {
				removeConnected(newIndex);
			}
		}
	}
	
	public void outputImage(int frameNum) {
		new Thread() {
			public void run() {
				// only output moveproc images if there's a folder for them
				// this is so that this class won't output more moveproc images in testing
				if (new File("./moveproc/").exists()) {
					BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
					int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

					for (int i = 0; i < movingPixels.length; ++i) {
						if (movingPixels[i]) pixels[i] = 0xFFFFFF;
					}

					try {
						ImageIO.write(image, "PNG", new File(String.format("./moveproc/test_moveproc_%d.png", frameNum)));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
}
