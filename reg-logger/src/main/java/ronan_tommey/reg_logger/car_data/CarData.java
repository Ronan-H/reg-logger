package ronan_tommey.reg_logger.car_data;

public class CarData {
    // positions of the leftmost and rightmost moving pixels in the image
    private int leftX, rightX;
    // positions of the topmost and bottommost moving pixels in the image
    private int topY;
    private int bottomY;
    private int regPosEstimate;
    // width of the car
    private int width;

    public CarData(int leftX, int rightX, int topY, int bottomY, int regPosEstimate) {
        this.leftX = leftX;
        this.rightX = rightX;
        this.topY = topY;
        this.bottomY = bottomY;
        this.regPosEstimate = regPosEstimate;

        width = rightX - leftX;
    }

    /**
     * CarData gets the data of the car from each frame
     * @param leftX The left-most pixel of the car in the frame
     * @param rightX The right-most pixel of the car in the frame
     * @param topY  The top-most pixel of the car in the frame
     * @param bottomY The bottom-most pixel of the car in the frame
     */
    public CarData(int leftX, int rightX, int topY, int bottomY) {
        this(leftX, rightX, topY, bottomY, -1);
    }

    public int getLeftX() {
        return leftX;
    }

    public int getRightX() {
        return rightX;
    }

    public int getTopY() {
        return topY;
    }

    public int getBottomY() {
        return bottomY;
    }

    /**
     * Sets the registration position estimate to the passed in value
     * @param regPosEstimate The registration's position estimate
     */
    public void setRegPosEstimate(int regPosEstimate) {
        this.regPosEstimate = regPosEstimate;
    }

    /**
     * Gets the registration's position estimate from CarUtils.AddRegPosEstimate
     */
    public int getRegPosEstimate() {
        return regPosEstimate;
    }

    public int getWidth() {
        return width;
    }

    /**
     * Checks if this carData is the same as another carData used in testing
     * @param otherObj CarData object used in testing
     * @return Boolean value if the two objects are equal
     */
    @Override
    public boolean equals(Object otherObj) {
        CarData other = null;

        //To catch error if two different types are input
        try {
            other = (CarData) otherObj;
        }
        catch (ClassCastException e) {
            return false;
        }

        return
                getLeftX() == other.getLeftX()
             && getRightX() == other.getRightX()
             && getTopY() == other.getTopY()
             && getBottomY() == other.getBottomY();
    }
}
