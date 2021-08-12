package qualityACR;

import java.io.File;
import java.util.List;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.io.DirectoryChooser;
import ij.plugin.PlugIn;

public class Folder_Selector implements PlugIn {

	public void run(String arg) {
		// ============================================================================================
		// nota bene: le seguenti istruzioni devono ASSOLUTAMENTE essere all'inizio, in
		// questo modo il messaggio viene emesso, altrimenti si ha una eccezione
		//
//		try {
//			Class.forName("utils.IW2AYV");
//		} catch (ClassNotFoundException e) {
//			IJ.error("ATTENZIONE, manca il file iw2ayv_xxx.jar");
//			return;
//		}
//		if (!ACRinputOutput.jarCount("iw2ayv_"))
//			return;
		// ============================================================================================

		String versionIJ = IJ.getVersion();
		String fullVersionIJ = IJ.getFullVersion();
		String versionJava = System.getProperty("java.version");

		IJ.log("ImageJ= " + versionIJ + " >>>> verificato usando la versione 1.53j");
		IJ.log("ImageJfull= " + fullVersionIJ);
		IJ.log("Java= " + versionJava);

		DirectoryChooser od1 = new DirectoryChooser("SELEZIONARE LA CARTELLA LOCALIZER");
		String startingDir1 = od1.getDirectory();
		IJ.log("startingDir1= " + startingDir1);
		if (startingDir1 == null)
			return;
		DirectoryChooser od2 = new DirectoryChooser("SELEZIONARE CARTELLA IMMAGINI T1");
		String startingDir2 = od2.getDirectory();
		IJ.log("startingDir2= " + startingDir2);
		if (startingDir2 == null)
			return;
		DirectoryChooser od3 = new DirectoryChooser("SELEZIONARE LA CARTELLA IMMAGINI T2");
		String startingDir3 = od3.getDirectory();
		IJ.log("startingDir3= " + startingDir3);
		if (startingDir3 == null)
			return;
		GenericDialog gd1 = new GenericDialog("REPORT");
		gd1.addMessage("DEVO INIZIALIZZARE/CANCELLARE LA CARTELLA REPORTS, procedo?");
		gd1.enableYesNoCancel("Continua", "No");
		gd1.hideCancelButton();
		gd1.showDialog();
		String outDir = "";
		if (gd1.wasOKed()) {
			String root1 = startingDir1.substring(0, startingDir1.lastIndexOf("\\"));
			String root = root1.substring(0, root1.lastIndexOf("\\"));
			int aux1 = startingDir1.lastIndexOf("\\");
			ACRlog.waitHere("start= " + startingDir1 + "\nroot= " + root + "\naux1= " + aux1);
			File fil1 = new File(root + "\\REPORT");
			boolean ok1 = ACRinputOutput.deleteDir(fil1);
			ACRlog.waitHere("root= " + root + " ok1= " + ok1);
			boolean ok2 = ACRinputOutput.createDir(fil1);
			ACRlog.waitHere("root= " + root + " ok2= " + ok2);
			outDir = fil1.getPath();
		} else
			return;

		// ora devo leggere i nomi di tutti i file contenuti nelle due cartelle
		// la prima cartella
		List<File> result1 = ACRinputOutput.getFileListing(new File(startingDir1));
		if (result1 == null) {
			ACRlog.here("getFileListing.result==null");
		} else {
			IJ.log("Selezionato cartella LOCALIZER= " + startingDir1);
		}
		String[] list1 = new String[result1.size()];
		String[] name1 = new String[result1.size()];
		int j1 = 0;
		int j2 = 0;
		for (File file1 : result1) {
			list1[j1++] = file1.getPath();
			name1[j2++] = file1.getName();
		}
		IJ.log("La cartella LOCALIZER contiene " + name1.length + " files");

		// la seconda cartella IMMAGINI T1
		List<File> result2 = ACRinputOutput.getFileListing(new File(startingDir2));
		if (result2 == null) {
			ACRlog.here("getFileListing.result==null");
		} else {
			IJ.log("Selezionato cartella immagini T1= " + startingDir1);
		}

		String[] list2 = new String[result2.size()];
		String[] name2 = new String[result2.size()];
		j1 = 0;
		j2 = 0;
		for (File file2 : result2) {
			list2[j1++] = file2.getPath();
			name2[j2++] = file2.getName();
		}
		IJ.log("La cartella immaginiT1 contiene" + name2.length + " files");

		// la terza cartella IMMAGINI T2
		List<File> result3 = ACRinputOutput.getFileListing(new File(startingDir3));
		if (result3 == null) {
			ACRlog.here("getFileListing.result==null");
		} else {
			IJ.log("Selezionato cartella immagini T2= " + startingDir1);
		}
		String[] list3 = new String[result3.size()];
		String[] name3 = new String[result3.size()];
		j1 = 0;
		j2 = 0;
		for (File file3 : result3) {
			list3[j1++] = file3.getPath();
			name3[j2++] = file3.getName();
		}
		IJ.log("La certella immaginiT2 contiene " + name3.length + " files");

		// Inserisco i path delle cartelle in un file temporaneo, che sara'letto dagli
		// altri plugins
		String tmpFolderPath = IJ.getDirectory("temp");
		String completePath = tmpFolderPath + "ACRlist.tmp";
		ACRlog.deleteTmpFile(completePath);
		String linea = "LOCALIZER#" + startingDir1;
		ACRlog.appendLog(completePath, linea);
		linea = "STACK_T1#" + startingDir2;
		ACRlog.appendLog(completePath, linea);
		linea = "STACK_T2#" + startingDir3;
		ACRlog.appendLog(completePath, linea);
		linea = "STACK_T2#" + startingDir3;
		ACRlog.appendLog(completePath, linea);
		linea = "OUT_DIR#" + outDir;
		ACRlog.appendLog(completePath, linea);
		ACRlog.waitHere("FINE SELEZIONE");
	}

}
