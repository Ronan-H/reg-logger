package ronan_hanley.reg_logger.remote_camera;

import org.gphoto2.Camera;
import org.gphoto2.CameraFile;
import org.gphoto2.CameraUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.IIOImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class NikonCapture {

    private long captureTime;
    private Camera nikon;

    public NikonCapture(long captureTime, Camera nikon) {
        this.captureTime = captureTime;
        this.nikon = nikon;
    }

    public BufferedImage capture() {
        final CameraFile cf2;

        long msSleep = captureTime / 1000000;
        int nsSleep = (int) (captureTime % 1000000);

        System.out.printf("Received command to capture image after %dms%n", msSleep);

        if (nsSleep > 0) {
            try {
                Thread.sleep(msSleep, nsSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Capturing...");

        cf2 = nikon.captureImage();

        String path = "./capture.jpg";
        File saveFile = new File(String.format(path));

        cf2.save(saveFile.getAbsolutePath());
        CameraUtils.closeQuietly(cf2);

        System.out.println("Image captured and saved as " + path);

        System.out.println("Reading saved image...");
        BufferedImage capturedImage = null;
        try {
            capturedImage = ImageIO.read(saveFile);

            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(1f);

            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            // specifies where the jpg image has to be written
            writer.setOutput(new FileImageOutputStream(
                    new File("test.jpg")));

            // writes the file with given compression level
            // from your JPEGImageWriteParam instance
            writer.write(null, new IIOImage(capturedImage, null, null), jpegParams);

            //ImageIO.write(capturedImage, "JPG", new File("test.jpg"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return capturedImage;
    }

}
