package ronan_tommey.reg_logger;

/**
 * Main class
 */
public class RegLogger {

    /**
     * Main method, breaks into OO code immediately by starting a RegCapturer object
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // start a RegCapturer object on it's own thread
        RegCapturer regCapturer = new RegCapturer();
        new Thread(regCapturer).start();
    }
}
