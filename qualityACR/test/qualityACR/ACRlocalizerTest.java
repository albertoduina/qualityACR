package qualityACR;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

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
		double[][] matin = { { 26, 162 }, { 83, 123 } };
		ACRlog.logMatrix(matin, ACRlog.qui() + "matin");
		double angle = ACRlocalizer.phantomRotation(matin, true, true, true, 0);
		IJ.log("Angle= " + angle);
		double expected = 163.6104596659652;
		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	@Test
	public final void testPhantomResolutionHoles() {
		boolean step = true;
		boolean fast = false;
		boolean verbose = true;
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
		
		ACRlog.logVector(phantomCircle, ACRlog.qui()+"phantomCircle");
		ACRlog.logMatrix(phantomVertices, ACRlog.qui()+"phantomVertices");
		IJ.log(ACRlog.qui()+"angle= "+angle);

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

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		imp1.show();
		ACRutils.zoom(imp1);
		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp1, false, false, false, timeout);
		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp1, phantomCircle, false, false, false, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, false, false, false, timeout);
		
		ACRlog.logVector(phantomCircle, ACRlog.qui()+"phantomCircle");
		ACRlog.logMatrix(phantomVertices, ACRlog.qui()+"phantomVertices");
		IJ.log(ACRlog.qui()+"angle= "+angle);
		double[][] matin = new double[2][4];
		matin[0][0]=74;
		matin[1][0]=84;
		matin[0][1]=92;
		matin[1][1]=88;
		
		

		double[][] matout= ACRlocalizer.rototrasla(imp1, matin, phantomCircle, angle, step, fast, verbose, timeout);

		ACRlog.waitHere();

//		IJ.log("Angle= " + angle);
//		double expected = 163.6104596659652;
//		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

}
