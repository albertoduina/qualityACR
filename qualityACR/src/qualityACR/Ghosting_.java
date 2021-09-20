package qualityACR;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.plugin.PlugIn;
import ij.process.ImageStatistics;

public class Ghosting_ implements PlugIn {

	private static final boolean debug = true;

	public void run(String arg) {
		mainGhosting();
	}

	public void mainGhosting() {

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
			fastdefault = Boolean.parseBoolean(prop.getProperty("Ghosting.fast"));
			stepdefault = Boolean.parseBoolean(prop.getProperty("Ghosting.step"));
			verbosedefault = Boolean.parseBoolean(prop.getProperty("Ghosting.verbose"));
			for (int i1 = 0; i1 < 7; i1++) {
	//			T1[i1] = Boolean.parseBoolean(prop.getProperty("Ghosting.SliceT1[" + i1 + "]"));
	//			T2[i1] = Boolean.parseBoolean(prop.getProperty("Ghosting.SliceT2[" + i1 + "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
	//			defaults[count++] = T1[i1];
	//			defaults[count++] = T2[i1];
			}
		}

		String pathLocalizer0 = "";
		String tmpFolderPath0 = IJ.getDirectory("temp");
		String completePath0 = tmpFolderPath0 + "ACRlist.tmp";
		String[] vetPath0 = ACRutils.readStringArrayFromFile(completePath0);
		for (int i1 = 0; i1 < vetPath0.length; i1++) {
			IJ.log("" + i1 + " " + vetPath0[i1]);

		}

		GenericDialog gd1 = new GenericDialog("GHOSTING0");
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
//		boolean geomLocalizer = gd1.getNextBoolean();
		boolean[] vetBoolSliceT1 = new boolean[7];
		boolean[] vetBoolSliceT2 = new boolean[7];
		for (int i1 = 0; i1 < 7; i1++) {
			vetBoolSliceT1[i1] = gd1.getNextBoolean();
			vetBoolSliceT2[i1] = gd1.getNextBoolean();
		}

		if (fast)
			ACRlog.waitHere("perche'mette fast????");

		// vado a scrivere i setup nel config file
		if (prop == null) {
			prop = new Properties();
		}
		prop.setProperty("Ghosting.fast", "" + fast);
		prop.setProperty("Ghosting.step", "" + step);
		prop.setProperty("Ghosting.verbose", "" + verbose);
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Ghosting.SliceT1[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT1[i1];
			prop.setProperty(aux1, aux2);
		}
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Ghosting.SliceT2[" + i1 + "]";
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
		String pathLocalizer = "";
		String tmpFolderPath = IJ.getDirectory("temp");
		String completePath = tmpFolderPath + "ACRlist.tmp";
		String[] vetPath = ACRutils.readStringArrayFromFile(completePath);
		String pathReport1 = vetPath[4];

		String[] listLocalizer = ACRinputOutput.readStackPathToSortedList(vetPath[1], "T1");
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

		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log("elaborazione slice T1 numero " + i1);
				roiGhost(sortedListT1[i1], pathReport1, "T1", i1+1, step, verbose, timeout);
			}
		}
		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log("elaborazione slice T2 numero " + i1);
				roiGhost(sortedListT2[i1], pathReport1, "T2", i1+1, step, verbose, timeout);
			}
		}

	}

	/**
	 * Ricerca posizione e diametro fantoccio. In pratica pero' bisogna usare il
	 * diametro esterno reale di 100 mm
	 * 
	 * @param path1
	 * @param slice
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public int[] phantomPositionSearch(String path1, int slice, boolean step, boolean fast, boolean verbose,
			int timeout) {

		double maxFitError = +20;

		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
//		if (fast) {

//			}
		// Ricerca delle coordinate e diametro del fantoccio su slice 5
		int[] out2 = ACRlocalizer.positionSearch2(imp1, maxFitError, step, verbose, timeout);
		int xphantom = (int) out2[0];
		int yphantom = (int) out2[1];
		int dphantom = (int) out2[2];
		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);

		if (true) {
			if (!imp1.isVisible()) {
				imp1.show();
				ACRutils.zoom(imp1);
			}
			imp1.setRoi(new OvalRoi(xphantom - dphantom / 2, yphantom - dphantom / 2, dphantom, dphantom));
			imp1.getRoi().setStrokeColor(Color.RED);
			over1.addElement(imp1.getRoi());
			imp1.killRoi();
			ACRlog.waitHere(ACRlog.qui() + "cerchio esterno rosso, fantoccio rilevato da positionSearch1", debug,
					timeout);
		}

		return out2;
	}

	public static void roiGhost(String path1, String pathReport, String group, int slice, boolean step, boolean verbose,
			int timeout) {

		// questa dovrebbe essere l'apertura comune a tutte le main delle varie classi
		// apertura immagine, display, zoom
		// chiamata prima subroutine passando l'immagine pronta
		// eccetraz ecceteraz
		// ------------- inizio comune ----------------
		IJ.log(ACRlog.qui() + "START>");
		double maxtolerance = 3.0;
		String aux1 = "_" + group + "S" + slice;
		String namepathReport = pathReport + "\\ReportGhosting" + aux1 + ".txt";
		String imageName1 = "image905" + aux1 + ".jpg";
		String namepathImage1 = pathReport + "\\" + imageName1;
//		String imageName2 = "positions906" + aux1 + ".jpg";
//		String namepathImage2 = pathReport + "\\" + imageName2;
		String imageName3 = "positions920" + aux1 + ".jpg";
		String namepathImage3 = pathReport + "\\" + imageName3;

		// ----- cancellazione cacchine precedenti -----
		boolean ok1 = ACRinputOutput.deleteFile(new File(namepathReport));
		IJ.log(ACRlog.qui());

		boolean ok2 = ACRinputOutput.deleteFile(new File(namepathImage1));
		IJ.log(ACRlog.qui());
		boolean ok3 = ACRinputOutput.deleteFile(new File(namepathImage3));
		IJ.log(ACRlog.qui());

		if (!(ok1 && ok2 ))
			ACRlog.waitHere("PROBLEMA CANCELLAZIONE");
		// ----- inizializzazione report----------------
		ACRlog.appendLog(namepathReport, "< calculated " + LocalDate.now() + " @ " + LocalTime.now() + " >");
		// ---------------------------------------------
		IJ.log(ACRlog.qui());
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);

		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);
		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);

		IJ.saveAs(imp2, "jpg", namepathImage1);

		// ----------------------------------------------------

		double[] phantomCircle = ACRlocalizer.phantomLocalizerAdvanced(imp2, step, verbose, timeout);
		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp2, ACRconst.DICOM_PIXEL_SPACING), 1));

		// estraggo i dati del phantomCircle
		int xphantom = (int) Math.round(phantomCircle[0]);
		int yphantom = (int) Math.round(phantomCircle[1]);
		int dphantom = (int) Math.round(phantomCircle[2]);

		// in realta' il nostro fantoccio e'piu'grande di quanto abbiamo misurato,
		// infatti la nostra misura e'falsata dal fatto che queste slices non sono
		// completamente piene di liquido. Quindi potremmo utilizzare il diametro
		// nominale del fantoccio che e'di 100 mm
		// Questo raggio viene anche indicato nelle istruzioni come R0

		int involucro = (int) Math.round(100 / dimPixel);

		// -----------------------------------------------------------------
// Visualizzo sull'immagine il posizionamento, ricevuto da  positionSearch1, che verra' utilizzato: 
// cerchio esterno fantoccio in rosso
// -----------------------------------------------------------------

		imp2.setRoi(new OvalRoi(xphantom - involucro / 2, yphantom - involucro / 2, involucro, involucro));
		imp2.getRoi().setStrokeColor(Color.RED);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();

		if (step) ACRlog.waitHere(ACRlog.qui() + "cerchio esterno blu, involucro esterno fantoccio", step, timeout);

		// -----------------------------------------------------------------
		// Visualizzo sull'immagine il posizionamento che verra' utilizzato
		// MROI in verde
		// ---------------------------------------------------------------------------
		// L'area del cerchio della MROI e'esplicitamente suggerita in "ACR small
		// phantom guidance" a pag 24 " Place a large, circular ROI on the image as
		// shown in Figure 17. This ROI must have an area of between 54 cm2 and 56 cm2
		// (5,400 to 5,600 mm2)
		// -----------------------------------------------------------------------------
		//

		double area = 5600; // 5600 mm2 sono stabiliti dal protocollo
		double diam = 2 * Math.sqrt(area / Math.PI); // stabilito dalla geometria
		double diampix = diam / dimPixel; // trasformo in pixel
		int xmroi = (int) phantomCircle[0];
		int ymroi = (int) phantomCircle[1];
		int dmroi = (int) Math.round(diampix);

		imp2.setRoi(new OvalRoi(xmroi - dmroi / 2, ymroi - dmroi / 2, dmroi, dmroi));
		// siccome ho impostato la MROI posso calcolare il valore medio di essa, si
		// utilizza nel calcolo dei ghosts
		ImageStatistics statMROI = imp2.getStatistics();
		double MROImean = statMROI.mean;

		imp2.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();
		if (step)
			ACRlog.waitHere(ACRlog.qui() + "cerchio interno verde MROI", step, timeout);

		int[] MROIcircle = new int[4];
		MROIcircle[0] = xmroi;
		MROIcircle[1] = ymroi;
		MROIcircle[2] = dmroi;
		
//		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #906#" + namepathImage2);
//		IJ.saveAs(imp2, "jpg", namepathImage2);


		// disegno 4 ROI rettangolari ai bordi dell'immagine, tale ROI deve avere un
		// area di 3 cmq ed un rapporto lunghezza/larghezza 8:1
		/*
		 * To automate the placement of the required ROIs and calculate the PSG, the
		 * following procedure was used. A main circular ROI, concentric with the
		 * phantom, with radius R = 0.8 x R0 and area approx. 64% of the phantom circle
		 * was used to define the useful field of view (UFOV). The dimensions of the
		 * four rectangular bar-shaped ROIs are set as the following: the value of the
		 * width, w (in pixels), is set such that the ROI lies 0.1 x R0 from the edge of
		 * the phantom as well as from the border of the image. Thus, the width w may be
		 * different for each one of the four (4) rectangular ROIs, denoted as wR, wL,
		 * wU, and wD. The long dimension H of each ROI is calculated (in pixels), so
		 * that the area of the ROI equals to 10 cm2. Consequently, H may also be
		 * different for each one of the four (4) rectangular ROIs, denoted as HR, HL,
		 * HU, and HD.
		 */

		// le istruzioni parlano sempre di raggio, ma per il nostro uso viene comodo
		// adottare sempre il diametro, in pratica non cambia nulla

		int width = imp2.getWidth();
		int height = imp2.getHeight();
		double diamUFOV = 0.8 * involucro;
		double gapFromOther = 0.1 * involucro;
		double sxgap = xphantom - involucro / 2; // spazio tra fantoccio e lato immagine
		double dxgap = width - xphantom - involucro / 2; // spazio tra fantoccio e lato immagine
		double upgap = yphantom - involucro / 2; // spazio tra fantoccio e lato immagine
		double logap = height - yphantom - involucro / 2; // spazio tra fantoccio e lato immagine
		int guard = 2; // metto un valore preciso)
//		int guard = (int) dmroi/50;		// spazio di rispetto attorno alle ROI
		double roimm = 300; // 300 mm2 sono stabiliti dal protocollo
		int roipix = (int) Math.round((roimm / dimPixel) / dimPixel); // lavoriamo sempre in pixel
		// se voglio usare le ellissi uso i valori con la E

		double sxroiwidth = sxgap - 2 * guard;
		double sxroiheight = roipix / sxroiwidth;
//		double sxroiheightE = (roipix * Math.PI) / (sxroiwidth * 2); /// NON NE SONO POI TAAAANTO SICURO!!!!!
//		ACRlog.waitHere(ACRlog.qui() + "diametro maggiore ellisse" + sxroiheightE);
//		double dxroiwidth = dxgap - gapFromOther * 2;
		double dxroiwidth = dxgap - 2 * guard;
		double dxroiheight = roipix / dxroiwidth;

		double uproiheight = upgap - 2 * guard;
		double uproiwidth = roipix / uproiheight;

//		double loroiwidth = logap - gapFromOther * 2;
		double dwroiheight = logap - 2 * guard;
		double dwroiwidth = roipix / dwroiheight;

		double areaX = sxroiwidth * sxroiheight * dimPixel * dimPixel;
		IJ.log("areaX= " + areaX);
		double areaY = dxroiwidth * dxroiheight * dimPixel * dimPixel;
		IJ.log("areaY= " + areaX);

		// adesso cerco di disegnare la ROI
		imp2.killRoi();
		imp2.setRoi(guard, yphantom - (int) sxroiheight / 2, (int) sxroiwidth, (int) sxroiheight);
		ImageStatistics statsxroi = imp2.getStatistics();
		double sxroimean = statsxroi.mean;
		imp2.getRoi().setStrokeColor(Color.GREEN);
		over2.addElement(imp2.getRoi());

		imp2.killRoi();
		imp2.setRoi(xphantom + involucro / 2 + guard, yphantom - (int) dxroiheight / 2, (int) dxroiwidth,
				(int) dxroiheight);
		ImageStatistics statdxroi = imp2.getStatistics();
		double dxroimean = statdxroi.mean;
		imp2.getRoi().setStrokeColor(Color.BLUE);
		over2.addElement(imp2.getRoi());

		imp2.killRoi();
		imp2.setRoi(xphantom - (int) uproiwidth / 2, guard, (int) uproiwidth, (int) uproiheight);
		ImageStatistics statuproi = imp2.getStatistics();
		double uproimean = statuproi.mean;
		imp2.getRoi().setStrokeColor(Color.YELLOW);
		over2.addElement(imp2.getRoi());

		imp2.killRoi();
		imp2.setRoi(xphantom - (int) dwroiwidth / 2, yphantom + involucro / 2 + guard, (int) dwroiwidth,
				(int) dwroiheight);
		ImageStatistics statdwroi = imp2.getStatistics();
		double dwroimean = statdwroi.mean;
		imp2.getRoi().setStrokeColor(Color.RED);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();

		// =====================================================
		// Misura ghosting ratio
		// =====================================================

		double ghostingratio = Math.abs((uproimean - dwroimean) - (sxroimean + dxroimean) / (2 * MROImean));

		
		String[] info1 = ACRutils.imageInformation(imp1);
		for (int i1 = 0; i1 < info1.length; i1++) {
			ACRlog.appendLog(namepathReport, ACRlog.qui() + "#" + String.format("%03d", i1) + "#  " + info1[i1]);
		}
		
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "imageName: #920#" + namepathImage3);
		IJ.saveAs(imp2, "jpg", namepathImage3);

		
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "Ghosting Ratio: #301#" + IJ.d2s(ghostingratio, 4));
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "maxtolerance: #302#" + IJ.d2s(maxtolerance, 4));
		boolean failmin = (Double.compare(ghostingratio, maxtolerance) >= 0);
		String response = "";
		if (failmin) {
			response = "FAIL";
		} else {
			response = "PASS";
		}

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "giudizio Ghosting Ratio: #303#" + response);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "TOP Area: #401#" + statuproi.area);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "TOP Mean: #402#" + statuproi.mean);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "TOP SD: #403#" + statuproi.stdDev);

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "BOT Area: #404#" + statdwroi.area);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "BOT Mean: #405#" + statdwroi.mean);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "BOT SD: #406#" + statdwroi.stdDev);
		
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "LEFT Area: #407#" + statsxroi.area);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "LEFT Mean: #408#" + statsxroi.mean);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "LEFT SD: #409#" + statsxroi.stdDev);

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "RIGHT Area: #410#" + statdxroi.area);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "RIGHT Mean: #411#" + statdxroi.mean);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "RIGHT SD: #412#" + statdxroi.stdDev);

		ACRlog.appendLog(namepathReport, ACRlog.qui() + "MROI Area: #413#" + statMROI.area);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "MROI Mean: #414#" + statMROI.mean);
		ACRlog.appendLog(namepathReport, ACRlog.qui() + "MROI SD: #415#" + statMROI.stdDev);
		ACRlog.appendLog(namepathReport, "< finished " + LocalDate.now() + " @ " + LocalTime.now() + " >");
		
		imp2.changes = false;
		imp2.close();
	
		

	}

}
