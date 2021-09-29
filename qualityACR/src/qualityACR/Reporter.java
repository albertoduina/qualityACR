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
import ij.ImagePlus;
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

		mainReporter1();
		mainReporter2();
		ACRlog.waitHere("FINE LAVORO");
	}

	/**
	 * Per ogni diversa pagina html, si occupa di reperire il template e caricarlo
	 * in memoria, far girare reporterEngine per ogni foglio di report, inserire la
	 * immagine dummy in tutte le posizioni rimaste non occupate ed infine salvare
	 * la pagnia html su disco. passa poi alla pagina successiva.
	 */
	public void mainReporter1() {

		String reportpath = getReportPath("ACRlist.tmp");
		IJ.log("reportpath= " + reportpath);

		String[] localizhtml0 = getTemplateFromJAR("templates/", "ReportLocalizer.html");
		String[] slice1html0 = getTemplateFromJAR("templates/", "ReportSlice1_T1A.html");
		String[] slice3html0 = getTemplateFromJAR("templates/", "ReportSlice3_T1A.html");
		String[] slice4html0 = getTemplateFromJAR("templates/", "ReportSlice4_T1B.html");

		String[] localizer = getMeasureReport(reportpath, "ReportLocalizer.txt");
		String[] thickness = getMeasureReport(reportpath, "ReportThickness_T1S1.txt");
		String[] geometrico = getMeasureReport(reportpath, "ReportGeometrico_T1S1.txt");
		String[] posizione = getMeasureReport(reportpath, "ReportPosition_T1S1.txt");
		String[] uniformity = getMeasureReport(reportpath, "ReportUniformity_T1S4.txt");
		String[] ghosting = getMeasureReport(reportpath, "ReportGhosting_T1S4.txt");
		String[] geometrico3 = getMeasureReport(reportpath, "ReportGeometrico_T1S3.txt");		

		// ----- cancellazione cacchine precedenti -----
		boolean ok1 = ACRinputOutput.deleteFile(new File(reportpath + "\\ReportLocalizer.html"));
		boolean ok2 = ACRinputOutput.deleteFile(new File(reportpath + "\\Report_T1S1.html"));
		boolean ok3 = ACRinputOutput.deleteFile(new File(reportpath + "\\Report_T1S4.html"));
		boolean ok4 = ACRinputOutput.deleteFile(new File(reportpath + "\\Report_T1S3.html"));
		if (!(ok1 && ok2&&ok3&&ok4))
			ACRlog.waitHere("PROBLEMA CANCELLAZIONE");
		// ----------------------------------------------

		String[] localizhtml2 = reporterEngine(localizhtml0, localizer, false);
		writeTextFile(localizhtml2, "ReportLocalizer.html", reportpath);
		
		String[] slice1html1 = reporterEngine(slice1html0, geometrico, false);
		String[] slice1html2 = reporterEngine(slice1html1, posizione, false);
		String[] slice1html3 = reporterEngine(slice1html2, thickness, false);
		writeTextFile(slice1html3, "Report_T1S1.html", reportpath);
		
		String[] slice3html1 = reporterEngine(slice3html0, geometrico3, false);
		writeTextFile(slice3html1, "Report_T1S3.html", reportpath);
		
		String[] slice4html1 = reporterEngine(slice4html0, uniformity, false);
		String[] slice4html2 = reporterEngine(slice4html1, ghosting, false);
		writeTextFile(slice4html2, "Report_T1S4.html", reportpath);
	}

	public void mainReporter2() {

		String reportpath = getReportPath("ACRlist.tmp");
		IJ.log("reportpath= " + reportpath);

		String[] slice1html0 = getTemplateFromJAR("templates/", "ReportSlice1_T1A.html");
		String[] slice3html0 = getTemplateFromJAR("templates/", "ReportSlice3_T1A.html");
		String[] slice4html0 = getTemplateFromJAR("templates/", "ReportSlice4_T1B.html");

		String[] thickness = getMeasureReport(reportpath, "ReportThickness_T2S1.txt");
		String[] geometrico = getMeasureReport(reportpath, "ReportGeometrico_T2S1.txt");
		String[] posizione = getMeasureReport(reportpath, "ReportPosition_T2S1.txt");
		String[] uniformity = getMeasureReport(reportpath, "ReportUniformity_T2S4.txt");
		String[] ghosting = getMeasureReport(reportpath, "ReportGhosting_T2S4.txt");
		String[] geometrico3 = getMeasureReport(reportpath, "ReportGeometrico_T2S3.txt");		
		
		// ----- cancellazione cacchine precedenti -----
		boolean ok2 = ACRinputOutput.deleteFile(new File(reportpath + "\\Report_T2S1.html"));
		boolean ok3 = ACRinputOutput.deleteFile(new File(reportpath + "\\Report_T2S4.html"));
		boolean ok4 = ACRinputOutput.deleteFile(new File(reportpath + "\\Report_T2S3.html"));
		if (!(ok2&&ok3&&ok4))
			ACRlog.waitHere("PROBLEMA CANCELLAZIONE");
		// ----------------------------------------------

		String[] slice1html1 = reporterEngine(slice1html0, geometrico, false);
		String[] slice1html2 = reporterEngine(slice1html1, posizione, false);
		String[] slice1html3 = reporterEngine(slice1html2, thickness, false);
		writeTextFile(slice1html3, "Report_T2S1.html", reportpath);
		
		String[] slice3html1 = reporterEngine(slice3html0, geometrico3, false);
		writeTextFile(slice3html1, "Report_T2S3.html", reportpath);
		
		String[] slice4html1 = reporterEngine(slice4html0, uniformity, false);
		String[] slice4html2 = reporterEngine(slice4html1, ghosting, false);
		writeTextFile(slice4html2, "Report_T2S4.html", reportpath);
		
	}

	/**
	 * legge il path della cartella REPORT dal file temporaneo ACRlist.tmp
	 * 
	 * @param tmpfile
	 * @return
	 */
	public String getReportPath(String tmpfile) {
		String tmppath = IJ.getDirectory("temp");
		String[] mytemp = null;

		try {
			mytemp = new Reporter().readTextFile(tmpfile, tmppath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String riga5 = mytemp[4];
		String resultpath = riga5.substring(riga5.lastIndexOf("#") + 1, riga5.length()) + "\\";
		return resultpath;
	}

	/**
	 * legge dal file jar il template della pagina HTML
	 */
	public String[] getTemplateFromJAR(String htmlpath, String htmlfile) {

		String[] myhtml = null;
		try {
			myhtml = new Reporter().getText3(htmlfile, htmlpath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return myhtml;
	}

	/**
	 * legge il file report coi risultati, in formato txt
	 */
	public String[] getMeasureReport(String reportpath, String measurefiletxt) {

		String[] myresult = null;
		try {
			myresult = new Reporter().readTextFile(measurefiletxt, reportpath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return myresult;
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
		int count = 0;
		try {
			myscanner = new Scanner(myfile);
			while (myscanner.hasNextLine()) {
				String line = myscanner.nextLine();
				list1.add(line);
			}
			IJ.log(ACRlog.qui() + "READ_OK " + sourcefile);
			myscanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			IJ.log(ACRlog.qui() + "FILE_NOT_FOUND_EXCEPTION " + sourcefile);
			ACRlog.waitHere("FILE NOT FOUND EXCEPTION " + sourcefile);
		}
		String[] out1 = new String[list1.size()];
		for (int i1 = 0; i1 < list1.size(); i1++) {
			out1[i1] = list1.get(i1);
		}
		return out1;
	}

	/**
	 * Modifica del template, includendo i dati al posto dei tag per le immagini
	 * (riconoscibili per il tag che appartiene al centinaio 900), se non viene
	 * trovata una sostituzione, viene inserita di defautl l'immagine di default,
	 * che ha il tag 999
	 * 
	 * @param myhtml
	 * @param myresult
	 * @return
	 */
	public static String[] reporterEngine(String[] myhtml, String[] myresult, boolean test) {

		String[] out1 = new String[myhtml.length];
		String str3 = null;
		boolean def1 = false;
		boolean trovato = false;
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
		if (test)
			IJ.log(ACRlog.qui() + "reporterEngine");
		for (String str1 : myhtml) {
			Matcher m1 = r.matcher(str1);
			if (m1.find()) {
				trovato = false;
				prima1 = m1.start();
				dopo1 = m1.end();
				part1 = str1.substring(0, prima1);
				part3 = str1.substring(dopo1);
				for (String str2 : myresult) {
					Matcher m2 = r.matcher(str2);
					if (m2.find()) {
						trovato = true;
						prima2 = m2.start();
						dopo2 = m2.end();
						if (m1.group(0).compareTo(m2.group(0)) == 0) {
							if (test) {
								IJ.log(ACRlog.qui() + "sostituisco: " + m1.group(0) + " con " + m2.group(0));
							}
							part2 = str2.substring(dopo2);
							str3 = part1 + part2 + part3;
							if (test) {
								IJ.log("str3= " + str3);
							}
						}
					}
					if (!trovato) {
						str3 = str1;
					}

				}
//				IJ.log("BBB " + m1.group(0));
//				str3 = str1;
			} else {
				str3 = str1;
			}
			if (test) {
				IJ.log("" + (count) + "::" + str3);
			}
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
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * crea una imagine fasulla da inserire dove non si avranno immagini disponibili
	 */
	public static String dummyImage(String path) {
		IJ.newImage("dummy", "16-bit ramp", 192, 192, 1);
		ImagePlus imp1 = IJ.getImage();
		String name = "dummy.png";
		String aux1 = path + name;
		IJ.log(ACRlog.qui());
		boolean ok = ACRinputOutput.deleteFile(new File(aux1));
		IJ.log(ACRlog.qui() + "ok= " + ok);
		IJ.saveAs(imp1, "PNG", aux1);
		IJ.log(ACRlog.qui());
		return (aux1);
	}
}
