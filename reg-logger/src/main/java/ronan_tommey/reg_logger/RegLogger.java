package ronan_tommey.reg_logger;

public class RegLogger {

    public static void main(String[] args) {
        RegCapturer regCapturer = new RegCapturer(600, 120, 0.6);
        new Thread(regCapturer).start();
    }
}
