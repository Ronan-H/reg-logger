package ronan_tommey.reg_logger.image_processing;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FrameUtilsTest {

    private static BufferedImage readTestImage() throws IOException {
        BufferedImage image = ImageIO.read(new File("./src/test/input/frame-utils/image-to-bool-array/tiny-frame.png"));

        BufferedImage imageCorrectFormat = new BufferedImage(
                image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_RGB
        );

        Graphics g = imageCorrectFormat.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return imageCorrectFormat;
    }

    @org.junit.jupiter.api.Test
    public void convertImageToBooleanArray() throws IOException {
        BufferedImage image = readTestImage();
        boolean[] tester = FrameUtils.convertImageToBooleanArray(image);
        //100101011100
        boolean[] expected = {true,false,false,true,false,true,false,true,true,true,false,false};

        assertArrayEquals(expected,tester);
    }

    @org.junit.jupiter.api.Test
    public void removeNoise() {
    }
}