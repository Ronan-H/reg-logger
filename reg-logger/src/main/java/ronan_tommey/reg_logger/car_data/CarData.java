package ronan_tommey.reg_logger.car_data;

public class CarData {
    // positions of the leftmost and rightmost moving pixels in the image
    private int leftX, rightX;
    // positions of the topmost and bottommost moving pixels in the image
    private int topY;
    private int bottomY;
    // width of the car
    private int width;

    public CarData(int leftX, int rightX, int topY, int bottomY) {
        this.leftX = leftX;
        this.rightX = rightX;
        this.topY = topY;
        this.bottomY = bottomY;
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

    public int getWidth() {
        return width;
    }
}
