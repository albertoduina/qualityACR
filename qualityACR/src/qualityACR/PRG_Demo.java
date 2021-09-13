package qualityACR;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.io.DirectoryChooser;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import ij.util.DicomTools;

public class PRG_Demo implements PlugIn {

	public void run(String arg) {

		// ==================== DEFAULT MEMORIZZATI ======================
		// lettura valore precedente directory scelta
		String defaultValue = "";
		String preferenceValue = Prefs.get("angio.string", defaultValue);
		// ================================================================

		DirectoryChooser od1 = new DirectoryChooser("SELEZIONARE LA CARTELLA LOCALIZER");
		DirectoryChooser.setDefaultDirectory(preferenceValue);
		String startingDir1 = od1.getDirectory();
		IJ.log("startingDir1= " + startingDir1);
		if (startingDir1 == null)
			return;

		List<File> result1 = ACRinputOutput.getFileListing(new File(startingDir1));
		if (result1 == null) {
			ACRlog.here("getFileListing.result==null");
		} else {
			IJ.log("Selezionato cartella LOCALIZER= " + startingDir1);
		}

		// ==================== DEFAULT MEMORIZZATI ======================
		// salvataggio valore precedente directory scelta
		Prefs.set("angio.string", startingDir1);
		// ================================================================

		String[] list1 = new String[result1.size()];
		String[] name1 = new String[result1.size()];
		int j1 = 0;
		int j2 = 0;
		for (File file1 : result1) {
			list1[j1++] = file1.getPath();
			name1[j2++] = file1.getName();
		}
		IJ.log("La cartella LOCALIZER contiene " + name1.length + " files");

		// -------------------------------------------------------

		String[] listafiles = listDicomFiles(startingDir1);

//		String path1 = "D:\\Dati\\ACRS_1_02\\Study_1_20210721\\Series_4_LOCALIZERFISICA SANITARIA^ACR\\4_HC1-7vNC1,2_LOCAL_0.86371147632599_-39.039205551147_63.371204853058";
		Opener opener = new Opener();
		ImagePlus imp1 = opener.openImage(listafiles[0]);
		if (imp1 == null) {
			ReadData.waitHere("imp==null");
			return;
		}
		imp1.show();
		imp1.getWindow().maximize();
		
		
		Overlay over1= new Overlay();
		imp1.setOverlay(over1);
		
		//======================= SCRITTE ==========================
		double width= imp1.getWidth();
		double height=imp1.getHeight();
		Font font;
		font = new Font("SansSerif", Font.BOLD, 8);
		imp1.setRoi(new TextRoi(width/2-20, height/2-20, "1", font));
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());
		
		imp1.setRoi(new TextRoi(width/2+20, height/2-20, "2", font));
		imp1.getRoi().setStrokeColor(Color.yellow);
		over1.addElement(imp1.getRoi());
		
		imp1.setRoi(new TextRoi(width/2+20, height/2+20, "3", font));
		imp1.getRoi().setStrokeColor(Color.cyan);
		over1.addElement(imp1.getRoi());
		
		imp1.setRoi(new TextRoi(width/2-20, height/2+20, "4", font));
		imp1.getRoi().setStrokeColor(Color.red);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		//==========================================================

		String string2 = DicomTools.getTag(imp1, "0028,0030");
		IJ.log("STRING string2 0028,0030" + " = " + string2);
		double dimPixel = ReadData.readDouble(ReadData.readSubstring(DicomTools.getTag(imp1, "0028,0030"), 1));
		IJ.log("DOUBLE dimPixel 0028,0030= " + dimPixel);
		String righe1 = DicomTools.getTag(imp1, "0028,0010");
		IJ.log("STRING 0028,0010" + " = " + righe1);
		String righe2 = righe1.replaceAll("\\s+", ""); // occorre eliminare gli eventuali spazi
		int righe3 = ReadData.readInt(righe2);
		IJ.log("INTEGER 0028,0010" + " = " + righe3);
		double frequenza = ReadData.readDouble(DicomTools.getTag(imp1, "0018,0084"));
		IJ.log(ReadData.qui() + "DOUBLE 0018,0084" + " = " + frequenza);

	}

	/**
	 * Lista i file Dicom validi presenti nella directory ed eventuali
	 * sottodirectory. Ignora i file non Dicom
	 * 
	 * @param startingDir directory "radice"
	 * @return lista dei file dicom visualizzabili
	 */
	public static String[] listDicomFiles(String startingDir) {
		List<File> result = getFileListing(new File(startingDir));
		String[] list = new String[result.size()];
		int j1 = 0;
		for (File file : result) {
			list[j1++] = file.getPath();
		}

		//
		// skip not Dicom files
		//
		List<String> list2 = new ArrayList<String>();
		for (int i1 = 0; i1 < list.length; i1++) {
			IJ.redirectErrorMessages();
			if ((i1 % 10) == 0) {
				IJ.showStatus(i1 + " / " + list.length);
			}

			int type = (new Opener()).getFileType(list[i1]);
			if (type != Opener.DICOM) {
				continue;
			}

			// ImagePlus imp1 = new Opener().openImage(list[i1]);
			// if (imp1 == null) {
			// continue;
			// }
			//
			// String info = readDicomInfo(list[i1]);
			// if (info == null || info.length() == 0) {
			// continue;
			// } else {
			list2.add(list[i1]);
			// }

		}
		String[] list3 = new String[list2.size()];
		for (int i1 = 0; i1 < list2.size(); i1++) {
			list3[i1] = list2.get(i1).toString();
		}
		//
		// and now list3 contains only good Dicom image files!
		//
		return list3;
	}

	/**
	 * Legge ricorsivamente la directory e relative sottodirectory
	 * 
	 * @author www.javapractices.com
	 * @author Alex Wong
	 * @author anonymous user
	 * 
	 * @param startingDir directory "radice"
	 * @return lista dei path dei file
	 */
	public static List<File> getFileListing(File startingDir) {

		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = startingDir.listFiles();
		int a1 = 0;
		if (filesAndDirs == null)
			return null;
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for (File file : filesDirs) {
			if (!file.isFile()) {
				// must be a directory
				// recursive call !!
				List<File> deeperList = getFileListing(file);
				result.addAll(deeperList);
			} else {
				a1++;

				result.add(file);
			}
			if ((a1 % 1000) == 0) {
				IJ.log("file= " + a1);
			}

		}
		return result;
	}

	
	
	
	/**
	 * Scrive una label all'interno di una textRoi, associata alla Roi gia'
	 * impostata nell'ImagePlus che viene passata, purtroppo lascia tracci della roi
	 * rettangolare in cui scrive
	 * 
	 * @param imp1
	 * @param text
	 */
	public static void drawLabel(ImagePlus imp1, String text, Color colore) {
		Roi roi1 = imp1.getRoi();
		if (roi1 == null)
			return;
		Rectangle r1 = roi1.getBounds();
		int x1 = r1.x;
		int y1 = r1.y;
		int x = x1 + 5;
		int y = y1 + 5;
		Font font;
		font = new Font("SansSerif", Font.PLAIN, 8);
		imp1.setRoi(new TextRoi(x, y, text, font));
		imp1.getRoi().setStrokeColor(colore);
	}

	
}
