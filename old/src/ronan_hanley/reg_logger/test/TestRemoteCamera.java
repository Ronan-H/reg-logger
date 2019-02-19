package ronan_hanley.reg_logger.test;

import ronan_hanley.reg_logger.RegLogger;
import ronan_hanley.reg_logger.RemoteCameraManager;
import ronan_hanley.reg_logger.remote_camera.RemoteCamera;

public class TestRemoteCamera {

    public static void main(String[] args) {
        RemoteCameraManager camManager = new RemoteCameraManager(RemoteCamera.PORT);
        camManager.captureImageToFile(System.nanoTime() + 1000000000, "capture.jpg");
    }

}
