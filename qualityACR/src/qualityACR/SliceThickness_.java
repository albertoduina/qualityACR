package qualityACR;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
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
		boolean[] defaults = { true, true, false, false, false, false, false, false, false, false, false, false, false,
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
	//			T1[i1] = Boolean.parseBoolean(prop.getProperty("Thickness.SliceT1[" + i1 + "]"));
	//			T2[i1] = Boolean.parseBoolean(prop.getProperty("Thickness.SliceT2[" + i1 + "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
	//			defaults[count++] = T1[i1];
	//			defaults[count++] = T2[i1];
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
			timeout = 200;
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
		String pathReport = vetPath[4];
		IJ.log(vetPath[0]);
		IJ.log(vetPath[1]);
		IJ.log(vetPath[2]);

		String[] listLocalizer = ACRinputOutput.readStackPathToSortedList(vetPath[0], "T1");
		if (listLocalizer != null)
			pathLocalizer = listLocalizer[0];
		String[] sortedListT1 = ACRinputOutput.readStackPathToSortedList(vetPath[1], "T1");
		if (sortedListT1 == null)
			IJ.log(ACRlog.qui() + "sortedListT1 ==null");
		String[] sortedListT2 = ACRinputOutput.readStackPathToSortedList(vetPath[2], "T2");
		if (sortedListT2 == null)
			IJ.log(ACRlog.qui() + "sortedListT2 ==null");

		// ora in base alle selezioni effettuate nelle checkbox del dialogo, dobbiamo
		// elaborare solo i file selezionati
		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log(ACRlog.qui() + "elaborazione slice T1 numero " + (i1+1));
				evalThickness(sortedListT1[i1], pathReport, "T1", i1+1, step, verbose, timeout);
			}
		}

		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log(ACRlog.qui() + "==================");
				IJ.log(ACRlog.qui() + "elaborazione slice T2 numero " + (i1+1));
				evalThickness(sortedListT2[i1], pathReport, "T2", i1+1, step, verbose, timeout);
			}
		}
		ACRlog.waitHere("SLICE THICKNESS TERMINATA");
	}

	public void evalThickness(String path1, String pathReport, String group,  int slice, boolean step, boolean verbose, int timeout) {

		// questa dovrebbe essere l'apertura comune a tutte le main delle varie classi
		// apertura immagine, display, zoom
		// chiamata prima subroutine passando l'immagine pronta
		// eccetraz ecceteraz
		// ------------- inizio comune ----------------
		IJ.log(ACRlog.qui() + "START>");
		double mintolerance = 4.3;
		double maxtolerance = 5.7;
		String aux1 = "_" + group + "S" + slice;
		String namepathReport = pathReport + "\\ReportThickness"+aux1+".txt";
		String imageName1 = "thickness910"+aux1+".jpg";
		String namepathImage1 = pathReport + "\\" + imageName1;
		String profileName1 = "greenprofile912"+aux1+".jpg";
		String namepathProfile1 = pathReport + "\\" + profileName1;

		String profileName2 = "yellowprofile911"+aux1+".jpg";
		String namepathProfile2 = pathReport + "\\" + profileName2;

		// ----- cancellazione cacchine precedenti -----
		boolean ok1 = ACRinputOutput.deleteFile(new File(namepathReport));
		IJ.log(ACRlog.qui());

		boolean ok2 = ACRinputOutput.deleteFile(new File(namepathImage1));
		IJ.log(ACRlog.qui());
		boolean ok3 = ACRinputOutput.deleteFile(new File(namepathProfile1));
		IJ.log(ACRlog.qui());
		boolean ok4 = ACRinputOutput.deleteFile(new File(namepathProfile2));
		IJ.log(ACRlog.qui());

		if (!(ok1 && ok2 && ok3 && ok4))
			ACRlog.waitHere("PROBLEMA CANCELLAZIONE");
		// ----- inizializzazione report----------------
		ACRlog.appendLog(namepathReport, "< calculated " + LocalDate.now() + " @ " + LocalTime.now() + " >");
		// ---------------------------------------------
		IJ.log(ACRlog.qui());

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
		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp2, ACRconst.DICOM_PIXEL_SPACING), 1));

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

		int EX = DX + 4 + (int) Math.round(parallela1[0]);
		int EY = DY + (int) Math.round(parallela1[1]);
		int FX = CX - 4 + (int) Math.round(parallela1[0]);
		int FY = CY + (int) Math.round(parallela1[1]);

		if (verbose)
			IJ.log("E= " + EX + " , " + EY + " F= " + FX + " , " + FY);

		ACRutils.plotPoints(imp3, over3, EX, EY, Color.MAGENTA, 4, 4);
		ACRutils.plotPoints(imp3, over3, FX, FY, Color.MAGENTA, 4, 4);

		double dist2 = -12;
		double[] parallela2 = ACRlocalizer.parallela(phantomVertices, dist2);

		int GX = DX + 4 + (int) Math.round(parallela2[0]);
		int GY = DY + (int) Math.round(parallela2[1]);
		int HX = CX - 4 + (int) Math.round(parallela2[0]);
		int HY = CY + (int) Math.round(parallela2[1]);

		if (verbose) {
			IJ.log("G= " + GX + " , " + GY + " H= " + HX + " , " + HY);

			ACRutils.plotPoints(imp3, over3, GX, GY, Color.CYAN, 4, 4);
			ACRutils.plotPoints(imp3, over3, HX, HY, Color.CYAN, 4, 4);
			ACRlog.waitHere("punti allineati?", debug, timeout);
		}

		over3.clear();
		Line.setWidth(5);
		imp3.setRoi(new Line(EX, EY, FX, FY));
		Roi roi3 = imp3.getRoi();
		imp3.updateAndDraw();
		imp3.getRoi().setStrokeColor(Color.GREEN);
		over3.addElement(imp3.getRoi());

		double[] profi3 = ((Line) roi3).getPixels();
		imp3.killRoi();

		String[] info1 = ACRutils.imageInformation(imp1);
		for (int i1 = 0; i1 < info1.length; i1++) {
			ACRlog.appendLog(namepathReport, ACRlog.qui() + "#" + String.format("%03d", i1) + "#  " + info1[i1]);
		}
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "mintolerance:" + mintolerance);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "maxtolerance:" + maxtolerance);

//		String imageName = "thickness910.jpg";
		String pathImage = pathReport + "\\" + imageName1;
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #910#" + pathImage);

		// linea calcolo segnale mediato
		// boolean invert = false;
//		String profilename1 = "greenprofile912.jpg";
		String pathname1 = pathReport + "\\" + profileName1;
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "profileName1: #912#" + pathname1);
		double fwhm2mm = ACRlocalizer.FWHMcalc(profi3, 3, "greenProfile", pathname1, pathReport, "#912#") * dimPixel;
		if (step || verbose)
			ACRlog.waitHere("PROFILO", true, timeout);
		imp3.setRoi(new Line(GX, GY, HX, HY));
		Roi roi4 = imp3.getRoi();
		imp3.getRoi().setStrokeColor(Color.YELLOW);
		over3.addElement(imp3.getRoi());
		imp3.updateAndDraw();
		double[] profi4 = ((Line) roi4).getPixels();
		imp3.killRoi();

//		String profilename2 = "yellowprofile911.jpg";
		String pathname2 = pathReport + "\\" + profileName2;
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "profileName2: #911#" + pathname2);

		double fwhm1mm = ACRlocalizer.FWHMcalc(profi4, 3, "yellowProfile", pathname2, pathReport, "#911#") * dimPixel;
		if (step || verbose)
			ACRlog.waitHere("PROFILO", true, timeout);
		// linea calcolo segnale mediato
		Line.setWidth(1);
//		title = "ABnormal";
//		Plot plot4 = ACRgraphic.basePlot(profi4, title, Color.RED);
//		plot4.draw();
//		plot4.show();
		if (step || verbose)
			ACRlog.waitHere("fwhm2= " + fwhm2mm + " fwhm2= " + fwhm1mm, debug, timeout);
		if ((Double.compare(fwhm2mm, 0) == 0 || Double.compare(fwhm1mm, 0) == 0))
			ACRlog.waitHere("IMPOSSIBILE CALCOLARE LA THICKNESS,\nPROBLEMA IMMAGINE???");

		double thickmm = 0.2 * (fwhm2mm * fwhm2mm) / (fwhm2mm + fwhm2mm);

		if (step || verbose)
			ACRlog.waitHere("thick_mm= " + thickmm);

		IJ.saveAs(imp3, "jpg", pathImage);

		boolean failmin = (Double.compare(thickmm, mintolerance) < 0);
		boolean failmax = (Double.compare(thickmm, maxtolerance) > 0);
		String response = "";
		if (failmin || failmax) {
			response = "FAIL";
		} else {
			response = "PASS";
		}

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "fwhm1_mm: #201#" + IJ.d2s(fwhm1mm, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "fwhm2_mm: #202#" + IJ.d2s(fwhm2mm, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "thick_mm: #203#" + IJ.d2s(thickmm, 4));
		ACRlog.appendLog(namepathReport,
				ACRlog.qui() + "thickLimits_mm: #204#" + IJ.d2s(mintolerance, 1) + " - " + IJ.d2s(maxtolerance, 1));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "thickPass: #205#" + response);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "phantomAngleDegrees: #206#" + IJ.d2s(angle, 4));
		ACRlog.appendLog(namepathReport, "< finished " + LocalDate.now() + " @ " + LocalTime.now() + " >");
		
		
		
		
		imp2.changes = false;
		imp2.close();
		imp3.changes = false;
		imp3.close();
		
		
		
		
		
		

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
