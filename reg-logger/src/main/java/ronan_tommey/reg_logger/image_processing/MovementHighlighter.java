package ronan_tommey.reg_logger.image_processing;

import boofcv.alg.background.BackgroundModelStationary;
import boofcv.factory.background.ConfigBackgroundBasic;
import boofcv.factory.background.FactoryBackgroundModel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.*;

import java.awt.image.BufferedImage;

/**
 * Uses BoofCV to highlight movement in an image, based on the background model.
 *
 * Adapted from https://boofcv.org/index.php?title=Example_Background_Stationary_Camera
 */
public class MovementHighlighter{
    private BackgroundModelStationary background;
    private ImageType imageType = ImageType.single(GrayF32.class);
    private GrayU8 segmented;
    private BufferedImage visualized;

    public MovementHighlighter(int imageWidth, int imageHeight) {
        // Declare storage for segmented image
        // 1 = moving foreground and 0 = background
        segmented = new GrayU8(imageWidth, imageHeight);
        // image for visualization of image movement
        visualized = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        // movement threshold; lower = more sensitive
        float threshold = 19;
        // rate at which new images become the background model; 0 = static, 1 = instant
        float learnRate = 0.005f;

        // initialize background model
        background = FactoryBackgroundModel.stationaryBasic(
                new ConfigBackgroundBasic(threshold, learnRate), imageType
        );
    }

    /**
     * Returns an image highlighting movement in the passed in image, based on the
     * background model
     * @param image Image to highlight movement in
     * @return Hightligted movement image
     */
    public BufferedImage getHighlightedImage(BufferedImage image) {
        // convert BufferedImage to BoofCV type
        ImageBase input = ConvertBufferedImage.convertFrom(image, true, imageType);

        // update background based on passed in image
        background.updateBackground(input, segmented);

        // return movement visualization
        return VisualizeBinaryData.renderBinary(segmented, false, visualized);
    }
}
