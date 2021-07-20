package qualityACR;

import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.plugin.PlugIn;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

public class Error_Demo implements PlugIn {

	public void run(String arg) {

		ImagePlus imp1 = IJ.openImage();
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		imp2.setRoi(new OvalRoi(width * 1 / 3, height * 3 / 4, 40, 40));
		ImageProcessor ip2= imp2.getProcessor();
		ip2.setColor(Color.white);
		ip2.fillOutside(imp2.getRoi());
		ip2.invert();
		imp2.updateAndDraw();
	}

}
