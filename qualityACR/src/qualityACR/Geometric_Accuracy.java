package qualityACR;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.RotatedRectRoi;
import ij.measure.CurveFitter;
import ij.plugin.PlugIn;
import ij.util.Tools;

public class Geometric_Accuracy implements PlugIn {

	public static final boolean debug = true;
	public static final boolean big = true;

	public void run(String arg) {
		mainGeometry();
	}

	public void mainGeometry() {

		int timeout = 2000; // preme automaticamente OK ai messaggi durante i test

		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
		boolean[] defaults = { true, false, true, false, false, false, false, true, true, true, false, false, false,
				true };
		String[] headings = { "slices T1", "slices T2" };
		GenericDialog gd1 = new GenericDialog("GEOMETRIC ACCURACY");
		gd1.addCheckbox("ANIMAZIONE 2 sec", true);
		gd1.addCheckbox("STEP", true);
		gd1.addCheckbox("VERBOSE", true);
		gd1.addCheckbox("LOCALIZER", false);
		gd1.addCheckboxGroup(7, 2, labels, defaults, headings);

//		gd1.addCheckbox("SLICE1 DIAMETER", false);
//		gd1.addCheckbox("SLICE5 DIAMETER", false);
//		gd1.addCheckbox("slice6 geometry", false);
//		gd1.addCheckbox("slice7 geometry", false);
		gd1.showDialog();
		if (gd1.wasCanceled()) {
			ACRlog.waitHere("premuto cancel");
			return;
		}

		String str1 = gd1.getNextRadioButton();
		boolean fast = gd1.getNextBoolean();
		boolean step = gd1.getNextBoolean();
		boolean verbose = gd1.getNextBoolean();
		boolean geomLocalizer = gd1.getNextBoolean();
		boolean[] vetBoolSliceT1 = new boolean[7];
		boolean[] vetBoolSliceT2 = new boolean[7];
		for (int i1 = 0; i1 < 7; i1++) {
			vetBoolSliceT1[i1] = gd1.getNextBoolean();
			vetBoolSliceT2[i1] = gd1.getNextBoolean();
		}

		// leggo i nomi di tutti i 15 file presenti
		String pathLocalizer = "";
		String tmpFolderPath = IJ.getDirectory("temp");
		String completePath = tmpFolderPath + "ACRlist.tmp";
		String[] vetPath = ACRutils.readStringArrayFromFile(completePath);
		String[] listLocalizer = ACRinputOutput.readStackPathToSortedList(vetPath[0], "T1");
		if (listLocalizer != null)
			pathLocalizer = listLocalizer[0];
		String[] sortedListT1 = ACRinputOutput.readStackPathToSortedList(vetPath[1], "T1");
		if (sortedListT1 == null)
			IJ.log("sortedListT1 ==null");
		String[] sortedListT2 = ACRinputOutput.readStackPathToSortedList(vetPath[2], "T2");
		if (sortedListT2 == null)
			IJ.log("sortedListT2 ==null");

		// ora in base alle selezioni effettuate nelle checkbox del dialogo, dobbiamo
		// elaborare solo i file selezionati

		if (geomLocalizer)
			mainLocalizer(pathLocalizer);

		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log("==================");
				IJ.log("elaborazione slice T1 numero " + i1);
				mainSliceDiameter(sortedListT1[i1], i1, step, fast, verbose, timeout);
			}
		}

		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log("==================");
				IJ.log("elaborazione slice T2 numero " + i1);
				mainSliceDiameter(sortedListT2[i1], i1, step, fast, verbose, timeout);
			}
		}
		ACRlog.waitHere("GEOMETRIC_ACCURACY TERMINATA");
	}

	/**
	 * Elaborazione immagine localizer per ottenere lunghezza fantoccio
	 * 
	 * @param path1
	 */
	public void mainLocalizer(String path1) {

		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		if (big)
			ACRutils.zoom(imp2);

		//
		// ========== TAROCCAMENTO IMMAGINE =============
		//
		IJ.run(imp2, "Rotate... ", "angle=6 grid=1 interpolation=Bilinear");
		//
		// ===============================================
		//

		int width = imp2.getWidth();
		int height = imp2.getHeight();

		int lato = 30;

		double[] max1 = maximumSearch(imp2, lato);

		double max2 = max1[0];

		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);

		//
		// Ricerca dei bordi in direzione verticale
		//

		// List importato da java.util
		// si tratta di un vettore arrayList a due dimensioni [][]
		List<List<Integer>> pointArrayXY = new ArrayList<List<Integer>>();
		//
		for (int i1 = 0; i1 < width; i1++) { // era i1 +=3
			int a1 = i1;
			int[] out1 = verticalSearch(imp2, max2, a1);
			if (out1[0] > 0 && out1[1] > 0) {
				List<Integer> pointXY = new ArrayList<Integer>();
				pointXY.add(out1[0]);
				pointXY.add(out1[1]);
				pointArrayXY.add(pointXY);
			}
		}
		//
		// Ricerca dei bordi in direzione orizzontale, molti saranno doppioni, ma questo
		// non ci dovrebbe creare problemi (ed in ogni caso diventerebbe un casino
		// toglierli!)
		//
		for (int i1 = 0; i1 < height; i1++) { // era i1 +=3
			int a1 = i1;
			int[] out1 = horizontalSearch(imp2, max2, a1);
			if (out1[0] > 0 && out1[1] > 0) {
				List<Integer> pointXY = new ArrayList<Integer>();
				pointXY.add(out1[0]);
				pointXY.add(out1[1]);
				pointArrayXY.add(pointXY);
			}
		}
		//
		//
		//
		List<Integer> pointList = new ArrayList<>();
		int count = 0;
		int[][] rotatedPoints = new int[pointArrayXY.size()][];

		for (List<Integer> tempor : pointArrayXY) {
			pointList = tempor;
			int[] vetOut = new int[pointList.size()];
			int i1 = 0;
			for (Integer n : pointList) {
				vetOut[i1++] = n;
			}
			rotatedPoints[count] = vetOut;
			count++;
		}

		int row = rotatedPoints.length;
		int col = rotatedPoints[0].length;
		int[][] vetPoints = new int[col][row];
		for (int i1 = 0; i1 < col; i1++) {
			for (int i2 = 0; i2 < row; i2++) {
				vetPoints[i1][i2] = rotatedPoints[i2][i1];
			}
		}
		ACRlog.logMatrix(vetPoints, "vetPoints");
		ACRlog.waitHere("SALVARE IL LOG");


		for (int i1 = 0; i1 < rotatedPoints.length; i1++) {
			// IJ.log("lower= " + lowerArrayA.get(i1) + "," + lowerArrayB.get(i1));
			imp2.setRoi(new PointRoi(rotatedPoints[i1][0], rotatedPoints[i1][1], "small red hybrid"));
			over2.addElement(imp2.getRoi());
			imp2.killRoi();
		}
		
		
		ACRlog.waitHere();

//		ACRlog.waitHere("003");
//
//		int[] vetX = ACRcalc.arrayListToArrayInt(pointArrayX);
//		int[] vetY = ACRcalc.arrayListToArrayInt(pointArrayY);
//
//		double[][] vetPoints = new double[2][vetX.length];
//		for (int i1 = 0; i1 < vetX.length; i1++) {
//			vetPoints[0][i1] = (double) vetX[i1];
//			vetPoints[1][i1] = (double) vetY[i1];
//		}
//		ACRlog.logMatrix(vetPoints, "vetPOints");
//
//		boolean max = true;
//		int key = 0;
//
//		double[][] vetMax = extractMax(vetPoints, key, max);
//
//		ACRlog.logMatrix(vetMax, "vetMax");
//		ACRlog.waitHere("004");

//		double[][] ordPoints1 = ACRutils.minsort(vetPoints, 0);
//		double[][] ordPoints2 = ACRutils.minsort(ordPoints1, 1);
//
//		ACRlog.logVector(vetX, "vetX");
//		ACRlog.logVector(vetY, "vetY");
//		ACRlog.waitHere();
//
//		// per trovare il bounding rectangle devo cercare il minimo e massimo su LX (e
//		// la relativa coordinata LY)
//		// lo stesso procedimento su LY. cosi' trovo i 4 vertici del rotated rectangle
//
//		int minx = Integer.MAX_VALUE;
//		int maxx = Integer.MIN_VALUE;
//		int miny = Integer.MAX_VALUE;
//		int maxy = Integer.MIN_VALUE;
//		int AX = 0;
//		int AY = 0;
//		int BX = 0;
//		int BY = 0;
//		int CX = 0;
//		int CY = 0;
//		int DX = 0;
//		int DY = 0;
//
//		for (int i1 = 0; i1 < vetY.length; i1++) {
//			if (vetY[i1] < miny) {
//				miny = vetY[i1];
//				AX = vetX[i1];
//				AY = vetY[i1];
//			}
//		}
//
////		ACRlog.waitHere("AX= " + AX + " AY= " + AY);
//
//		for (int i1 = 0; i1 < vetX.length; i1++) {
//			if (vetX[i1] > maxx) {
//				maxx = vetX[i1];
//				BX = vetX[i1];
//				BY = vetY[i1];
//			}
//		}
////		ACRlog.waitHere("BX= " + BX + " BY= " + BY);
//
//		for (int i1 = 0; i1 < vetY.length; i1++) {
//			if (vetY[i1] > maxy) {
//				maxy = vetY[i1];
//				CX = vetX[i1];
//				CY = vetY[i1];
//			}
//		}
//
//		for (int i1 = 0; i1 < vetX.length; i1++) {
//			if (vetX[i1] < minx) {
//				minx = vetX[i1];
//				DX = vetX[i1];
//				DY = vetY[i1];
//			}
//		}
//
////		ACRlog.waitHere("AX= " + AX + " AY= " + AY + " BX= " + BX + " BY= " + BY + " CX= " + CX + " CY= " + CY + " DX= "
////				+ DX + " DY= " + DY);
//
////		pr2.setPointType(2);
////		PointRoi pr2 = new PointRoi();
////		pr2.setSize(4);
//
//		ACRutils.plotPoints(imp2, over2, (int) AX, (int) AY, 4);
//		ACRutils.plotPoints(imp2, over2, (int) BX, (int) BY, 4);
//		ACRutils.plotPoints(imp2, over2, (int) CX, (int) CY, 4);
//		ACRutils.plotPoints(imp2, over2, (int) DX, (int) DY, 4);
//
////		imp2.setRoi(new PointRoi(AX, AY));
////		imp2.getRoi().setFillColor(Color.GREEN);
////		over2.addElement(imp2.getRoi());
////		imp2.killRoi();
////		ACRlog.waitHere("verde!");
////		imp2.setRoi(new PointRoi(BX, BY));
////		imp2.getRoi().setFillColor(Color.YELLOW);
////		over2.addElement(imp2.getRoi());
////		imp2.killRoi();
////		ACRlog.waitHere();
////		imp2.setRoi(new PointRoi(CX, CY));
////		imp2.getRoi().setFillColor(Color.RED);
////		over2.addElement(imp2.getRoi());
////		imp2.killRoi();
////		ACRlog.waitHere();
////
////		imp2.setRoi(new PointRoi(DX, DY));
////		imp2.getRoi().setFillColor(Color.BLUE);
////		imp2.killRoi();
////		over2.addElement(imp2.getRoi());
////		ACRlog.waitHere();
//
//		double MX = (AX + DX) / 2;
//		double MY = (AY + DY) / 2;
//		double PX = (BX + CX) / 2;
//		double PY = (BY + CY) / 2;
//		double LL = Math.sqrt((AX - DX) * (AX - DX) + (AY - DY) * (AX - DY));
//
//		imp2.setRoi(new RotatedRectRoi(MX, MY, PX, PY, LL));
//
//		ACRlog.waitHere();
//
//		double medLB = ACRcalc.vetMedian(vetY);
//		double sdLB = ACRcalc.vetSd(vetY);
//
//		int[] histo1 = getHistogram(vetY, height);
////		for (int i1 = 0; i1 < histo1.length; i1++) {
////			IJ.log("vetLB histogram " + i1 + ">" + histo1[i1]);
////		}
//
////		int[] vetUA = ACRcalc.arrayListToArrayInt(upperArrayA);
////		int[] vetUB = ACRcalc.arrayListToArrayInt(upperArrayB);
////		double medUB = ACRcalc.vetMedian(vetUB);
////		double sdUB = ACRcalc.vetSd(vetUB);
////
////		int[] histo2 = getHistogram(vetUB, height);
//////		for (int i1 = 0; i1 < histo2.length; i1++) {
//////			IJ.log("vetUB histogram " + i1 + ">" + histo2[i1]);
//////		}
////
//////		IJ.log("medLB= " + medLB + " medUB= " + medUB);
//////		IJ.log("sdLB= " + sdLB + " sdUB= " + sdUB);
////
////		ArrayList<Integer> lowerAA = new ArrayList<>();
////		ArrayList<Integer> upperAA = new ArrayList<>();
////		ArrayList<Integer> lowerAB = new ArrayList<>();
////		ArrayList<Integer> upperAB = new ArrayList<>();
////
////		double[] vetLowerA = ACRcalc.arrayListToArrayDouble3(lowerArrayA);
////		double[] vetLowerB = ACRcalc.arrayListToArrayDouble3(lowerArrayB);
////
////		double[] vetUpperA = ACRcalc.arrayListToArrayDouble3(upperArrayA);
////		double[] vetUpperB = ACRcalc.arrayListToArrayDouble3(upperArrayB);
////
////		// ImageProcessor ip1= imp1.getProcessor();
////
////		// provo ad usare un CurveFitter
////		CurveFitter lineFitter = new CurveFitter(vetUpperA, vetUpperB);
////		lineFitter.doFit(CurveFitter.STRAIGHT_LINE);
////		imp2.setRoi(new Line((1 - lineFitter.getParams()[0]) / lineFitter.getParams()[1], 1,
////				(imp2.getHeight() - 1 - lineFitter.getParams()[0]) / lineFitter.getParams()[1], imp2.getHeight() - 1));
////		double angleGrad = Math.toDegrees(Math.atan(1.0 / (lineFitter.getParams())[1]));
////		ACRlog.waitHere("mainLocalizer terminato");

	}

//	public static void removeDuplicates(int[] vetX, int[] vetY) {
//
//		int ax = 0;
//		int ay = 0;
//		for (int i1 = 0; i1 < vetX.length; i1++) {
//			ax = vetX[i1];
//			ay = vetY[i1];
//			for (int i2 = 1; i1 < vetX.length; i1++) {
//				if (ax)
//				
//				
//			}
//
//		}
//
//	}

	/**
	 * Ricerca della ROI con maggior segnale, evitando il bordo, per prevenire
	 * segnali digitali spuri. Restituisce il segnale mediato dell'acqua sulla roi
	 * latoXlato e le coordinate del centro
	 * 
	 * @param imp1
	 * @param lato
	 * @return
	 */
	public static double[] maximumSearch(ImagePlus imp1, int lato) {

		imp1.deleteRoi();

		int width = imp1.getWidth();
		int height = imp1.getHeight();
		double roimax = 0;
		int xmax = 0;
		int ymax = 0;
		// imp1.setRoi(86, 19, lato, lato);
		// IJ.run(imp1, "Add...", "value=500"); // questo serviva unicamente per i primi
		// test
		imp1.updateAndDraw();

		for (int i1 = 5; i1 < width - (lato + 5); i1++) {
			for (int i2 = 5; i2 < height - (lato + 5); i2++) {
				int px = i2;
				int py = i1;
				imp1.setRoi(px, py, lato, lato);
				double roimean = imp1.getStatistics().mean;
				if (roimean > roimax) {
					roimax = roimean;
					xmax = px;
					ymax = py;
				}
				// over2.addElement(imp2.getRoi()); // serviva per i test
			}
		}
		// ACRutils.waitHere("xmax= " + xmax + " ymax= " + ymax + "roimax= " + roimax);
		double[] out1 = new double[3];
		out1[0] = roimax;
		out1[1] = (double) xmax + (double) lato / 2;
		out1[2] = (double) ymax + (double) lato / 2;
		return out1;

	}

	/**
	 * Ricerca posizione e diametro Phantom per calcolo uniformita', utilizza un
	 * Canny Edge Detector
	 * 
	 * @param imp1
	 */
	public static void rectangleSearch1(ImagePlus imp1) {
		// faccio la ricerca delle posizioni fantoccio, tenendo conto che potrebbe
		// essere anche non allineato

		Overlay over2 = new Overlay();

		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));

		ACRcannyEdgeDetector mce = new ACRcannyEdgeDetector();
		mce.setGaussianKernelRadius(2.0f);
		mce.setLowThreshold(2.5f);
		mce.setHighThreshold(10.0f);
		mce.setContrastNormalized(false);

		ImagePlus imp2 = mce.process(imp1);
		imp2.show();
		ACRutils.zoom(imp2);

		imp2.setOverlay(over2);
		ACRlog.waitHere("rectangle search terminato");

	}

	public static int[] verticalSearch(ImagePlus imp1, double water, int xposition) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
		double halfwater = water / 2;
		int spessore = 1;
		imp1.deleteRoi();
		int height = imp1.getHeight();
		Line.setWidth(spessore);
		int x1 = xposition;
		int y1 = 0;
		int x2 = xposition;
		int y2 = height;
		imp1.setRoi(new Line(x1, y1, x2, y2));
		imp1.updateAndDraw();
		Roi roi1 = imp1.getRoi();
		double[] profi1 = ((Line) roi1).getPixels();
		boolean valido1 = false;
		int upperpoint = -1;
		for (int j1 = 0; j1 < profi1.length; j1++) {
			if (profi1[j1] < halfwater)
				valido1 = true;
			if (valido1 && (profi1[j1] > halfwater)) {
				upperpoint = j1;
				break;
			}
		}
		int lowerpoint = -1;
		boolean valido2 = false;
		for (int j1 = height - 1; j1 >= 0; j1--) {
			if (profi1[j1] < halfwater)
				valido2 = true;
			if (valido2 && (profi1[j1] > halfwater)) {
				lowerpoint = j1;
				break;
			}
		}

		int[] out1 = new int[2];
		out1[0] = lowerpoint;
		out1[1] = upperpoint;
		return out1;
	}

	public static int[] horizontalSearch(ImagePlus imp1, double water, int yposition) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
		double halfwater = water / 2;
		int spessore = 1;
		imp1.deleteRoi();
		int width = imp1.getWidth();
		Line.setWidth(spessore);
		int x1 = 0;
		int y1 = yposition;
		int x2 = width;
		int y2 = yposition;
		imp1.setRoi(new Line(x1, y1, x2, y2));
		imp1.updateAndDraw();
		Roi roi1 = imp1.getRoi();
		double[] profi1 = ((Line) roi1).getPixels();
		boolean valido1 = false;
		int upperpoint = -1;
		for (int j1 = 0; j1 < profi1.length; j1++) {
			if (profi1[j1] < halfwater)
				valido1 = true;
			if (valido1 && (profi1[j1] > halfwater)) {
				upperpoint = j1;
				break;
			}
		}
		int lowerpoint = -1;
		boolean valido2 = false;
		for (int j1 = width - 1; j1 >= 0; j1--) {
			if (profi1[j1] < halfwater)
				valido2 = true;
			if (valido2 && (profi1[j1] > halfwater)) {
				lowerpoint = j1;
				break;
			}
		}

		int[] out1 = new int[2];
		out1[0] = lowerpoint;
		out1[1] = upperpoint;
		return out1;
	}

	/**
	 * Display di un profilo
	 * 
	 * @param profile1
	 * @param sTitolo
	 */
	public static void displayPlot1(double[] profile1, String sTitolo) {

		Plot plot1 = new Plot(sTitolo, "pixel", "valore");
		plot1.add("LINE", profile1);

		plot1.setColor(Color.red);
		plot1.show();

		plot1.draw();

	}

	/**
	 * Genera istogramma punti
	 * 
	 * @param vet1
	 * @param matrix
	 * @return
	 */
	public static int[] getHistogram(int[] vet1, int matrix) {

		int[] histo = new int[matrix];
		for (int i1 = 0; i1 < vet1.length; i1++) {
			histo[vet1[i1]]++;
		}
		return (histo);
	}

	/**
	 * Genera istogramma punti
	 * 
	 * @param vet1
	 * @param matrix
	 * @return
	 */
	public static int[] getHistogram2(int[] vet1, int matrix) {

		int[] histo = new int[matrix];
		for (int i1 = 0; i1 < vet1.length; i1++) {
			histo[vet1[i1]]++;
		}
		ArrayList<Integer> lowerAA = new ArrayList<>();

		return (histo);
	}

	public void mainSliceDiameter(String path1, int slice, boolean step, boolean fast, boolean verbose, int timeout) {
		double maxFitError = +20;
//		double maxBubbleGapLimit = 2;

//		ACRutils.waitHere("mainSliceDiameter");

		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		if (imp1 == null)
			ACRlog.waitHere("imp1==null");
		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));

		// IW2AYV sposto con il fantoccio, in modo da testare la corretta acquisizione,
		// indipendente dal centro immagine!

		imp1.show();
		imp1.setRoi(22, 21, 153, 148);
		imp1.copy();
		imp1.setRoi(19, 21, 153, 148);
		imp1.cut();
		imp1.setRoi(18, 4, 153, 148);
		imp1.paste();

		ImagePlus imp2 = imp1.duplicate();
		ImagePlus imp3 = imp1.duplicate();
//		int[] dummy1 = new int[0];

		boolean step1 = true;
		boolean fast1 = false;
		boolean verbose1 = true;

		int[] out1 = ACRlocalizer.positionSearch2(imp2, maxFitError, step1, fast1, verbose1, timeout);

		int xphantom = (int) out1[0];
		int yphantom = (int) out1[1];
		int dphantom = (int) out1[2];

		int[] phantomCircle = new int[3];
		phantomCircle[0] = xphantom;
		phantomCircle[1] = yphantom;
		phantomCircle[2] = dphantom;

		if (step)
			ACRlog.waitHere("FIT DEL CERCHIO ESEGUITO, FANTOCCIO LOCALIZZATO", debug, timeout, fast);
		boolean step2 = true;
		boolean fast2 = false;
		boolean verbose2 = true;

		double[] out3 = ACRlocalizer.positionSearch3(imp3, phantomCircle, step2, fast2, verbose2, timeout);
		double[] out4 = new double[out3.length];
		for (int i1 = 0; i1 < out3.length; i1++) {
			out4[i1] = out3[i1] * dimPixel;
		}
		IJ.log("Quattro diametri finali in millimetri calcolati per la slice " + slice);
		ACRlog.logVector(out4, "out4");

	}

	public double[][] extractMax(double[][] vetIn, int key, boolean max) {

		double[] vetKey = new double[vetIn[0].length];
		for (int i1 = 0; i1 < vetKey.length; i1++) {
			vetKey[i1] = vetIn[key][i1];
			IJ.log("vetIn" + i1 + "," + vetIn[0][i1] + "," + vetIn[1][i1]);
		}
		ArrayList<double[]> borderPoints = new ArrayList<>();
		double[] pointxy = new double[2];
		double[] minmax = Tools.getMinMax(vetKey);
		if (max)
			IJ.log("cerchiamo il max= " + minmax[1]);
		else
			IJ.log("cerchiamo il min= " + minmax[0]);
		int count = 0;
		for (int i1 = 0; i1 < vetIn[0].length; i1++) {
			if (max) {
				if (Double.compare(vetIn[key][i1], minmax[1]) == 0) {
					pointxy[0] = vetIn[0][i1];
					pointxy[1] = vetIn[1][i1];
					IJ.log("aggiunto MAX " + vetIn[0][i1] + "," + vetIn[1][i1]);
					borderPoints.add(pointxy);
					count++;
				}
			} else {
				if (Double.compare(vetIn[key][i1], minmax[0]) == 0) {
					pointxy[0] = vetIn[0][i1];
					pointxy[1] = vetIn[1][i1];
					IJ.log("aggiunto MIN " + vetIn[0][i1] + "," + vetIn[1][i1]);
					borderPoints.add(pointxy);
					count++;
				}
			}
		}

		IJ.log("[borderPoints.size()= " + borderPoints.size() + "count= " + count);

		double[][] outIntArr = new double[2][borderPoints.size()];
		int i2 = 0;
		for (double[] n1 : borderPoints) {
			outIntArr[0][i2] = n1[0];
			outIntArr[1][i2] = n1[1];
			i2++;
		}
		ACRlog.logMatrix(outIntArr, "outIntArr");
		return outIntArr;

	}
}