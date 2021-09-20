package qualityACR;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.plugin.filter.GaussianBlur;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;

/**
 * Classe per misura Uniformita' e ghosts, fa riferimento all'articolo:
 * jimaging-06-00111-v2.pdf An Automated Method for Quality Control in MRI
 * Systems: Methods and Considerations Pubblicato su jimaging 18 October 2020
 * Commento: non ci si capisce un kaiser!
 * 
 * 
 * @author Alberto
 *
 */
public class Uniformity_ implements PlugIn {

	private static final boolean debug = true;

	public void run(String arg) {

		mainUnifor();
	}

	public void mainUnifor() {

		Properties prop = ACRutils.readConfigACR();
		int timeout = 0; // preme automaticamente OK ai messaggi durante i test
		boolean fastdefault = false;
		boolean stepdefault = false;
		boolean verbosedefault = false;
		boolean[] T1 = new boolean[7];
		boolean[] T2 = new boolean[7];
		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
		boolean[] defaults = { false, false, false, false, false, false, true, true, false, false, false, false, false,
				false };
		String[] headings = { "slices T1", "slices T2" };

		if (prop != null) {
			fastdefault = Boolean.parseBoolean(prop.getProperty("Uniformity.fast"));
			stepdefault = Boolean.parseBoolean(prop.getProperty("Uniformity.step"));
			verbosedefault = Boolean.parseBoolean(prop.getProperty("Uniformity.verbose"));
			for (int i1 = 0; i1 < 7; i1++) {
				T1[i1] = Boolean.parseBoolean(prop.getProperty("Uniformity.SliceT1[" + i1 + "]"));
				T2[i1] = Boolean.parseBoolean(prop.getProperty("Uniformity.SliceT2[" + i1 + "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
	//			defaults[count++] = T1[i1];
	//			defaults[count++] = T2[i1];
			}
		}

		GenericDialog gd1 = new GenericDialog("UNIFORMITY");
		gd1.addCheckbox("ANIMAZIONE 2 sec", fastdefault);
		gd1.addCheckbox("STEP", stepdefault);
		gd1.addCheckbox("VERBOSE", verbosedefault);
		gd1.addCheckboxGroup(7, 2, labels, defaults, headings);

		gd1.showDialog();
		if (gd1.wasCanceled()) {
			ACRlog.waitHere("premuto cancel");
			return;
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
			timeout = 2000;


		// vado a scrivere i setup nel config file
		if (prop == null) {
			prop = new Properties();
		}
		prop.setProperty("Uniformity.fast", "" + fast);
		prop.setProperty("Uniformity.step", "" + step);
		prop.setProperty("Uniformity.verbose", "" + verbose);
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Uniformity.SliceT1[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT1[i1];
			prop.setProperty(aux1, aux2);
		}
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Uniformity.SliceT2[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT2[i1];
			prop.setProperty(aux1, aux2);
		}
		try {
			ACRutils.writeConfigACR(prop);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String uno = "";
		String due = "";

		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			uno = uno + "," + i1 + " " + vetBoolSliceT1[i1];
			due = due + "," + i1 + " " + vetBoolSliceT2[i1];
		}
		// leggo i nomi di tutti i 15 file presenti
		String tmpFolderPath = IJ.getDirectory("temp");
		String completePath = tmpFolderPath + "ACRlist.tmp";
		String[] vetPath = ACRutils.readStringArrayFromFile(completePath);
		String pathReport1 = vetPath[4];

		String[] sortedListT1 = ACRinputOutput.readStackPathToSortedList(vetPath[1], "T1");
		if (sortedListT1 == null)
			IJ.log("sortedListT1 ==null");
		String[] sortedListT2 = ACRinputOutput.readStackPathToSortedList(vetPath[2], "T2");
		if (sortedListT2 == null)
			IJ.log("sortedListT2 ==null");

		//
		// Effettuo le varie elaborazioni richieste su ogni slice selezionata
		//

		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log("elaborazione slice T1 numero " + i1);
				phantomCalculations(sortedListT1[i1], pathReport1, "T1", i1 + 1, step, verbose, timeout);
			}
		}
		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log("elaborazione slice T2 numero " + i1);
				phantomCalculations(sortedListT2[i1], pathReport1, "T2", i1 + 1, step, verbose, timeout);
			}
		}

	}

	public void phantomCalculations(String path1, String pathReport, String group, int slice, boolean step,
			boolean verbose, int timeout) {

		// questa dovrebbe essere l'apertura comune a tutte le main delle varie classi
		// apertura immagine, display, zoom
		// chiamata prima subroutine passando l'immagine pronta
		// eccetraz ecceteraz
		// ------------- inizio comune ----------------
		IJ.log(ACRlog.qui() + "START>");
		double mintolerance = 85;
		String aux1 = "_" + group + "S" + slice;
		String namepathReport = pathReport + "\\ReportUniformity" + aux1 + ".txt";
		String imageName1 = "image905" + aux1 + ".jpg";
		String namepathImage1 = pathReport + "\\" + imageName1;
		String imageName2 = "positions906" + aux1 + ".jpg";
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

		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);
		IJ.saveAs(imp2, "jpg", namepathImage1);

// -----------------------------------------------------------------
// PIU - Percentage Image Uniformity	
// -----------------------------------------------------------------

		double[] phantomCircle = ACRlocalizer.phantomLocalizerAdvanced(imp2, step, verbose, timeout);

		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp2, ACRconst.DICOM_PIXEL_SPACING), 1));

		// -------------------------------- MROI ------------------------------
		// L'area del cerchio della MROI e'esplicitamente stabilita in "ACR small
		// phantom guidance" a pag 24 " Place a large, circular ROI on the image as
		// shown in Figure 17. This ROI must have an area of between 54 cm2 and 56 cm2
		// (5,400 to 5,600 mm2)
		// -----------------------------------------------------------------------------

		double areammq = 5600; // 5600 mm2 sono stabiliti dal protocollo
		double diam = 2 * Math.sqrt(areammq / Math.PI); // stabilito dalla geometria
		double dmroi = diam / dimPixel; // trasformo in pixel
		// ----------------------------------------------------------------------------

		// --------------------- 100 mmq ROI -----------------------------------------
		// calcolo che diametro deve avere una Roi di 100 mmq
		double pixeldia100 = 2 * Math.sqrt(100. / Math.PI) / dimPixel;
		// arrotondo all'int superiore (come dice l'articolo)
		int pixint100 = (int) Math.ceil(pixeldia100);
		// ----------------------------------------------------------------------------

		// estraggo i dati del phantomCircle
		int xphantom = (int)  Math.round(phantomCircle[0]);
		int yphantom = (int)  Math.round(phantomCircle[1]);
		int dphantom = (int)  Math.round(phantomCircle[2]);

		// ------- overlay trasparente per disegni ------------------
		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);

		imp2.setRoi(new OvalRoi(xphantom - dmroi / 2, yphantom - dmroi / 2, dmroi, dmroi));
		imp2.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();
		if (verbose || step)
			ACRlog.waitHere("MainUnifor> cerchio interno verde MROI", debug, timeout);
		over2.setStrokeWidth(2.0);

		if (step)
			ACRlog.waitHere("vedere MROI verde");

		int[] phantomcircle = new int[4];
		phantomcircle[0] = xphantom;
		phantomcircle[1] = yphantom;
		phantomcircle[2] = dphantom;

		int[] MROIcircle = new int[4];
		MROIcircle[0] = xphantom;
		MROIcircle[1] = yphantom;
		MROIcircle[2] = (int) dmroi;
		
		
		imp2.setRoi(new OvalRoi(xphantom - dmroi / 2, yphantom - dmroi / 2, dmroi, dmroi));
		ImageStatistics stat2 = imp2.getStatistics();
		double mean2 = stat2.mean;
	
		

		//
		// RICERCA PIXELS MAX E MINIMO E LORO MARCATURA GIALLO E AZZURRO
		//
		IJ.log("pixeldia100= " + pixeldia100);
		int[] vetout3 = searchMinMaxPixelsInteger(imp2, MROIcircle);
		ACRlog.logVector(vetout3, ACRlog.qui() + "vetout3");
		ACRutils.plotPoints(imp2, over2, vetout3[2], vetout3[3], Color.YELLOW, 2, 3);
		ACRutils.plotPoints(imp2, over2, vetout3[4], vetout3[5], Color.CYAN, 2, 3);
		imp2.killRoi();
		imp2.updateAndDraw();
		//
		// RICERCA AREE MAX E MIN MARCATE ROSSO E BLU
		//

		int[] vetout1 = searchMinMaxCircleMean(imp2, MROIcircle, (int) Math.round(pixeldia100));
		ACRlog.logVector(vetout1, ACRlog.qui() + "vetout1");

		imp2.setRoi(new OvalRoi(vetout1[2] - pixeldia100 / 2, vetout1[3] - pixeldia100 / 2, pixeldia100, pixeldia100));
		imp2.getRoi().setStrokeColor(Color.RED);
		over2.addElement(imp2.getRoi());
		imp2.setRoi(new OvalRoi(vetout1[4] - pixeldia100 / 2, vetout1[5] - pixeldia100 / 2, pixeldia100, pixeldia100));
		imp2.getRoi().setStrokeColor(Color.BLUE);
		over2.addElement(imp2.getRoi());

		//
		// RICERCA AR
		// RICERCA AREE MAX E MIN DOPO SMOOTH GAUSSIANO
		// RISULTATI IDENTICI ALL'INTERNO DI MROI

		//
		double sigma = 2.0;
		double accuracy = 0.0002;
		FloatProcessor fp1 = gaussian_blur(imp1, sigma, accuracy);
		ImagePlus imp3 = new ImagePlus("filtrataGauss", fp1);
		imp3.show();
		ACRutils.zoom(imp3);
		IJ.log(ACRlog.qui());
		// ==================================

		int[] vetout2 = searchMinMaxCircleMean(imp3, MROIcircle, (int) Math.round(pixeldia100));
		ACRlog.logVector(vetout2, ACRlog.qui() + "vetout2");

		double xmax = vetout2[2];
		double ymax = vetout2[3];
		double xmin = vetout2[4];
		double ymin = vetout2[5];

		imp2.setRoi(new OvalRoi(xmax - pixeldia100 / 2, ymax - pixeldia100 / 2, pixeldia100, pixeldia100));
		imp2.getRoi().setStrokeColor(Color.ORANGE);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();
		imp2.setRoi(new OvalRoi(xmin - pixeldia100 / 2, ymin - pixeldia100 / 2, pixeldia100, pixeldia100));
		imp2.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();
		imp2.updateAndDraw();

		//
		double PIU = uniforACR(imp2, phantomcircle, MROIcircle, vetout1, pixint100);

		String[] info1 = ACRutils.imageInformation(imp1);
		for (int i1 = 0; i1 < info1.length; i1++) {
			ACRlog.appendLog(namepathReport, ACRlog.qui() + "#" + String.format("%03d", i1) + "#  " + info1[i1]);
		}
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "PercentIntegralUniformity: #101#" + IJ.d2s(PIU, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "mintolerance: #102#" + IJ.d2s(mintolerance, 4));
		boolean failmin = (Double.compare(PIU, mintolerance) < 0);
		String response = "";
		if (failmin) {
			response = "FAIL";
		} else {
			response = "PASS";
		}

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "giudizio PIU: #103#" + response);

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "maxMeanRoiValue: #201#" + IJ.d2s(mean2, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "maxMeanRoiValue: #202#" + IJ.d2s(vetout1[0], 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "maxMeanRoi xpos: #203#" + IJ.d2s(vetout1[2], 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "maxMeanRoi ypos: #204#" + IJ.d2s(vetout1[3], 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "minMeanRoiValue: #205#" + IJ.d2s(vetout1[1], 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "minMeanRoi xpos: #206#" + IJ.d2s(vetout1[4], 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "minMeanRoi ypos: #207#" + IJ.d2s(vetout1[5], 4));
//		String namepathImage2 = pathReport + "\\" + imageName2;
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #905#" + namepathImage1);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #906#" + namepathImage2);
		IJ.saveAs(imp2, "jpg", namepathImage2);
		ACRlog.appendLog(namepathReport, "< finished " + LocalDate.now() + " @ " + LocalTime.now() + " >");

		imp2.changes = false;
		imp2.close();
		imp3.changes = false;
		imp3.close();

	}

	/**
	 * Non ho capito se vada calcolata nuovamente la statistica di max e min, sulla
	 * immagine iniziale, o se basta andarsi a leggere il valore dei due pixel sulla
	 * immagine della media, calcolata secondo l'articolo CHIEDERE
	 * 
	 * @param imp1          immagine di input
	 * @param phantomCircle cerchio esterno fantoccio
	 * @param MROIcircle    cerchio MROI
	 * @param minmaxValues  vettore coordinate minmax
	 * @param pixint100     dimensioni roi di calcolo (dispari) >100 mmq
	 * @param timeout
	 * @param verbose
	 * @param fast
	 * @return
	 */
	public static double uniforACR(ImagePlus imp1, int[] phantomCircle, int[] MROIcircle, int[] minmaxValues,
			int pixint100) {

		IJ.log(ACRlog.qui() + "START");
		imp1.killRoi();
		ImagePlus imp2 = imp1.duplicate();

		// =====================================================================
		// ================ ELABORAZIONE PRINCIPALE PER UNIFOR SU MROI =========
		// =====================================================================
		int xposmin = minmaxValues[0];
		int yposmin = minmaxValues[1];
		int amin = minmaxValues[2];
//		int xposmax = (int) minmaxValues[3];
//		int yposmax = (int) minmaxValues[4];
		int amax = minmaxValues[5];
		imp2.setRoi(new OvalRoi(xposmin - pixint100 / 2, yposmin - pixint100 / 2, pixint100, pixint100));

		// non so se mi basti utilizzare il valore del pixel, calcolato come
		// dal'articolo, oppure se devo ricalcolare la media in maniera tradizionale (se
		// siamo sul bordo otterremo risultati totalmente diversi CHIEDERE
		//
		double UNIFORlow = (double) amin;
		double UNIFORhigh = (double) amax;
		double PIU_UNIFOR = 100 * (1.0 - ((UNIFORhigh - UNIFORlow) / (UNIFORhigh + UNIFORlow)));
		IJ.log("uniforACR> calcolo uniformita': UNIFORhigh= " + String.format("%.4f", UNIFORhigh) + " UNIFORlow= "
				+ String.format("%.4f", UNIFORlow) + " PIU_UNIFOR= " + String.format("%.4f", PIU_UNIFOR));
		IJ.log(ACRlog.qui() + "END");
		return PIU_UNIFOR;
	}

	/**
	 * Si basa su una tesi di Atiba Fitzpatrick, che spiega molto nei particolari un
	 * suo/loro programma in Matlab. La differenza rispetto a tutti i programmi in
	 * Matlab disponibili in rete e'che qui vengono chiaramente spiegati algoritmi e
	 * metodi. Pertanto non occorre tentare un reverse engineering del sorgente (che
	 * non ho trovato), poiche'la spiegazione pare sufficiente. VEDIAMO La procedura
	 * proposta per trovare gli spot massimo e minimo: 1. The slice is blurred in
	 * order to lessen the effect of noise. A Gaussian kernel with standard
	 * deviation 2 and size 6 squared is used.
	 * 
	 * @param imp1          immagine di input
	 * @param phantomCircle cerchio esterno fantoccio
	 * @param MROIcircle    cerchio MROI
	 * @param minmaxValues  vettore coordinate minmax
	 * @param pixint100     dimensioni roi di calcolo (dispari) >100 mmq
	 * @param timeout
	 * @param verbose
	 * @param fast
	 * @return
	 */
	public static double uniforAYV(ImagePlus imp1, int[] phantomCircle, int[] MROIcircle, double[] minmaxValues,
			int pixint100, boolean step, boolean verbose, int timeout) {

		IJ.log(ACRlog.qui() + "START");
		imp1.killRoi();
		ImagePlus imp2 = imp1.duplicate();

		// =====================================================================
		// ================ ELABORAZIONE PRINCIPALE PER UNIFOR SU MROI =========
		// =====================================================================
		int xposmin = (int) minmaxValues[0];
		int yposmin = (int) minmaxValues[1];
		double amin = minmaxValues[2];
//		int xposmax = (int) minmaxValues[3];
//		int yposmax = (int) minmaxValues[4];
		double amax = minmaxValues[5];
		imp2.setRoi(new OvalRoi(xposmin - pixint100 / 2, yposmin - pixint100 / 2, pixint100, pixint100));

		// non so se mi basti utilizzare il valore del pixel, calcolato come
		// dal'articolo, oppure se devo ricalcolare la media in maniera tradizionale (se
		// siamo sul bordo otterremo risultati totalmente diversi CHIEDERE
		//
		double UNIFORlow = amin;
		double UNIFORhigh = amax;
		double PIU_UNIFOR = 100 * (1.0 - ((UNIFORhigh - UNIFORlow) / (UNIFORhigh + UNIFORlow)));
		IJ.log("uniforACR> calcolo uniformita': UNIFORhigh= " + String.format("%.4f", UNIFORhigh) + " UNIFORlow= "
				+ String.format("%.4f", UNIFORlow) + " PIU_UNIFOR= " + String.format("%.4f", PIU_UNIFOR));
		IJ.log(ACRlog.qui() + "END");
		return PIU_UNIFOR;
	}

	/**
	 * 1. The slice is blurred in order to lessen the effect of noise. A Gaussian
	 * kernel with standard deviation 2 and size 6 squared is used.
	 * 
	 * @param imp1
	 * @param phantomCircle
	 * @param MROIcircle
	 * @param minmaxValues
	 * @param pixint100
	 * @param step
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static int[] minmaxAYV(ImagePlus imp1, double[] phantomCircle, boolean step, boolean verbose, int timeout) {

		IJ.log(ACRlog.qui() + "START");
		double sigma = 2.0;
		double accuracy = 0.0002;
		FloatProcessor fp1 = gaussian_blur(imp1, sigma, accuracy);
		ImagePlus imp3 = new ImagePlus("filtrataGauss", fp1);
		imp3.show();
		ACRutils.zoom(imp3);
		IJ.log(ACRlog.qui());

		// estraggo i dati del phantomCircle
		double xphantom = phantomCircle[0];
		double yphantom = phantomCircle[1];
		double dphantom = phantomCircle[2];
		// ---------------------------------------------------------------------------
		// L'area del cerchio della MROI e'esplicitamente suggerita in "ACR small
		// phantom guidance" a pag 24 " Place a large, circular ROI on the image as
		// shown in Figure 17. This ROI must have an area of between 54 cm2 and 56 cm2
		// (5,400 to 5,600 mm2)
		// -----------------------------------------------------------------------------

		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));
		// calcolo che diametro deve avere una Roi di 100 mmq
		double pixeldia100 = 2 * Math.sqrt(100. / Math.PI) / dimPixel; // geometria
		int pixint100 = (int) Math.ceil(pixeldia100); // arrotondo all'int superiore (come dice l'articolo)
		double areammq = 5600; // 5600 mm2 sono stabiliti dal protocollo
		double dmroi = 2 * Math.sqrt(areammq / Math.PI) / dimPixel; // geometria
		imp3.setRoi(new OvalRoi(xphantom - dmroi / 2, yphantom - dmroi / 2, dmroi, dmroi));

		Overlay over3 = new Overlay(); // con questo definisco un overlay trasparente per i disegni
		imp3.setOverlay(over3);
		imp3.getRoi().setStrokeColor(Color.GREEN);
		over3.addElement(imp3.getRoi());
		over3.setStrokeWidth(1.5);
		imp3.updateAndDraw();

		int[] MROIcircle = new int[3];
		MROIcircle[0] = (int) Math.round(xphantom);
		MROIcircle[1] = (int) Math.round(yphantom);
		MROIcircle[2] = (int) Math.round(dmroi);

		int[] vetout = searchMinMaxPixelsFloat(imp3, MROIcircle);
		ACRlog.logVector(vetout, ACRlog.qui() + "vetout");
		IJ.log(ACRlog.qui() + "END");

		// applico due distinte strategie: cerco minmax e loro posizione e vi metto
		// attorno una ROI, oppure faccio muovere una ROI all'interno della MROI e cerco
		// il meanmin e il meanmax

		return vetout;
	}

	/**
	 * 1. The slice is blurred in order to lessen the effect of noise. A Gaussian
	 * kernel with standard deviation 2 and size 6 squared is used.
	 * 
	 * @param imp1
	 * @param phantomCircle
	 * @param MROIcircle
	 * @param minmaxValues
	 * @param pixint100
	 * @param step
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static int[] blurImage(ImagePlus imp1, double[] phantomCircle, boolean step, boolean verbose, int timeout) {

		IJ.log(ACRlog.qui() + "START");
		double sigma = 2.0;
		double accuracy = 0.0002;
		FloatProcessor fp1 = gaussian_blur(imp1, sigma, accuracy);
		ImagePlus imp3 = new ImagePlus("filtrataGauss", fp1);
		imp3.show();
		ACRutils.zoom(imp3);
		IJ.log(ACRlog.qui());

		// estraggo i dati del phantomCircle
		double xphantom = phantomCircle[0];
		double yphantom = phantomCircle[1];
		double dphantom = phantomCircle[2];
		// ---------------------------------------------------------------------------
		// L'area del cerchio della MROI e'esplicitamente suggerita in "ACR small
		// phantom guidance" a pag 24 " Place a large, circular ROI on the image as
		// shown in Figure 17. This ROI must have an area of between 54 cm2 and 56 cm2
		// (5,400 to 5,600 mm2)
		// -----------------------------------------------------------------------------

		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));
		// calcolo che diametro deve avere una Roi di 100 mmq
		double pixeldia100 = 2 * Math.sqrt(100. / Math.PI) / dimPixel; // geometria
		int pixint100 = (int) Math.ceil(pixeldia100); // arrotondo all'int superiore (come dice l'articolo)
		double areammq = 5600; // 5600 mm2 sono stabiliti dal protocollo
		double dmroi = 2 * Math.sqrt(areammq / Math.PI) / dimPixel; // geometria
		imp3.setRoi(new OvalRoi(xphantom - dmroi / 2, yphantom - dmroi / 2, dmroi, dmroi));

		Overlay over3 = new Overlay(); // con questo definisco un overlay trasparente per i disegni
		imp3.setOverlay(over3);
		imp3.getRoi().setStrokeColor(Color.GREEN);
		over3.addElement(imp3.getRoi());
		over3.setStrokeWidth(1.5);
		imp3.updateAndDraw();

		int[] MROIcircle = new int[3];
		MROIcircle[0] = (int) Math.round(xphantom);
		MROIcircle[1] = (int) Math.round(yphantom);
		MROIcircle[2] = (int) Math.round(dmroi);

		int[] vetout = searchMinMaxPixelsFloat(imp3, MROIcircle);
		ACRlog.logVector(vetout, ACRlog.qui() + "vetout");

		// applico due distinte strategie: cerco minmax e loro posizione e vi metto
		// attorno una ROI, oppure faccio muovere una ROI all'interno della MROI e cerco
		// il meanmin e il meanmax
		IJ.log(ACRlog.qui() + "END");

		return vetout;
	}

	public static FloatProcessor gaussian_blur(ImagePlus imp1, double sigma, double accuracy) {

		ImagePlus imp2 = imp1.duplicate();
		ImageProcessor ip2 = imp2.getProcessor();
		FloatProcessor fp2 = ip2.convertToFloatProcessor();
		(new GaussianBlur()).blurFloat(fp2, sigma, sigma, accuracy);
		return fp2;
	}

	/**
	 * genera una mask circolare
	 * 
	 * @param imp1
	 * @return
	 */
	public static ImagePlus generateBinaryMask(int[] MROIcircle, int width, int height) {

		int xmroi = MROIcircle[0];
		int ymroi = MROIcircle[1];
		int dmroi = MROIcircle[2];

		IJ.log(ACRlog.qui() + "START");

		ImagePlus impMask = NewImage.createByteImage("MASK", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ipMask = impMask.getProcessor();
		ipMask.setColor(Color.WHITE);
		Roi roi2 = new OvalRoi(xmroi - dmroi / 2, ymroi - dmroi / 2, dmroi, dmroi);
		ipMask.setMask(roi2.getMask());
		ipMask.setRoi(roi2.getBounds());
		ipMask.fill(ipMask.getMask());
		impMask.updateAndDraw();
		IJ.log(ACRlog.qui() + "END");
		return (impMask);
	}

	/**
	 * Ricerca di minimo e massimo utilizzando la media su una ROI circolare di 100
	 * mmq che effettua una scansione interna a MROI
	 * 
	 * @param imp2
	 * @param mroiCircle
	 * @param scandiameter
	 * @return
	 */
	public static int[] searchMinMaxCircleMean(ImagePlus imp1, int[] mroiCircle, int scandiameter) {

		IJ.log(ACRlog.qui() + "START");
		ImagePlus imp2 = imp1.duplicate();
		double xmroi = mroiCircle[0];
		double ymroi = mroiCircle[1];
		double dmroi = mroiCircle[2];
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		int scanrad = scandiameter / 2;
		imp2.show();
		ACRutils.zoom(imp2);
		Overlay over1 = new Overlay(); // con questo definisco un overlay trasparente per i disegni
		imp2.setOverlay(over1);
		imp2.setRoi(new OvalRoi(xmroi - dmroi / 2, ymroi - dmroi / 2, dmroi, dmroi));
		imp2.getRoi().setStrokeColor(Color.BLUE);
		over1.addElement(imp2.getRoi());
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		int xmax = 0;
		int ymax = 0;
		int xmin = 0;
		int ymin = 0;
		double distance = 0;
		int xcenter = 0;
		int ycenter = 0;
		for (int x1 = 0; x1 < width - scandiameter; x1++) {
			for (int y1 = 0; y1 < height - scandiameter; y1++) {
				xcenter = x1 + scandiameter / 2;
				ycenter = y1 + scandiameter / 2;
				distance = Math.sqrt(Math.pow(xmroi - xcenter, 2) + Math.pow(ymroi - ycenter, 2));
				if ((dmroi / 2 - scandiameter / 2) < distance) {
					continue;
				}
				imp2.setRoi(new OvalRoi(x1, y1, scandiameter, scandiameter));
				imp2.getRoi().setStrokeColor(Color.GREEN);
				over1.addElement(imp2.getRoi());
				ImageStatistics stat1 = imp2.getStatistics();
				double mean = stat1.mean;
				if (Double.compare(mean, max) > 0) {
					max = mean;
					xmax = xcenter;
					ymax = ycenter;
				}
				if (Double.compare(mean, min) < 0) {
					min = mean;
					xmin = xcenter;
					ymin = ycenter;
				}
			}
		}

		imp2.close();

		int[] vetout = new int[6];
		vetout[0] = (int) Math.round(max);
		vetout[1] = (int) Math.round(min);
		vetout[2] = xmax;
		vetout[3] = ymax;
		vetout[4] = xmin;
		vetout[5] = ymin;
		IJ.log(ACRlog.qui() + "END");

		return vetout;
	}

	public static int[] searchMinMaxPixelsFloat(ImagePlus imp1, int[] MROIcircle) {

		IJ.log(ACRlog.qui() + "START");
		ImageProcessor ip1 = imp1.getProcessor();
		float[] pixels1 = (float[]) ip1.getPixels();
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		ImagePlus mask = generateBinaryMask(MROIcircle, width, height);
		ImageProcessor ipmask = mask.getProcessor();
		byte[] masks1 = (byte[]) ipmask.getPixels();
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		int xmax = 0;
		int ymax = 0;
		int xmin = 0;
		int ymin = 0;
		for (int y1 = 0; y1 < height; y1++) {
			int offset1 = y1 * width;
			for (int x1 = 0; x1 < height; x1++) {
				int index1 = offset1 + x1;
				if (masks1[index1] != 0) {
					float val = pixels1[index1];
					if (val > max) {
						max = val;
						xmax = x1;
						ymax = y1;
					}
					if (val < min) {
						min = val;
						xmin = x1;
						ymin = y1;
					}
				}
			}
		}
		int[] vetout = new int[6];
		vetout[0] = Math.round(max);
		vetout[1] = Math.round(min);
		vetout[2] = xmax;
		vetout[3] = ymax;
		vetout[4] = xmin;
		vetout[5] = ymin;
		IJ.log(ACRlog.qui() + "END");

		return vetout;
	}

	public static int[] searchMinMaxPixelsInteger(ImagePlus imp1, int[] MROIcircle) {

		IJ.log(ACRlog.qui() + "START");
		ImageProcessor ip1 = imp1.getProcessor();
		short[] pixels1 = (short[]) ip1.getPixels();
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		ImagePlus mask = generateBinaryMask(MROIcircle, width, height);
		ImageProcessor ipmask = mask.getProcessor();
		byte[] masks1 = (byte[]) ipmask.getPixels();
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int xmax = 0;
		int ymax = 0;
		int xmin = 0;
		int ymin = 0;
		for (int y1 = 0; y1 < height; y1++) {
			int offset1 = y1 * width;
			for (int x1 = 0; x1 < height; x1++) {
				int index1 = offset1 + x1;
				if (masks1[index1] != 0) {
					short val = pixels1[index1];
					if (val > max) {
						max = val;
						xmax = x1;
						ymax = y1;
					}
					if (val < min) {
						min = val;
						xmin = x1;
						ymin = y1;
					}
				}
			}
		}
		int[] vetout = new int[6];
		vetout[0] = max;
		vetout[1] = min;
		vetout[2] = xmax;
		vetout[3] = ymax;
		vetout[4] = xmin;
		vetout[5] = ymin;
		IJ.log(ACRlog.qui() + "END");

		return vetout;
	}

}
