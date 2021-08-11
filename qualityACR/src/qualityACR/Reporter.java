package qualityACR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ij.IJ;
import ij.plugin.PlugIn;

/**
 * Compilazione dei reports Il meccanismo di funzionamento dovrebbe essere il
 * seguente: i vari plugins individuali si limitano a creare un file di testo
 * contenente i risultati, in modo che sia sempre possibile, volendo, importarli
 * in Excel senza troppe difficolta'. I plugins individuali si occupano anche di
 * salvare le immagini, complete di overlay delle varie ROI, in una apposita
 * cartella. Il Reporter, apre dei template HTML e vi inserisce, in campi
 * predeterminati da una apposita sigla alfanumerica, i risultati delle misure,
 * il giudizio pass/fail ed il link alle immagini complete di overlay. Il
 * template compilato viene salvato con un acconcio nome, assieme ai file dei
 * risulatti in formato txt ed alle immagini nella cartella dei reports, che
 * infine viene zippata con la data all'interno del nome.
 * 
 * @author Alberto
 *
 */
public class Reporter implements PlugIn {

	public void run(String arg) {
		// ============================================================================================

		mainReporter();
	}

	public static void mainReporter() {

		String htmlfile = "ReportLocalizer.html";
		String htmlpath = "templates/";
		String result1 = "D:\\Dati\\ACR_TEST\\Study_1_20210527\\REPORTS\\ReportGeometrico.txt";

//		String myhtml = null;
//		try {
//			myhtml = new Reporter().getText(htmlfile, htmlpath);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		String[] myhtml = null;
		try {
			myhtml = new Reporter().getText3(htmlfile, htmlpath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String str : myhtml)
			IJ.log(str);

		String[] myresult = null;
		try {
			myresult = new Reporter().readTextFile(result1, "");
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String str : myresult)
			IJ.log(str);

		changeTemplate(myhtml, myresult);
//		ACRlog.logVector(myresult, ACRlog.qui() + "myresult");

	}

	/**
	 * Loads a text file from within a JAR file using getResourceAsStream().
	 * 
	 * @param sourcefile
	 * @return
	 */
	public String getText(String sourcefile, String path) {
		String text = "";
		try {
			ClassLoader CLDR = this.getClass().getClassLoader();
			InputStream in = CLDR.getResourceAsStream(path + sourcefile);
			if (in == null) {
				ACRlog.waitHere("file non trovato: " + sourcefile);
				return "";
			}
			InputStreamReader inr = new InputStreamReader(in);
			StringBuffer sb = new StringBuffer();
			char[] b = new char[8192];
			int n;
			// read a block and append any characters
			while ((n = inr.read(b)) > 0)
				sb.append(b, 0, n);
			// display the text in a TextWindow
			text = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return text;
	}

	/**
	 * Loads a text fileusing getResourceAsStream().
	 * 
	 * @param sourcefile
	 * @return
	 */
	public String getText2(String sourcefile, String path) {

		String text = "";

		File myfile = new File(path + sourcefile);
		Scanner myscanner;
		try {
			myscanner = new Scanner(myfile);
			while (myscanner.hasNextLine()) {
				text = text + myscanner.nextLine();
			}
			myscanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return text;
	}

	/**
	 * Loads a text file from within a JAR file using getResourceAsStream().
	 * 
	 * @param sourcefile
	 * @return
	 */
	public String[] getText3(String sourcefile, String path) {
		ArrayList<String> list1 = new ArrayList<String>();
		ClassLoader CLDR = this.getClass().getClassLoader();
		InputStream in = CLDR.getResourceAsStream(path + sourcefile);
		if (in == null) {
			ACRlog.waitHere("file non trovato: " + sourcefile);
			return null;
		}
		Scanner myscanner = new Scanner(in);
		while (myscanner.hasNextLine()) {
			String line = myscanner.nextLine();
			list1.add(line);
		}
		myscanner.close();
		String[] out1 = new String[list1.size()];
		for (int i1 = 0; i1 < list1.size(); i1++) {
			out1[i1] = list1.get(i1);
		}
		return out1;
	}

	public String[] readTextFile(String sourcefile, String path) {

		ArrayList<String> list1 = new ArrayList<String>();
		File myfile = new File(path + sourcefile);
		Scanner myscanner;
		try {
			myscanner = new Scanner(myfile);
			while (myscanner.hasNextLine()) {
				String line = myscanner.nextLine();
				list1.add(line);
			}
			myscanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String[] out1 = new String[list1.size()];
		for (int i1 = 0; i1 < list1.size(); i1++) {
			out1[i1] = list1.get(i1);
		}
		return out1;
	}

	public static void changeTemplate(String[] myhtml, String[] myresult) {

		String line = "This order was placed #001# for QT3000! OK?";
		// String pattern = "(.*)(\\d+)(.*)";
		String pattern = "(#)(\\d+)(#)";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		for (String str1 : myhtml) {
			// IJ.log(str);
			Matcher m1 = r.matcher(str1);
			String str3 = null;
			if (m1.find()) {
				// IJ.log("Found value1: " + m1.group(0));
				for (String str2 : myresult) {
					Matcher m2 = r.matcher(str2);
					if (m2.find()) {
						if (m1.group(0).compareTo(m2.group(0)) == 0) {
							IJ.log("Found value1: " + m1.group(0) + " Found value2: " + m2.group(0));
							StringBuffer sb2 = new StringBuffer();
							sb2.append(str2);
							StringBuffer sb4 = m2.appendTail(sb2);
							String str4 = sb4.toString();
							StringBuffer sb1 = new StringBuffer();
							sb1.append(str1);
							m1.appendReplacement(sb1, str4);
							str3 = m1.group();
						}

					}

				}

			} else {
				str3 = str1;

			}
			IJ.log("str3= " + str3);
		}
	}

}
