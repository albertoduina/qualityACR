package qualityACR;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.awt.Color;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;

public class ACRlocalizerTest {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}

	@Test
	public final void testParallela() {
		double[][] matin = { { 26, 162 }, { 83, 123 } };
		double distanza = -17;
		ACRlog.logMatrix(matin, ACRlog.qui() + "matin");
		double[] vetout = ACRlocalizer.parallela(matin, distanza);
		ACRlog.logVector(vetout, ACRlog.qui() + "vetout");
		double[] vetexpected = { 5.375872022286244, -16.127616066858735, };
		assertTrue(ACRutils.compareVectors(vetout, vetexpected, 1e-11, ""));
	}

	@Test
	public final void testPhantomRotation() {
		double[][] matin = { { 0, 1, 156, 20 }, { 0, 1, 138, 99 } };
		ACRlog.logMatrix(matin, ACRlog.qui() + "matin");
		double angle = ACRlocalizer.phantomRotation(matin, true, true, true, 0);
		IJ.log("Angle= " + angle);
		double expected = -16.001022853845996;
		ACRlog.waitHere("Angle= " + angle + " gradi");
		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	@Test
	public final void testGridLocalizerAdvanced() {
		int timeout = 200;

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		imp1.show();
		ACRutils.zoom(imp1);
		double[] phantomCircle = ACRlocalizer.gridLocalizerAdvanced(imp1, true, false, true, timeout);


//		IJ.log("Angle= " + angle);
//		double expected = 163.6104596659652;
//		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	@Test
	public final void testPhantomResolutionHoles() {
		int timeout = 200;

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		imp1.show();
		ACRutils.zoom(imp1);
		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp1, false, false, false, timeout);

//		double[][] matout = ACRlocalizer.phantomResolutionHoles(imp1, phantomCircle, step, fast, verbose, timeout);
		ACRlog.waitHere();

//		IJ.log("Angle= " + angle);
//		double expected = 163.6104596659652;
//		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	@Test
	public final void testGridMatrix() {
		boolean step = true;
		boolean fast = false;
		boolean verbose = true;
		int timeout = 200;

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		imp1.show();
		ACRutils.zoom(imp1);
		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp1, false, false, false, timeout);
		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp1, phantomCircle, false, false, false, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, false, false, false, timeout);

		ACRlog.logVector(phantomCircle, ACRlog.qui() + "phantomCircle");
		ACRlog.logMatrix(phantomVertices, ACRlog.qui() + "phantomVertices");
		IJ.log(ACRlog.qui() + "angle= " + angle);

		ACRlocalizer.gridMatrix(imp1, phantomCircle, angle, step, fast, verbose, timeout);

		ACRlog.waitHere();

//		IJ.log("Angle= " + angle);
//		double expected = 163.6104596659652;
//		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	@Test
	public final void testRototrasla() {
		boolean step = true;
		boolean fast = false;
		boolean verbose = true;
		int timeout = 200;

		double[][] matin = new double[2][4];
		matin[0][0] = 57;
		matin[1][0] = 70;
		matin[0][1] = 139;
		matin[1][1] = 62;

		double AX1 = matin[0][0];
		double AY1 = matin[1][0];
		double BX1 = matin[0][0];
		double BY1 = matin[1][0];

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		imp1.show();
		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);
		ACRutils.zoom(imp1);

		imp1.setRoi(new PointRoi(AX1, AY1));
		imp1.getRoi().setStrokeColor(Color.GREEN);
		over1.addElement(imp1.getRoi());
		imp1.setRoi(new PointRoi(BX1, BY1));
		imp1.getRoi().setStrokeColor(Color.GREEN);
		over1.addElement(imp1.getRoi());

		ACRlog.waitHere();

		ImagePlus imp2 = imp1.duplicate();
		IJ.run(imp2, "Rotate... ", "angle=15 grid=1 interpolation=Bilinear");
//		imp2.setRoi(10, 10, 170, 170);
//		imp2.cut();
//		imp2.setRoi(3, 25, 170, 170);
//		imp2.paste();
//		imp2.killRoi();

//		imp1.updateAndDraw();
//		imp1.show();

		Overlay over2 = new Overlay();
		imp2.setOverlay(over1);

		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp2, false, false, false, timeout);
		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp2, phantomCircle, false, false, false, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, false, false, false, timeout);
		ACRlog.logVector(phantomCircle, ACRlog.qui() + "phantomCircle");
		ACRlog.logMatrix(phantomVertices, ACRlog.qui() + "phantomVertices");
		IJ.log(ACRlog.qui() + "angle= " + angle);

		double[][] matout = ACRlocalizer.rototrasla(imp2, matin, phantomCircle, angle, step, fast, verbose, timeout);

		for (int i1 = 0; i1 < matin[0].length; i1++) {
			imp2.setRoi(new PointRoi(matout[0][i1], matout[1][i1]));
			imp2.getRoi().setStrokeColor(Color.red);
			over2.addElement(imp2.getRoi());
			imp2.killRoi();
		}

		ACRlog.waitHere("il punto di coordinate " + matin[0][0] + "," + matin[1][0] + " trasformato in " + matout[0][0]
				+ "," + matout[1][0]);
		ACRlog.logMatrix(matout, ACRlog.qui() + "matout");
		ACRlog.waitHere("FINE");
//		double expected = 163.6104596659652;
//		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));

	}

}
