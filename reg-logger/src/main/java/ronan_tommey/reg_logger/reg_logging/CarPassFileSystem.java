package ronan_tommey.reg_logger.reg_logging;

import javax.imageio.ImageIO;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CarPassFileSystem implements CarPassLogger {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private File rootDir;

    public CarPassFileSystem(String rootDirPath) {
        rootDir = new File(rootDirPath);

        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
    }

    @Override
    public void logPass(CarPassDetails carPassDetails) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(carPassDetails.getTimestamp());

        dateFormat.setTimeZone(calendar.getTimeZone());
        timeFormat.setTimeZone(calendar.getTimeZone());

        String folderName = dateFormat.format(calendar.getTime());
        String filePrefix = timeFormat.format(calendar.getTime());
        String fullTimestamp = folderName + " " + filePrefix;

        String dirPath = String.format("%s/%s", rootDir.getAbsolutePath(), folderName);
        String textFilePath = String.format("%s/%s.txt",dirPath, filePrefix);
        String imgFilePath = String.format("%s/%s.jpg",dirPath, filePrefix);

        File folderPath = new File(dirPath);
        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }

        try {
            logTextFile(carPassDetails, fullTimestamp, new File(textFilePath));
            logCapturedImage(carPassDetails, new File(imgFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logTextFile(CarPassDetails carPassDetails, String timestamp, File path) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path)));

        out.printf("Registration: %s%n", carPassDetails.getRegText());
        out.printf("Timestamp: %s%n", timestamp);
        out.printf("Direction: %s%n", carPassDetails.getDirection());
        out.printf("Pixel speed: %s%n", carPassDetails.getPixelSpeed());
        out.printf("KMPH speed: %.2fkmph%n", carPassDetails.getKmphSpeed());

        out.close();
    }

    private void logCapturedImage(CarPassDetails carPassDetails, File path) throws IOException {
        ImageIO.write(carPassDetails.getCapturedImage(), "JPG", path);
    }
}
