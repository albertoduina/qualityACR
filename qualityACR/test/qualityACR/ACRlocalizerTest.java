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
import ij.gui.Roi;

public class ACRlocalizerTest {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}

	/**
	 * >>>>>> FUNZIONA E VIENE UTILIZZATO 02aug2021 20:26 <<<<<<
	 */
	@Test
	public final void testParallela() {
		double[][] matin = { { 26, 162 }, { 83, 123 } };
		double distanza = -17;
		ACRlog.logMatrix(matin, ACRlog.qui() + "matin");
		double[] vetout = ACRlocalizer.parallela(matin, distanza);
		ACRlog.logVector(vetout, ACRlog.qui() + "vetout");
		double[] vetexpected = { 4.796827507856353, -16.3092135267116 };
		assertTrue(ACRutils.compareVectors(vetout, vetexpected, 1e-11, ""));
	}

	/**
	 * >>>>> FUNZIONA E VIENE UTILIZZATO 02aug2021 20:28 <<<<
	 */
	@Test
	public final void testPhantomRotation() {
		double[][] matin = { { 0, 1, 156, 20 }, { 0, 1, 138, 99 } };
		ACRlog.logMatrix(matin, ACRlog.qui() + "matin");
		double angle = ACRlocalizer.phantomRotation(matin, true, true, true, 0);
		IJ.log("Angle= " + angle);
		double expected = -16.001022853845996;
		// ACRlog.waitHere("Angle= " + angle + " gradi");
		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	/**
	 * >>>>>>> FUNZIONA E VIENE USATO 02aug2021 20:21 <<<<<
	 */
	@Test
	public final void testGridLocalizerAdvanced() {
		int timeout = 200;

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		imp1.show();
		ACRutils.zoom(imp1);
		double[] phantomCircle = ACRlocalizer.gridLocalizerAdvanced(imp1, false, false, false, timeout);

//		IJ.log("Angle= " + angle);
//		double expected = 163.6104596659652;
//		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	/**
	 * IN FASE DI TEST
	 */
	@Test
	public final void testPhantomResolutionHoles() {
		int timeout = 200;

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		imp1.show();
		ACRutils.zoom(imp1);
		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp1, false, false, false, timeout);

		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp1, phantomCircle, false, false, false, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, false, false, false, timeout);

		double[][] matout = ACRlocalizer.phantomResolutionHoles(imp1, phantomVertices, true, false, true, timeout);

//		IJ.log("Angle= " + angle);
//		double expected = 163.6104596659652;
//		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	/**
	 * SUPERFLUO POSIZIONA IN MODO STATICO DEI QUADRATINI
	 */
	@Test
	public final void testStaticGridMatrix() {
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

		ACRlocalizer.staticGridMatrix(imp1, phantomCircle, angle, step, fast, verbose, timeout);

		ACRlog.waitHere();

//		IJ.log("Angle= " + angle);
//		double expected = 163.6104596659652;
//		assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	/**
	 * >>>> FUNZIONA E VIENE UTILIZZATO 02aug2021 20:30 <<<<<
	 */
	@Test
	public final void testRototrasla() {

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
		Overlay over2 = new Overlay();
		imp2.setOverlay(over1);

		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp2, false, false, false, 0);
		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp2, phantomCircle, false, false, false, 0);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, false, false, false, 0);
		ACRlog.logVector(phantomCircle, ACRlog.qui() + "phantomCircle");
		ACRlog.logMatrix(phantomVertices, ACRlog.qui() + "phantomVertices");
		IJ.log(ACRlog.qui() + "angle= " + angle);

		double[][] matout = ACRlocalizer.rototrasla(imp2, matin, phantomCircle, angle, false, false, false, 0);

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

	/**
	 * >>>>>>> FUNZIONA E VIENE USATO 02aug2021 20:10 <<<<<<
	 */
	@Test
	public final void testMovableGridMatrix() {
		boolean step = true;
		boolean fast = false;
		boolean verbose = true;
		int timeout = 200;

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		IJ.run(imp1, "Rotate... ", "angle=15 grid=1 interpolation=Bilinear");
		imp1.setRoi(14, 11, 167, 166);
		imp1.cut();
		imp1.setRoi(1, 1, 167, 166);
		imp1.paste();
		imp1.killRoi();
		imp1.show();
		ACRutils.zoom(imp1);
		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp1, false, false, false, timeout);
		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp1, phantomCircle, false, false, false, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, false, false, false, timeout);
		ACRlog.logVector(phantomCircle, ACRlog.qui() + "phantomCircle");

		double[][] matout = ACRlocalizer.movableGridMatrix(imp1, phantomCircle, angle, step, fast, verbose, timeout);

		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);

		for (int i1 = 0; i1 < matout[0].length; i1++) {
			Roi pr1 = new Roi(matout[0][i1], matout[1][i1], 1, 1);
			imp1.setRoi(pr1);
			imp1.getRoi().setFillColor(Color.GREEN);
			over1.addElement(imp1.getRoi());
			imp1.updateAndDraw();
			imp1.killRoi();
		}

		ACRlog.waitHere("VERIFICARE COINCIDENZA PUNTI VERDI CON I FOR DELLA RISOLUZIONE");

		// IJ.log("Angle= " + angle);
		// double expected = 163.6104596659652;
		// assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	/**
	 * SUPERFLUO SOSTITUITO DA MOVABLEGRIDMATRIX
	 */
	@Test
	public final void testMovableSubMatrix() {

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		IJ.run(imp1, "Rotate... ", "angle=15 grid=1 interpolation=Bilinear");

		imp1.show();
		ACRutils.zoom(imp1);
		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);

		int width = imp1.getWidth();
		int height = imp1.getHeight();
		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp1, false, false, false, 0);
		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp1, phantomCircle, false, false, false, 0);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, false, false, false, 0);

		ACRlog.logVector(phantomCircle, ACRlog.qui() + "phantomCircle");
		ACRlog.logMatrix(phantomVertices, ACRlog.qui() + "phantomVertices");
		IJ.log(ACRlog.qui() + "angle= " + angle);

		// calcoliamo l'offset del centro fantoccio, rispetto al centro immagine
		double[] offset = new double[2];
		offset[0] = phantomCircle[0] - width / 2;
		offset[1] = phantomCircle[1] - height / 2;

		// innanzitutto vado a tracciare una struttura di punti, che potrebbe essere
		// fatta coincidere con la matrice di fori della risoluzione
		double lato = 2.2;
		double k1 = 0.0;
		double k2 = 0.2;

		double[] primo = new double[2];
		primo[0] = 64;
		primo[1] = 70;

		double hole = 0.9;
//		double[] secondo = new double[2];
//		secondo[0] = 57;
//		secondo[1] = 70;

		ACRlocalizer.movableSubMatrix22(imp1, over1, primo, hole, offset, angle, lato, k1, k2);

		ACRlog.waitHere();

		// IJ.log("Angle= " + angle);
		// double expected = 163.6104596659652;
		// assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

	/**
	 * >>>>>> FUNZIONA E VIENE USATO 02aug2021 20:20 <<<<<<
	 */
	@Test
	public final void testPerfectSubMatrix() {

		String path1 = ".\\testdata\\001.dcm";
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		IJ.run(imp1, "Rotate... ", "angle=-1 grid=1 interpolation=Bilinear");

		imp1.show();
		ACRutils.zoom(imp1);

		int width = imp1.getWidth();
		int height = imp1.getHeight();
		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));

		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp1, false, false, false, 0);
		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp1, phantomCircle, false, false, false, 0);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, false, false, false, 0);

		ACRlog.logVector(phantomCircle, ACRlog.qui() + "phantomCircle");
		ACRlog.logMatrix(phantomVertices, ACRlog.qui() + "phantomVertices");
		IJ.log(ACRlog.qui() + "angle= " + angle);

		// calcoliamo l'offset del centro fantoccio, rispetto al centro immagine
//		double[] offset = new double[2];
//		offset[0] = phantomCircle[0] - width / 2;
//		offset[1] = phantomCircle[1] - height / 2;

		// innanzitutto vado a tracciare una struttura di punti, che potrebbe essere
		// fatta coincidere con la matrice di fori della risoluzione
//		double lato = 2.2;
//		double k1 = 0.0;
//		double k2 = 0.2;

		double[] primo = new double[2];
		primo[0] = 63.5;
		primo[1] = 70;
		double hole = 0.7 / dimPixel;

		double[][] punti1 = ACRlocalizer.perfectSubMatrix(primo, hole, width, height);

		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);
		for (int i1 = 0; i1 < punti1[0].length; i1++) {
			Roi pr1 = new Roi(punti1[0][i1], punti1[1][i1], 1, 1);
			imp1.setRoi(pr1);
			imp1.getRoi().setFillColor(Color.RED);
			over1.addElement(imp1.getRoi());
			imp1.killRoi();
//			ACRlog.waitHere();
		}

		double[] secondo = new double[2];
		secondo[0] = 95.5;
		secondo[1] = 70;
		hole = 0.8 / dimPixel;
		double[][] punti2 = ACRlocalizer.perfectSubMatrix(secondo, hole, width, height);

		for (int i1 = 0; i1 < punti2[0].length; i1++) {
			Roi pr1 = new Roi(punti2[0][i1], punti2[1][i1], 1, 1);
			imp1.setRoi(pr1);
			imp1.getRoi().setFillColor(Color.YELLOW);
			over1.addElement(imp1.getRoi());
			imp1.killRoi();
		}

		double[] terzo = new double[2];
		terzo[0] = 128.5;
		terzo[1] = 70;
		hole = 0.9 / dimPixel;
		double[][] punti3 = ACRlocalizer.perfectSubMatrix(terzo, hole, width, height);
		for (int i1 = 0; i1 < punti3[0].length; i1++) {
			Roi pr1 = new Roi(punti3[0][i1], punti3[1][i1], 1, 1);
			imp1.setRoi(pr1);
			imp1.getRoi().setFillColor(Color.GREEN);
			over1.addElement(imp1.getRoi());
			imp1.killRoi();
		}
		imp1.updateAndDraw();

		ACRlog.waitHere();

		// IJ.log("Angle= " + angle);
		// double expected = 163.6104596659652;
		// assertTrue(ACRutils.compareDoublesWithTolerance(angle, expected, 1e-11));
	}

}
