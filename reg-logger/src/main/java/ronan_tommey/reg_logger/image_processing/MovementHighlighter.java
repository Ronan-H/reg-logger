package ronan_tommey.reg_logger.image_processing;

import boofcv.alg.background.BackgroundModelStationary;
import boofcv.factory.background.ConfigBackgroundBasic;
import boofcv.factory.background.FactoryBackgroundModel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

import java.awt.image.BufferedImage;

public class MovementHighlighter{
    private BackgroundModelStationary background;
    private ImageType imageType = ImageType.single(GrayF32.class);
    private BufferedImage visualized;

    public MovementHighlighter(int imageWidth, int imageHeight) {
        visualized = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        float threshold = 35; // lower = more sensitive
        float learnRate = 0.005f; // 0 = static, 1 = instant

        background = FactoryBackgroundModel.stationaryBasic(
                new ConfigBackgroundBasic(threshold, learnRate), imageType
        );
    }

    public BufferedImage getHighlightedImage(BufferedImage image) {
        // Declare storage for segmented image.  1 = moving foreground and 0 = background
        GrayU8 segmented = new GrayU8(image.getWidth() ,image.getHeight());

        ImageBase input = ConvertBufferedImage.convertFrom(image, true, imageType);

        background.updateBackground(input, segmented);

        return VisualizeBinaryData.renderBinary(segmented, false, visualized);
    }
}
