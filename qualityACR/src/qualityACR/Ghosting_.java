package qualityACR;

import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.plugin.PlugIn;
import ij.process.ImageStatistics;

public class Ghosting_ implements PlugIn {

	private static final boolean debug = true;
	private static final boolean big = true;

	public void run(String arg) {
		mainGhosting();
	}

	public void mainGhosting() {

		boolean demo = true; // inserisce il modo di funzionamento dimostrativo di tutte le operazioni
		// eseguite

		int timeout = 2000; // preme automaticamente OK ai messaggi durante i test

		String[] labels = { "1", "1", "2", "2", "3", "3", "4", "4", "5", "5", "6", "6", "7", "7" };
		boolean[] defaults = { false, false, false, false, false, false, true, true, false, false, false, false, false,
				false };
		String[] headings = { "slices T1", "slices T2" };

		String pathLocalizer0 = "";
		String tmpFolderPath0 = IJ.getDirectory("temp");
		String completePath0 = tmpFolderPath0 + "ACRlist.tmp";
		String[] vetPath0 = ACRutils.readStringArrayFromFile(completePath0);
		for (int i1 = 0; i1 < vetPath0.length; i1++) {
			IJ.log("" + i1 + " " + vetPath0[i1]);
		}

		GenericDialog gd1 = new GenericDialog("GHOSTING0");
		gd1.addCheckbox("ANIMAZIONE 2 sec", false);
		gd1.addCheckbox("STEP", true);
		gd1.addCheckbox("VERBOSE", true);
//		gd1.addCheckbox("LOCALIZER", false);
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

//		String str1 = gd1.getNextRadioButton();
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

		// leggo i nomi di tutti i 15 file presenti
		String pathLocalizer = "";
		String tmpFolderPath = IJ.getDirectory("temp");
		String completePath = tmpFolderPath + "ACRlist.tmp";
		String[] vetPath = ACRutils.readStringArrayFromFile(completePath);
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
				IJ.log("mainGhosting001 >==================");
				IJ.log("mainGhosting001 > elaborazione slice T1 numero " + i1);
				ImagePlus imp1 = ACRgraphic.openImageNoDisplay(sortedListT1[i1], false);
				ACRlocalizer.gridLocalizer1(imp1, step, fast, verbose, timeout);
				
//				int xphantom = (int) out2[0];
//				int yphantom = (int) out2[1];
//				int dphantom = (int) out2[2];
//
//
//				int[] phantomCircle = phantomPositionSearch(sortedListT1[i1], i1, step, fast, verbose, timeout1);
////				sliceGhost(sortedListT1[i1], phantomCircle, i1, step, fast, verbose);
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
		int[] out2 = ACRlocalizer.positionSearch2(imp1, maxFitError, step, fast, verbose, timeout);
		//
		// per rendere le cose piu' interessanti durante il debug disegno un buco nel
		// fantoccio riempiendolo con segnale a 1.0.n ed un altro buco con segnale 3000.
		// RICORDARSI DI COMMENTARE PRIMA DELL' IMPIEGO EFFETIVO
		//
//		IJ.run(imp1, "Specify...", "width=20 height=20 x=96 y=96 oval");
//		IJ.run(imp1, "Set...", "value=1");
//		IJ.run(imp1, "Specify...", "width=20 height=20 x=96 y=50 oval");
//		IJ.run(imp1, "Set...", "value=3000");
//		imp1.updateAndDraw();

		int xphantom = (int) out2[0];
		int yphantom = (int) out2[1];
		int dphantom = (int) out2[2];

		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);
// -----------------------------------------------------------------
// Visualizzo sull'immagine il posizionamento, ricevuto da  positionSearch1, che verra' utilizzato: 
// cerchio esterno fantoccio in rosso
// -----------------------------------------------------------------

		if (true) {
			if (!imp1.isVisible()) {
				imp1.show();
				ACRutils.zoom(imp1);
			}
			imp1.setRoi(new OvalRoi(xphantom - dphantom / 2, yphantom - dphantom / 2, dphantom, dphantom));
			imp1.getRoi().setStrokeColor(Color.RED);
			over1.addElement(imp1.getRoi());
			imp1.killRoi();
			ACRlog.waitHere("MainUnifor> cerchio esterno rosso, fantoccio rilevato da positionSearch1", debug, timeout,
					fast);
		}

		return out2;
	}

	public static void sliceGhost(String path1, int[] phantomCircle, int slice, boolean step, boolean fast,
			boolean verbose) {
		double maxFitError = +20;
		// eseguite
		int timeout = 2000; // preme automaticamente OK ai messaggi durante i test

		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);

//		double[] out2 = ACRlocalizer.positionSearch1(imp1, maxFitError, maxBubbleGapLimit, step, fast, verbose, timeout1);

//		nt[] positionSearch2(ImagePlus imp1, double maxFitError, boolean step, boolean fast, boolean verbose,
//				int timeout)

//		int[] out2 = ACRlocalizer.positionSearch2(imp1, maxFitError, step, fast, verbose, timeout);

		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));

		int xphantom = phantomCircle[0];
		int yphantom = phantomCircle[1];

//		int dphantom = phantomCircle[2];

		int dphantom = (int) Math.round(100 / dimPixel);
// =========================================
		Overlay over1 = new Overlay(); // con questo definisco un overlay trasparente per i disegni
		imp1.setOverlay(over1);
// -----------------------------------------------------------------
// Visualizzo sull'immagine il posizionamento, ricevuto da  positionSearch1, che verra' utilizzato: 
// cerchio esterno fantoccio in rosso
// -----------------------------------------------------------------

		if (true) {
			if (!imp1.isVisible()) {
				imp1.show();
				if (big)
					ACRutils.zoom(imp1);
			}
			imp1.setRoi(new OvalRoi(xphantom - dphantom / 2, yphantom - dphantom / 2, dphantom, dphantom));
			imp1.getRoi().setStrokeColor(Color.RED);
			over1.addElement(imp1.getRoi());
			imp1.killRoi();
			ACRlog.waitHere("MainUnifor> cerchio esterno rosso, fantoccio rilevato da positionSearch1", step, timeout,
					fast);
		}

// -----------------------------------------------------------------
// Visualizzo sull'immagine il posizionamento che verra' utilizzato
// MROI in verde
// -----------------------------------------------------------------

		// devo posizionare una area di 54-56 cmq
		// quindi ne calcolo il diametro in pixels
		double area = 5600;
		double diam = 2 * Math.sqrt(area / Math.PI);
		double diampix = diam / dimPixel;
		int xmroi = (int) phantomCircle[0];
		int ymroi = (int) phantomCircle[1];
		int dmroi = (int) Math.round(diampix);

		imp1.setRoi(new OvalRoi(xmroi - dmroi / 2, ymroi - dmroi / 2, dmroi, dmroi));
		// siccome ho impostato la MROI posso calcolare il valore medio di essa, si
		// utilizza nel calcolo dei ghosts
		ImageStatistics stat1 = imp1.getStatistics();
		double MROImean = stat1.mean;

		imp1.getRoi().setStrokeColor(Color.GREEN);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		if (true)
			ACRlog.waitHere("MainUnifor> cerchio interno verde MROI", step, timeout, fast);

		int[] MROIcircle = new int[4];
		MROIcircle[0] = xmroi;
		MROIcircle[1] = ymroi;
		MROIcircle[2] = dmroi;

		// applico al centro del fantoccio una ROI di area 54-56 cmq
		// calcolo e memorizzo la media

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
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		double diamUFOV = 0.8 * dphantom;
		double gapFromOther = 0.1 * dphantom;
		double sxgap = xphantom - dphantom / 2;
		double dxgap = width - xphantom - dphantom / 2;
		double upgap = yphantom - dphantom / 2;
		double logap = height - yphantom - dphantom / 2;
		int guard = (int) dmroi / 50;
		double roimm = 300;
		int roipix = (int) Math.round((roimm / dimPixel) / dimPixel);

		double sxroiwidth = sxgap - 2 * guard;
		double sxroiheight = roipix / sxroiwidth;
//		double dxroiwidth = dxgap - gapFromOther * 2;
		double dxroiwidth = dxgap - 2 * guard;
		double dxroiheight = roipix / dxroiwidth;

		double uproiheight = upgap - 2 * guard;
		double uproiwidth = roipix / uproiheight;

//		double loroiwidth = logap - gapFromOther * 2;
		double loroiheight = logap - 2 * guard;
		double loroiwidth = roipix / loroiheight;

		// adesso cerco di disegnare la ROI

		imp1.setRoi(guard, yphantom - (int) sxroiheight / 2, (int) sxroiwidth, (int) sxroiheight);
		imp1.getRoi().setStrokeColor(Color.BLUE);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		imp1.setRoi(xphantom + dphantom / 2 + guard, yphantom - (int) sxroiheight / 2, (int) dxroiwidth,
				(int) dxroiheight);
		imp1.getRoi().setStrokeColor(Color.BLUE);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();

		imp1.setRoi(xphantom - (int) uproiwidth / 2, guard, (int) uproiwidth, (int) uproiheight);
		imp1.getRoi().setStrokeColor(Color.BLUE);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();

		imp1.setRoi(xphantom - (int) loroiwidth / 2, yphantom + dphantom / 2 + guard, (int) loroiwidth,
				(int) loroiheight);
		imp1.getRoi().setStrokeColor(Color.BLUE);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();

		ACRlog.waitHere();

	}

}
