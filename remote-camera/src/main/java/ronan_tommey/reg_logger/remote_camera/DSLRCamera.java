package ronan_tommey.reg_logger.remote_camera;

import org.gphoto2.*;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;

public class DSLRCamera {
    private long captureTime;
    private Camera camera;

    public DSLRCamera(long captureTime, Camera camera) {
        this.captureTime = captureTime;
        this.camera = camera;
    }

    public BufferedImage captureImage(long captureTime) throws IOException {
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

        cf2 = camera.captureImage();

        String path = "./capture.jpg";
        File saveFile = new File(path);

        cf2.save(saveFile.getAbsolutePath());
        CameraUtils.closeQuietly(cf2);

        System.out.println("Image captured and saved as " + path);
        System.out.println("Reading saved image...");

        BufferedImage capturedImage = null;

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

        return capturedImage;
    }
}
