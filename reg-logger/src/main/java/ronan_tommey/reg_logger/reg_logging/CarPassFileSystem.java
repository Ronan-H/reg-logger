package ronan_tommey.reg_logger.reg_logging;

import javax.imageio.ImageIO;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Logs CarPassDetails to disk
 */
public class CarPassFileSystem implements CarPassLogger {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private File rootDir;

    /**
     * @param rootDirPath Path of root directory for log files
     */
    public CarPassFileSystem(String rootDirPath) {
        rootDir = new File(rootDirPath);

        // create directories if they don't exist
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
    }

    /**
     * Logs pass details to disk
     * @param carPassDetails Pass details to log
     */
    @Override
    public void logPass(CarPassDetails carPassDetails) {
        // set pass timestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(carPassDetails.getTimestamp());

        // set formatter timezones
        dateFormat.setTimeZone(calendar.getTimeZone());
        timeFormat.setTimeZone(calendar.getTimeZone());

        // use the formatted date string as the directory name
        String folderName = dateFormat.format(calendar.getTime());
        // use the formatted time string as the file names
        String filePrefix = timeFormat.format(calendar.getTime());
        // create the full path string
        String fullTimestamp = folderName + " " + filePrefix;

        // generate paths for image and text file using the strings above
        String dirPath = String.format("%s/%s", rootDir.getAbsolutePath(), folderName);
        String textFilePath = String.format("%s/%s.txt",dirPath, filePrefix);
        String imgFilePath = String.format("%s/%s.jpg",dirPath, filePrefix);

        File folderPath = new File(dirPath);
        // create directories if they don't exist
        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }

        // log the image and text file using the above paths
        try {
            logTextFile(carPassDetails, fullTimestamp, new File(textFilePath));
            logCapturedImage(carPassDetails, new File(imgFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log text component of CarPassDetails to a text file (including, speed, direction, etc.)
     * @param carPassDetails Contains the CarPassDetails to log
     * @param timestamp Timestamp string to log along with the other details
     * @param path File path to write the text file to
     * @throws IOException Relating to issues writing to disk
     */
    private void logTextFile(CarPassDetails carPassDetails, String timestamp, File path) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path)));

        // write out each CarPassDetails field one by one
        out.printf("Registration: %s%n", carPassDetails.getRegText());
        out.printf("Timestamp: %s%n", timestamp);
        out.printf("Direction: %s%n", carPassDetails.getDirection());
        out.printf("Pixel speed: %s%n", carPassDetails.getPixelSpeed());
        out.printf("KMPH speed: %.2fkmph%n", carPassDetails.getKmphSpeed());

        out.close();
    }

    /**
     * Log the image component of CarPasDetails to an image file
     * @param carPassDetails Contains the captured image to log
     * @param path File path to write the image
     * @throws IOException
     */
    private void logCapturedImage(CarPassDetails carPassDetails, File path) throws IOException {
        // save image as JPEG file
        ImageIO.write(carPassDetails.getCapturedImage(), "JPG", path);
    }
}
