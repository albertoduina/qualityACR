package qualityACR;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.WaitForUserDialog;
import ij.io.Opener;
import ij.util.DicomTools;

public class ReadData {


	public static String readDicomString(ImagePlus imp1, String tag) {

		String parameter = tag + " -- " + DicomTools.getTagName(tag) + ":" + DicomTools.getTag(imp1, tag);
		return (parameter);
	}

	/**
	 * legge un valore double da una stringa
	 * 
	 * @param s1 stringa input
	 * @return valore double letto in s1
	 */
	public static double readDouble(String s1) {
		double x = 0;
		try {
			x = Double.valueOf(s1);
			// x = (new Double(s1)).doubleValue();
		} catch (Exception e) {
			IJ.error("readDouble >> invalid double number");
			// tolto il messaggio per evitare isterismi nell'utenza
		}
		return x;
	}

	/**
	 * legge un valore float da una stringa
	 * 
	 * @param s1 stringa input
	 * @return valore float letto in s1
	 */
	public static float readFloat(String s1) {
		float x = 0;
		try {
			x = Float.valueOf(s1);
//			x = (new Float(s1)).floatValue();
		} catch (Exception e) {
			 IJ.error("readFloat >> invalid float number");
		}
		return x;
	}

	/**
	 * legge un valore integer da una stringa
	 * 
	 * @param s1 stringa input
	 * @return valore integer letto in s1
	 */
	public static int readInt(String s1) {
		int x = 0;
		try {
			x = Integer.valueOf(s1);
//			x = (new Integer(s1)).intValue();
		} catch (Exception e) {
			IJ.error(" readInt >> invalid integer number ");
		}
		return x;
	}

	/**
	 * estrae una parte di parametro dicom costituito da una stringa multipla
	 * 
	 * @param s1     stringa multipla
	 * @param number selezione parte da restituire
	 * @return stringa con la parte selezionata
	 */
	public static String readSubstring(String s1, int number) {
		StringTokenizer st = new StringTokenizer(s1, "\\ ");
		int nTokens = st.countTokens();
		String substring = "ERROR";
		if (number > nTokens)
			return substring;
		else
			substring = st.nextToken();
		for (int i1 = 1; i1 < number; i1++) {
			substring = st.nextToken();
		}
		return substring;
	}

	/**
	 * arresta il programma in modeless, presentando nome file sorgente e numero di linea
	 */
	public static void waitHere() {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber()).show();
	}

	/**
	 * arresta il programma in modeless, presentando nome file sorgente e numero di linea e la stringa ricevuta
	 * 
	 * @param str
	 */
	public static void waitHere(String str) {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n \n" + str).show();
	}

	/**
	 * permette di insereire file e numero di linea in una stringa
	 * @return
	 */
	public static String qui() {
		String out = ("<" + Thread.currentThread().getStackTrace()[2].getClassName() + "."
				+ Thread.currentThread().getStackTrace()[2].getMethodName()) + " line= "
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + ">  ";
		return out;
	}

}
