package ronan_hanley.reg_logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class RemoteCameraManager extends CameraManager {
    private Socket clientSock;
    private PrintWriter out;
    private BufferedInputStream in;
    private DataInputStream dis;

    public RemoteCameraManager(int port) {
        try {
            ServerSocket serverSock = new ServerSocket(port);

            System.out.println("Waiting for remote camera client to connect...");
            Socket clientSock = serverSock.accept();
            System.out.println("Remote camera client conected.");
            out = new PrintWriter(new OutputStreamWriter(clientSock.getOutputStream()));
            in = new BufferedInputStream(clientSock.getInputStream());
            dis = new DataInputStream(clientSock.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BufferedImage captureImage(long captureTime) {
        BufferedImage capturedImage = null;

        long sleepTime = captureTime - System.nanoTime();

        out.println(sleepTime);
        out.flush();
        byte[] sizeArr = new byte[4];
        try {
            in.read(sizeArr);
            int size = ByteBuffer.wrap(sizeArr).asIntBuffer().get();
            System.out.println("Image size (bytes): " + size);
            byte[] imageAr = new byte[size];
            dis.readFully(imageAr);
            capturedImage = ImageIO.read(new ByteArrayInputStream(imageAr));

            System.out.println("Image received successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return capturedImage;
    }

}
