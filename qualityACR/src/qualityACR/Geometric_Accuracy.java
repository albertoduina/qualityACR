package qualityACR;

import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
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
import ij.text.TextWindow;
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
		gd1.addCheckbox("ANIMAZIONE 2 sec", false);
		gd1.addCheckbox("STEP", true);
		gd1.addCheckbox("VERBOSE", true);
		gd1.addCheckbox("LOCALIZER", true);
		gd1.addCheckboxGroup(7, 2, labels, defaults, headings);

		gd1.showDialog();
		if (gd1.wasCanceled()) {
			ACRlog.waitHere("premuto cancel");
			return;
		}

		IJ.log(" ");
		Frame f1 = WindowManager.getFrame("Log");
		if (f1 != null) {
			f1.setSize(100, 400);
			f1.setLocation(10, 10);
		}
//		String str1 = gd1.getNextRadioButton();
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
			mainLocalizer(pathLocalizer, step, fast, verbose, timeout);

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
	public void mainLocalizer(String path1, boolean step, boolean fast, boolean verbose, int timeout) {

		IJ.log("mainLocalizer");
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		if (big)
			ACRutils.zoom(imp2);

		//
		// ========== TAROCCAMENTO IMMAGINE =============

		// IJ.run(imp2, "Flip Horizontally", "");
		//
		IJ.run(imp2, "Rotate... ", "angle=3 grid=1 interpolation=Bilinear");
		//
		// IJ.run(imp2, "Rotate... ", "angle=-6 grid=1 interpolation=Bilinear");
		//
		// ===============================================
		//
		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));

		int width = imp2.getWidth();
		int height = imp2.getHeight();
		int lato = 30; // lato con cui cerco lo pseudomassimo
		double[] max1 = maximumSearch(imp2, lato);
		double max2 = max1[0];

		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);

		//
		// Ricerca dei bordi in direzione verticale
		//

		// List importato dalla libreria java.util (ne esiste anche un altra)
		// si tratta di un vettore arrayList a due dimensioni [][]
		List<List<Integer>> pointArrayXY = new ArrayList<List<Integer>>();
		//
		for (int i1 = 0; i1 < width; i1++) { // era i1 +=3
			int a1 = i1;
			int[] out1 = ACRlocalizer.verticalSearch(imp2, max2, a1, false);
			if (out1 != null) {
				List<Integer> pointXY1 = new ArrayList<Integer>();
				pointXY1.add(a1);
				pointXY1.add(out1[0]);
				pointXY1.add(2);
				pointXY1.add(3);
				pointXY1.add(4);
				pointXY1.add(5);
				pointArrayXY.add(pointXY1);
				List<Integer> pointXY2 = new ArrayList<Integer>();
				pointXY2.add(a1);
				pointXY2.add(out1[1]);
				pointXY2.add(2);
				pointXY2.add(3);
				pointXY2.add(4);
				pointXY2.add(5);
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
			int[] out2 = ACRlocalizer.horizontalSearch(imp2, max2, b1, false);

			if (out2 != null) {
				List<Integer> pointXY3 = new ArrayList<Integer>();
				pointXY3.add(out2[0]);
				pointXY3.add(b1);
				pointXY3.add(2);
				pointXY3.add(3);
				pointXY3.add(4);
				pointXY3.add(5);
				pointArrayXY.add(pointXY3);
				List<Integer> pointXY4 = new ArrayList<Integer>();
				pointXY4.add(out2[1]);
				pointXY4.add(b1);
				pointXY4.add(2);
				pointXY4.add(3);
				pointXY4.add(4);
				pointXY4.add(5);
				pointArrayXY.add(pointXY4);
			}
		}
		// ACRlog.waitHere("pointArrayXY.size= " + pointArrayXY.size());
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
		if (verbose) {
			for (int i1 = 0; i1 < rotatedPoints.length; i1++) {
				imp2.setRoi(new PointRoi(rotatedPoints[i1][0], rotatedPoints[i1][1], "medium yellow dot"));
				over2.addElement(imp2.getRoi());
				imp2.killRoi();
			}
		}

		if (step)
			ACRlog.waitHere("riportati in giallo su immagine i bordi trovati", step, timeout, fast);

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
		// si hanno cosi' 4 colonne, coincidenti con le coordinate X ed Y del punto.
		// Il minimo per ogni colonna rappresenta quel particolare vertice
		//
		// SE FUNZIONA E'UNA FIGATA, PURE ELEGANTE E LOGICA COME SOLUZIONE!
		//
		//
		for (int i1 = 0; i1 < rotatedPoints.length; i1++) {
			px = rotatedPoints[i1][0];
			py = rotatedPoints[i1][1];
			//
			rotatedPoints[i1][2] = Math.abs(px - ax) + Math.abs(py - ay); // vertice a
			rotatedPoints[i1][3] = Math.abs(px - bx) + Math.abs(py - by); // vertice b
			rotatedPoints[i1][4] = Math.abs(px - cx) + Math.abs(py - cy); // vertice c
			rotatedPoints[i1][5] = Math.abs(px - dx) + Math.abs(py - dy); // vertice d
//
//			STRANAMENTE il calcolo qui sopra da'risultati migliori del calcolo dell'ipotenusa, piu'complicato			
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

		if (verbose)
			ACRlog.printMatrix(rotatedPoints, "rotatedPoints");
		//
		// disegno la boundingbox parallela agli assi, per ricavarne il centerOfMass che
		// in seguito mi serve per capire in che direzione e' ruotato il fantoccio
		//
		imp2.setRoi(minx, miny, maxx - minx, maxy - miny);

//		ImageProcessor ip2 = imp2.getProcessor();
//
//		ImageStatistics stat2 = ip2.getStatistics();
//		double mx = stat2.xCenterOfMass;
//		double my = stat2.yCenterOfMass;

		imp2.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();

		if (step)
			ACRlog.waitHere("riportato in verde il BoundingRectangle non orientato", step, timeout, fast);

		// estraggo dalla matrice gli array con il calcolo per i vertici
		int[] vertexa = ACRutils.matrixExtractor(rotatedPoints, 2);
		int[] vertexb = ACRutils.matrixExtractor(rotatedPoints, 3);
		int[] vertexc = ACRutils.matrixExtractor(rotatedPoints, 4);
		int[] vertexd = ACRutils.matrixExtractor(rotatedPoints, 5);
		if (verbose) {
			ACRlog.logVector(vertexa, "vertexa");
			ACRlog.logVector(vertexb, "vertexb");
			ACRlog.logVector(vertexc, "vertexc");
			ACRlog.logVector(vertexd, "vertexd");
		}

		int[] posmina = ACRutils.minsearch(vertexa);
		int[] posminb = ACRutils.minsearch(vertexb);
		int[] posminc = ACRutils.minsearch(vertexc);
		int[] posmind = ACRutils.minsearch(vertexd);
		if (verbose) {
			ACRlog.logVector(posmina, "posmina");
			ACRlog.logVector(posminb, "posminb");
			ACRlog.logVector(posminc, "posminc");
			ACRlog.logVector(posmind, "posmind");
		}

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
		ACRutils.plotPoints(imp2, over2, (int) AX, (int) AY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp2, over2, (int) BX, (int) BY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp2, over2, (int) CX, (int) CY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp2, over2, (int) DX, (int) DY, Color.RED, 4, 4);
//
		if (step)
			ACRlog.waitHere("Riportati in i vertici oggetto trovati A= " + AX + "," + AY + " B= " + BX + "," + BY
					+ " C= " + CX + "," + CY + " D= " + DX + "," + DY, step, timeout, fast);

		double MX = Math.round((double) (CX + DX) / (double) 2);
		double MY = Math.round((double) (CY + DY) / (double) 2);
		double PX = Math.round((double) (AX + BX) / (double) 2);
		double PY = Math.round((double) (AY + BY) / (double) 2);
		double LL = Math.sqrt((AX - BX) * (AX - BX) + (AY - BY) * (AX - BY));

		ACRutils.plotPoints(imp2, over2, (int) MX, (int) MY, Color.CYAN, 2, 4);
		ACRutils.plotPoints(imp2, over2, (int) PX, (int) PY, Color.CYAN, 2, 4);

		over2.clear();
		ACRutils.plotPoints(imp2, over2, (int) AX, (int) AY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp2, over2, (int) BX, (int) BY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp2, over2, (int) CX, (int) CY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp2, over2, (int) DX, (int) DY, Color.RED, 4, 4);

		imp2.setRoi(new RotatedRectRoi(MX, MY, PX, PY, LL));
		imp2.getRoi().setStrokeColor(Color.RED);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();

		if (step)
			ACRlog.waitHere("Riportato in rosso il RotatedRectangle trovato, piu' o meno coincidente con l'oggetto",
					step, timeout, fast);

		double slope = (MX - PX) / (MY - PY);
		// IJ.log("slope= " + slope);
		double latoA = 12;
		double latoB = latoA * slope;
		double xoffset = Math.sqrt(latoB * latoB + latoA * latoA);
		// il nuovo punto da cui passare con la retta diventa
		double RX = MX + xoffset;
		double RY = MY;

		// ACRutils.plotPoints(imp2, over2, (int) RX, (int) RY, Color.GREEN, 3, 5);

		double edgeLeft = 0.;
		double edgeRight = width;
		double edgeBottom = 0.;
		double edgeTop = height;
		double b = RX - slope * RY;

		double y1src = -1 / 4 * (double) height;
		double x1src = slope * y1src + b;

		double y0src = 5 * (double) height / 4;
		double x0src = slope * y0src + b;
		double[] clippings1 = ACRgraphic.liangBarsky(edgeLeft, edgeRight, edgeBottom, edgeTop, x0src, y0src, x1src,
				y1src);

		imp2.setRoi(new Line(clippings1[0], clippings1[1], clippings1[2], clippings1[3]));
		imp2.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp2.getRoi());

		double dim1 = dimPixel * profAnal(imp2, step, fast, verbose, timeout);
		imp2.killRoi();
		if (step)
			ACRlog.waitHere("Profilo analizzato");
		double SX = MX - xoffset;
		double SY = MY;

		// ACRutils.plotPoints(imp2, over2, (int) SX, (int) SY, Color.GREEN, 3, 5);

		b = SX - slope * SY;
		y1src = -1 / 4 * (double) height;
		x1src = slope * y1src + b;

		y0src = 5 * (double) height / 4;
		x0src = slope * y0src + b;
		double[] clippings2 = ACRgraphic.liangBarsky(edgeLeft, edgeRight, edgeBottom, edgeTop, x0src, y0src, x1src,
				y1src);

		imp2.setRoi(new Line(clippings2[0], clippings2[1], clippings2[2], clippings2[3]));
		imp2.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp2.getRoi());
		double dim2 = dimPixel * profAnal(imp2, step, fast, verbose, timeout);
		imp2.killRoi();

		if (step)
			ACRlog.waitHere("Profilo analizzato");

		IJ.log("dim1= " + dim1 + " di2= " + dim2);
		ACRlog.waitHere("dim1= " + dim1 + " dim2= " + dim2 + " misurate in millimetri");
	}

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

	/**
	 * distanza di un punto da una retta
	 * 
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public double pDistance(double x, double y, double x1, double y1, double x2, double y2) {

		double A = x - x1;
		double B = y - y1;
		double C = x2 - x1;
		double D = y2 - y1;

		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = -1;
		if (len_sq != 0) // in case of 0 length line
			param = dot / len_sq;

		double xx, yy;

		if (param < 0) {
			xx = x1;
			yy = y1;
		} else if (param > 1) {
			xx = x2;
			yy = y2;
		} else {
			xx = x1 + param * C;
			yy = y1 + param * D;
		}

		double dx = x - xx;
		double dy = y - yy;
		return (double) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Altra soluzione di distanza da un segmento
	 * 
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private static double distBetweenPointAndLine(double x, double y, double x1, double y1, double x2, double y2) {
		// A - the standalone point (x, y)
		// B - start point of the line segment (x1, y1)
		// C - end point of the line segment (x2, y2)
		// D - the crossing point between line from A to BC

		double AB = distBetween(x, y, x1, y1);
		double BC = distBetween(x1, y1, x2, y2);
		double AC = distBetween(x, y, x2, y2);

		// Heron's formula
		double s = (AB + BC + AC) / 2;
		double area = (double) Math.sqrt(s * (s - AB) * (s - BC) * (s - AC));

		// but also area == (BC * AD) / 2
		// BC * AD == 2 * area
		// AD == (2 * area) / BC
		// TODO: check if BC == 0
		double AD = (2 * area) / BC;
		return AD;
	}

	private static double distBetween(double x, double y, double x1, double y1) {
		double xx = x1 - x;
		double yy = y1 - y;

		return (double) Math.sqrt(xx * xx + yy * yy);
	}

	/**
	 * Analizza il profilo di una linea, cercando di determinare le dimensioni
	 * dell'oggetto. La linea deve essere attiva come ROI sull'immagine passata
	 * 
	 * @param imp2
	 * @return
	 */
	public static double profAnal(ImagePlus imp2, boolean step, boolean fast, boolean verbose, int timeout) {
		// e come caspita potrei abbreviare profile analyzer? Ci sono 2 soluzioni:
		// analProf oppure profAnal ai posteri(ori) la scelta!!
		int pseudomaxlen = 3; // dimensioni roi di ricerca pseudomassimo
		double[][] decomposed3v = ACRutils.decomposer3v(imp2);
		double[][] zeropadded3v = ACRutils.zeropadProfile3v(decomposed3v);
		double[][] vetout = ACRlocalizer.FWHMpoints3vNEW(zeropadded3v, pseudomaxlen, "PROFILO LINEA", step, fast,
				verbose);
//		if (step) ACRlog.waitHere("profilo analizzato");
		//
		// posso misurare il diametro "con precisione?" utilizzando i due punti
		// interpolati
		//
		double dimension = (vetout[0][3] - vetout[0][2]); // IN PIXEL
		return dimension;
	}
}