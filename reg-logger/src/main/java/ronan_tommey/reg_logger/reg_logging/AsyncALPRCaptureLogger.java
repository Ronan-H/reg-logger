package ronan_tommey.reg_logger.reg_logging;

import ronan_tommey.reg_logger.RemoteCamera;

import java.awt.image.BufferedImage;

public class AsyncALPRCaptureLogger implements Runnable {
    private RemoteCamera remoteCamera;
    private CarPassLogger carPassLogger;

    private CarPassDetails carPassDetails;
    private long captureTime;
    private boolean readyToCapture;

    private boolean running;

    public AsyncALPRCaptureLogger(RemoteCamera remoteCamera, CarPassLogger carPassLogger) {
        this.remoteCamera = remoteCamera;
        this.carPassLogger = carPassLogger;
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            while (!readyToCapture) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            BufferedImage capturedImage = remoteCamera.captureImage(captureTime);
            String regText = ALPR.extractRegText(capturedImage);
            regText = RegHasher.hashReg(regText);

            carPassDetails.setRegText(regText);

            carPassLogger.logPass(carPassDetails);

            readyToCapture = false;
        }
    }

    public synchronized void capturePass(CarPassDetails carPassDetails, long captureTime) {
        this.carPassDetails = carPassDetails;
        this.captureTime = captureTime;

        readyToCapture = true;
    }
}
