package ronan_tommey.reg_logger;

import org.gphoto2.*;

import javax.imageio.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class RemoteCameraClient {

    public static final String RASPI_IP = "192.168.0.27";
    public static final int PORT = 52197;

    private BufferedReader in;
    private BufferedOutputStream out;
    private Camera nikon;
    private boolean running;

    public void go() throws IOException {
        System.out.println("Starting remote camera client...");

        initCamera();
        Socket socket;

        socket = new Socket(RASPI_IP, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedOutputStream(socket.getOutputStream()) ;

        String line;
        long captureTime;

        running = true;
        while (running) {
            line = in.readLine();

            // line read; capture image at given time
            captureTime = Long.parseLong(line);

            BufferedImage capturedImage = new DSLRCamera(captureTime, nikon).captureImage(captureTime);

            System.out.println("Uploading captured image...");

            uploadImage(capturedImage);
        }
    }

    private void initCamera() {
        nikon = null;

        final CameraList cl = new CameraList();
        System.out.println("Cameras: " + cl);
        CameraUtils.closeQuietly(cl);
        nikon = new Camera();
        nikon.initialize();
    }

    public void uploadImage(BufferedImage image){
        try {

            ByteArrayOutputStream bStream = new ByteArrayOutputStream();

            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(1f);

            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            // specifies where the jpg image has to be written
            ImageOutputStream imageOutputStream =
                    new MemoryCacheImageOutputStream(bStream);
            writer.setOutput(imageOutputStream);

            // writes the image with given compression level
            // from your JPEGImageWriteParam instance
            writer.write(null, new IIOImage(image, null, null), jpegParams);

            imageOutputStream.flush();
            imageOutputStream.close();
            byte[] imgBytes = bStream.toByteArray();

            System.out.println("Image size (bytes): " + imgBytes.length);
            byte[] size = ByteBuffer.allocate(4).putInt(bStream.size()).array();

            out.write(size);
            out.write(imgBytes);
            out.flush();
            System.out.println("Finished.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new RemoteCameraClient().go();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
