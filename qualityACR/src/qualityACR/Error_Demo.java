package qualityACR;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.plugin.PlugIn;
import ij.process.FloatPolygon;

public class Error_Demo implements PlugIn {

	public void run(String arg) {

		ImagePlus imp1 = IJ.openImage();
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();

		// TODO Auto-generated method stub
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		imp2.setRoi(new Line(0, 0, width, height / 2));
		FloatPolygon fp1 = imp2.getRoi().getInterpolatedPolygon();
		Line line1 = (Line) imp2.getRoi();
		double[] signal = line1.getPixels();
		for (int i1 = 0; i1 < fp1.npoints; i1++) {
			IJ.log("i1 X = " + fp1.xpoints[i1] + " Y = " + fp1.ypoints[i1] + " Z= " + signal[i1]);
			//test
		}

	}

}
