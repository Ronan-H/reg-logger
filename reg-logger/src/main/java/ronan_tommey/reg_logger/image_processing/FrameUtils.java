package ronan_tommey.reg_logger.image_processing;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Utilities class for processing individual frames, including functions like
 * scaling, removing noise, and converting frames to a boolean array for performance.
 */
public class FrameUtils {

    /**
     * Converts a BufferedImage frame to a boolean array, where black pixels
     * are converted to false (little or no movement for that frame pixel),
     * and white pixels are converted to true (movement detected for that pixel.
     *
     * Note that this returns a single dimension array, where a pixel from (x, y)
     * in the input image is found at index (y * width + x), width being
     * the width of the image.
     *
     * @param frame The BufferedImage frame to be converted
     * @return The input image represented as a boolean array
     */
    public static boolean[] convertImageToBooleanArray(BufferedImage frame) {
        // get pixel values as int array
        int[] pixels = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();

        boolean[] movingPixels = new boolean[pixels.length];
        for (int i = 0; i < pixels.length; ++i) {
            // boolean value is true if pixel is not black; false otherwise
            movingPixels[i] = (pixels[i] != 0);
        }

        return movingPixels;
    }

    /**
     * Removes noise from a frame. That is, removes any clusters of pixels which have
     * less than minClusterSize joined pixels. This includes adjacent pixels, but NOT
     * diagonally adjacent pixels.
     *
     * @param movingPixels Frame to remove noise from (represented as a boolean array)
     * @param imageWidth Width of the input frame
     * @param minClusterSize Minimum size each cluster has to be to not be removed
     */
    public static void removeNoise(boolean[] movingPixels, int imageWidth, int minClusterSize) {
        // delegates to an inner class for cleaner code
        FrameUtils.FrameNoiseRemover frameNoiseRemover =
                new FrameUtils().new FrameNoiseRemover(movingPixels, imageWidth, minClusterSize);
        frameNoiseRemover.removeNoise();
    }

    /**
     * Counts the number of moving pixels in a frame represented as a boolean array.
     *
     * Simply counts the number of "true" elements in the array.
     *
     * @param movingPixels Frame to count moving pixels in
     * @return Number of moving pixels found in the input frame
     */
    public static int countMoving(boolean[] movingPixels) {
        int count = 0;

        for (boolean movingPixel : movingPixels) {
            if (movingPixel) {
                count++;
            }
        }

        return count;
    }

    /**
     * Scales the given BufferedImage to become the specified target size.
     *
     * Note: leaves input image unchanged, image returned is a copy.
     *
     * @param before BufferdImage to scale
     * @param newSize Target size for the image
     * @return Input image, scaled to the specified target size
     */
    public static BufferedImage scaleImage(BufferedImage before, Dimension newSize) {
        // store target width and height in two int variables for convenience
        int newWidth = newSize.width;
        int newHeight = newSize.height;

        // create new scaled image
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, before.getType());
        Graphics2D g = scaled.createGraphics();

        // set rendering hints to make scaling take as little time as possible
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // draw old image on new scaled image (this is the line that does the actual scaling)
        g.drawImage(before, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return scaled;
    }

    /**
     * Converts a boolean array to a black and white image, and saves the resulting image
     * as a PNG file to disk.
     *
     * @param movingPixels The boolean array to be converted
     * @param width Width of the frame
     * @param outPath Image output file path
     * @throws IOException When failing to write image to disk
     */
    public static void saveBoolArrayAsImage(boolean[] movingPixels, int width, String outPath) throws IOException {
        // method adapted from:
        // https://stackoverflow.com/questions/20321606/convert-2d-binary-matrix-to-black-white-image-in-java

        // calculate height of the frame
        int height = movingPixels.length / width;

        // define colors and image color model
        final byte BLACK = 0;
        final byte WHITE = (byte) 255;

        byte[] map = {BLACK, WHITE};
        IndexColorModel icm = new IndexColorModel(1, 2, map, map, map);

        // create array to use as image raster
        int[] data = new int[movingPixels.length];

        // fill data array based on boolean array values
        for (int i = 0; i < movingPixels.length; i++) {
            data[i] = (movingPixels[i] ? WHITE : BLACK);
        }

        // load data into image raster
        WritableRaster raster = icm.createCompatibleWritableRaster(width, height);
        raster.setPixels(0, 0, width, height, data);

        // create BufferedImage from raster
        BufferedImage imgOut = new BufferedImage(icm, raster, false, null);

        // write image to disk
        ImageIO.write(imgOut, "PNG", new File(outPath));
    }

    /**
     * Convenience function; saves an image to disk as PNG.
     *
     * @param image Image to save to disk
     * @param outPath Path of image file to save
     * @throws IOException When failing to write image to disk
     */
    public static void saveImage(BufferedImage image, String outPath) throws IOException {
        ImageIO.write(image, "PNG", new File(outPath));
    }

    /**
     * A class that takes in a frame as a constructor argument, and can
     * remove noise for that frame.
     */
    class FrameNoiseRemover {
        private boolean[] movingPixels;
        private int minClusterSize;

        private final int[] offsets;

        /**
         * @param movingPixels Frame as boolean array
         * @param imageWidth Width of frame
         * @param minClusterSize Minimum size all pixel clusters in the frame must be to "survive" noise removal
         */
        FrameNoiseRemover(boolean[] movingPixels, int imageWidth, int minClusterSize) {
            this.movingPixels = movingPixels;
            this.minClusterSize = minClusterSize;

            // calculate offsets for use in finding adjacent pixels in the array
            offsets = new int[] {-1, +1, -imageWidth, +imageWidth};
        }

        /**
         * Removes noise in the frame, based on the passed in constructor arguments.
         */
        void removeNoise() {
            // pixel indexes that have already been processed; used for efficiency
            boolean[] processed = new boolean[movingPixels.length];

            // lopo through each pixel, checking for small clusters
            for (int i = 0; i < movingPixels.length; i++) {
                if (movingPixels[i] && !processed[i]) {
                    // this cluster must be processed

                    // call small cluster removal method, getting back a list of
                    // visited/processed indexes
                    Deque<Integer> visited = removeClusterIfSmall(i);

                    // mark off each visited index to ensure they don't get processed again
                    for (int j : visited) {
                        processed[j] = true;
                    }
                }
            }
        }

        /**
         * Recursively (done with a Deque, not method stack recursion) checks neighbouring pixels
         * of a specified index to determine if it is too small (ie. considered "noise"), and should
         * be removed, and removes the cluster if it is.
         *
         * @param startIndex Starting index
         * @return Deque of visited pixels
         */
        private Deque<Integer> removeClusterIfSmall(int startIndex) {
            // visited indexes
            boolean[] marked = new boolean[movingPixels.length];

            // stack of discovered neighbour pixels that need to be visited in turn
            Deque<Integer> unvisited = new ArrayDeque<>();
            // stack of pixels that we've already visited
            Deque<Integer> visited = new ArrayDeque<>();

            // push starting index
            unvisited.push(startIndex);
            marked[startIndex] = true;

            int index, newIndex;

            // while there are still pixels whose neighbours we haven't pushed yet...
            while (!unvisited.isEmpty()) {
                // visit this pixel
                index = unvisited.pop();
                visited.push(index);

                // search through pixel's neighbours to see if they're part of the cluster
                // (offsets, when added, find the pixel to the left, right, up and down)
                for (int offset : offsets) {
                    newIndex = index + offset;

                    if (newIndex >= 0 && newIndex < movingPixels.length // check if pixel is in bounds
                            && movingPixels[newIndex] // check if pixel is moving
                            && !marked[newIndex]) { // check if pixel has not been processed yet
                        // push neighbouring pixel (part of the cluster)
                        unvisited.push(newIndex);
                        marked[newIndex] = true;
                    }
                }
            }

            // remove cluster if it's smaller than the specified minimum size for clusters
            if (visited.size() < minClusterSize) {
                for (int removalIndex : visited) {
                    movingPixels[removalIndex] = false;
                }
            }
            
            return visited;
        }
    }
}
