package qualityACR;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.gui.RotatedRectRoi;
import ij.plugin.PlugIn;
import ij.util.Tools;

/**
 * Misure geometriche 
 * 
 * @author Alberto
 *
 */
public class Geometric_Accuracy implements PlugIn {

	public static final boolean debug = true;

	public void run(String arg) {
		mainGeometry();
	}

	public void mainGeometry() {
		Properties prop = ACRutils.readConfigACR();
		int timeout = 0; // preme automaticamente OK ai messaggi durante i test
		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
		boolean[] defaults = { true, true, false, false, true, true, false, false, false, false, false, false,
				false, false };
		String[] headings = { "slices T1", "slices T2" };
		boolean fastdefault = false;
		boolean stepdefault = false;
		boolean verbosedefault = false;
		boolean localizerdefault = true;
		boolean[] T1 = new boolean[7];
		boolean[] T2 = new boolean[7];

		if (prop != null) {
			fastdefault = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.fast"));
			stepdefault = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.step"));
			verbosedefault = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.verbose"));
		//	localizerdefault = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.geomLocalizer"));
			for (int i1 = 0; i1 < 7; i1++) {
		//		T1[i1] = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.SliceT1[" + i1 + "]"));
		//		T2[i1] = Boolean.parseBoolean(prop.getProperty("Geometric_Accuracy.SliceT2[" + i1 + "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
		//		defaults[count++] = T1[i1];
		//		defaults[count++] = T2[i1];
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
		String pathReport1 = vetPath[4];

		if (!ACRinputOutput.isPathValid(pathReport1))
			ACRlog.waitHere("Path non valido= " + pathReport1);

		String[] listLocalizer = ACRinputOutput.readStackPathToSortedList(vetPath[0], "T1");
		if (listLocalizer != null)
			pathLocalizer = listLocalizer[0];
		String[] sortedListT1 = ACRinputOutput.readStackPathToSortedList(vetPath[1], "T1");
		if (sortedListT1 == null)
			IJ.log(ACRlog.qui() + "sortedListT1 ==null");
		String[] sortedListT2 = ACRinputOutput.readStackPathToSortedList(vetPath[2], "T2");
		if (sortedListT2 == null)
			IJ.log(ACRlog.qui() + "sortedListT2 ==null");

		// elaborazione file selezionati dall'operatore
		if (geomLocalizer) {
			if (!ACRinputOutput.isDirOK(pathReport1))
				ACRlog.waitHere("Directory svampata= " + pathReport1);
			mainLocalizer(pathLocalizer, pathReport1, step, verbose, timeout);

		}

		String pathReport2 = vetPath[4];
		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log(ACRlog.qui() + "elaborazione slice T1 numero " + i1);
				mainSliceDiameter(sortedListT1[i1], pathReport2, "T1", i1 + 1, step, verbose, timeout);
			}
		}

		String pathReport3 = vetPath[4];
		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log(ACRlog.qui() + "elaborazione slice T2 numero " + i1);
				mainSliceDiameter(sortedListT2[i1], pathReport3, "T2", i1 + 1, step, verbose, timeout);
			}
		}
		ACRlog.waitHere("GEOMETRIC_ACCURACY TERMINATA", debug, timeout);
	}

	/**
	 * Elaborazione immagine localizer per ottenere lunghezza fantoccio
	 * 
	 * @param path1
	 */
	public void mainLocalizer(String path1, String pathReport, boolean step, boolean verbose, int timeout) {

		IJ.log(ACRlog.qui() + "START>");
		// questa dovrebbe essere l'apertura comune a tutte le main delle varie classi
		// apertura immagine, display, zoom
		// chiamata prima subroutine passando l'immagine pronta
		// eccetraz ecceteraz
		String namepathReport = pathReport + "\\ReportLocalizer.txt";
		String imageName = "localizer001.png";
		String namepathImage = pathReport + "\\" + imageName;

		// ----- cancellazione cacchine precedenti -----
		boolean ok1 = ACRinputOutput.deleteFile(new File(namepathReport));
		boolean ok2 = ACRinputOutput.deleteFile(new File(namepathImage));
		if (!(ok1 && ok2))
			ACRlog.waitHere("PROBLEMA CANCELLAZIONE");
		// ----- inizializzazione report----------------
		ACRlog.appendLog(namepathReport, "< calculated " + LocalDate.now() + " @ " + LocalTime.now() + " >");
		// ---------------------------------------------

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
		// IJ.run(imp2, "Rotate... ", "angle=8 grid=1 interpolation=Bilinear");
		//
		// IJ.run(imp2, "Rotate... ", "angle=-8 grid=1 interpolation=Bilinear");
		//
		// ===============================================
		//
		double[] vetout1 = localizerImageLength(imp2, step, verbose, timeout);
		double dim1 = vetout1[0];
		double dim2 = vetout1[1];
		double angle = vetout1[2];
		if (step)
			ACRlog.waitHere(" Profilo analizzato", debug, timeout);

		IJ.log(ACRlog.qui() + "END>  lunghezzaFantoccio 1= " + IJ.d2s(dim1, 4) + " mm");
		IJ.log(ACRlog.qui() + "END>  lunghezzaFantoccio 2= " + IJ.d2s(dim2, 4) + " mm");
		IJ.log(ACRlog.qui() + "END>  angoloFantoccio= " + IJ.d2s(angle, 4) + " °");
		String[] info1 = ACRutils.imageInformation(imp1);
		for (int i1 = 0; i1 < info1.length; i1++) {
			ACRlog.appendLog(namepathReport, ACRlog.qui() + "#" + String.format("%03d", i1) + "#  " + info1[i1]);
		}

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #900#" + namepathImage);
		IJ.saveAs(imp2, "PNG", namepathImage);

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "length1: #101#" + IJ.d2s(vetout1[0], 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "length2: #102#" + IJ.d2s(vetout1[1], 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "length limits: #103#" + "98 - 102");
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "length1 p/f: #104#" + "Pass");
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "length2 p/f: #105#" + "Fail");
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "angle: #106#" + IJ.d2s(vetout1[2], 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "angle limits: #107#" + "+-1");
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "angle p/f: #108#" + "Fail");
		ACRlog.appendLog(namepathReport, "< finished " + LocalDate.now() + " @ " + LocalTime.now() + " >");

		imp2.changes = false;
		imp2.close();
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

		// i rotatedPoints vengono buoni se si vuole esportarli nel mio excel2003 che ha
		// solo 256 colonne

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
		if (verbose || step)
			ACRlog.waitHere("angle= " + angle, debug, timeout);

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
	public void mainSliceDiameter(String path1, String pathReport, String group, int slice, boolean step,
			boolean verbose, int timeout) {

		// questa dovrebbe essere l'apertura comune a tutte le main delle varie classi
		// apertura immagine, display, zoom
		// chiamata prima subroutine passando l'immagine pronta
		// eccetraz ecceteraz
		// ------------- inizio comune ----------------
		IJ.log(ACRlog.qui() + "START>");
		double mintolerance = 98;
		double maxtolerance = 102;
		String aux1 = "_" + group + "S" + slice;
		String namepathReport = pathReport + "\\ReportGeometrico" + aux1 + ".txt";
		String imageName1 = "image905" + aux1 + ".jpg";
		String namepathImage1 = pathReport + "\\" + imageName1;
		String imageName2 = "diameter906" + aux1 + ".jpg";
		String namepathImage2 = pathReport + "\\" + imageName2;

		// ----- cancellazione cacchine precedenti -----
		boolean ok1 = ACRinputOutput.deleteFile(new File(namepathReport));
		IJ.log(ACRlog.qui());

		boolean ok2 = ACRinputOutput.deleteFile(new File(namepathImage1));
		IJ.log(ACRlog.qui());
		boolean ok3 = ACRinputOutput.deleteFile(new File(namepathImage2));
		IJ.log(ACRlog.qui());

		if (!(ok1 && ok2 && ok3))
			ACRlog.waitHere("PROBLEMA CANCELLAZIONE");
		// ----- inizializzazione report----------------
		ACRlog.appendLog(namepathReport, "< calculated " + LocalDate.now() + " @ " + LocalTime.now() + " >");
		// ---------------------------------------------
		IJ.log(ACRlog.qui());

		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		if (imp1 == null)
			ACRlog.waitHere("imp1==null");
		IJ.log(ACRlog.qui());
		imp1.show();
		ACRutils.zoom(imp1);
		imp1.updateAndDraw();
		IJ.saveAs(imp1, "jpg", namepathImage1);
		IJ.log(ACRlog.qui());
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);

		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp2, ACRconst.DICOM_PIXEL_SPACING), 1));

		double maxFitError = +20;
		int[] out1 = ACRlocalizer.positionSearch2(imp2, maxFitError, step, verbose, timeout);
		int xphantom = (int) out1[0];
		int yphantom = (int) out1[1];
		int dphantom = (int) out1[2];
		int[] phantomCircle = new int[3];
		phantomCircle[0] = xphantom;
		phantomCircle[1] = yphantom;
		phantomCircle[2] = dphantom;

		imp2.changes = false;
		imp2.close();

		if (step)
			ACRlog.waitHere("FIT DEL CERCHIO ESEGUITO, FANTOCCIO LOCALIZZATO", debug, timeout);

		double[] out3 = ACRlocalizer.positionSearch3(imp1, phantomCircle, step, verbose, timeout);

		String[] info1 = ACRutils.imageInformation(imp1);
		for (int i1 = 0; i1 < info1.length; i1++) {
			ACRlog.appendLog(namepathReport, ACRlog.qui() + "#" + String.format("%03d", i1) + "#  " + info1[i1]);
		}
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "mintolerance:" + mintolerance);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "maxtolerance:" + maxtolerance);

		double[] out4 = new double[out3.length];
		boolean failmin = false;
		boolean failmax = false;
		for (int i1 = 0; i1 < out3.length; i1++) {
			out4[i1] = out3[i1] * dimPixel;
			failmin = (Double.compare(out4[i1], mintolerance) < 0);
			failmax = (Double.compare(out4[i1], maxtolerance) > 0);
			String response = "";
			if (failmin || failmax) {
				response = "FAIL";
			} else {
				response = "PASS";
			}
			ACRlog.appendLog(namepathReport, ACRlog.qui() + "diametro " + i1 + " slice " + slice + "= #10" + (i1 + 1)
					+ "#" + IJ.d2s(out4[i1], 4));

			ACRlog.appendLog(namepathReport,
					ACRlog.qui() + "giudizio " + i1 + " slice " + slice + "= #10" + (i1 + 6) + "#" + response);

			IJ.log(ACRlog.qui() + "END> diametro " + i1 + " slice " + slice + "= " + IJ.d2s(out4[i1], 4));
		}
		ACRlog.appendLog(namepathReport,
				ACRlog.qui() + "#105#" + IJ.d2s(mintolerance, 1) + " - " + IJ.d2s(maxtolerance, 1));

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #905#" + namepathImage1);

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #906#" + namepathImage2);
		IJ.saveAs(imp1, "jpg", namepathImage2);
		ACRlog.appendLog(namepathReport, "< finished " + LocalDate.now() + " @ " + LocalTime.now() + " >");

		imp1.changes = false;
		imp1.close();

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