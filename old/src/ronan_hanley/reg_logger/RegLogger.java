package ronan_hanley.reg_logger;

import net.coobird.thumbnailator.Thumbnails;
import ronan_hanley.reg_logger.remote_camera.RemoteCamera;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class RegLogger implements Runnable, RaspicamListener {
	public static final String VERSION = "2";

	public static long programStart = System.currentTimeMillis();

	private static final boolean USE_REMOTE_CAMERA = true;
	private static final boolean USE_DUMMY_CAMERA = false;

	// If true, will output frames, along with their movement frames. Deletes old frames (like a dashcam)
	private static final boolean OUTPUT_IMAGES = false;

	private MovementProcessor moveProc;
	private TriggerEstimator trigEstimator;
	private LatencyCalculator latencyCalc;
	private CarLocator carLoc;
	
	private long lastNikonCapture;
	private static final long minNsBetweenCapture = 10 * 1000000000L;
	
	private BufferedImage nextImage;
	private long frameDelay;
	
	private int frameNum;
	
	private CameraManager camera;
	
	private Dimension viewSize, scaledViewSize;
	
	private RaspividReader raspiReader;
	
	private boolean running;
	
	private int numFiles = 0;
	
	public static final int NIKON_DELAY = 475 * 1000000;
	public static final int MAX_ESTIMATE_BEFORE_CAPTURE = 85 * 1000000;

	double lastProcessTime = 0;
	
	@Override
	public void run() {
		long processStart = System.nanoTime();
		// ignore frames where camera is adjusting
		if (frameNum < 50 || nextImage == null) {
			if (nextImage == null) System.out.println("Image was null!");
			
			++frameNum;
			return;
		}
		
		final int imgBuffSize = 500;
		
		BufferedImage imageMovement;
		
		//System.out.printf("Processing image %d...%n", frameNum);
		//if (i % 20 == 0) System.out.printf("Took %d ms to capture a frame.%n", frameTime);
		nextImage = scaleImage(nextImage, scaledViewSize);
		
		
		long carlocStart = System.nanoTime();
		imageMovement = carLoc.processImage(nextImage);
		double msTaken = (System.nanoTime() - carlocStart) / 1000000d;
		//System.out.printf("Took %.2fms to find moving pixels.%n", msTaken);
		
		processImageMovement(imageMovement, frameNum);
		
		if (trigEstimator.estimateReady()) {
			System.out.println("Frame: " + frameNum);
			System.out.printf("Previous frame was processed in %.3fms.%n", lastProcessTime);
			long estimate = trigEstimator.getEstimate(latencyCalc, NIKON_DELAY);
			long nsWait = estimate - System.nanoTime();
			long nsSinceLastCapture = System.nanoTime() - lastNikonCapture;
			
			System.out.printf("Estimate for this frame: %dms%n", (estimate - System.nanoTime()) / 1000000);
			
			if (estimate >= (System.nanoTime() - NIKON_DELAY)
					&& nsSinceLastCapture >= minNsBetweenCapture
					// experimental: wait until the last moment to "lock in" the estimate
					&& (nsWait <= MAX_ESTIMATE_BEFORE_CAPTURE || trigEstimator.getCarDistFromEdge() < 70))  {
				//new Thread(new NikonCaptureThread(estimate, nikon, frameNum)).start();
				System.out.println("Locking in estimate.");
				new Thread() {
					@Override
					public void run() {
						camera.captureImageToFile(estimate, String.format("./nikon/nikon_%d.jpg", frameNum));
					}
				}.start();

				lastNikonCapture = estimate;
			}
		}

		if (OUTPUT_IMAGES) {
			if (frameNum % 3 == 0) {
				try {
					ImageIO.write(nextImage, "JPEG", new File(String.format("./images/test_%d.jpg", frameNum)));
					ImageIO.write(imageMovement, "PNG", new File(String.format("./images/test_diff_%d.png", frameNum))); ++numFiles;
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			if (frameNum % 20 == 0) {
				if (numFiles > imgBuffSize) {
					File[] files = new File("./images/").listFiles();
					int numToDel = files.length - imgBuffSize;

					if (numToDel > 0) {
						sortByNumber(files);

						for (int j = 0; j < numToDel; ++j) {
							if (files[j].getName().startsWith("test_")) {
								files[j].delete();
								--numFiles;
							}
						}
					}
				}
			}
		}
		
		//if (frameNum % 20 == 0) System.out.println("Delay: " + latencyCalc.getAverageDelay(10));
		
		latencyCalc.addDelay(frameDelay);
		++frameNum;
		
		long processEnd = System.nanoTime();
		long processTime = processEnd - processStart;
		double processTimeMs = processTime / 1000000d;
		//System.out.printf("Processed frame %d in %.3fms.%n", frameNum, processTimeMs);
		lastProcessTime = processTimeMs;
	}
	
	public void init() {
		System.out.printf("RegLogger version %s%n%n", VERSION);

		frameNum = 0;
		
		initCamera();
		
		lastNikonCapture = 0;
		
		double downsizeScale = 0.6;
		
		//viewSize = new Dimension(1785, 350);
		viewSize = new Dimension(600, 120);
		scaledViewSize = new Dimension((int)(viewSize.width * downsizeScale), (int)(viewSize.height * downsizeScale));
		
		raspiReader = new RaspividReader(viewSize.width, viewSize.height, this);
		new Thread(raspiReader).start();
		
		carLoc = new CarLocator(scaledViewSize.width, scaledViewSize.height);
		moveProc = null;
		latencyCalc = new LatencyCalculator(500);
		trigEstimator = new TriggerEstimator(scaledViewSize.width, scaledViewSize.height, 2, 4, 2, 266, 51);
		Scanner console = new Scanner(System.in);
		
		System.out.println("Starting...");
		
		int numImages = Integer.MAX_VALUE;
		
		//System.out.printf("Timing the capture of %d images...%n", numImages);
		
		// clear out images dir
		System.out.println("\nDeleting old images...");
		File[] files = new File("./images/").listFiles();
		for (File f : files) if (f.getName().startsWith("test_")) f.delete();
		System.out.println("Finished deleting old images.");
		
		//System.out.println("\nPress enter to begin taking images.");
		//console.nextLine();
		
		running = true;
		
		while (running) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
	
	private static BufferedImage scaleImage2(BufferedImage before, double scale) {
		int newWidth = (int)(before.getWidth() * scale);
		int newHeight = (int)(before.getHeight() * scale);
		
		try {
			return Thumbnails.of(before).size(newWidth, newHeight).asBufferedImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static BufferedImage scaleImage3(BufferedImage before, double scale) {
		int newWidth = (int)(before.getWidth() * scale);
		int newHeight = (int)(before.getHeight() * scale);
		
		// System.out.println("Before type: " + before.getType());
		
		BufferedImage after = new BufferedImage(newWidth, newHeight, before.getType());
		AffineTransform at = new AffineTransform();
		at.scale(scale, scale);
		AffineTransformOp scaleOp = 
		   new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		after = scaleOp.filter(before, after);
		
		return after;
	}
	
	private static void sortByNumber(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    int s = name.lastIndexOf('_')+1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch(Exception e) {
                    i = 0; // if filename does not match the format
                           // then default to 0
                }
                return i;
            }
        });
	}
	
	private void processImageMovement(BufferedImage img, int frameNum) {
		long nsStart = System.nanoTime();
		
		if (moveProc == null) {
			moveProc = new MovementProcessor(img.getWidth(), img.getHeight());
		}
		
		moveProc.feedImage(img);
		CarData data = moveProc.estimateCar(frameNum);
		trigEstimator.addCarData(data);
		
		double msTaken = (System.nanoTime() - nsStart) / 1000000d;
		if (msTaken > 35) System.out.printf("Took %.2fms to process image movement.%n", msTaken);
	}
	
	private void initCamera() {
		if (USE_DUMMY_CAMERA) {
			camera = new DummyCameraManager();
			return;
		}

		if (USE_REMOTE_CAMERA) {
			camera = new RemoteCameraManager(RemoteCamera.PORT);
		}

		/*
		nikon = null;
		
		final CameraList cl = new CameraList();
		System.out.println("Cameras: " + cl);
		CameraUtils.closeQuietly(cl);
		nikon = new Camera();
		nikon.initialize();
		*/
		//CameraUtils.closeQuietly(c);
	}
	
	public synchronized void processNextImage(BufferedImage nextImage, long frameDelay) {}
	
	@Override
	public void nextImageRetrieved(BufferedImage image, long frameDelay) {
		this.nextImage = image;
		this.frameDelay = frameDelay;
		
		//System.out.println("Frame delay: " + frameDelay);
		
		new Thread(this).start();
	}
	
	public static void main(String[] args) {
		new RegLogger().init();
	}
	
}
