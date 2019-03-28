package ronan_tommey.reg_logger.image_processing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class PiCamFrameStreamer implements Runnable{
    public static final int FPS = 35;
    public static final long NS_BETWEEN_FRAMES = 1000000000 / FPS;

    private static final int blockSize = 4096;
    private static final int numBlocks = 64;

    private InputStream inputStream;
    private boolean running;
    private PiCamFrameListener camListener;

    public PiCamFrameStreamer(int frameWidth, int frameHeight, PiCamFrameListener camListener) {
        this.camListener = camListener;

        String[] command = { "/bin/sh", "-c",
                String.format("raspivid -o - -t 0 -cd MJPEG -w %d -h %d -fps %d -hf -vf", frameWidth, frameHeight, FPS) };
        Runtime rt = Runtime.getRuntime();
        Process proc;
        try {
            proc = rt.exec(command);
            inputStream = proc.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        int currBlock = 0;
        int blockPos;
        int lastBlock = 0;
        int lastBlockPos = 0;
        byte[] block;
        int numImageBytes;
        byte[] imageBytes;
        int offset;
        int imgBlock;
        int imgBlockPos;
        ByteArrayInputStream bais;
        BufferedImage nextImage;
        long imageTime = 0;
        long lastImageTime = System.nanoTime();
        boolean eofFound;
        int highestNumBytes = 0;
        int nextBlock;
        int byteCounter;
        int oldBytesFromStart;
        int newBytesFromStart;

        running = true;

        DataInputStream in = new DataInputStream(inputStream);
        byte[][] blocks = new byte[numBlocks][blockSize];

        // initial first block read

        if (imageTime == 0) {
            imageTime = System.nanoTime();
        }

        while (running) {
            block = blocks[currBlock];

            for (blockPos = 0; blockPos < blockSize; ++blockPos) {
                if (blockPos == blockSize - 1) {
                    // must read next block to see next byte
                    nextBlock = (currBlock + 1) % numBlocks;
                    try {
                        in.readFully(blocks[nextBlock]);

                        if (imageTime == 0) {
                            imageTime = System.nanoTime();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    eofFound = (block[blockPos] == (byte) 0xff
                            && blocks[nextBlock][0] == (byte) 0xd9);
                }
                else {
                    eofFound = (block[blockPos] == (byte) 0xff && block[blockPos + 1] == (byte) 0xd9);
                }

                if (eofFound) {
                    oldBytesFromStart = (lastBlock * blockSize) + lastBlockPos;
                    newBytesFromStart = (currBlock * blockSize) + blockPos;

                    if (newBytesFromStart >= oldBytesFromStart) {
                        numImageBytes = newBytesFromStart - oldBytesFromStart;
                    }
                    else {
                        numImageBytes = (numBlocks * blockSize) - oldBytesFromStart + newBytesFromStart;
                    }

                    numImageBytes += 2;

                    if (numImageBytes > highestNumBytes) {
                        highestNumBytes = numImageBytes;
                    }

                    imageBytes = new byte[numImageBytes];

                    byteCounter = 0;
                    while (byteCounter < numImageBytes) {
                        offset = lastBlockPos + byteCounter;
                        imgBlock = (lastBlock + (offset / blockSize)) % numBlocks;
                        imgBlockPos = offset % blockSize;

                        imageBytes[byteCounter++] = blocks[imgBlock][imgBlockPos];
                    }

                    bais = new ByteArrayInputStream(imageBytes);
                    try {
                        nextImage = ImageIO.read(bais);

                        camListener.onFrameRead(nextImage, imageTime - lastImageTime);
                        lastImageTime = imageTime;
                        imageTime = 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    lastBlock = currBlock;
                    lastBlockPos = blockPos + 2;

                    if (lastBlockPos > blockSize - 1) {
                        lastBlock = (lastBlock + 1) % numBlocks;
                        lastBlockPos %= blockSize;
                    }
                }
            }

            currBlock = (currBlock + 1) % numBlocks;
        }
    }
}
