package qualityACR;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class ACRinputOutput {

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

	public static String[] readStackPathToSortedList(String startingDir1, String title) {
		// IJ.log("startingDir= " + startingDir1);
		// ora devo leggere i nomi di tutti i file contenuti nella cartella
		List<File> result1 = ACRinputOutput.getFileListing(new File(startingDir1));
		if (result1 == null) {
			IJ.log("result1==null");
		}
		// IJ.log("result1.size= " + result1.size());
//		String[] list1 = new String[result1.size()];
//		String[] name1 = new String[result1.size()];
//		int j1 = 0;
//		int j2 = 0;
//		for (File file1 : result1) {
//			list1[j1++] = file1.getPath();
//			name1[j2++] = file1.getName();
//		}
//		
//		if (list1 == null || list1.length==0) {
//			IJ.log("list1==null or 0 length.");
//			return null;
//		}
//
//		ImagePlus imp0 = UtilAyv.openImageNoDisplay(list1.get(0), true);
//		int rows = imp0.getHeight();
//		int columns = imp0.getWidth();
//		ImageStack newStack = new ImageStack(rows, columns);

		String[] pathSortato = ACRinputOutput.bubbleSortPath(result1);
		if (pathSortato == null)
			IJ.log("pathSortato==null");
		// IJ.log("PathSortato.length= " + pathSortato.length);

		// dopo avere sortato le immagini secondo la posizione, le inserisco in uno
		// stack
//		ImagePlus impStack1 = ACRinputOutput.stackBuilder(result1, title);
//		ACRinputOutput.stackAnalyzer(impStack1);
//		impStack1.show();

		return pathSortato;
	}

	/**
	 * Legge ricorsivamente la directory e relative sottodirectory copied from
	 * www.javapractices.com (Alex Wong
	 * 
	 * @param startingDir directory "radice"
	 * @return lista dei path dei file
	 */
	public static List<File> getFileListing(File startingDir) {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = startingDir.listFiles();
		if (filesAndDirs == null) {
			IJ.log("fiesAndDirs==null");
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
				result.add(file);
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
		if (count <1) {
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

}
