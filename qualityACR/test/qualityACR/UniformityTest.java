package qualityACR;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class UniformityTest {
	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}

	@Test
	public final void testExtractImageMatrix() {

		/// qualityACR/testdata/slice5
		// C:\Users\Alberto\Repository\Workspace\workspaceACR\qualityACR\testdata\slice5

		// NON C'ENTRA, PER ORA LAVORA SU UN VETTORE DI NUMERI
		String path1 = "../qualityACR/testdata/slice5_A1.tif";
		// ImagePlus imp1 = UtilAyv.openImageNoDisplay(path1, true);
		// ImagePlus imp1 = UtilAyv.openImageMaximized(path1);
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
//		MyLog.waitHere();

		// ******************************************
		// test riempimento
		// ******************************************
		int imawidth = imp1.getWidth();
		int imaheight = imp1.getHeight();
		double[] vetPixels = Uniformity_.doubleVetPixelsExtractor(imp1);

		String riga = "";
		for (int y1 = 0; y1 < imaheight; y1++) {
			int offset = y1 * imawidth;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < imawidth; x1++) {
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + String.format("%04.0f", vetPixels[offset + x1]) + ",";
			}
			IJ.log(riga);
		}

		ACRlog.waitHere("ora pare funzionare !!!");

//		assertTrue(UtilAyv.compareVectors(expected, result, ""));
	}

	@Test
	public final void testExtractSubmatrixZero() {

		/// qualityACR/testdata/slice5
		// C:\Users\Alberto\Repository\Workspace\workspaceACR\qualityACR\testdata\slice5

		// NON C'ENTRA, PER ORA LAVORA SU UN VETTORE DI NUMERI
//		String path1 = "../qualityACR/testdata/slice5_A1.tif";
//		// ImagePlus imp1 = UtilAyv.openImageNoDisplay(path1, true);
//		ImagePlus imp1 = UtilAyv.openImageMaximized(path1);
//		MyLog.waitHere();

		// ******************************************
		// test riempimento
		// ******************************************
		int imawidth = 50;
		int imaheight = 50;
		int[] vetIntMatrix = new int[imawidth * imaheight];
		Uniformity_.fillIntMatrix(vetIntMatrix, imawidth, imaheight);
		String riga = "";
		for (int y1 = 0; y1 < imaheight; y1++) {
			int offset = y1 * imawidth;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < imawidth; x1++) {
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + String.format("%04d", vetIntMatrix[offset + x1]) + " , ";
			}
			IJ.log(riga);
		}
		// ******************************************
		// test estrazione
		// ******************************************
		int subxcenter = 0;
		int subycenter = 0;
		int sublato = 7;

		int[] vetSubmatrix = Uniformity_.extractSubmatrix(vetIntMatrix, imawidth, imaheight, subxcenter, subycenter,
				sublato, 200, true, false, false);
		if (vetSubmatrix != null) {
			String riga1 = "";
			for (int y1 = 0; y1 < sublato; y1++) {
				int offset = y1 * sublato;
				riga1 = "riga1 " + String.format("%03d", y1) + " | ";
				for (int x1 = 0; x1 < sublato; x1++) {
					// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
					riga1 = riga1 + String.format("%04d", vetSubmatrix[offset + x1]) + " , ";
				}
				IJ.log(riga1);
			}
		}

		ACRlog.waitHere("ora pare funzionare !!!");

//		assertTrue(UtilAyv.compareVectors(expected, result, ""));
	}

	@Test
	public final void testExtractSubMatrix() {

		/// qualityACR/testdata/slice5
		// C:\Users\Alberto\Repository\Workspace\workspaceACR\qualityACR\testdata\slice5

		// NON C'ENTRA, PER ORA LAVORA SU UN VETTORE DI NUMERI
		// String path1 = "../qualityACR/testdata/slice5_A1.tif";
		// // ImagePlus imp1 = UtilAyv.openImageNoDisplay(path1, true);
		// ImagePlus imp1 = UtilAyv.openImageMaximized(path1);
		// MyLog.waitHere();

		// ******************************************
		// test riempimento
		// ******************************************
		int imawidth = 50;
		int imaheight = 50;
		int[] vetIntMatrix = new int[imawidth * imaheight];
		Uniformity_.fillIntMatrix(vetIntMatrix, imawidth, imaheight);
		String riga = "";
		for (int y1 = 0; y1 < imaheight; y1++) {
			int offset = y1 * imawidth;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < imawidth; x1++) {
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + String.format("%04d", vetIntMatrix[offset + x1]) + " , ";
			}
			IJ.log(riga);
		}
		// ******************************************
		// test estrazione
		// ******************************************
		int subxcenter = 28;
		int subycenter = 9;
		int sublato = 7;

		int[] vetSubmatrix = Uniformity_.extractSubmatrix(vetIntMatrix, imawidth, imaheight, subxcenter, subycenter,
				sublato, 200, true, false, false);
		String riga1 = "";
		for (int y1 = 0; y1 < sublato; y1++) {
			int offset = y1 * sublato;
			riga1 = "riga1 " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < sublato; x1++) {
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga1 = riga1 + String.format("%04d", vetSubmatrix[offset + x1]) + " , ";
			}
			IJ.log(riga1);
		}

		ACRlog.waitHere("ora pare funzionare !!!");

		// assertTrue(UtilAyv.compareVectors(expected, result, ""));
	}

	@Test
	public final void testExtractSubMatrixDouble() {

		// ******************************************
		// test riempimento
		// ******************************************
		int imawidth = 192;
		int imaheight = 192;
		double[] vetDoublePixels = new double[imawidth * imaheight];
		Uniformity_.fillDoubleMatrix(vetDoublePixels, imawidth, imaheight);
		ACRlog.vetPrint(vetDoublePixels, "vetDoublePixels");
		// ******************************************
		// test estrazione
		// ******************************************
		int subxcenter = 99;
		int subycenter = 95;
		int sublato = 19;

		double[] vetSubmatrix = Uniformity_.extractSubmatrix(vetDoublePixels, imawidth, imaheight, subxcenter,
				subycenter, sublato, 200, false, true, false);
		ACRlog.vetPrint(vetSubmatrix, "vetSubmatrix");

		ACRlog.waitHere();

		// assertTrue(UtilAyv.compareVectors(expected, result, ""));
	}

}
