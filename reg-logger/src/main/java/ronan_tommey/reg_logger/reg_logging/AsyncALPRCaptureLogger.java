package ronan_tommey.reg_logger.reg_logging;

import ronan_tommey.reg_logger.RemoteCamera;

import java.awt.image.BufferedImage;

/**
 * Class used to capture images and use ALPR to read the registration
 * plate text, on another thread.
 */
public class AsyncALPRCaptureLogger implements Runnable {
    private RemoteCamera remoteCamera;
    private CarPassLogger carPassLogger;

    private CarPassDetails carPassDetails;
    private long captureTime;
    private boolean readyToCapture;

    private boolean running;

    /**
     * @param remoteCamera Remote camera to use for capturing images
     * @param carPassLogger Logging system to use (database or filesystem)
     */
    public AsyncALPRCaptureLogger(RemoteCamera remoteCamera, CarPassLogger carPassLogger) {
        this.remoteCamera = remoteCamera;
        this.carPassLogger = carPassLogger;
    }

    /**
     * Continuously waits for the signal to capture an image, and operates
     * the remote camera when it does.
     */
    @Override
    public void run() {
        running = true;

        while (running) {
            // wait for signal to capture
            while (!readyToCapture) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // capture and store image using capture wait time
            BufferedImage capturedImage = remoteCamera.captureImage(captureTime);
            // add captured image to car pass details
            carPassDetails.setCapturedImage(capturedImage);

            // use OpenALPR to extract the registration plate text from the image
            String regText = ALPR.extractRegText(capturedImage);
            // hash the reg text to avoid GDPR concerns (see related wiki page)
            regText = RegHasher.hashReg(regText);
            // add reg text to car pass details
            carPassDetails.setRegText(regText);

            // log pass using whatever logging system is being used (database or filesystem)
            carPassLogger.logPass(carPassDetails);

            readyToCapture = false;
        }
    }

    /**
     * Called to trigger the capturing of the remote camera, and storing of the
     * pass details to the pass logger system.
     * @param carPassDetails CarPassDetails object to add more details to (image and reg text)
     * @param captureTime Time to wait before capturing the image
     */
    public synchronized void capturePass(CarPassDetails carPassDetails, long captureTime) {
        // set variables and wake up thread
        this.carPassDetails = carPassDetails;
        this.captureTime = captureTime;

        readyToCapture = true;

        notifyAll();
    }
}
