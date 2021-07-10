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
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.Tools;

public class Geometric_Accuracy implements PlugIn {

	public static final boolean debug = true;
	public static final boolean big = true;

	public void run(String arg) {
		mainGeometry();
	}

	public void mainGeometry() {

		int timeout = 2000; // preme automaticamente OK ai messaggi durante i test

//		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
//		boolean[] defaults = { true, false, true, false, false, false, false, true, true, true, false, false, false,
//				true };
		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
		boolean[] defaults = { false, false, false, false, false, false, false, false, false, false, false, false,
				false, false };
		String[] headings = { "slices T1", "slices T2" };
		GenericDialog gd1 = new GenericDialog("GEOMETRIC ACCURACY");
		gd1.addCheckbox("ANIMAZIONE 2 sec", true);
		gd1.addCheckbox("STEP", true);
		gd1.addCheckbox("VERBOSE", true);
		gd1.addCheckbox("LOCALIZER", true);
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
		// IJ.run(imp2, "Rotate... ", "angle=6 grid=1 interpolation=Bilinear");
		//
		//
		IJ.run(imp2, "Rotate... ", "angle=-6 grid=1 interpolation=Bilinear");
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
			if (out1[0] > 0) {
				List<Integer> pointXY1 = new ArrayList<Integer>();
				pointXY1.add(a1);
				pointXY1.add(out1[0]);
				pointXY1.add(1);
				pointXY1.add(2);
				pointXY1.add(3);
				pointXY1.add(4);
				pointXY1.add(5);
				pointXY1.add(6);
				pointXY1.add(7);
				pointXY1.add(8);
				pointXY1.add(9);
				pointXY1.add(10);
				pointArrayXY.add(pointXY1);
				List<Integer> pointXY2 = new ArrayList<Integer>();
				pointXY2.add(a1);
				pointXY2.add(out1[1]);
				pointXY2.add(1);
				pointXY2.add(2);
				pointXY2.add(3);
				pointXY2.add(4);
				pointXY2.add(5);
				pointXY2.add(6);
				pointXY2.add(7);
				pointXY2.add(8);
				pointXY2.add(9);
				pointXY2.add(10);
				pointArrayXY.add(pointXY2);
			}
		}
		//
		// Ricerca dei bordi in direzione orizzontale, molti saranno doppioni, ma questo
		// non ci dovrebbe creare problemi (ed in ogni caso diventerebbe un casino
		// toglierli!)
		//
		for (int i1 = 0; i1 < height; i1++) { // era i1 +=3
			int b1 = i1;
			int[] out2 = horizontalSearch(imp2, max2, b1);

			if (out2[0] > 0) {
				List<Integer> pointXY3 = new ArrayList<Integer>();
				pointXY3.add(out2[0]);
				pointXY3.add(b1);
				pointXY3.add(1);
				pointXY3.add(2);
				pointXY3.add(3);
				pointXY3.add(4);
				pointXY3.add(5);
				pointXY3.add(6);
				pointXY3.add(7);
				pointXY3.add(8);
				pointXY3.add(9);
				pointXY3.add(10);
				pointArrayXY.add(pointXY3);
				List<Integer> pointXY4 = new ArrayList<Integer>();
				pointXY4.add(out2[1]);
				pointXY4.add(b1);
				pointXY4.add(1);
				pointXY4.add(2);
				pointXY4.add(3);
				pointXY4.add(4);
				pointXY4.add(5);
				pointXY4.add(6);
				pointXY4.add(7);
				pointXY4.add(8);
				pointXY4.add(9);
				pointXY4.add(10);
				pointArrayXY.add(pointXY4);
			}
		}
		//
		//
		//
		// ACRlog.waitHere("pointArrayXY.size= " + pointArrayXY.size());

		List<Integer> pointList = new ArrayList<>();
		int count = 0;
		int[][] rotatedPoints = new int[pointArrayXY.size()][];
		boolean shot = true;

		for (List<Integer> tempor : pointArrayXY) {
			pointList = tempor;
			if (shot) {
				ACRlog.waitHere("pointList.size()= " + pointList.size());
				shot = false;
			}
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

		for (int i1 = 0; i1 < rotatedPoints.length; i1++) {
			// IJ.log("lower= " + lowerArrayA.get(i1) + "," + lowerArrayB.get(i1));
			imp2.setRoi(new PointRoi(rotatedPoints[i1][0], rotatedPoints[i1][1], "tiny red dot"));
			over2.addElement(imp2.getRoi());
			imp2.killRoi();
		}
		// mi piacciono i rotatedPoints perche'li posso impunemente importare in
		// excel2003, altrimenti dovrei utilizzare excel2007 perche'ha piu'colonne!
		// ma alla lunga ho ceduto
		//
		// lo strano problema dei punti non coincidenti con i vertici NON E' AFFATTO
		// LEGATO AI DOPPIONI, che sono troppo pigro per cercare di rimuovere, e poi
		// "esercitano" il programma!
		//
		// estraggo l'arrayX ed arrayY dalle coordinate dei punti

		int[] vetX = new int[rotatedPoints.length];
		int[] vetY = new int[rotatedPoints.length];
		for (int i1 = 0; i1 < rotatedPoints.length; i1++) {
			vetX[i1] = rotatedPoints[i1][0];
			vetY[i1] = rotatedPoints[i1][1];
		}

		int minx = ACRutils.minsearch(vetX)[0];
		int maxx = ACRutils.maxsearch(vetX)[0];
		int miny = ACRutils.minsearch(vetY)[0];
		int maxy = ACRutils.maxsearch(vetY)[0];

		int px = 0;
		int py = 0;
		int ax = 0;
		int ay = 0;
		int bx = 191;
		int by = 0;
		int cx = 191;
		int cy = 191;
		int dx = 0;
		int dy = 191;

		//
		// FORSE HO TROVATO IL MODO: PER OGNI PUNTO SI CALCOLA:
		// ABS(distanza da un vertice su X)+ABS(distanza da un vertice su Y)
		// si hanno cosi' 4 colonne, coincidenti con le coordinate X efd Y del punto.
		// Il minimo per ogni colonna rappresenta quel particolare vertice
		//
		// SE FUNZIONA E'UNA FIGATA, PURE ELEGANTE E LOGICA COME SOLUZIONE!
		//
		//
		for (int i1 = 0; i1 < rotatedPoints.length; i1++) {
			px = rotatedPoints[i1][0];
			py = rotatedPoints[i1][1];
			//
			rotatedPoints[i1][2] = 0;
			rotatedPoints[i1][3] = Math.abs(px - ax) + Math.abs(py - ay); // vertice a
			rotatedPoints[i1][4] = Math.abs(px - bx) + Math.abs(py - by); // vertice b
			rotatedPoints[i1][5] = Math.abs(px - cx) + Math.abs(py - cy); // vertice c
			rotatedPoints[i1][6] = Math.abs(px - dx) + Math.abs(py - dy); // vertice d
			rotatedPoints[i1][7] = 0;
			rotatedPoints[i1][8] = i1;
			rotatedPoints[i1][9] = 0;
			rotatedPoints[i1][10] = 0;
//
//			STRANAMENTE il calcolo qui sopra da'risultati migliori del calcolo dell'ipotenusa			
//
//			rotatedPoints[i1][2] = 0;
//			rotatedPoints[i1][3] = (int) Math.sqrt((px - ax) * (px - ax) + (py - ay) * (py - ay)); // vertice a
//			rotatedPoints[i1][4] = (int) Math.sqrt((px - bx) * (px - bx) + (py - by) * (py - by)); // vertice b
//			rotatedPoints[i1][5] = (int) Math.sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy)); // vertice c
//			rotatedPoints[i1][6] = (int) Math.sqrt((px - dx) * (px - dx) + (py - dy) * (py - dy)); // vertice d
//			rotatedPoints[i1][7] = 0;
//			rotatedPoints[i1][8] = i1;
//			rotatedPoints[i1][9] = 0;
//			rotatedPoints[i1][10] = 0;
		}

		ACRlog.printMatrix(rotatedPoints, "rotatedPoints");
		//
		// disegno la boundingbox parallela agli assi, per ricavarne il centerOfMass che
		// in seguito mi serve per capire in che direzione e' ruotato il fantoccio
		//
		imp2.setRoi(minx, miny, maxx - minx, maxy - miny);
		ImageProcessor ip2 = imp2.getProcessor();

		ImageStatistics stat2 = ip2.getStatistics();
		double mx = stat2.xCenterOfMass;
		double my = stat2.yCenterOfMass;

		imp2.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();

		// estraggo dalla matrice gli array con il calcolo per i vertici
		int[] vertexa = ACRutils.matrixExtractor(rotatedPoints, 3);
		int[] vertexb = ACRutils.matrixExtractor(rotatedPoints, 4);
		int[] vertexc = ACRutils.matrixExtractor(rotatedPoints, 5);
		int[] vertexd = ACRutils.matrixExtractor(rotatedPoints, 6);

		ACRlog.logVector(vertexa, "vertexa");
		ACRlog.logVector(vertexb, "vertexb");
		ACRlog.logVector(vertexc, "vertexc");
		ACRlog.logVector(vertexd, "vertexd");

		int[] posmina = ACRutils.minsearch(vertexa);
		int[] posminb = ACRutils.minsearch(vertexb);
		int[] posminc = ACRutils.minsearch(vertexc);
		int[] posmind = ACRutils.minsearch(vertexd);

		ACRlog.logVector(posmina, "posmina");
		ACRlog.logVector(posminb, "posminb");
		ACRlog.logVector(posminc, "posminc");
		ACRlog.logVector(posmind, "posmind");

		// VERDE
		int AX = rotatedPoints[posmina[1]][0];
		int AY = rotatedPoints[posmina[1]][1];
		// GIALLO
		int BX = rotatedPoints[posminb[1]][0];
		int BY = rotatedPoints[posminb[1]][1];
		// ROSSO
		int CX = rotatedPoints[posminc[1]][0];
		int CY = rotatedPoints[posminc[1]][1];
		// AZZURRO
		int DX = rotatedPoints[posmind[1]][0];
		int DY = rotatedPoints[posmind[1]][1];

		// int[][] sortedPoints = null;
		//
		// sortedPoints = ACRutils.minsort(rotatedPoints, key); /// NON SERVE A UN CAXXO
		//
		// ora che ho ordinato secondo X, cerco il min ed il max di x, saranno
		// certamente piu'di un punto, tra questi punti scegliero' quello che ha il min
		// Y eccetera. Spero che il tutto mi fornisca le coordinate dei vertici del
		// rettangolo ruotato.
		//

		//
		// ROSSO VERTICE con maxima x e relativa minima y
		//
//		int selX = 0;
//		int selY = 1;
//		int[][] out1 = ACRutils.searchValue(rotatedPoints, maxx, selX);
//		ACRlog.logMatrix(out1, "ROSSO out1 maxx");
//		int CX = out1[0][0];
//		IJ.log("CX= " + CX);
//		int[] aux10 = ACRutils.matrixExtractor(out1, selY);
//		int CY = 0;
//
//		IJ.log("guardo se " + CX + " minore di " + cx);
//		if (CX < cx) {
//			CY = ACRutils.maxsearch(aux10)[0];
//			IJ.log("cerco il massimo su Y e trovo= " + CY);
//		} else {
//			CY = ACRutils.minsearch(aux10)[0];
//			IJ.log("cerco il minimo su Y e trovo= " + CY);
//		}
//		//
//		// VERDE VERTICE con minima x e relativa minima y
//		//
//		int[][] out2 = ACRutils.searchValue(rotatedPoints, minx, selX);
//		ACRlog.logMatrix(out2, "VERDE out2 minx");
//		int AX = out2[0][0];
//		IJ.log("AX= " + AX);
//		int[] aux20 = ACRutils.matrixExtractor(out2, selY);
//		int AY = 0;
//		if (AX > cx)
//			AY = ACRutils.maxsearch(aux20)[0];
//		else
//			AY = ACRutils.minsearch(aux20)[0];
//		//
//		// GIALLO VERTICE con maxima y e relativa massima x
//		//
//		int[][] out3 = ACRutils.searchValue(rotatedPoints, maxy, selY);
//		ACRlog.logMatrix(out3, "GIALLO out3 maxy");
//		int BY = out3[0][1];
//		IJ.log("BY= " + BY);
//		int[] aux30 = ACRutils.matrixExtractor(out3, selX);
//		int BX = 0;
//		if (BY < cy)
//			BX = ACRutils.minsearch(aux30)[0];
//		else
//			BX = ACRutils.maxsearch(aux30)[0];
//		//
//		// AZZURRO VERTICE con maxima y e relativa minima x
//		//
//		int[][] out4 = ACRutils.searchValue(rotatedPoints, miny, selY);
//		ACRlog.logMatrix(out4, "AZZURRO out4 miny");
//		int DY = out4[0][1];
//		IJ.log("DY= " + DY);
//		int[] aux40 = ACRutils.matrixExtractor(out4, selX);
//		int DX = 0;
//		if (DY < cy)
//			DX = ACRutils.minsearch(aux40)[0];
//		else
//			DX = ACRutils.maxsearch(aux40)[0];
//
//		IJ.log("boundingCenter= " + cx + "," + cy);
////
//
		ACRutils.plotPoints(imp2, over2, (int) AX, (int) AY, Color.GREEN, 4, 3);
		ACRutils.plotPoints(imp2, over2, (int) BX, (int) BY, Color.YELLOW, 4, 3);
		ACRutils.plotPoints(imp2, over2, (int) CX, (int) CY, Color.RED, 4, 3);
		ACRutils.plotPoints(imp2, over2, (int) DX, (int) DY, Color.CYAN, 4, 3);
//
		ACRlog.waitHere("VERDE= " + AX + " , " + AY + " GIALLO= " + BX + " , " + BY + " ROSSO= " + CX + " , " + CY
				+ " AZZURRO= " + DX + " , " + DY);

////		ACRlog.waitHere();
		//
		// ora vado a calcolare i dati del RotatedRectangle VERIFICARE SE CON I DOUBLE MIGLIORA!
		//

		double MX = Math.round((double) (CX + DX) / 2.0);
		double MY = Math.round((double) (CY + DY) / 2.0);
		double PX = Math.round((double) (AX + BX) / 2.0);
		double PY = Math.round((double) (AY + BY) / 2.0);
		double LL = Math.sqrt((AX - BX) * (AX - BX) + (AY - BY) * (AX - BY));

		ACRutils.plotPoints(imp2, over2, (int) MX, (int) MY, Color.ORANGE, 3, 4);
		ACRutils.plotPoints(imp2, over2, (int) PX, (int) PY, Color.ORANGE, 3, 4);

		imp2.setRoi(new RotatedRectRoi(MX, MY, PX, PY, LL));

		ACRlog.waitHere();
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
		int startpoint = -1;
		for (int j1 = 0; j1 < profi1.length; j1++) {
			if (profi1[j1] < halfwater)
				valido1 = true;
			if (valido1 && (profi1[j1] > halfwater)) {
				startpoint = j1;
				break;
			}
		}
		int endpoint = -1;
		boolean valido2 = false;
		for (int j1 = height - 1; j1 >= 0; j1--) {
			if (profi1[j1] < halfwater)
				valido2 = true;
			if (valido2 && (profi1[j1] > halfwater)) {
				endpoint = j1;
				break;
			}
		}
		int[] out1 = new int[2];
		out1[0] = endpoint;
		out1[1] = startpoint;
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
		int startpoint = -1;
		for (int j1 = 0; j1 < profi1.length; j1++) {
			if (profi1[j1] < halfwater)
				valido1 = true;
			if (valido1 && (profi1[j1] > halfwater)) {
				startpoint = j1;
				break;
			}
		}
		int endpoint = -1;
		boolean valido2 = false;
		for (int j1 = width - 1; j1 >= 0; j1--) {
			if (profi1[j1] < halfwater)
				valido2 = true;
			if (valido2 && (profi1[j1] > halfwater)) {
				endpoint = j1;
				break;
			}
		}
		int[] out1 = new int[2];
		out1[0] = endpoint;
		out1[1] = startpoint;
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