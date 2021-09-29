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
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.PlugIn;

public class SlicePosition_ implements PlugIn {
	public static final boolean debug = true;

	public void run(String arg) {

		mainSlicePositionAccuracy(arg);

	}

	public void mainSlicePositionAccuracy(String arg) {
		// se riceve 1 allora vuol dire che abbiamo l'elaborazione automatica per tutti
		// i controlli
		boolean auto = false;
		if (arg.equals("1"))
			auto = true;

		Properties prop = ACRutils.readConfigACR();
		int timeout = 0; // preme automaticamente OK ai messaggi durante i test
		IJ.log(ACRlog.qui() + "START");
		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
		boolean[] defaults = { true, true, false, false, false, false, false, false, false, false, false, false, false,
				false };
		String[] headings = { "slices T1", "slices T2" };
		boolean fastdefault = false;
		boolean stepdefault = false;
		boolean verbosedefault = false;
		boolean[] T1 = new boolean[7];
		boolean[] T2 = new boolean[7];

		if (prop != null && !auto) {
			fastdefault = Boolean.parseBoolean(prop.getProperty("Position.fast"));
			stepdefault = Boolean.parseBoolean(prop.getProperty("Position.step"));
			verbosedefault = Boolean.parseBoolean(prop.getProperty("Position.verbose"));
			for (int i1 = 0; i1 < 7; i1++) {
				// T1[i1] = Boolean.parseBoolean(prop.getProperty("Position.SliceT1[" + i1 +
				// "]"));
				// T2[i1] = Boolean.parseBoolean(prop.getProperty("Position.SliceT2[" + i1 +
				// "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
				// defaults[count++] = T1[i1];
				// defaults[count++] = T2[i1];
			}
		}

		boolean fast;
		boolean step;
		boolean verbose;
		boolean[] vetBoolSliceT1 = new boolean[7];
		boolean[] vetBoolSliceT2 = new boolean[7];

		if (auto) {
			timeout = 500;
			ACRlog.waitHere("SLICE POSITION AUTO", false, timeout);
			fast = fastdefault;
			step = stepdefault;
			verbose = verbosedefault;
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
				vetBoolSliceT1[i1] = defaults[count++];
				vetBoolSliceT2[i1] = defaults[count++];
			}
		} else {

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
			fast = gd1.getNextBoolean();
			step = gd1.getNextBoolean();
			verbose = gd1.getNextBoolean();
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
				IJ.log(ACRlog.qui() + "elaborazione slice T1 numero " + i1);
				evalPosition(sortedListT1[i1], pathReport, "T1", i1 + 1, step, verbose, timeout);
			}
		}

		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log(ACRlog.qui() + "==================");
				IJ.log(ACRlog.qui() + "elaborazione slice T2 numero " + i1);
				evalPosition(sortedListT2[i1], pathReport, "T2", i1 + 1, step, verbose, timeout);
			}
		}
		ACRlog.waitHere("SLICE POSITION TERMINATA", false, timeout);
	}

	public void evalPosition(String path1, String pathReport, String group, int slice, boolean step, boolean verbose,
			int timeout) {

		IJ.log(ACRlog.qui() + "START>");
		// questa dovrebbe essere l'apertura comune a tutte le main delle varie classi
		// apertura immagine, display, zoom
		// chiamata prima subroutine passando l'immagine pronta
		// eccetraz ecceteraz
		String aux1 = "_" + group + "S" + slice;
		String namepathReport = pathReport + "\\ReportPosition" + aux1 + ".txt";
		String imageName1 = "position920" + aux1 + ".jpg";
		String namepathImage1 = pathReport + "\\" + imageName1;
		String profileName1 = "greenprofile921" + aux1 + ".jpg";
		String namepathProfile1 = pathReport + "\\" + profileName1;

		String profileName2 = "yellowprofile922" + aux1 + ".jpg";
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
		// ========== AZZERAMENTO IMMAGINE =============
		//
		// IJ.run(imp1, "Rotate... ", "angle=-0.80 grid=1 interpolation=Bilinear");
		// IJ.run(imp1, "Translate...", "x=-0.29 y=1.0 interpolation=Bilinear");
		//
		// ========== INTROIAMENTO IMMAGINE =============
		// IJ.run(imp1, "Rotate... ", "angle=40 grid=1 interpolation=Bilinear");
		// ATTENZIONE SE L'ANGOLO DI ROTAZIONE SUPERA I +-45° LA ROUTINE DI
		// SELEZIONE DEI VERTICI CAMBIA BRUSCAMENTE L?ORDINE DEI VERTICI ED IL PROGRAMMA
		// FALLISCE (CI HO PERSO DUE GIORNI PORCAPALETTA)
		//
		// IJ.run(imp1, "Translate...", "x=-20 y=20 interpolation=Bilinear");
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

//		ACRutils.plotPoints(imp1, over1, EX, EY, Color.GREEN, 1, 3);
//		ACRutils.plotPoints(imp1, over1, FX, FY, Color.GREEN, 1, 3);

		Line.setWidth(4);
		imp1.setRoi(new Line(EX, EY, FX, FY));
		Roi roi3 = imp1.getRoi();
		imp1.updateAndDraw();
		imp1.getRoi().setStrokeColor(Color.RED);
		over1.addElement(imp1.getRoi());

		double[] profi3 = ((Line) roi3).getPixels();
		imp1.killRoi();

		String[] info1 = ACRutils.imageInformation(imp1);
		for (int i1 = 0; i1 < info1.length; i1++) {
			ACRlog.appendLog(namepathReport, ACRlog.qui() + "#" + String.format("%03d", i1) + "#  " + info1[i1]);
		}

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "profileName1: #921#" + namepathProfile1);
		double fwhm1pixel = ACRlocalizer.FWHMcalc(profi3, 3, "redProfile", namepathProfile1, pathReport, " #921#");
		if (step || verbose)
			ACRlog.waitHere("PROFILO1", true, timeout);

		if (verbose)
			IJ.log("G= " + GX + " , " + GY + " H= " + HX + " , " + HY);

//		ACRutils.plotPoints(imp1, over1, GX, GY, Color.YELLOW, 1, 3);
//		ACRutils.plotPoints(imp1, over1, HX, HY, Color.YELLOW, 1, 3);

		Line.setWidth(4);
		imp1.setRoi(new Line(GX, GY, HX, HY));
		Roi roi4 = imp1.getRoi();
		imp1.updateAndDraw();
		imp1.getRoi().setStrokeColor(Color.BLUE);
		over1.addElement(imp1.getRoi());

		double[] profi4 = ((Line) roi4).getPixels();
		imp1.killRoi();
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "profileName2: #922#" + namepathProfile2);
		double fwhm2pixel = ACRlocalizer.FWHMcalc(profi4, 3, "blueProfile", namepathProfile2, pathReport, "#922#");
		double fwhm1mm = fwhm1pixel * dimPixel;
		double fwhm2mm = fwhm2pixel * dimPixel;
		double differencemm = fwhm1mm - fwhm2mm;
		double difflimitmm = 5;
		String passfail = "";
		if (Double.compare(differencemm, difflimitmm) > 0)
			passfail = "FAIL";
		else
			passfail = "PASS";

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #920#" + namepathImage1);
		IJ.saveAs(imp1, "jpg", namepathImage1);

		if (step || verbose)
			ACRlog.waitHere("PROFILO1", true, timeout);

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "fwhm1_mm: #301#" + IJ.d2s(fwhm1mm, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "fwhm2_mm: #302#" + IJ.d2s(fwhm2mm, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "difference_mm: #303#" + IJ.d2s(differencemm, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "difference_mm: #304#" + IJ.d2s(difflimitmm, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "passafail: #305#" + passfail);

		// ACRlog.waitHere("fwhm1pixel= " + fwhm1pixel + " fwhm2pixel= " + fwhm2pixel);
		ACRlog.appendLog(namepathReport, "< finished " + LocalDate.now() + " @ " + LocalTime.now() + " >");
		imp2.changes = false;
		imp2.close();
		imp1.changes = false;
		imp1.close();

		return;
	}
}
