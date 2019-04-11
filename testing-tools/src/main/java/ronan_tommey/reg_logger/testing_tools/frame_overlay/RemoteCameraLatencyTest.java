package ronan_tommey.reg_logger.testing_tools.frame_overlay;

import ronan_tommey.reg_logger.RegCapturer;
import ronan_tommey.reg_logger.RemoteCamera;

import java.util.Scanner;

public class RemoteCameraLatencyTest {
    private RemoteCamera remoteCamera;

    public RemoteCameraLatencyTest() {
        remoteCamera = new RemoteCamera(RegCapturer.REMOTE_CAMERA_PORT);
    }

    public void go() {
        Scanner console = new Scanner(System.in);

        System.out.println("Press enter to capture an image ASAP, type \"exit\" to exit.");
        while (!console.nextLine().toLowerCase().equals("exit")) {
            remoteCamera.captureImage(-1);
            System.out.println("Capture complete.");
        }
    }

    public static void main(String[] args) {
        new RemoteCameraLatencyTest().go();
    }

}
