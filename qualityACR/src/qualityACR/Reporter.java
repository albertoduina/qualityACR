package qualityACR;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
//		String result1 = "D:\\Dati\\ACR_TEST\\Study_1_20210527\\REPORTS\\ReportGeometrico.txt";
//		String path1 = "D:\\Dati\\ACR_TEST\\Study_1_20210527\\REPORTS\\";
		String result2="ReportGeometrico.txt";
		String tmpfile="ACRlist.tmp";

		String tmpFolderPath = IJ.getDirectory("temp");
//		ACRlog.waitHere(tmpFolderPath);
//		String completePath = tmpFolderPath + "ACRlist.tmp";
//		File cmpath = new File(completePath);
		String[] mytemp = null;
		try {
			mytemp = new Reporter().readTextFile(tmpfile, tmpFolderPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String uno = mytemp[4];
		String root = uno.substring(uno.lastIndexOf("#") + 1, uno.length())+"\\";

		ACRlog.waitHere("root= " + root);
		// path1=root;

		int count = 0;
		String[] myhtml = null;
		try {
			myhtml = new Reporter().getText3(htmlfile, htmlpath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String str : myhtml) {
			IJ.log("" + (count++) + "::" + str);
		}

		String[] myresult = null;
		try {
			myresult = new Reporter().readTextFile(result2, root);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String str : myresult)
			IJ.log(str);

		String[] out1 = changeTemplate(myhtml, myresult);

		writeTextFile(out1, "ReportLocalizer.html", root);
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

	/**
	 * 
	 * @param sourcefile
	 * @param path
	 * @return
	 */
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

	/**
	 * Modifica del template, includendo i dati al posto dei tag
	 * 
	 * @param myhtml
	 * @param myresult
	 * @return
	 */
	public static String[] changeTemplate(String[] myhtml, String[] myresult) {

		String[] out1 = new String[myhtml.length];
		String str3 = null;
		// String line = "This order was placed #001# for QT3000! OK?";
		// String pattern = "(.*)(\\d+)(.*)";
		String pattern = "(#)(\\d+)(#)";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		int count = 0;
		int prima1 = 0;
		int dopo1 = 0;
		int prima2 = 0;
		int dopo2 = 0;
		String part1 = "";
		String part2 = "";
		String part3 = "";
		for (String str1 : myhtml) {
			// IJ.log(str);
			Matcher m1 = r.matcher(str1);

			if (m1.find()) {
				prima1 = m1.start();
				dopo1 = m1.end();
				part1 = str1.substring(0, prima1);
				part3 = str1.substring(dopo1);
				// IJ.log("Found value1: " + m1.group(0));
				for (String str2 : myresult) {
					Matcher m2 = r.matcher(str2);
					if (m2.find()) {
						prima2 = m2.start();
						dopo2 = m2.end();
						if (m1.group(0).compareTo(m2.group(0)) == 0) {
							part2 = str2.substring(dopo2);
							str3 = part1 + part2 + part3;
						}
					}
				}
			} else {
				str3 = str1;
			}
			// IJ.log("" + (count++) + "::" + str3);
			out1[count++] = str3;

		}
		return out1;
	}

	/**
	 * Scrittura file di testo, usa BufferedWriter
	 * 
	 * @param content  array di stringhe da scrivere nel file html
	 * @param destfile nome file destinazione
	 * @param path
	 */
	public static void writeTextFile(String[] content, String destfile, String path) {
		String pathname = path + destfile;

		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(pathname, true));
			for (String str1 : content) {
				writer.append(str1);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
