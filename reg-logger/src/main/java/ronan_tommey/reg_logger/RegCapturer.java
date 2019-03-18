package ronan_tommey.reg_logger;

import ronan_tommey.reg_logger.image_processing.FrameUtils;
import ronan_tommey.reg_logger.image_processing.MovementHighlighter;
import ronan_tommey.reg_logger.image_processing.PiCamFrameListener;
import ronan_tommey.reg_logger.image_processing.PiCamFrameStreamer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class RegCapturer implements PiCamFrameListener, Runnable {
    private PiCamFrameStreamer piCamFrameStreamer;
    private MovementHighlighter movementHighlighter;

    private BufferedImage nextFrame;
    private long nextFrameDelta;

    private Dimension scaledFrameDim;
    private long frameCounter = 0;
    private boolean running;

    private static final int numIgnoreFirst = 25 * 5;

    public RegCapturer(int capWidth, int capHeight, double frameScaling) {
        piCamFrameStreamer = new PiCamFrameStreamer(capWidth, capHeight, this);

        scaledFrameDim = new Dimension(
                (int) Math.round(capWidth * frameScaling),
                (int) Math.round(capHeight * frameScaling)
        );

        movementHighlighter = new MovementHighlighter(scaledFrameDim.width, scaledFrameDim.height);
    }

    public void run() {
        running = true;

        new Thread(piCamFrameStreamer).start();

        while (running) {
            while (nextFrame == null) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            processFrame(nextFrame, nextFrameDelta);
            frameCounter++;
        }
    }

    private void processFrame(BufferedImage frame, long delta) {
        BufferedImage scaledFrame = FrameUtils.scaleImage(frame, scaledFrameDim);
        BufferedImage movementImage = movementHighlighter.getHighlightedImage(scaledFrame);

        if (frameCounter < numIgnoreFirst) {
            // ignore frame
            return;
        }

        boolean[] movingPixels = FrameUtils.convertImageToBooleanArray(movementImage);
        FrameUtils.removeNoise(movingPixels, scaledFrame.getWidth(), 125);

        try {
            int movingPixelCount = FrameUtils.countMoving(movingPixels);
            if (movingPixelCount > 0) {
                System.out.println("Moving pixel count: " + movingPixelCount);

                FrameUtils.saveImage(scaledFrame, String.format("./test-images/scaled/scaled_%d.png", frameCounter));
                FrameUtils.saveImage(movementImage, String.format("./test-images/movement/movement_%d.png", frameCounter));
                FrameUtils.saveBoolArrayAsImage(movingPixels, scaledFrame.getWidth(), String.format("./test-images/noise-removed/noise-rem_%d.png", frameCounter));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void onFrameRead(BufferedImage image, long delta) {
        nextFrame = image;
        nextFrameDelta = delta;

        notifyAll();
    }
}
