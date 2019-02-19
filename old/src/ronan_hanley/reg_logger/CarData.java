package ronan_hanley.reg_logger;

public class CarData {
	private int fromFrame;
	private int x, y;
	private int xVel, yVel;
	private int numPixels;
	// x positions of left and rightmost pixel
	private int leftX, rightX;
	private int leftY, rightY;
	private int topY, bottomY;
	// leftX and rightX adjusted to account for parallax
	private double trueLeftX, trueRightX;
	private int noShadowFront;
	private int[][] points;
	private int length;

	public CarData(int imgWidth,
				   int x, int y,
				   int xVel, int yVel,
				   int numPixels,
				   int leftX, int leftY,
				   int rightX, int rightY,
				   int topY, int bottomY,
				   int noShadowFront) {
		this.x = x;
		this.y = y;
		this.xVel = xVel;
		this.yVel = yVel;
		this.numPixels = numPixels;
		this.leftX = leftX;
		this.rightX = rightX;
		this.topY = topY;
		this.bottomY = bottomY;
		this.noShadowFront = noShadowFront;
		
		points = new int[][] {{leftX, leftY}, {x, y}, {rightX, rightY}};

		// TODO update for cars coming right side
		length = Math.abs(rightX - noShadowFront);
		
		calculateParallax(imgWidth);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getXVel() {
		return xVel;
	}
	
	public int getYVel() {
		return yVel;
	}
	
	public int getNumPixels() {
		return numPixels;
	}
	
	public int getLeftX() {
		return leftX;
	}
	
	public int getLeftY() {
		return leftY;
	}
	
	public int getRightX() {
		return rightX;
	}
	
	public double getTrueRightX() {
		return trueRightX;
	}
	
	public double getTrueLeftX() {
		return trueLeftX;
	}
	
	public int getRightY() {
		return rightY;
	}
	
	public int[][] getPoints() {
		return points;
	}
	
	public int getLength() {
		return length;
	}

	public int getTopY() {
		return topY;
	}

	public int getBottomY() {
		return bottomY;
	}

	public int getNoShadowFront() {
		return noShadowFront;
	}

	public void calculateParallax(int imgWidth) {
		//System.out.println();
		trueLeftX = TriggerEstimator.adjustForParallax(imgWidth, leftX);
		trueRightX = TriggerEstimator.adjustForParallax(imgWidth, rightX);
		//System.out.println("Old rightX: " + rightX);
		//System.out.println("Parallax: " + parallax);
		//System.out.println("New rightX: " + trueRightX);
	}
	
}
