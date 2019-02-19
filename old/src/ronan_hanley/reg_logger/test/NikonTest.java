package ronan_hanley.reg_logger.test;

import java.util.Scanner;

import org.gphoto2.Camera;
import org.gphoto2.CameraList;
import org.gphoto2.CameraUtils;

import ronan_hanley.reg_logger.RegLogger;
import ronan_hanley.reg_logger.NikonCaptureThread;

public class NikonTest {
	
	public static void main(String[] args) {
		final CameraList cl = new CameraList();
		System.out.println("Cameras: " + cl);
		CameraUtils.closeQuietly(cl);
		final Camera c = new Camera();
		c.initialize();
		
		System.out.println("Press enter to capture an image");
		
		Scanner console = new Scanner(System.in);
		console.nextLine();
		
		new Thread(new NikonCaptureThread(1000000000 - RegLogger.NIKON_DELAY + System.nanoTime(), c, 0)).run();
		
		/*
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		
		/*
		Random rand = new Random();
		double sqrt;
		long startTime = System.nanoTime();
		long endTime = startTime + (10 * 1000000000L);
		while (System.nanoTime() < endTime) {
			for (int i = 0; i < 1000; ++i) {
				sqrt = (Math.sqrt(rand.nextInt()));
				rand.nextInt(Math.abs((int)sqrt) + 10);
			}
		}
		*/
		
		/*
		long timerStart = System.nanoTime();
		final CameraFile cf2 = c.captureImage();
		long nsTaken = System.nanoTime() - timerStart;
		double ms = nsTaken / 1000000d;
		
		System.out.printf("Image took %.2f ms to capture. Exiting...%n", ms);
		
		cf2.save(new File("./nikon/nikon.jpg").getAbsolutePath());
		CameraUtils.closeQuietly(cf2);
		*/
		
		CameraUtils.closeQuietly(c);
	}
	
}
