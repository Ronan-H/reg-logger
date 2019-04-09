package ronan_tommey.reg_logger;

public class RegLogger {

    public static void main(String[] args) {
        RegCapturer regCapturer = new RegCapturer();
        new Thread(regCapturer).start();
    }
}
