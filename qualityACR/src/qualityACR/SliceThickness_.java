package qualityACR;

import java.awt.Color;
import java.awt.Frame;
import java.io.IOException;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.plugin.PlugIn;

public class SliceThickness_ implements PlugIn {
	public static final boolean debug = true;

	public void run(String arg) {

		ACRlog.waitHere("3 - SliceThickness");

		mainThickness();
	}

	// 20july2021
	// qualche avanzamento, ma per ora si naviga nelle piccolezze,
	// comunque il ParticleAnalyzer promette bene. Altrettanto il RemoveDuplicate
	// messo in ACRutils. Anche se questo lavora per le stringhe, nulla vieta di
	// adattarlo ai double od agli int ed eliminare dalla mia lista di punti
	// ricavati dalle scansioni fantoccio, tutti i duplicati

	public void mainThickness() {
		Properties prop = ACRutils.readConfigACR();
		int timeout = 0; // preme automaticamente OK ai messaggi durante i test
		IJ.log(ACRlog.qui() + "START");
//		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
//		boolean[] defaults = { true, false, true, false, false, false, false, true, true, true, false, false, false,
//				true };
		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
		boolean[] defaults = { true, false, false, false, false, false, false, false, false, false, false, false, false,
				false };
		String[] headings = { "slices T1", "slices T2" };
		boolean fastdefault = false;
		boolean stepdefault = false;
		boolean verbosedefault = false;
		boolean[] T1 = new boolean[7];
		boolean[] T2 = new boolean[7];

		if (prop != null) {
			fastdefault = Boolean.parseBoolean(prop.getProperty("Thickness.fast"));
			stepdefault = Boolean.parseBoolean(prop.getProperty("Thickness.step"));
			verbosedefault = Boolean.parseBoolean(prop.getProperty("Thickness.verbose"));
			for (int i1 = 0; i1 < 7; i1++) {
				T1[i1] = Boolean.parseBoolean(prop.getProperty("Thickness.SliceT1[" + i1 + "]"));
				T2[i1] = Boolean.parseBoolean(prop.getProperty("Thickness.SliceT2[" + i1 + "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
				defaults[count++] = T1[i1];
				defaults[count++] = T2[i1];
			}
		}

		GenericDialog gd1 = new GenericDialog("SLICE THICKNESS");
		gd1.addCheckbox("ANIMAZIONE 2 sec", fastdefault);
		gd1.addCheckbox("STEP", stepdefault);
		gd1.addCheckbox("VERBOSE", verbosedefault);
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
		boolean[] vetBoolSliceT1 = new boolean[7];
		boolean[] vetBoolSliceT2 = new boolean[7];
		for (int i1 = 0; i1 < 7; i1++) {
			vetBoolSliceT1[i1] = gd1.getNextBoolean();
			vetBoolSliceT2[i1] = gd1.getNextBoolean();
		}

		if (fast)
			timeout = 2000;
		else
			timeout = 0;

		// vado a scrivere i setup nel config file
		if (prop == null)
			prop = new Properties();
		prop.setProperty("Thickness.fast", "" + fast);
		prop.setProperty("Thickness.step", "" + step);
		prop.setProperty("Thickness.verbose", "" + verbose);
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Thickness.SliceT1[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT1[i1];
			prop.setProperty(aux1, aux2);
		}
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Thickness.SliceT2[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT2[i1];
			prop.setProperty(aux1, aux2);
		}

		try {
			ACRutils.writeConfigACR(prop);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// leggo i nomi di tutti i 15 file presenti
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

		// ora in base alle selezioni effettuate nelle checkbox del dialogo, dobbiamo
		// elaborare solo i file selezionati
		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log(ACRlog.qui() + "elaborazione slice T1 numero " + i1);
				evalThickness(sortedListT1[i1], pathReport, i1, step, fast, verbose, timeout);
			}
		}

		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log(ACRlog.qui() + "==================");
				IJ.log(ACRlog.qui() + "elaborazione slice T2 numero " + i1);
				evalThickness(sortedListT2[i1], pathReport, i1, step, fast, verbose, timeout);
			}
		}
		ACRlog.waitHere("SLICE THICKNESS TERMINATA");
	}

	public void evalThickness(String path1, String pathReport, int i1, boolean step, boolean fast, boolean verbose,
			int timeout) {

		IJ.log(ACRlog.qui() + "<START>");
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		//
		// ========== TAROCCAMENTO IMMAGINE =============
		//
		// MI RESTA DA SPIEGARE PERCHE' RUOTANDO L'IMMAGINE
		// CAMBIA IL RISULTATO!!!!!
		//
		// IJ.run(imp2, "Flip Horizontally", "");
		//
		// IJ.run(imp1, "Rotate... ", "angle=10 grid=1 interpolation=Bilinear");
		//
		// IJ.run(imp2, "Rotate... ", "angle=-8 grid=1 interpolation=Bilinear");
		//
		// ===============================================
		//

		ImagePlus imp2 = imp1.duplicate();
		imp2.setTitle("002");
		imp2.show();
		ACRutils.zoom(imp2);
		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);

//		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp2, step, fast, verbose, timeout);

		double[] phantomCircle = ACRlocalizer.phantomLocalizerAdvanced(imp2, step, verbose, timeout);

		IJ.log(ACRlog.qui());
		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp2, phantomCircle, step, verbose, timeout);
		IJ.log(ACRlog.qui());
		if (step)
			ACRlog.waitHere("VERTICI", debug, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, step, verbose, timeout);
		double dist1 = -5;
		IJ.log(ACRlog.qui());
		if (step)
			ACRlog.waitHere("ANGOLO", debug, timeout);
		double[] parallela1 = ACRlocalizer.parallela(phantomVertices, dist1);
		if (step)
			ACRlog.waitHere("PARALLELA", debug, timeout);
		IJ.log(ACRlog.qui());

		ImagePlus imp3 = imp1.duplicate();
		imp3.setTitle("003");
		imp3.show();
		ACRutils.zoom(imp3);
		Overlay over3 = new Overlay();
		imp3.setOverlay(over3);

		if (verbose)
			ACRlog.logMatrix(phantomVertices, ACRlog.qui() + "phantomVertices");

		// andiamo a plottare i punti trovati
		// VERDE
		int CX = (int) Math.round(phantomVertices[0][2]);
		int CY = (int) Math.round(phantomVertices[1][2]);
		// AZZURRO
		int DX = (int) Math.round(phantomVertices[0][3]);
		int DY = (int) Math.round(phantomVertices[1][3]);

		int EX = DX + (int) Math.round(parallela1[0]);
		int EY = DY + (int) Math.round(parallela1[1]);
		int FX = CX + (int) Math.round(parallela1[0]);
		int FY = CY + (int) Math.round(parallela1[1]);

		if (verbose)
			IJ.log("E= " + EX + " , " + EY + " F= " + FX + " , " + FY);

		ACRutils.plotPoints(imp3, over3, EX, EY, Color.MAGENTA, 4, 4);
		ACRutils.plotPoints(imp3, over3, FX, FY, Color.MAGENTA, 4, 4);

		double dist2 = -12;
		double[] parallela2 = ACRlocalizer.parallela(phantomVertices, dist2);

		int GX = DX + (int) Math.round(parallela2[0]);
		int GY = DY + (int) Math.round(parallela2[1]);
		int HX = CX + (int) Math.round(parallela2[0]);
		int HY = CY + (int) Math.round(parallela2[1]);

		if (verbose) {
			IJ.log("G= " + GX + " , " + GY + " H= " + HX + " , " + HY);

			ACRutils.plotPoints(imp3, over3, GX, GY, Color.CYAN, 4, 4);
			ACRutils.plotPoints(imp3, over3, HX, HY, Color.CYAN, 4, 4);
			ACRlog.waitHere("punti allineati?", debug, timeout);
		}

		Line.setWidth(5);
		imp3.setRoi(new Line(EX, EY, FX, FY));
		imp3.updateAndDraw();
		imp3.getRoi().setStrokeColor(Color.GREEN);
		over3.addElement(imp3.getRoi());

		Roi roi3 = imp3.getRoi();
		double[] profi3 = ((Line) roi3).getPixels();
		// linea calcolo segnale mediato
		// boolean invert = false;
		double fwhm1 = ACRlocalizer.FWHMcalc(profi3, 3, "primoProfilo");
		ACRlog.waitHere("PROFILO", debug, timeout);
		imp3.setRoi(new Line(GX, GY, HX, HY));
		imp3.getRoi().setStrokeColor(Color.GREEN);
		over3.addElement(imp3.getRoi());
		imp3.updateAndDraw();
		Roi roi4 = imp3.getRoi();
		double[] profi4 = ((Line) roi4).getPixels();
		double fwhm2 = ACRlocalizer.FWHMcalc(profi4, 3, "secondoProfilo");
		ACRlog.waitHere("PROFILO", debug, timeout);
		// linea calcolo segnale mediato
		Line.setWidth(1);
//		title = "ABnormal";
//		Plot plot4 = ACRgraphic.basePlot(profi4, title, Color.RED);
//		plot4.draw();
//		plot4.show();
		ACRlog.waitHere("fwhm1= " + fwhm1 + " fwhm2= " + fwhm2, debug, timeout);
		if ((Double.compare(fwhm1, 0) <= 0 || Double.compare(fwhm2, 0) <= 0))
			ACRlog.waitHere("IMPOSSIBILE CALCOLARE LA THICKNESS,\nPROBLEMA IMMAGINE???");

		return;
	}

	/*
	 * Calcolo della Edge Response Function (ERF)
	 * 
	 * @param profile1 profilo da elaborare
	 * 
	 * @param invert true se da invertire
	 * 
	 * @return profilo con ERF
	 */
	public static double[] createErf(double[] profile1, boolean invert) {

		int len1 = profile1.length;

		double[] erf = new double[len1];
		if (invert) {
			for (int j1 = 0; j1 < len1 - 1; j1++)
				erf[j1] = (profile1[j1] - profile1[j1 + 1]) * (-1);

		} else {
			for (int j1 = 0; j1 < len1 - 1; j1++)
				erf[j1] = (profile1[j1] - profile1[j1 + 1]);
		}
		erf[len1 - 1] = erf[len1 - 2];
		return (erf);
	}

}
