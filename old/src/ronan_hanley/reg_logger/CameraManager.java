package ronan_hanley.reg_logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Manages a camera, either locally via USB, or remote on a network (using sockets)
 */
public abstract class CameraManager {

    public abstract BufferedImage captureImage(long captureTime);

    public void captureImageToFile(long captureTime, String path) {
        BufferedImage image = captureImage(captureTime);
        System.out.println("Writing captured image...");
        try {
            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(1f);

            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            // specifies where the jpg image has to be written
            writer.setOutput(new FileImageOutputStream(
                    new File(path)));

            // writes the file with given compression level
            // from your JPEGImageWriteParam instance
            writer.write(null, new IIOImage(image, null, null), jpegParams);

            //ImageIO.write(capturedImage, "JPG", new File("test.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully wrote image.");
    }


}
