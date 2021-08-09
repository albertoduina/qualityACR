package qualityACR;

import java.awt.Color;
import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

	public void run(String arg) {
		mainGeometry();
	}

	public void mainGeometry() {
		Properties prop = ACRutils.readConfigACR();
		int timeout = 0; // preme automaticamente OK ai messaggi durante i test
		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
		boolean[] defaults = { false, false, false, false, false, false, false, false, false, false, false, false,
				false, false };
		String[] headings = { "slices T1", "slices T2" };
		boolean fastdefault = false;
		boolean stepdefault = false;
		boolean verbosedefault = false;
		boolean localizerdefault = false;
		boolean[] T1 = new boolean[7];
		boolean[] T2 = new boolean[7];

		if (prop != null) {
			fastdefault = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.fast"));
			stepdefault = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.step"));
			verbosedefault = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.verbose"));
			localizerdefault = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.geomLocalizer"));
			for (int i1 = 0; i1 < 7; i1++) {
				T1[i1] = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.SliceT1[" + i1 + "]"));
				T2[i1] = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.SliceT2[" + i1 + "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
				defaults[count++] = T1[i1];
				defaults[count++] = T2[i1];
			}
		}

		GenericDialog gd1 = new GenericDialog("GEOMETRIC ACCURACY");
		gd1.addCheckbox("ANIMAZIONE 2 sec", fastdefault);
		gd1.addCheckbox("STEP", stepdefault);
		gd1.addCheckbox("VERBOSE", verbosedefault);
		gd1.addCheckbox("LOCALIZER", localizerdefault);
		gd1.addCheckboxGroup(7, 2, labels, defaults, headings);
		gd1.showDialog();
		if (gd1.wasCanceled()) {
			ACRlog.waitHere("premuto cancel");
			return;
		}

		Frame f1 = WindowManager.getFrame("Log");
		if (f1 != null) {
			f1.setSize(100, 400);
			f1.setLocation(10, 10);
		}
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

		if (fast)
			timeout = 2000;

		// scrive nel config file
		if (prop == null)
			prop = new Properties();
		prop.setProperty("Geometric_Accuracy.fast", "" + fast);
		prop.setProperty("Geometric_Accuracy.step", "" + step);
		prop.setProperty("Geometric_Accuracy.verbose", "" + verbose);
		prop.setProperty("Geometric_Accuracy.geomLocalizer", "" + geomLocalizer);
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Geometric_Accuracy.SliceT1[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT1[i1];
			prop.setProperty(aux1, aux2);
		}
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Geometric_Accuracy.SliceT2[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT2[i1];
			prop.setProperty(aux1, aux2);
		}

		try {
			ACRutils.writeConfigACR(prop);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// leggo i nomi di tutti i file presenti (dovrebbero essere 15)
		String pathLocalizer = "";
		String tmpFolderPath = IJ.getDirectory("temp");
		String completePath = tmpFolderPath + "ACRlist.tmp";
		String[] vetPath = ACRutils.readStringArrayFromFile(completePath);
		String pathReport = vetPath[4] + "\\Report1.txt";

		String[] listLocalizer = ACRinputOutput.readStackPathToSortedList(vetPath[0], "T1");
		if (listLocalizer != null)
			pathLocalizer = listLocalizer[0];
		String[] sortedListT1 = ACRinputOutput.readStackPathToSortedList(vetPath[1], "T1");
		if (sortedListT1 == null)
			IJ.log(ACRlog.qui() + "sortedListT1 ==null");
		String[] sortedListT2 = ACRinputOutput.readStackPathToSortedList(vetPath[2], "T2");
		if (sortedListT2 == null)
			IJ.log(ACRlog.qui() + "sortedListT2 ==null");

		// DEVO creare un nuovo report, senno' che controllo faccio?
		if (!ACRlog.initLog(pathReport))
			return;

		// elaborazione file selezionati dall'operatore
		if (geomLocalizer) {
			mainLocalizer(pathLocalizer, pathReport, step, verbose, timeout);

		}

		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log(ACRlog.qui() + "elaborazione slice T1 numero " + i1);
				mainSliceDiameter(sortedListT1[i1], pathReport, i1, step, fast, verbose, timeout);
			}
		}

		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log(ACRlog.qui() + "==================");
				IJ.log(ACRlog.qui() + "elaborazione slice T2 numero " + i1);
				mainSliceDiameter(sortedListT2[i1], pathReport, i1, step, fast, verbose, timeout);
			}
		}
		ACRlog.waitHere("GEOMETRIC_ACCURACY TERMINATA");
	}

	/**
	 * Elaborazione immagine localizer per ottenere lunghezza fantoccio
	 * 
	 * @param path1
	 */
	public void mainLocalizer(String path1, String pathReport, boolean step, boolean verbose, int timeout) {

		// questa dovrebbe essere l'apertura comune a tutte le main delle varie classi
		// apertura immagine, display, zoom
		// chiamata prima subroutine passando l'immagine pronta
		// eccetraz ecceteraz

		IJ.log(ACRlog.qui() + "START>");
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);

		//
		// ========== TAROCCAMENTO IMMAGINE =============
		//
		// RUOTANDO L'IMMAGINE CAMBIA(va) IL RISULTATO!!!!!
		//
		// IJ.run(imp2, "Flip Horizontally", "");
		//
		IJ.run(imp2, "Rotate... ", "angle=8 grid=1 interpolation=Bilinear");
		//
		// IJ.run(imp2, "Rotate... ", "angle=-8 grid=1 interpolation=Bilinear");
		//
		// ===============================================
		//

		double[] vetout1 = localizerImageLength(imp2, step, verbose, timeout);
		double dim1 = vetout1[0];
		double dim2 = vetout1[0];

		if (step)
			ACRlog.waitHere(" Profilo analizzato", debug, timeout);

		IJ.log(ACRlog.qui() + "END>  lunghezzaFantoccio 1= " + IJ.d2s(dim1, 3) + " mm");
		IJ.log(ACRlog.qui() + "END>  lunghezzaFantoccio 2= " + IJ.d2s(dim2, 3) + " mm");

		/// PROBABILE SALVATAGGIO IMMAGINE PER REPORT HTML
//		ImagePlus imp = IJ.getImage();
//		IJ.saveAs(imp, "PNG", "D:/Dati/ACR_TEST/Study_1_20210527/REPORTS/localizer.png");
		
		

		ACRlog.appendLog(pathReport,
				"lunghezzaFantoccio1= " + IJ.d2s(dim1, 3) + " lunghezzaFantoccio2= " + IJ.d2s(dim2, 3) + " mm");
//		ACRlog.waitHere("dim1= " + dim1 + " dim2= " + dim2 + " mm");
	}

	/**
	 * Elaborazione immagine localizer per ottenere lunghezza fantoccio
	 * 
	 * @param path1
	 */
	public double[] localizerImageLength(ImagePlus imp1, boolean step, boolean verbose, int timeout) {

		IJ.log(ACRlog.qui() + "START>");

		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));

		int width = imp1.getWidth();
		int height = imp1.getHeight();
		int latoRoiPseudomax = 30;
		double[] max1 = maximumSearch(imp1, latoRoiPseudomax);
		double max2 = max1[0] / 2;

		Overlay over2 = new Overlay();
		imp1.setOverlay(over2);

		//
		// Ricerca dei bordi in direzione verticale
		//

		// List importato dalla libreria java.util (ne esiste anche un altra)
		// si tratta di un vettore arrayList a due dimensioni [][]
		List<List<Integer>> pointArrayXY = new ArrayList<List<Integer>>();
		//
		for (int i1 = 0; i1 < width; i1++) { // era i1 +=3
			int a1 = i1;
			int[] out1 = ACRlocalizer.verticalSearch(imp1, max2, a1, false);
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

				Roi pr1 = new Roi(a1, out1[0], 1, 1);
				imp1.setRoi(pr1);
				imp1.getRoi().setFillColor(Color.GREEN);
				over2.addElement(imp1.getRoi());
				imp1.killRoi();
				Roi pr2 = new Roi(a1, out1[1], 1, 1);
				imp1.setRoi(pr2);
				imp1.getRoi().setFillColor(Color.GREEN);
				over2.addElement(imp1.getRoi());
				imp1.killRoi();
			}

		}

		//
		// Ricerca dei bordi in direzione orizzontale, molti saranno doppioni, ma questo
		// non ci dovrebbe creare problemi (ed in ogni caso diventerebbe un casino
		// toglierli!)
		//
		for (int i1 = 0; i1 < height; i1++) { // era i1 +=3
			int b1 = i1;
			int[] out2 = ACRlocalizer.horizontalSearch(imp1, max2, b1, false);

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

				Roi pr1 = new Roi(out2[0], b1, 1, 1);
				imp1.setRoi(pr1);
				imp1.getRoi().setFillColor(Color.RED);
				over2.addElement(imp1.getRoi());
				imp1.killRoi();
				Roi pr2 = new Roi(out2[1], b1, 1, 1);
				imp1.setRoi(pr2);
				imp1.getRoi().setFillColor(Color.RED);
				over2.addElement(imp1.getRoi());
				imp1.killRoi();

			}

		}
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
		if (step)
			ACRlog.waitHere("riportati in giallo su immagine i bordi trovati", step, timeout);

		// i rotatedPoints vengono buoni se si vuole esportarli in excel2003 che ha solo
		// 256 colonne

		int[] vetX = new int[rotatedPoints.length];
		int[] vetY = new int[rotatedPoints.length];
		for (int i1 = 0; i1 < rotatedPoints.length; i1++) {
			vetX[i1] = rotatedPoints[i1][0];
			vetY[i1] = rotatedPoints[i1][1];
		}
		int px = 0;
		int py = 0;
		int ax = 0;
		int ay = 0;
		int bx = width;
		int by = 0;
		int cx = width;
		int cy = height;
		int dx = 0;
		int dy = height;
		//
		// PER OGNI PUNTO SI CALCOLA:
		// ABS(distanza da un vertice su X) + ABS(distanza da un vertice su Y)
		// si hanno cosi' 4 colonne, coincidenti con le coordinate X ed Y del punto.
		// Il minimo per ogni colonna rappresenta il punto piu'vicino a quel vertice.
		//
		for (int i1 = 0; i1 < rotatedPoints.length; i1++) {
			px = rotatedPoints[i1][0];
			py = rotatedPoints[i1][1];
			rotatedPoints[i1][2] = Math.abs(px - ax) + Math.abs(py - ay); // vertice a
			rotatedPoints[i1][3] = Math.abs(px - bx) + Math.abs(py - by); // vertice b
			rotatedPoints[i1][4] = Math.abs(px - cx) + Math.abs(py - cy); // vertice c
			rotatedPoints[i1][5] = Math.abs(px - dx) + Math.abs(py - dy); // vertice d
//			STRANAMENTE il calcolo qui sopra da'risultati migliori del calcolo dell'ipotenusa, piu'complicato			
		}

		// estraggo dalla matrice gli array con il calcolo per i vertici
		int[] vertexa = ACRutils.matExtractor(rotatedPoints, 2);
		int[] vertexb = ACRutils.matExtractor(rotatedPoints, 3);
		int[] vertexc = ACRutils.matExtractor(rotatedPoints, 4);
		int[] vertexd = ACRutils.matExtractor(rotatedPoints, 5);
//		if (false) {
//			ACRlog.logVector(vertexa, ACRlog.qui() + "vertexa");
//			ACRlog.logVector(vertexb, ACRlog.qui() + "vertexb");
//			ACRlog.logVector(vertexc, ACRlog.qui() + "vertexc");
//			ACRlog.logVector(vertexd, ACRlog.qui() + "vertexd");
//		}

		int[] posmina = ACRutils.minsearch(vertexa);
		int[] posminb = ACRutils.minsearch(vertexb);
		int[] posminc = ACRutils.minsearch(vertexc);
		int[] posmind = ACRutils.minsearch(vertexd);
//		if (false) {
//			ACRlog.logVector(posmina, ACRlog.qui() + "posmina");
//			ACRlog.logVector(posminb, ACRlog.qui() + "posminb");
//			ACRlog.logVector(posminc, ACRlog.qui() + "posminc");
//			ACRlog.logVector(posmind, ACRlog.qui() + "posmind");
//		}

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
		ACRutils.plotPoints(imp1, over2, (int) AX, (int) AY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp1, over2, (int) BX, (int) BY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp1, over2, (int) CX, (int) CY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp1, over2, (int) DX, (int) DY, Color.RED, 4, 4);
//
		if (step) {
			ACRlog.waitHere("Riportati in i vertici oggetto trovati\n A= " + AX + "," + AY + " B= " + BX + "," + BY
					+ " C= " + CX + "," + CY + " D= " + DX + "," + DY, step, timeout);
			IJ.log("ACRlog.qui()+Riportati in i vertici oggetto trovati A= " + AX + "," + AY + " B= " + BX + "," + BY
					+ " C= " + CX + "," + CY + " D= " + DX + "," + DY);
		}
		double[][] phantomVertices = new double[2][4];
		phantomVertices[0][0] = AX;
		phantomVertices[1][0] = AY;
		phantomVertices[0][1] = BX;
		phantomVertices[1][1] = BY;
		phantomVertices[0][2] = CX;
		phantomVertices[1][2] = CY;
		phantomVertices[0][3] = DX;
		phantomVertices[1][3] = DY;
		double angle = ACRlocalizer.phantomRotation(phantomVertices, step, verbose, timeout);
		ACRlog.waitHere("angle= " + angle);

		double MX = Math.round((double) (CX + DX) / (double) 2);
		double MY = Math.round((double) (CY + DY) / (double) 2);
		double PX = Math.round((double) (AX + BX) / (double) 2);
		double PY = Math.round((double) (AY + BY) / (double) 2);
		double LL1 = Math.sqrt((AX - BX) * (AX - BX) + (AY - BY) * (AY - BY));
		double LL2 = Math.sqrt((CX - DX) * (CX - DX) + (CY - DY) * (CY - DY));
		double LL = (LL1 + LL2) / 2;

		over2.clear();

//		ACRutils.plotPoints(imp2, over2, (int) MX, (int) MY, Color.CYAN, 2, 4);
//		ACRutils.plotPoints(imp2, over2, (int) PX, (int) PY, Color.CYAN, 2, 4);

		ACRutils.plotPoints(imp1, over2, (int) AX, (int) AY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp1, over2, (int) BX, (int) BY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp1, over2, (int) CX, (int) CY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp1, over2, (int) DX, (int) DY, Color.RED, 4, 4);

		imp1.setRoi(new RotatedRectRoi(MX, MY, PX, PY, LL));
		imp1.getRoi().setStrokeColor(Color.RED);
		over2.addElement(imp1.getRoi());
		imp1.killRoi();

		imp1.setRoi(new Line(MX, MY, PX, PY));
		imp1.getRoi().setStrokeColor(Color.CYAN);
		over2.addElement(imp1.getRoi());

		if (step)
			ACRlog.waitHere("Riportato in rosso il RotatedRectangle trovato,\npiu' o meno coincidente con l'oggetto",
					step, timeout);

		double slope = (MX - PX) / (MY - PY);
		if (verbose)
			IJ.log(ACRlog.qui() + "slope= " + slope);
		double latoA = 12; // questo e'l'offset corrispondente a dove fare la misura lunghezza, rispetto
							// all'asse verticale del fantoccio
		double latoB = latoA * slope;

		double xoffset = Math.sqrt(latoB * latoB + latoA * latoA);
		// il nuovo punto da cui passare con la retta diventa
		double RX = MX + xoffset;
		double RY = MY;
		double q = RX - slope * RY;
		double y0src = 0;
		double y1src = height;
		double x0src = y0src * slope + q;
		double x1src = y1src * slope + q;
		// traccio una Line e poi analizzero'imagePlus estraendo la matrice della linea
		// per trovare la FWHM
		imp1.setRoi(new Line(x0src, y0src, x1src, y1src));
		imp1.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp1.getRoi());

		double dim1 = dimPixel * ACRlocalizer.profAnal(imp1, step, verbose, timeout);
		imp1.killRoi();
		if (step)
			ACRlog.waitHere("Profilo analizzato", debug, timeout);
		double SX = MX - xoffset;
		double SY = MY;

		// ACRutils.plotPoints(imp2, over2, (int) SX, (int) SY, Color.GREEN, 3, 5);
		// qui determino il parametro q
		q = SX - slope * SY;
		y0src = 0;
		y1src = height;
		x0src = y0src * slope + q;
		x1src = y1src * slope + q;

		imp1.setRoi(new Line(x0src, y0src, x1src, y1src));
		imp1.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp1.getRoi());

		double dim2 = dimPixel * ACRlocalizer.profAnal(imp1, step, verbose, timeout);
		imp1.killRoi();

		if (step)
			ACRlog.waitHere(" Profilo analizzato", debug, timeout);

		IJ.log(ACRlog.qui() + "END>  lunghezzaFantoccio 1= " + IJ.d2s(dim1, 3) + " mm");
		IJ.log(ACRlog.qui() + "END>  lunghezzaFantoccio 2= " + IJ.d2s(dim2, 3) + " mm");

		double[] vetout1 = new double[10];
		vetout1[0] = dim1;
		vetout1[1] = dim2;
		vetout1[2] = angle;

		return vetout1;
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

		// i settaggi sono quelli gia' a lungo sperimentati ... funzionano!
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

	/**
	 * MAIN per la ricerca del diametro
	 * 
	 * @param path1
	 * @param pathReport
	 * @param slice
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 */
	public void mainSliceDiameter(String path1, String pathReport, int slice, boolean step, boolean fast,
			boolean verbose, int timeout) {
		double maxFitError = +20;
//		double maxBubbleGapLimit = 2;

		IJ.log(ACRlog.qui() + "START>");

		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		if (imp1 == null)
			ACRlog.waitHere("imp1==null");
		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));

		// IW2AYV sposto con il fantoccio, in modo da testare la corretta acquisizione,
		// indipendente dal centro immagine!

//		imp1.show();
//		imp1.setRoi(22, 21, 153, 148);
//		imp1.copy();
//		imp1.setRoi(19, 21, 153, 148);
//		imp1.cut();
//		imp1.setRoi(18, 4, 153, 148);
//		imp1.paste();

		ImagePlus imp2 = imp1.duplicate();
		ImagePlus imp3 = imp1.duplicate();
//		int[] dummy1 = new int[0];

		boolean step1 = false;
		boolean fast1 = false;
		boolean verbose1 = false;

		int[] out1 = ACRlocalizer.positionSearch2(imp2, maxFitError, step1, fast1, verbose1, timeout);

		int xphantom = (int) out1[0];
		int yphantom = (int) out1[1];
		int dphantom = (int) out1[2];

		int[] phantomCircle = new int[3];
		phantomCircle[0] = xphantom;
		phantomCircle[1] = yphantom;
		phantomCircle[2] = dphantom;

		if (step)
			ACRlog.waitHere("FIT DEL CERCHIO ESEGUITO, FANTOCCIO LOCALIZZATO", debug, timeout);
		boolean step2 = step;
		boolean fast2 = fast;
		boolean verbose2 = verbose;

		double[] out3 = ACRlocalizer.positionSearch3(imp3, phantomCircle, step2, fast2, verbose2, timeout);
		double[] out4 = new double[out3.length];
		for (int i1 = 0; i1 < out3.length; i1++) {
			out4[i1] = out3[i1] * dimPixel;
			ACRlog.appendLog(pathReport, "diametro " + i1 + " slice " + slice + "= " + IJ.d2s(out4[i1], 3) + " mm");
			IJ.log(ACRlog.qui() + "END> diametro " + i1 + " slice " + slice + "= " + IJ.d2s(out4[i1], 3) + " mm");
		}
	}

	/**
	 * Ricerca del max o min in una matrice
	 * 
	 * @param vetIn matrice di input
	 * @param key   chiave di ricerca
	 * @param max   mettere true= ricerca max
	 * @return
	 */
	public double[][] extractMax(double[][] vetIn, int key, boolean max) {

		double[] vetKey = new double[vetIn[0].length];
		for (int i1 = 0; i1 < vetKey.length; i1++) {
			vetKey[i1] = vetIn[key][i1];
			IJ.log(ACRlog.qui() + "vetIn" + i1 + "," + vetIn[0][i1] + "," + vetIn[1][i1]);
		}
		ArrayList<double[]> borderPoints = new ArrayList<>();
		double[] pointxy = new double[2];
		double[] minmax = Tools.getMinMax(vetKey);
		if (max)
			IJ.log(ACRlog.qui() + "ricerca max= " + minmax[1]);
		else
			IJ.log(ACRlog.qui() + "ricerca min= " + minmax[0]);
		int count = 0;
		for (int i1 = 0; i1 < vetIn[0].length; i1++) {
			if (max) {
				if (Double.compare(vetIn[key][i1], minmax[1]) == 0) {
					pointxy[0] = vetIn[0][i1];
					pointxy[1] = vetIn[1][i1];
					IJ.log(ACRlog.qui() + "aggiunto MAX " + vetIn[0][i1] + "," + vetIn[1][i1]);
					borderPoints.add(pointxy);
					count++;
				}
			} else {
				if (Double.compare(vetIn[key][i1], minmax[0]) == 0) {
					pointxy[0] = vetIn[0][i1];
					pointxy[1] = vetIn[1][i1];
					IJ.log(ACRlog.qui() + "aggiunto MIN " + vetIn[0][i1] + "," + vetIn[1][i1]);
					borderPoints.add(pointxy);
					count++;
				}
			}
		}

		IJ.log(ACRlog.qui() + "[borderPoints.size()= " + borderPoints.size() + "count= " + count);

		double[][] outIntArr = new double[2][borderPoints.size()];
		int i2 = 0;
		for (double[] n1 : borderPoints) {
			outIntArr[0][i2] = n1[0];
			outIntArr[1][i2] = n1[1];
			i2++;
		}
		ACRlog.logMatrix(outIntArr, ACRlog.qui() + "outIntArr");
		return outIntArr;

	}

	/**
	 * distanza di un punto da una retta [pixel]
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
	 * Altra soluzione di distanza da un segmento [pixel]
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

		double AB = distBetweenPoints(x, y, x1, y1);
		double BC = distBetweenPoints(x1, y1, x2, y2);
		double AC = distBetweenPoints(x, y, x2, y2);

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

	/**
	 * Calcolo della distanza tra due punti [pixel]
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private static double distBetweenPoints(double x1, double y1, double x2, double y2) {
		double xx = x2 - x1;
		double yy = y2 - y1;

		return (double) Math.sqrt(xx * xx + yy * yy);
	}

}