package ronan_hanley.reg_logger;

import java.io.File;

import org.gphoto2.Camera;
import org.gphoto2.CameraFile;
import org.gphoto2.CameraUtils;

public class NikonCaptureThread implements Runnable {
	private long captureTime;
	private Camera nikon;
	private int frameNum;
	
	public NikonCaptureThread(long captureTime, Camera nikon, int frameNum) {
		this.captureTime = captureTime;
		this.nikon = nikon;
		this.frameNum = frameNum;
	}
	
	public void run() {
		final CameraFile cf2;
		int nsSleep = (int)(captureTime - System.nanoTime());
		
		System.out.printf("Sleeping for %d ns before capturing...%n", nsSleep);
		
		if (nsSleep > 0) {
			try {
				Thread.sleep(nsSleep / 1000000, nsSleep % 1000000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Capturing...");
		
		cf2 = nikon.captureImage();
		
		String path = String.format("./nikon/nikon_%d.jpg", frameNum);
		
		cf2.save(new File(String.format(path)).getAbsolutePath());
		CameraUtils.closeQuietly(cf2);
		
		System.out.println("Image captured and saved as " + path);
		
	}
	
}
