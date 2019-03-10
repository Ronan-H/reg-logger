package ronan_tommey.reg_logger.car_data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class Utils {

    /**
     * Reads an image from a file, and returns a BufferedImage object whose image type
     * matches the images that are created by the PiCamFrameStreamer class.
     *
     * @throws IOException
     */
    public static BufferedImage readTestImage(String path) throws IOException {
        // read in image
        BufferedImage image = ImageIO.read(new File(path));

        // create new BufferedImage with correct image type
        BufferedImage imageCorrectFormat = new BufferedImage(
                image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_RGB
        );

        // draw old image on new BufferedImage with correct image type
        Graphics g = imageCorrectFormat.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return imageCorrectFormat;
    }

    public static BufferedImage[] readTestImageFolder(String dir) throws IOException {
        File[] imageFiles = new File(dir).listFiles();
        // sort files by name
        Arrays.sort(imageFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                String[] names = new String[] {f1.getName(), f2.getName()};
                int[] numbers = new int[2];

                for (int i = 0; i < 2; i++) {
                    numbers[i] = Integer.parseInt(names[i].substring(names[i].lastIndexOf("_") + 1, names[i].lastIndexOf(".")));
                }

                if (numbers[0] > numbers[1]) {
                    return 1;
                }
                else if (numbers[0] < numbers[1]) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        });

        BufferedImage[] images = new BufferedImage[imageFiles.length];

        for (int i = 0; i < imageFiles.length; i++) {
            images[i] = readTestImage(imageFiles[i].getAbsolutePath());
        }

        return images;
    }

    public static CarEstimate TestCarDirectionRight(BufferedImage[] frames)
    {
        CarDataSeries tester = new CarDataSeries();

        for(int i=0; i < frames.length; i++)
        {
            BufferedImage image = frames[i];
            CarData data = CarDataUtils.generateCarData(image);
            tester.addNextCarData(data);
        }

        FrameTimeManager timeTest = new FrameTimeManager(10);
        CarEstimate car = CarDataUtils.generateCarEstimate(tester, timeTest);

        return car;
    }

}
