package ronan_tommey.reg_logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Class used for taking pictures with a camera over a network. This program
 * acts as the server, while a remote camera client has to connect to it.
 */
public class RemoteCamera {
    private PrintWriter out;
    private BufferedInputStream in;
    private DataInputStream dis;

    /**
     * @param port Network port to use for the server
     */
    public RemoteCamera(int port) {
        try {
            // create server socket
            ServerSocket serverSock = new ServerSocket(port);

            // wait for a remote camera client to connect
            System.out.println("Waiting for remote camera client to connect...");
            Socket clientSock = serverSock.accept();
            System.out.println("Remote camera client connected.");

            // initialize streams
            out = new PrintWriter(new OutputStreamWriter(clientSock.getOutputStream()));
            in = new BufferedInputStream(clientSock.getInputStream());
            dis = new DataInputStream(clientSock.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Captures an image using the remote camera
     * @param captureTime The time to wait before capturing
     * @return Captured image
     */
    public BufferedImage captureImage(long captureTime) {
        // send the capture wait time to the remote camera client
        // (this also prompts the remote client to capture an image)
        out.println(captureTime);
        out.flush();

        BufferedImage capturedImage = null;
        try {
            // read image size (4 bytes as 1 int)
            byte[] sizeArr = new byte[4];
            in.read(sizeArr);
            int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
            System.out.println("Image size (bytes): " + size);

            // read image bytes
            byte[] imageAr = new byte[size];
            dis.readFully(imageAr);

            // convert image bytes to BufferedImage
            capturedImage = ImageIO.read(new ByteArrayInputStream(imageAr));

            System.out.println("Image received successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return capturedImage;
    }
}
