package ronan_hanley.reg_logger;

import java.util.ArrayList;
import java.util.List;

public class TriggerEstimator {
	private List<CarData> carDataList;
	private int frameWidth;
	private int frameHeight;
	private int frameTimeout;
	private int framesSinceCar;
	private int estimateCarFrames;
	private int minFramesInFrame;
	private int takePosX;
	private int takePosY;
	private boolean carSideFrom; // left = false, right = true
	private int firstInFrameNum;

	// perpendicular distance from the camera to the road
	private static final double distCamToRoad = 13.8;
	// image width, in meters
	private static final double imageWidthMeters = 20;
	private final double pixelsPerMeter;
	
	// minimum amount of pixels the left/rightmost side of a car has moved from it's original position
	// before it's seen as "in frame"
	private static final int minDistBeforeInFrame = 35;

	// x position of the car in the most recent frame
	private int currentCarX;

	public TriggerEstimator(int frameWidth, int frameHeight, int frameTimeout, int minCarFrames, int minFramesInFrame, int takePosX, int takePosY) {
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.frameTimeout = frameTimeout;
		this.estimateCarFrames = minCarFrames;
		this.minFramesInFrame = minFramesInFrame;
		this.takePosX = takePosX;
		this.takePosY = takePosY;
		this.takePosX = (int)Math.round(adjustForParallax(frameWidth, takePosX));
		
		pixelsPerMeter = frameWidth / imageWidthMeters;
		
		carDataList = new ArrayList<CarData>();
		framesSinceCar = 0;
	}
	
	public void addCarData(CarData carData) {
		if (carData == null) {
			++framesSinceCar;
			
			if (framesSinceCar >= frameTimeout && carDataList.size() > 0) {
				clearCarDataList();
			}
		}
		else {
			if (carDataList.size() == 0) {
				// find which side the car is coming from
				carSideFrom = carData.getX() > (frameWidth / 2);
				
				System.out.printf("Car approaching from: %s side%n", (carSideFrom ? "Right" : "Left"));
			}
			
			carDataList.add(carData);
			framesSinceCar = 0;
			currentCarX = (carSideFrom ? carData.getLeftX() : carData.getRightX());
		}
	}
	
	public boolean estimateReady() {
		int numInFrame = 0;
		boolean firstFound = false;
		if (carDataList.size() >= estimateCarFrames) {
			CarData first = carDataList.get(0);
			
			for (int i = 1; i < carDataList.size(); ++i) {
				CarData data = carDataList.get(i);
				
				int distFromSide;
				if (carSideFrom) { 
					// car from right side
					distFromSide = Math.abs(first.getRightX() - data.getRightX());
				}
				else {
					// car from left side
					distFromSide = data.getLeftX() - first.getLeftX();
				}
				
				if (distFromSide >= minDistBeforeInFrame) {
					if (!firstFound) {
						firstFound = true;
						firstInFrameNum = i;
					}
					
					++numInFrame;
				}
			}
			
			// TODO change back so both car sides are used
			return firstFound && !carSideFrom && numInFrame >= minFramesInFrame;
		}
		
		return false;
	}
	
	// assumes estimateReady()
	public long getEstimate(LatencyCalculator latencyCalc, int triggerLatency) {
		int totXVel = 0;
		int totYVel = 0;
		double avgXVel = 0;
		double avgYVel = 0;
		int i, j, k;
		
		int count;
		
		int totalXDiff;
		int totalYDiff;
		int totalDiff;
		
		int lowestXDiff = Integer.MAX_VALUE;
		int lowestYDiff = Integer.MAX_VALUE;
		int lowestTotalDiff = Integer.MAX_VALUE;
		
		int lowestDiffIndex = 0;
		
		int[][] vels;
		
		CarData d, prev;
		
		// car points
		int[][] p = carDataList.get(0).getPoints();
		double[] avgXVels = new double[p.length];
		double[] avgYVels = new double[p.length];
		
		/*
		for (i = 0; i < p.length; ++i) {
			totalXDiff = 0;
			totalYDiff = 0;
			totalDiff = 0;
			
			//System.out.printf("%nFor point type %d...%n", i);
			
			vels = new int[carDataList.size() - 1][2];
			for (j = 1; j < carDataList.size(); ++j) {
				int adjIndex = (j - 1);
				d = carDataList.get(j);
				prev = carDataList.get(j - 1);
				
				for (k = 0; k < 2; ++k) {
					vels[adjIndex][k] = d.getPoints()[i][k] - prev.getPoints()[i][k];
					
					if (k == 0) {
						//System.out.printf("\tDiff %d: %d%n", (adjIndex), vels[adjIndex][k]);
					}
				}
			}
			
			//System.out.println();
			
			for (j = 0; j < vels.length; ++j) {
				//System.out.println("actual diff: " + Math.abs(vels[j][0] - vels[j - 1][0]));
				totalXDiff += vels[j][0];
				totalYDiff += vels[j][1];
				
				totalDiff += totalXDiff;
				totalDiff += totalYDiff;
			}
			
			//System.out.println("totalXDiff: " + totalXDiff);
			
			if (totalDiff < lowestTotalDiff) {
				lowestTotalDiff = totalDiff;
				lowestDiffIndex = i;
			}
			
			avgXVels[i] = (double)totalXDiff / vels.length;
			avgYVels[i] = (double)totalYDiff / vels.length;
		}
		*/

		// number of frames user to produce the estimate
		int estimateFrames = 4;
		int startFrom = carDataList.size() - estimateFrames - 1;
		int numToUse = estimateFrames;
		
		CarData first = carDataList.get(startFrom);
		CarData last = carDataList.get(carDataList.size() - 1);
		double distTravelled;
		if (carSideFrom) {
			// coming from right side
			distTravelled = last.getTrueLeftX() - first.getTrueLeftX();
		}
		else {
			distTravelled = last.getTrueRightX() - first.getTrueRightX();
		}
		
		//System.out.println("Using num cars: " + numToUse);
		avgXVel = distTravelled / estimateFrames;
		
		System.out.printf("Avg xVel: %.2f%n", avgXVel);
		//System.out.printf("Avg xVel after parallax: %.2f%n", avgXVel);
		
		/*
		int totalCarLen = 0;
		
		count = 0;
		for (i = 0; i < carDataList.size(); ++i) {
			totalCarLen += carDataList.get(i).getLength();
			++count;
		}
		*/
		// TODO median of 3 for length
		int carLen = last.getLength();
		
		//int carLen = carDataList.get(firstInFrameNum).getLength();
		//int carLen = last.getLength();
		
		int[] lengthOffsets = {0, carLen / 2, carLen};
		int lengthOffset = lengthOffsets[(carSideFrom ? 0 : 2)];
		//System.out.println("Length offset: " + lengthOffset);
		
		System.out.println("right x: " + last.getRightX());
		
		int xDist = (int)Math.round(takePosX - (carSideFrom ? last.getTrueLeftX() : last.getTrueRightX()) + lengthOffset);
		int yDist = takePosY - last.getY();
		
		//System.out.println("xDist: " + xDist);
		long nsBetweenFrames = latencyCalc.getAverageDelay(numToUse);
		//System.out.println("NS Between frames: " + nsBetweenFrames);
		//System.out.println("Last latency calc: " + latencyCalc.getLastDelay());
		
		double framesTillCapture = (avgXVel == 0 ? 0 : (xDist / avgXVel));
		
		System.out.printf("Frames till capture: %.2f%n", framesTillCapture);
		//System.out.println("Trigger latency: " + triggerLatency);
		
		long nsWait = (int)Math.round((framesTillCapture * nsBetweenFrames) - triggerLatency);
		
		//System.out.println("nsWait: " + nsWait);
		
		// calculate km/h
		double pixelsPerSecond = avgXVel * (1000000000 / nsBetweenFrames);
		double metersPerSecond = pixelsPerSecond / pixelsPerMeter;
		double kph = (metersPerSecond * 60 * 60) / 1000;
		
		//System.out.printf("Estimated speed: %.1fkm/h%n", kph);
		
		System.out.println();
		
		return System.nanoTime() + nsWait;
	}
	
	public static double adjustForParallax(int imgWidth, double x) {
		final double pixelsPerMeter = imgWidth / imageWidthMeters;
		
		double distFromMid = x - (imgWidth / 2);
		double metersFromMid = distFromMid / pixelsPerMeter;
		//System.out.printf("rightX: %d%nMeters from mid: %.2f%n", rightX, metersFromMid);
		
		double metersFromCam = Math.sqrt(Math.pow(metersFromMid, 2) + Math.pow(distCamToRoad, 2));
		//System.out.println("Meters from cam: " + metersFromCam);
		double parallax = metersFromCam / distCamToRoad;
		
		return (distFromMid * parallax) + (imgWidth / 2);
	}

	/**
	 * Returns the number of pixels between the front of the car and the edge
	 * of the frame.
	 * @return
	 */
	public int getCarDistFromEdge() {
		return Math.abs(frameWidth - currentCarX);
	}

	public void clearCarDataList() {
		carDataList.clear();
	}

}
