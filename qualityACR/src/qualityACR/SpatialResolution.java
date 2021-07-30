package qualityACR;

import java.awt.Frame;
import java.io.IOException;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class SpatialResolution implements PlugIn {
	public static final boolean debug = false;
	public static final boolean big = true;

	public void run(String arg) {

		// IJ.log("2 - High contrast spatial resolution");
		ACRlog.waitHere("2 - High contrast spatial resolution");

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

	public void mainResolution() {

		Properties prop = ACRutils.readConfigACR();
		int timeout = 2000; // preme automaticamente OK ai messaggi durante i test
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
		IJ.log(ACRlog.qui());

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
		IJ.log(ACRlog.qui());

		try {
			ACRutils.writeConfigACR(prop);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IJ.log(ACRlog.qui());
		ACRlog.waitHere();

		// leggo i nomi di tutti i 15 file presenti
		String pathLocalizer = "";
		String tmpFolderPath = IJ.getDirectory("temp");
		String completePath = tmpFolderPath + "ACRlist.tmp";
		String[] vetPath = ACRutils.readStringArrayFromFile(completePath);
		String pathReport = vetPath[4] + "\\Report1.txt";
		IJ.log(ACRlog.qui());

		String[] listLocalizer = ACRinputOutput.readStackPathToSortedList(vetPath[0], "T1");
		if (listLocalizer != null)
			pathLocalizer = listLocalizer[0];
		IJ.log(ACRlog.qui());

		String[] sortedListT1 = ACRinputOutput.readStackPathToSortedList(vetPath[1], "T1");
		if (sortedListT1 == null)
			IJ.log(ACRlog.qui() + "sortedListT1 ==null");
		IJ.log(ACRlog.qui());

		String[] sortedListT2 = ACRinputOutput.readStackPathToSortedList(vetPath[2], "T2");
		if (sortedListT2 == null)
			IJ.log(ACRlog.qui() + "sortedListT2 ==null");

		IJ.log(ACRlog.qui());

		// DEVO creare un nuovo report, senno' che controllo faccio?
		if (!ACRlog.initLog(pathReport))
			return;
		IJ.log(ACRlog.qui());

		// ora in base alle selezioni effettuate nelle checkbox del dialogo, dobbiamo
		// elaborare solo i file selezionati
		for (int i1 = 0; i1 < vetBoolSliceT1.length; i1++) {
			if (vetBoolSliceT1[i1]) {
				IJ.log(ACRlog.qui() + "elaborazione slice T1 numero " + i1);
				evalResolution(sortedListT1[i1], pathReport, i1, step, fast, verbose, timeout);
			}
		}
		IJ.log(ACRlog.qui());

		for (int i1 = 0; i1 < vetBoolSliceT2.length; i1++) {
			if (vetBoolSliceT2[i1]) {
				IJ.log(ACRlog.qui() + "==================");
				IJ.log(ACRlog.qui() + "elaborazione slice T2 numero " + i1);
				evalResolution(sortedListT2[i1], pathReport, i1, step, fast, verbose, timeout);
			}
		}
		ACRlog.waitHere("SPATIAL_RESOLUTION TERMINATA");
	}

	public void evalResolution(String path1, String pathReport, int i1, boolean step, boolean fast, boolean verbose,
			int timeout) {

		IJ.log(ACRlog.qui() + "<START>");
		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		if (big)
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

		boolean step1 = false;
		boolean fast1 = false;
		boolean verbose1 = false;

		double[] phantomCircle = ACRlocalizer.gridLocalizer1(imp2, step1, fast1, verbose1, timeout);

		double[][] phantomVertices = ACRlocalizer.phantomReferences(imp2, phantomCircle, step, fast, verbose, timeout);
		double angle = ACRlocalizer.phantomRotation(phantomVertices, step, fast, verbose, timeout);
		double[][] resolutionHoles = ACRlocalizer.phantomResolutionHoles(imp2, phantomVertices, step, fast, verbose,
				timeout);

		IJ.log(ACRlog.qui() + " angoloRotazione fantoccio= " + angle);
		
		
		ACRlocalizer.gridMatrix(imp2, phantomCircle, angle, step, fast, verbose, timeout);
		

		return;
	}
}
