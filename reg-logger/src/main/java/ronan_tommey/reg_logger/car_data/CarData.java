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

    public void setRegPosEstimate(int regPosEstimate) {
        this.regPosEstimate = regPosEstimate;
    }

    public int getRegPosEstimate() {
        return regPosEstimate;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public boolean equals(Object otherObj) {
        CarData other = null;

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
