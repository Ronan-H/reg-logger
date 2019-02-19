package ronan_hanley.reg_logger;

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

public class CarLocator {
	private BackgroundModelStationary background;
	// Comment/Uncomment to switch input image type
	private ImageType imageType = ImageType.single(GrayF32.class);
//	ImageType imageType = ImageType.il(3, InterleavedF32.class);
//	ImageType imageType = ImageType.il(3, InterleavedU8.class);
	private BufferedImage visualized;
	
	public CarLocator(int imageWidth, int imageHeight) {
		visualized = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		
		// ConfigBackgroundGmm configGmm = new ConfigBackgroundGmm();
		
		// Comment/Uncomment to switch algorithms
		float threshold = 25; // lower = more sensitive
		float learnRate = 0.015f; // 0 = static, 1 = instant
		background =
				FactoryBackgroundModel.stationaryBasic(new ConfigBackgroundBasic(threshold, learnRate), // args: threshhold, learnrate
				// http://boofcv.org/javadoc/boofcv/factory/background/ConfigBackgroundBasic.html#ConfigBackgroundBasic-float-float-
														imageType);
//								FactoryBackgroundModel.stationaryGmm(configGmm, imageType);
	}
	
	public BufferedImage processImage(BufferedImage image) {
		// Declare storage for segmented image.  1 = moving foreground and 0 = background
		GrayU8 segmented = new GrayU8(image.getWidth() ,image.getHeight());
		
		ImageBase input = ConvertBufferedImage.convertFrom(image, true, imageType);
		
		background.updateBackground(input, segmented);

		return VisualizeBinaryData.renderBinary(segmented, false, visualized);
	}
	
}
