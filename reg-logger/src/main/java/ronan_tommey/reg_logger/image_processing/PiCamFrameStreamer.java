package ronan_tommey.reg_logger.image_processing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Class used to stream video from the Raspberry Pi camera module,
 * and separate it out into individual images.
 */
public class PiCamFrameStreamer implements Runnable{
    private static final int blockSize = 4096;
    private static final int numBlocks = 8;

    private InputStream inputStream;
    private boolean running;
    private PiCamFrameListener camListener;

    /**
     * @param frameWidth Image width
     * @param frameHeight Image height
     * @param fps Frames per second setting to use
     * @param camListener Receives every frame read in (the "observer design pattern")
     */
    public PiCamFrameStreamer(int frameWidth, int frameHeight, int fps, PiCamFrameListener camListener) {
        this.camListener = camListener;

        // build raspivid command using passed in arguments
        String[] command = { "/bin/sh", "-c",
                String.format("raspivid -o - -t 0 -cd MJPEG -md 5 -w %d -h %d -fps %d -hf -vf", frameWidth, frameHeight, fps) };

        // execute the above command
        Runtime rt = Runtime.getRuntime();
        Process proc;
        try {
            proc = rt.exec(command);

            // get the InputStream to read the video bytes back later
            inputStream = proc.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the video stream loop. Uses a circular buffer to read and process
     * image bytes.
     */
    public void run() {
        // index of current byte array in the 2d array
        int currBlock = 0;
        // current index in that array
        int blockPos;
        // array that contains the start of the previously read in image
        int lastBlock = 0;
        // block index of the first byte of the previously read image
        int lastBlockPos = 0;
        // current byte array in the 2d array
        byte[] block;
        // number of bytes the next image to be read uses
        int numImageBytes;
        // bytes of the image being read
        byte[] imageBytes;
        int offset;
        int imgBlock;
        int imgBlockPos;
        ByteArrayInputStream bais;
        BufferedImage nextImage;
        // time (nanoseconds) that the last image was read in
        long imageTime = 0;
        long lastImageTime = System.nanoTime();
        boolean eofFound;
        int nextBlock;
        int byteCounter;
        int oldBytesFromStart;
        int newBytesFromStart;

        // running variable to allow interruption
        running = true;

        DataInputStream in = new DataInputStream(inputStream);

        // initialize circular buffer
        byte[][] blocks = new byte[numBlocks][blockSize];

        // initial first block read
        imageTime = System.nanoTime();

        while (running) {
            // set block variable for convenience
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

                    // check if this byte and next byte match EOF marker
                    eofFound = (block[blockPos] == (byte) 0xff
                            && blocks[nextBlock][0] == (byte) 0xd9);
                }
                else {
                    // check if this byte and next byte match EOF marker
                    eofFound = (block[blockPos] == (byte) 0xff && block[blockPos + 1] == (byte) 0xd9);
                }

                if (eofFound) {
                    // EOF found, next image has been read fully into the circular buffer

                    oldBytesFromStart = (lastBlock * blockSize) + lastBlockPos;
                    newBytesFromStart = (currBlock * blockSize) + blockPos;

                    if (newBytesFromStart >= oldBytesFromStart) {
                        numImageBytes = newBytesFromStart - oldBytesFromStart;
                    }
                    else {
                        numImageBytes = (numBlocks * blockSize) - oldBytesFromStart + newBytesFromStart;
                    }

                    numImageBytes += 2;

                    // create new array to hold image bytes
                    imageBytes = new byte[numImageBytes];

                    // copy bytes from circular array into single dimension array
                    byteCounter = 0;
                    while (byteCounter < numImageBytes) {
                        offset = lastBlockPos + byteCounter;
                        imgBlock = (lastBlock + (offset / blockSize)) % numBlocks;
                        imgBlockPos = offset % blockSize;

                        imageBytes[byteCounter++] = blocks[imgBlock][imgBlockPos];
                    }

                    bais = new ByteArrayInputStream(imageBytes);
                    try {
                        // convert byte array to BufferedImage
                        nextImage = ImageIO.read(bais);

                        // notify listener with new image read in, along with the time
                        // (in nanoseconds) since the last frame
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

            // start reading from the next block
            currBlock = (currBlock + 1) % numBlocks;
        }
    }
}
