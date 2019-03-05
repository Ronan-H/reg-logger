package ronan_tommey.reg_logger.image_processing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FrameUtilsTest {

    @org.junit.jupiter.api.Test
    public void convertImageToBooleanArray() throws IOException {
        BufferedImage image = ImageIO.read(new File("./src/test/input/frame-utils/image-to-bool-array/tiny-frame.png"));
        boolean[] tester = FrameUtils.convertImageToBooleanArray(image);
        //100101011100
        boolean[] expected = {true,false,false,true,false,true,false,true,true,true,false,false};

        assertEquals(expected,tester);
    }

    @org.junit.jupiter.api.Test
    public void removeNoise() {
    }
}