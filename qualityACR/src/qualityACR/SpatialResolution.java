package qualityACR;

import java.awt.Color;
import java.awt.Frame;
import java.io.IOException;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.PlugIn;

public class SpatialResolution implements PlugIn {
	public static final boolean debug = false;

	public void run(String arg) {

		mainResolution();
	}

	// 20july2021
	// qualche avanzamento, ma per ora si naviga nelle piccolezze,
	// comunque il ParticleAnalyzer promette bene. Altrettanto il RemoveDuplicate
	// messo in ACRutils. Anche se questo lavora per le stringhe, nulla vieta di
	// adattarlo ai double od agli int ed eliminare dalla mia lista di punti
	// ricavati dalle scansioni fantoccio, tutti i duplicati
	// 30jly2021
	// duplicati che non sono comunque mai tantissimi!
	//
	// Del pattern in alto si analizzano le righe, del pattern in basso si
	// analizzano le colonne

	public void mainResolution() {

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
			fastdefault = Boolean.parseBoolean(prop.getProperty("Resolution.fast"));
			stepdefault = Boolean.parseBoolean(prop.getProperty("Resolution.step"));
			verbosedefault = Boolean.parseBoolean(prop.getProperty("Resolution.verbose"));
			for (int i1 = 0; i1 < 7; i1++) {
				T1[i1] = Boolean.parseBoolean(prop.getProperty("Resolution.SliceT1[" + i1 + "]"));
				T2[i1] = Boolean.parseBoolean(prop.getProperty("Resolution.SliceT2[" + i1 + "]"));
			}
			int count = 0;
			for (int i1 = 0; i1 < 7; i1++) {
				defaults[count++] = T1[i1];
				defaults[count++] = T2[i1];
			}
		}

		GenericDialog gd1 = new GenericDialog("SPATIAL RESOLUTION");
		gd1.addCheckbox("ANIMAZIONE 2 sec", fastdefault);
		gd1.addCheckbox("STEP", stepdefault);
		gd1.addCheckbox("VERBOSE", verbosedefault);
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
		prop.setProperty("Resolution.fast", "" + fast);
		prop.setProperty("Resolution.step", "" + step);
		prop.setProperty("Resolution.verbose", "" + verbose);
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Resolution.SliceT1[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT1[i1];
			prop.setProperty(aux1, aux2);
		}
		for (int i1 = 0; i1 < 7; i1++) {
			String aux1 = "Resolution.SliceT2[" + i1 + "]";
			String aux2 = "" + vetBoolSliceT2[i1];
			prop.setProperty(aux1, aux2);
		}

		try {
			ACRutils.writeConfigACR(prop);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// leggo i nomi di tutti i 15 file presenti
		String tmpFolderPath = IJ.getDirectory("temp");
		String completePath = tmpFolderPath + "ACRlist.tmp";
		String[] vetPath = ACRutils.readStringArrayFromFile(completePath);
		String pathReport = vetPath[4] + "\\Report1.txt";

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
				evalResolution(sortedListT1[i1], pathReport, step, fast, verbose, timeout);
			}
		}

		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log(ACRlog.qui() + "==================");
				IJ.log(ACRlog.qui() + "elaborazione slice T2 numero " + i1);
				evalResolution(sortedListT2[i1], pathReport, step, fast, verbose, timeout);
			}
		}
		ACRlog.waitHere("SPATIAL_RESOLUTION TERMINATA");
	}

	public void evalResolution(String path1, String pathReport, boolean step, boolean fast, boolean verbose,
			int timeout) {

		IJ.log(ACRlog.qui() + "<START>");
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);

		//
		// ========== TAROCCAMENTO IMMAGINE =============
		//
		// MI RESTA DA SPIEGARE PERCHE' RUOTANDO L'IMMAGINE
		// CAMBIA IL RISULTATO!!!!!
		//
		// IJ.run(imp2, "Flip Horizontally", "");
		//
		IJ.run(imp2, "Rotate... ", "angle=10 grid=1 interpolation=Bilinear");
		//
		// IJ.run(imp2, "Rotate... ", "angle=-8 grid=1 interpolation=Bilinear");
		//
		// ===============================================
		//

		boolean step1 = step;
		boolean verbose1 = verbose;

		double[] phantomCircle = ACRlocalizer.phantomLocalizerAdvanced(imp2, step1, verbose1, timeout);

		// QUESTO SI CHIAMA BARARE, SIGNOR BARONE......
		// in pratica se applico le seguenti correzioni ai parametri del cerchio
		// miglioro la situazione, ovviamente funziona solo per questa immagine, pertanto inutile

//		phantomCircle[0] = phantomCircle[0];
//		phantomCircle[1] = phantomCircle[1] + 1;
//		phantomCircle[2] = phantomCircle[2] + 1;

		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp2, phantomCircle, step, verbose, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, step, verbose, timeout);
		angle = angle + 0;

		double[][] matout = ACRlocalizer.movableGridMatrix(imp2, phantomCircle, angle, step, fast, verbose, timeout);

		ImagePlus imp3 = imp2.duplicate();
		imp3.show();
		ACRutils.zoom(imp3);

		// istruzioni inutili, da eliminare, se non introduco altro
		imp2.changes = false;   // permette di chiudere l'immagine senza domande da parte del sistema
		imp2.close();
		Overlay over3 = new Overlay();
		imp3.setOverlay(over3);

		double xcircle = phantomCircle[0];
		double ycircle = phantomCircle[1];
		double dcircle = phantomCircle[2];

		imp3.setRoi(new OvalRoi(xcircle - dcircle / 2, ycircle - dcircle / 2, dcircle, dcircle));
		imp3.getRoi().setStrokeColor(Color.GREEN);
		over3.addElement(imp3.getRoi());

		for (int i1 = 0; i1 < matout[0].length; i1++) {
			Roi pr1 = new Roi(matout[0][i1] - 2, matout[1][i1] - 2, 4, 4);
			imp3.setRoi(pr1);
			imp3.getRoi().setStrokeColor(Color.YELLOW);
			over3.addElement(imp3.getRoi());
			imp3.updateAndDraw();
			imp3.killRoi();
		}

		
// NON SO COME ANDARE AVANTI, NON HO SUFFICIENTE PRECISIONE		
//		double[][] resolutionHoles = ACRlocalizer.phantomResolutionHoles(imp2, phantomVertices, step, fast, verbose,
//				timeout);

//		IJ.log(ACRlog.qui() + " angoloRotazione fantoccio= " + angle);

//		ACRlocalizer.staticGridMatrix(imp2, phantomCircle, angle, step, fast, verbose, timeout);

		return;
	}
}
