package qualityACR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.process.ImageProcessor;

public class ACRinputOutput {

	public static boolean deleteFile(File file) {
		boolean ok = false;
		if (file.exists()) {
			ok = file.delete();
		} else
			ok = true;
		return ok;
	}

	/**
	 * Deletes all files and subdirectories under dir. Returns true if all deletions
	 * were successful. If a deletion fails, the method stops attempting to delete
	 * and returns false. http://javaalmanac.com/egs/java.io/DeleteDir.html
	 */
	public static boolean deleteDir(File dir) {

		if (!dir.exists())
			IJ.log("non esiste la dir= " + dir);

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i1 = 0; i1 < children.length; i1++) {
				boolean success = deleteDir(new File(dir, children[i1]));
				if (!success) {
					IJ.log("errore delete dir= " + dir);
					return false;
				}
			}
			int num = dir.list().length;
			if (num > 0)
				ACRlog.waitHere("files residui= " + num);
		}

		// The directory is now empty so delete it
		boolean ok = dir.delete();
		if (!ok)
			IJ.log("errore cancellazione dir da java= " + dir);
		return ok;
	}

	/**
	 * Create a directory; all ancestor directories must exist
	 * http://javaalmanac.com/egs/java.io/DeleteDir.html
	 * 
	 * @param dir
	 * @return booleano true se ok
	 */
	public static boolean createDir(File dir) {

		boolean success = dir.mkdir();
		if (!success) {
			return false;
		} else
			return true;
	}

	public static void purgeDirectory(File dir) {
//		ACRlog.waitHere("dir= " + dir);
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				purgeDirectory(file);
			file.delete();
		}
	}

	public static ImagePlus readStackPathsToImage(String startingDir1, String title) {

		// ora devo leggere i nomi di tutti i file contenuti nella cartella
		List<File> result1 = ACRinputOutput.getFileListing(new File(startingDir1));
		if (result1 == null) {
			ACRlog.here("getFileListing.result==null");
		}
//		String[] list1 = new String[result1.size()];
//		String[] name1 = new String[result1.size()];
//		int j1 = 0;
//		int j2 = 0;
//		for (File file1 : result1) {
//			list1[j1++] = file1.getPath();
//			name1[j2++] = file1.getName();
//		}
		// dopo avere sortato le immagini secondo la posizione, le inserisco in uno
		// stack
		ImagePlus impStack1 = ACRinputOutput.stackBuilder(result1, title);
		ACRinputOutput.stackAnalyzer(impStack1);
		impStack1.show();

		return impStack1;
	}

	/**
	 * Legge i nomi di tutte le immagini e le ordina secondo la loro posizione
	 * 
	 * @param startingDir1
	 * @param title
	 * @return
	 */
	public static String[] readStackPathToSortedList(String startingDir1, String title) {

		// ora devo leggere i nomi di tutti i file contenuti nella cartella
		List<File> result1 = ACRinputOutput.getFileListing(new File(startingDir1));
		if (result1 == null) {
			IJ.log("result1==null");
		}
		String[] pathSortato = ACRinputOutput.bubbleSortPath(result1);
		if (pathSortato == null)
			IJ.log("pathSortato==null");
		return pathSortato;
	}

	/**
	 * Legge ricorsivamente la directory e relative sottodirectory copied from
	 * www.javapractices.com (Alex Wong)
	 * 
	 * @param startingDir directory "radice"
	 * @return lista dei path dei file
	 */
	public static List<File> getFileListing(File startingDir) {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = startingDir.listFiles();
		if (filesAndDirs == null) {
			IJ.log("filesAndDirs==null");
			return null;
		}
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		if (filesDirs == null) {
			IJ.log("filesDirs==null");
			return null;
		}
		for (File file : filesDirs) {
			if (!file.isFile()) {
				// must be a directory
				// recursive call !!
				List<File> deeperList = getFileListing(file);
				result.addAll(deeperList);
			} else {
				if (isImage(file)) {
					result.add(file);
				}
			}
		}
		return result;
	}

	/**
	 * Costruisce uno stack a partire dal path delle immagini e lo riordina secondo
	 * la posizione delle fette.
	 * 
	 * @param path vettore contenente il path delle immagini
	 * @return ImagePlus contenente lo stack generato
	 */
	public static ImagePlus stackBuilder(List<File> list1, String stackName) {

		if ((list1 == null) || (list1.size() == 0)) {
			IJ.log("stackBuilder path==null or 0 length.");
			return null;
		}

		ImagePlus imp0 = ACRgraphic.openImageNoDisplay(list1.get(0), true);
		int rows = imp0.getHeight();
		int columns = imp0.getWidth();
		ImageStack newStack = new ImageStack(rows, columns);

		String[] pathSortato = ACRinputOutput.bubbleSortPath(list1);
		for (int w1 = 0; w1 < pathSortato.length; w1++) {
			ImagePlus imp1 = ACRgraphic.openImageNoDisplay(pathSortato[w1], true);
			ImageProcessor ip1 = imp1.getProcessor();
			if (w1 == 0)
				newStack.update(ip1);
			String sliceInfo1 = imp1.getTitle();
			String sliceInfo2 = (String) imp1.getProperty("Info");
			// aggiungo i dati header alle singole immagini dello stack
			if (sliceInfo2 != null)
				sliceInfo1 += "\n" + sliceInfo2;
			newStack.addSlice(sliceInfo1, ip1);
		}
//		// 180419 aggiunto eventuale codice del nome immagine anche allo stack
//		File f = list1.get(0);
//		String nome1 = f.getName();
//		String nome2 = "";
//		if (nome1.length() > 5) {
//			nome2 = nome1.substring(0, 5);
//		} else {
//			nome2 = nome1;
//		}
		ImagePlus newImpStack = new ImagePlus(stackName, newStack);
		if (pathSortato.length == 1) {
			String sliceInfo3 = imp0.getTitle();
			sliceInfo3 += "\n" + (String) imp0.getProperty("Info");
			newImpStack.setProperty("Info", sliceInfo3);
		}
		newImpStack.getProcessor().resetMinAndMax();
		return newImpStack;
	}

	/**
	 * Effettua il Bubblesort delle immagini, secondo la slice position
	 * 
	 * @param path
	 * @param slicePosition
	 * @return String[] dei path sortati
	 */
	public static String[] bubbleSortPath(List<File> list1) {

		if (list1 == null)
			return null;

		ImagePlus imp0;

		String[] slicePosition = new String[list1.size()];
		for (int w1 = 0; w1 < list1.size(); w1++) {
//			IJ.log("NomiFile iniziali " + w1 + "   " + list1.get(w1).getName());
		}
		for (int w1 = 0; w1 < list1.size(); w1++) {
			imp0 = ACRgraphic.openImageNoDisplay(list1.get(w1), true);
			String dicomPosition = ACRutils.readDicomParameter(imp0, ACRconst.DICOM_IMAGE_POSITION);
			slicePosition[w1] = ACRutils.readSubstring(dicomPosition, 3);
//			IJ.log("slicePosition iniziale= " + w1 + "   " + slicePosition[w1]);
		}
		//
		// bubblesort
		//
		String[] sortedPath = new String[list1.size()];
		// ora devo estrarre la lista dei path delle immagini non ancora sortati
		for (int i1 = 0; i1 < list1.size(); i1++) {
			sortedPath[i1] = list1.get(i1).getPath();
		}
		if (sortedPath.length < 2) {
			return sortedPath;
		}

		for (int i1 = 0; i1 < sortedPath.length; i1++) {
			for (int i2 = 0; i2 < sortedPath.length - 1 - i1; i2++) {
				double position1 = ACRutils.readDouble(slicePosition[i2]);
				double position2 = ACRutils.readDouble(slicePosition[i2 + 1]);
				if (position1 > position2) {
					String positionSwap = slicePosition[i2];
					slicePosition[i2] = slicePosition[i2 + 1];
					slicePosition[i2 + 1] = positionSwap;
					String pathSwap = sortedPath[i2];
					sortedPath[i2] = sortedPath[i2 + 1];
					sortedPath[i2 + 1] = pathSwap;
				}
			}
		}
		// bubblesort end
		return sortedPath;
	}

	/**
	 * Stampa informazioni sullo stack. Inteso per provare se lo stack builder
	 * effettua ancora il sort delle immagini secondo posizione
	 * 
	 * @param imp1 Immagine stack da analizzare
	 * 
	 */
	public static void stackAnalyzer(ImagePlus impStack) {
		if (impStack == null) {
			IJ.log("impStack== null");
			return;
		}
		// IJ.log("stack bitDepth= "+stack.getBitDepth());
		ImageStack imaStack = impStack.getImageStack();
		if (imaStack == null) {
			IJ.log("imageFromStack.imaStack== null");
			return;
		}

		for (int i1 = 0; i1 < imaStack.getSize(); i1++) {
			// String titolo = imaStack.getShortSliceLabel(i1);
			String sliceInfo1 = imaStack.getSliceLabel(i1 + 1);
			String position = ACRutils.readDicomParameter(sliceInfo1, ACRconst.DICOM_SLICE_LOCATION);
			IJ.log("stackAnalyzer " + (i1 + 1) + " position= " + position);
		}
	}

	public static boolean jarCount(String nome1) {
		List<File> lista1 = listJars2(new File(ij.Menus.getPlugInsPath()));
		String[] list = new String[lista1.size()];
		int j1 = 0;
		for (File file : lista1) {
			list[j1++] = file.getName();
		}
		int count = 0;
		for (File file : lista1) {
			String str = file.getName();
			if (str.startsWith(nome1)) {
				count++;
			}
		}
		String msg = "";
		if (count < 1) {
			IJ.error("ATTENZIONE, manca il file " + nome1 + "xxx.jar");
		}
		if (count > 1) {
			for (File file : lista1) {
				String str = file.getName();
				if (str.startsWith(nome1)) {
					msg = msg + "\n" + file.getPath();
				}
			}
			IJ.error("ATTENZIONE, si vedono versioni multiple del file " + nome1 + "xxx.jar" + msg);
		}
		if (count == 1)
			return true;
		else
			return false;
	}

	public static List<File> listJars2(File startingDir) {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = startingDir.listFiles();
		if (filesAndDirs == null)
			return null;
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for (File file : filesDirs) {
			if (!file.isFile()) {
				List<File> deeperList = listJars2(file);
				result.addAll(deeperList);
			} else {
				if (file.getName().endsWith(".jar")) {
					result.add(file);
				}
			}
		}
		return result;
	}

	/***
	 * Testa se e' un immagine visualizzabile
	 * 
	 * @param fileName1 nome immagine
	 * @return true se tiff
	 */
	public static boolean isImage2(String fileName1) {
		IJ.redirectErrorMessages(true);
		ImagePlus imp1 = new Opener().openImage(fileName1);
		IJ.redirectErrorMessages(false);
		if (imp1 == null) {
			return false;
		}
		return true;
	}

	public static boolean isImage2(File file1) {
		IJ.redirectErrorMessages(true);
		ImagePlus imp1 = new Opener().openImage(file1.getPath());
		IJ.redirectErrorMessages(false);
		if (imp1 == null) {
			return false;
		}
		return true;
	}

	public static boolean isImage(String fileName1) {
		Opener o1 = new Opener();
		int type = o1.getFileType(fileName1);
		if (type == Opener.UNKNOWN || type == Opener.JAVA_OR_TEXT || type == Opener.ROI || type == Opener.TEXT)
			return false;
		else
			return true;
	}

	public static boolean isImage(File file1) {
		Opener o1 = new Opener();
		int type = o1.getFileType(file1.getPath());
		if (type == Opener.UNKNOWN || type == Opener.JAVA_OR_TEXT || type == Opener.ROI || type == Opener.TEXT)
			return false;
		else
			return true;
	}

	/***
	 * Legge i dati da un file e li restituisce in un array string
	 * 
	 * @param fileName
	 * @return
	 */
	public static String[] readStringArrayFromFile(String fileName) {

		File file = new File(fileName);
		if (!file.exists()) {
			IJ.log("readStringArrayFromFile.fileNotExists " + fileName);
		}

		ArrayList<String> vetList = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str = "";
			while ((str = in.readLine()) != null) {
				vetList.add(str);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ora trasferiamo tutto nel vettore
		String[] vetResult = new String[vetList.size()];
		for (int i1 = 0; i1 < vetList.size(); i1++) {
			vetResult[i1] = vetList.get(i1).trim();
		}
		return vetResult;
	}

	/**
	 * Trova un file risorsa, partendo dal nome del file.
	 * 
	 * @param name nome del file
	 * @return path del file
	 */
	public static String findResource(String name) {
		URL url1 = new ACRinputOutput().getClass().getClassLoader().getResource(name);
		String path = "";
		if (url1 == null)
			return null;
		else
			path = url1.getPath();
		return path;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public boolean findTemplate(String name) {

		// URL url3 =
		// this.getClass().getClassLoader().getResource("contMensili/Sequenze_.class");
		URL url3 = new ACRinputOutput().getClass().getClassLoader().getResource(name);
		String myString = url3.toString();
		int start = myString.indexOf("plugins");
		int end = myString.lastIndexOf("!");
		String myPart1 = myString.substring(start, end);
		end = myPart1.lastIndexOf("/");
		String myPart2 = myPart1.substring(0, end + 1);
		// definizione del nome del file che andremo a scrivere
		File outFile = new File(myPart2 + name);
		// Viene testata l'esistenza del file, se esiste non lo si copia, cos�
		// vengono mantenute eventuali modifiche dell'utlizzatore
		boolean present = checkFile(outFile.getPath());
		if (present) {
			// NON CANCELLO; in modo che l'utilizzatore possa personalizzare il
			// file. Per me organizzo in modo che sia lo script di
			// distribuzione a cancellare i file
			// outFile.delete();
			// MyLog.waitHere("skip perch� file gi� esistente");
			return true;
		}
		// ricerco la risorsa da copiare, perch� qui arrivo solo se la risorsa
		// non esiste al di fuori del file jar
		URL url1 = this.getClass().getResource("/" + name);
		if (url1 == null) {
			ACRlog.waitHere("file " + name + " not found in jar");
			return false;
		}
		try {
			// tento la copia
			InputStream is = this.getClass().getResourceAsStream("/" + name);
			FileOutputStream fos = new FileOutputStream(outFile);
			while (is.available() > 0) {
				// MyLog.waitHere("SCRIVO "+fileName);
				fos.write(is.read());
			}
			fos.close();
			is.close();
		} catch (IOException e) {
			ACRlog.waitHere("ERRORE ACCESSO");
		}
		present = checkFile(outFile.getPath());
		if (present) {
			// MyLog.waitHere("file estratto");
		} else {
			ACRlog.waitHere("FALLIMENTO, FILE NON COPIATO");
		}
		return present;
	}

	/***
	 * Verifica la disponibilit� di un file
	 * 
	 * @param name
	 * @return
	 */
	public static boolean checkFile(String name) {
		File fileCheck = new File(name);
		if (!fileCheck.exists())
			return false;
		else
			return true;
	}

	public static void extractor(String name1, String name2) throws java.io.IOException {
		name1 = "E:/sqljdbc4.jar";
		name2 = "E:/abc/";
		java.util.jar.JarFile jarfile = new java.util.jar.JarFile(new java.io.File(name1)); // jar file path(here
																							// sqljdbc4.jar)
		java.util.Enumeration<java.util.jar.JarEntry> enu = jarfile.entries();
		while (enu.hasMoreElements()) {
			String destdir = name2; // abc is my destination directory
			java.util.jar.JarEntry je = enu.nextElement();

			ACRlog.waitHere(je.getName());

			java.io.File fl = new java.io.File(destdir, je.getName());
			if (!fl.exists()) {
				fl.getParentFile().mkdirs();
				fl = new java.io.File(destdir, je.getName());
			}
			if (je.isDirectory()) {
				continue;
			}
			java.io.InputStream is = jarfile.getInputStream(je);
			java.io.FileOutputStream fo = new java.io.FileOutputStream(fl);
			while (is.available() > 0) {
				fo.write(is.read());
			}
			fo.close();
			is.close();
		}

	}

}
