package ronan_hanley.reg_logger.remote_camera;

import org.gphoto2.Camera;
import org.gphoto2.CameraList;
import org.gphoto2.CameraUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class RemoteCamera {
    public static final String VERSION = "3";

    public static final String RASPI_IP = "192.168.0.27";
    public static final int PORT = 52197;
    private boolean running;

    private Camera nikon;

    public void go() {
        System.out.printf("Remote camera client version %s starting...%n%n", VERSION);

        initNikon();
        Socket socket;
        BufferedReader in = null;
        BufferedOutputStream out = null;
        ByteArrayOutputStream bStream;

        try {
            socket = new Socket(RASPI_IP, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedOutputStream(socket.getOutputStream()) ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line = null;
        long captureTime;

        running = true;
        while (running) {
            try {
                line = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // line read; capture image at given time
            captureTime = Long.parseLong(line);

            BufferedImage capturedImage = new NikonCapture(captureTime, nikon).capture();

            /*
            BufferedImage capturedImage = null;
            try {
                capturedImage = ImageIO.read(new File("/home/ronan/Pictures/JuneSilentDiscoStreetParty.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            System.out.println("Uploading captured image...");

            try {
                bStream = new ByteArrayOutputStream();

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
                writer.write(null, new IIOImage(capturedImage, null, null), jpegParams);

                imageOutputStream.flush();
                imageOutputStream.close();
                byte[] imgBytes = bStream.toByteArray();

                System.out.println("Image size (bytes): " + imgBytes.length);
                byte[] size = ByteBuffer.allocate(4).putInt(bStream.size()).array();
                /*
                int n = 100;
                System.out.printf("Last %d bytes:%n", n);

                for (int i = imgBytes.length - 100; i < imgBytes.length; ++i) {
                    System.out.printf("Byte %d: %d%n", i, imgBytes[i]);
                }
                */

                out.write(size);
                out.write(imgBytes);
                out.flush();
                System.out.println("Finished.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initNikon() {
        nikon = null;

        final CameraList cl = new CameraList();
        System.out.println("Cameras: " + cl);
        CameraUtils.closeQuietly(cl);
        nikon = new Camera();
        nikon.initialize();

        //CameraUtils.closeQuietly(c);
    }

    public static void main(String[] args) {
        new RemoteCamera().go();
    }

}
