package ronan_hanley.reg_logger.test;

import ronan_hanley.reg_logger.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TestTriggerEstimator {

	public static void main(String[] args) throws IOException {
		System.out.println("Starting...");
		
		String inputFolder = "/home/ronan/Documents/Programming/RegLogger/output/moveproc/temp2";
		File[] inputFiles = new File(inputFolder).listFiles();
		
		sortByNumber(inputFiles);
		
		MovementProcessor moveProc = null;
		TriggerEstimator trigEstimator = null;
		List<CarData> carDataList = new ArrayList<CarData>();
		LatencyCalculator latencyCalc = new LatencyCalculator(200);
		CarDataOverlay overlay;
		int lastNumber = -1;
		boolean firstFrame = true;

		for (int i = 0; i < 50; ++i) {
			latencyCalc.addDelay(40000000);
		}
		
		long frameTimeStart;
		for (File f : inputFiles) {
			frameTimeStart = System.nanoTime();
			
			String name = f.getName();
			int s = name.lastIndexOf('_')+1;
            int e = name.lastIndexOf('.');
            int number = Integer.parseInt(name.substring(s, e));

            int frameTimeout = 2;

            System.out.println("Frame num: " + number);
            
            BufferedImage img = ImageIO.read(f);
            
            BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

            if (trigEstimator == null) {
            	trigEstimator = new TriggerEstimator(img.getWidth(), img.getHeight(), frameTimeout, 5, 2, 272, 41);
            }
            
            Graphics g = convertedImg.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            
			if (moveProc == null) {
				moveProc = new MovementProcessor(convertedImg.getWidth(), convertedImg.getHeight());
			}

			if (number - lastNumber - 1 >= frameTimeout && !firstFrame) {
				trigEstimator.clearCarDataList();
			}

			moveProc.feedImage(convertedImg);
			CarData carData = moveProc.estimateCar(number);
			
			if (carData == null) System.out.println("NULL CAR DATA");
			
			trigEstimator.addCarData(carData);
			
			
			if (trigEstimator.estimateReady()) {
				long estimate = trigEstimator.getEstimate(latencyCalc, RegLogger.NIKON_DELAY);
				//System.out.printf("Photo trigger wait estimate: %.3f sec%n%n", (estimate - System.nanoTime()) / 1000000000d);
			}
			else {
				System.out.println("\n\nNo estimate ready this frame.\n\n");
			}
			
			long frameTimeTaken = System.nanoTime() - frameTimeStart;
			double frameTimeTakenMs = frameTimeTaken / 1000000d;
			
			System.out.printf("Processed frame in %.2fms.%n",  frameTimeTakenMs);

			if (carData != null) {
				overlay = new CarDataOverlay(carData, img);
				overlay.saveToFile(name);
			}

			lastNumber = number;
			firstFrame = false;
		}
		
		System.out.println("Finished.");
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
	
}
