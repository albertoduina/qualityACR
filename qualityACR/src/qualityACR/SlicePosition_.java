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
import ij.gui.Roi;
import ij.plugin.PlugIn;

public class SlicePosition_ implements PlugIn {
	public static final boolean debug = true;

	public void run(String arg) {

		ACRlog.waitHere("4 - Slice position accuracy");

		mainSlicePositionAccuracy();

	}

	public void mainSlicePositionAccuracy() {
		Properties prop = ACRutils.readConfigACR();
		int timeout = 0; // preme automaticamente OK ai messaggi durante i test
		IJ.log(ACRlog.qui() + "START");
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
			fastdefault = Boolean.parseBoolean(prop.getProperty("Position.fast"));
			stepdefault = Boolean.parseBoolean(prop.getProperty("Position.step"));
			verbosedefault = Boolean.parseBoolean(prop.getProperty("Position.verbose"));
			for (int i1 = 0; i1 < 7; i1++) {
				T1[i1] = Boolean.parseBoolean(prop.getProperty("Position.SliceT1[" + i1 + "]"));
				T2[i1] = Boolean.parseBoolean(prop.getProperty("Position.SliceT2[" + i1 + "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
				defaults[count++] = T1[i1];
				defaults[count++] = T2[i1];
			}
		}

		GenericDialog gd1 = new GenericDialog("SLICE Position");
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
		prop.setProperty("Position.fast", "" + fast);
		prop.setProperty("Position.step", "" + step);
		prop.setProperty("Position.verbose", "" + verbose);
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Position.SliceT1[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT1[i1];
			prop.setProperty(aux1, aux2);
		}
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Position.SliceT2[" + i1 + "]";
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
		String pathReport = vetPath[4] + "\\ReportPosition.txt";
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

		// DEVO creare un nuovo report, senno' che controllo faccio?
		if (!ACRlog.initLog(pathReport))
			return;

		// ora in base alle selezioni effettuate nelle checkbox del dialogo, dobbiamo
		// elaborare solo i file selezionati
		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log(ACRlog.qui() + "elaborazione slice T1 numero " + i1);
				evalPosition(sortedListT1[i1], pathReport, i1, step, verbose, timeout);
			}
		}

		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log(ACRlog.qui() + "==================");
				IJ.log(ACRlog.qui() + "elaborazione slice T2 numero " + i1);
				evalPosition(sortedListT2[i1], pathReport, i1, step, verbose, timeout);
			}
		}
		ACRlog.waitHere("SLICE Position TERMINATA");
	}

	public void evalPosition(String path1, String pathReport, int s1, boolean step, boolean verbose, int timeout) {

		IJ.log(ACRlog.qui() + "<START>");
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		//
		// ========== AZZERAMENTO IMMAGINE =============
		//
		// IJ.run(imp1, "Rotate... ", "angle=-0.80 grid=1 interpolation=Bilinear");
		// IJ.run(imp1, "Translate...", "x=-0.29 y=1.0 interpolation=Bilinear");
		//
		// ========== INTROIAMENTO IMMAGINE =============
		IJ.run(imp1, "Rotate... ", "angle=40 grid=1 interpolation=Bilinear");
		// ATTENZIONE SE L'ANGOLO DI ROTAZIONE SUPERA I +-45° LA ROUTINE DI
		// SELEZIONE DEI VERTICI CAMBIA BRUSCAMENTE L?ORDINE DEI VERTICI ED IL PROGRAMMA
		// FALLISCE (CI HO PERSO DUE GIORNI PORCAPALETTA)
		//
		IJ.run(imp1, "Translate...", "x=-20 y=20 interpolation=Bilinear");
		//
		// MI RESTA DA SPIEGARE PERCHE' RUOTANDO L'IMMAGINE
		// CAMBIA IL RISULTATO!!!!!
		//
		// IJ.run(imp2, "Flip Horizontally", "");
		//
		// IJ.run(imp1, "Rotate... ", "angle=10 grid=1 interpolation=Bilinear");
		//
		//
		// ===============================================
		//
		imp1.show();
		ACRutils.zoom(imp1);

		ImagePlus imp2 = imp1.duplicate();
		imp2.setTitle("002");
		imp2.show();
		ACRutils.zoom(imp2);
		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);
		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp2, ACRconst.DICOM_PIXEL_SPACING), 1));

		double[] phantomCircle = ACRlocalizer.phantomLocalizerAdvanced(imp2, step, verbose, timeout);

		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp2, phantomCircle, step, verbose, timeout);
		if (step)
			ACRlog.waitHere("VERTICI", debug, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, step, verbose, timeout);

		if (verbose)
			IJ.log("phantomCircle= x=" + phantomCircle[0] + " y=" + phantomCircle[1] + " d=" + phantomCircle[2]
					+ " angle= " + angle);
		if (step)
			ACRlog.waitHere("ANGOLO", debug, timeout);

		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);
		imp1.getWindow().toFront();

		if (verbose)
			ACRlog.logMatrix(phantomVertices, ACRlog.qui() + "phantomVertices");

		// le coordinate, riferite ad una figura di fantoccio perfettamente centrato e
		// livellato, sono le seguenti:
		// A1= 93,18 A2= 93,48
		// B1= 99,18 B2= 99,48
		// 
		double[][] matin1 = new double[2][4];
		matin1[0][0] = 93;
		matin1[1][0] = 18;
		matin1[0][1] = 93;
		matin1[1][1] = 48;
		matin1[0][2] = 99;
		matin1[1][2] = 18;
		matin1[0][3] = 99;
		matin1[1][3] = 48;

		double width = imp1.getWidth();
		double height = imp1.getHeight();
		double[][] matout1 = ACRlocalizer.rototrasla(matin1, width, height, phantomCircle, angle, step, verbose,
				timeout);

		// andiamo ad estrarre e poi plottare i punti trovati
		int EX = (int) Math.round(matout1[0][0]);
		int EY = (int) Math.round(matout1[1][0]);
		int FX = (int) Math.round(matout1[0][1]);
		int FY = (int) Math.round(matout1[1][1]);
		int GX = (int) Math.round(matout1[0][2]);
		int GY = (int) Math.round(matout1[1][2]);
		int HX = (int) Math.round(matout1[0][3]);
		int HY = (int) Math.round(matout1[1][3]);

		if (verbose)
			IJ.log("E= " + EX + " , " + EY + " F= " + FX + " , " + FY);

		ACRutils.plotPoints(imp1, over1, EX, EY, Color.GREEN, 1, 3);
		ACRutils.plotPoints(imp1, over1, FX, FY, Color.GREEN, 1, 3);

		Line.setWidth(3);
		imp1.setRoi(new Line(EX, EY, FX, FY));
		Roi roi3 = imp1.getRoi();
		imp1.updateAndDraw();
		imp1.getRoi().setStrokeColor(Color.GREEN);
		over1.addElement(imp1.getRoi());

		double[] profi3 = ((Line) roi3).getPixels();
		imp1.killRoi();
		
		String[] info1 = ACRutils.imageInformation(imp1);
		for (int i1 = 0; i1 < info1.length; i1++) {
			ACRlog.appendLog(pathReport, ACRlog.qui() + "#" + String.format("%03d", i1) + "#  " + info1[i1]);
		}
		String imageName = "position01.png";
		String path10 = pathReport.substring(0, pathReport.lastIndexOf("\\"));
		String pathImage = path10 + "\\" + imageName;
		ACRlog.appendLog(pathReport, ACRlog.qui() + "imageName: #100#" + pathImage);
		IJ.saveAs(imp1, "PNG", pathImage);
	
		String profilename1 = "greenprofile901.png";
		String pathname1 = pathReport.substring(0, pathReport.lastIndexOf("\\")) + "\\" + profilename1;
		ACRlog.appendLog(pathReport, ACRlog.qui() + "profileName1: #901#" + pathname1);
		double fwhm1pixel = ACRlocalizer.FWHMcalc(profi3, 3, "greenProfile", pathname1, pathReport);
		if (step || verbose)
			ACRlog.waitHere("PROFILO1", true, timeout);

		if (verbose)
			IJ.log("G= " + GX + " , " + GY + " H= " + HX + " , " + HY);

		ACRutils.plotPoints(imp1, over1, GX, GY, Color.YELLOW, 1, 3);
		ACRutils.plotPoints(imp1, over1, HX, HY, Color.YELLOW, 1, 3);

		Line.setWidth(3);
		imp1.setRoi(new Line(GX, GY, HX, HY));
		Roi roi4 = imp1.getRoi();
		imp1.updateAndDraw();
		imp1.getRoi().setStrokeColor(Color.YELLOW);
		over1.addElement(imp1.getRoi());

		double[] profi4 = ((Line) roi4).getPixels();
		imp1.killRoi();
		String profilename2 = "yellowprofile902.png";
		String pathname2 = pathReport.substring(0, pathReport.lastIndexOf("\\")) + "\\" + profilename2;
		ACRlog.appendLog(pathReport, ACRlog.qui() + "profileName2: #902#" + pathname2);
		double fwhm2pixel = ACRlocalizer.FWHMcalc(profi4, 3, "yellowProfile", pathname2, pathReport);
		double fwhm1mm=fwhm1pixel*dimPixel;
		double fwhm2mm=fwhm2pixel*dimPixel;
		
		
		
		if (step || verbose)
			ACRlog.waitHere("PROFILO1", true, timeout);
		
		
		ACRlog.appendLog(pathReport, ACRlog.qui() + "fwhm1_mm: #101#" + IJ.d2s(fwhm1mm, 4));
		ACRlog.appendLog(pathReport, ACRlog.qui() + "fwhm2_mm: #102#" + IJ.d2s(fwhm2mm, 4));

		ACRlog.waitHere("fwhm1pixel= " + fwhm1pixel + " fwhm2pixel= " + fwhm2pixel);

		return;
	}
}
