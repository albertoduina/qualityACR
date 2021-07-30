package qualityACR;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;

public class ACRutils {

	static final boolean debug = true;
	static final boolean big = true;

	public static void writeConfig(String[][] property) {
		String tmpFolderPath = IJ.getDirectory("temp");
		String configPath = tmpFolderPath + "ACRsetup.tmp";

		try (OutputStream output = new FileOutputStream(configPath)) {

			Properties prop = new Properties();

			for (int i1 = 0; i1 < property[0].length; i1++) {
				prop.setProperty(property[0][i1], property[1][i1]);
				prop.store(output, null);
			}

			// save properties to project root folder

		} catch (IOException io) {
			io.printStackTrace();
		}

	}

	public static Properties readConfig() {

		String tmpFolderPath = IJ.getDirectory("temp");
		String configPath = tmpFolderPath + "ACRsetup.tmp";
		Properties prop = new Properties();

		try (InputStream input = new FileInputStream(configPath)) {

			// load a properties file
			prop.load(input);

//			// get the property value and print it out
//			System.out.println(prop.getProperty("db.url"));
//			System.out.println(prop.getProperty("db.user"));
//			System.out.println(prop.getProperty("db.password"));

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return prop;

	}

	/**
	 * legge dalla cartella tmp dell'utente (visibile in IJ sotto
	 * File/showFolder/temp) il file ACRsetup.tmp contenente i defaults per i vari
	 * menu di avviamento dei vari plugin
	 * 
	 * @return
	 */
	public static Properties readConfigACR() {

		String tmpFolderPath = IJ.getDirectory("temp");
		String configPath = tmpFolderPath + "ACRsetup.tmp";
		Properties prop = new Properties();
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(configPath));
			prop.load(is);
			is.close();
			return prop;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * scrive nella cartella tmp dell'utente (visibile in IJ sotto
	 * File/showFolder/temp) il file ACRsetup.tmp contenente i defaults per i vari
	 * menu di avviamento dei vari plugin
	 * 
	 * @return
	 */
	public static void writeConfigACR(Properties prefs) throws IOException {

		String tmpFolderPath = IJ.getDirectory("temp");
		String configPath = tmpFolderPath + "ACRsetup.tmp";

		FileOutputStream fos = new FileOutputStream(configPath);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		prefs.store(bos, "ACR Preferences");
		bos.close();
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
		IJ.log(ACRlog.qui());
		Scanner myReader;
		try {
			myReader = new Scanner(file);
			while (myReader.hasNextLine()) {
				String str1 = myReader.nextLine();
				String[] str2 = str1.split("#");
				vetList.add(str2[1]);
			}
			myReader.close();	
			IJ.log(ACRlog.qui());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		try {
//			FileReader fileReader = new FileReader(fileName);
//			BufferedReader in = new BufferedReader(fileReader);
//			String str1 = "";
//			while ((str1 = in.readLine()) != null) {
//				String[] str2 = str1.split("#");
//				vetList.add(str2[1]);
//			}
//			in.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		// ora trasferiamo tutto nel vettore
		String[] vetResult = new String[vetList.size()];
		for (int i1 = 0; i1 < vetList.size(); i1++) {
			vetResult[i1] = vetList.get(i1).trim();
		}
		return vetResult;
	}

	/**
	 * Serve ad evitare gli SPORCHINI digitali nei pixel zero e/o ultimo delle
	 * immagini di alcuni pro/duttori
	 * 
	 * @param prof1
	 * @return
	 */
	public static double[] zeropadProfile1v(double[] prof1) {

		double[] prof2 = new double[prof1.length];
		for (int i1 = 0; i1 < prof1.length; i1++) {
			prof2[i1] = prof1[i1];
		}
		prof2[0] = (double) 0;
		prof2[prof2.length - 1] = (double) 0;
//		ACRutils.vetPrint(prof2, "prof2");
//		ACRutils.waitHere("zeropadProfile");

		return prof2;
	}

	/**
	 * azzera primo ed ultimo pixel del profilo
	 * 
	 * @param prof1
	 * @return
	 */
	public static double[][] zeropadProfile3v(double[][] prof1) {

		double[][] prof2 = new double[prof1.length][prof1[0].length];
		for (int i1 = 0; i1 < prof1.length; i1++) {
			for (int i2 = 0; i2 < prof1[0].length; i2++) {
				prof2[i1][i2] = prof1[i1][i2];
			}
		}
		prof2[2][0] = (double) 0;
		prof2[2][prof2[0].length - 1] = (double) 0;
		return prof2;
	}

	/**
	 * Estrae dalla matrice line3 il valore di segnale nei pixel ovvero il segnale Z
	 * 
	 * @param line3
	 * @return
	 */
	public static double[] signal1vfrom3v(double[][] line3) {

		// line3 [4][n]
		// line3[0] = coordinata X punto
		// line3[1] = coordinata Y punto
		// line3[2] = segnale Z punto;
		// line3[3] = coordinata W su profilo;

		double[] line1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			line1[i1] = line3[2][i1];
		}
		return line1;
	}

	public static double pseudomax(double[] vet1, int len1) {

		double max1 = Double.MIN_VALUE;
		for (int i1 = 0; i1 < (vet1.length - len1); i1++) {
			double[] subvet1 = ACRutils.subvet(vet1, len1, i1);
			double mean1 = ACRcalc.vetMean(subvet1);
			if (Double.compare(mean1, max1) > 0) {
				max1 = mean1;
			}
		}
		return max1;
	}

	public static double pseudomin(double[] vet1, int len1) {

		double min1 = Double.MAX_VALUE;
		for (int i1 = 0; i1 < (vet1.length - len1); i1++) {
			double[] subvet1 = ACRutils.subvet(vet1, len1, i1);
			double mean1 = ACRcalc.vetMean(subvet1);
			if (Double.compare(mean1, min1) < 0) {
				min1 = mean1;
			}
		}
		return min1;
	}

	/**
	 * Restituisce un subvector
	 * 
	 * @param vet1 array di input
	 * @param len1 lunghezza sottoarray
	 * @param pos1 starting point
	 * @return
	 */
	public static double[] subvet(double[] vet1, int len1, int pos1) {
		if (pos1 >= vet1.length - len1)
			return null;
		double[] out1 = new double[len1];
		for (int i1 = 0; i1 < len1; i1++) {
			out1[i1] = vet1[pos1 + i1];
		}

		return out1;
	}

	/**
	 * Read preferences from IJ_Prefs.txt
	 * 
	 * @param width  image width
	 * @param height image height
	 * @param limit  border limit for object placement on image
	 * @return
	 */
	public static int[] readPreferences(int width, int height, int limit) {

		int diam = ACRutils.readInt(Prefs.get("prefer.p12rmnDiamFantoc", Integer.toString(width * 2 / 3)));
		int xRoi1 = ACRutils.readInt(Prefs.get("prefer.p12rmnXRoi1", Integer.toString(height / 2 - diam / 2)));
		int yRoi1 = ACRutils.readInt(Prefs.get("prefer.p12rmnYRoi1", Integer.toString(width / 2 - diam / 2)));
		if (diam < limit)
			diam = height * 2 / 3;
		if (xRoi1 < limit)
			xRoi1 = height / 2 - diam / 2;
		if (yRoi1 < limit)
			yRoi1 = width / 2 - diam / 2;
		int[] defaults = { xRoi1, yRoi1, diam };
		return defaults;
	}

	/**
	 * Riceve una ImagePlus derivante da un CannyEdgeDetector con impostata una
	 * Line, restituisce le coordinate dei 2 picchi, se non sono esattamente 2
	 * emette un messaggio e poi restituisce null.
	 * 
	 * @param imp1
	 * @param dimPixel
	 * @param title
	 * @param showProfiles
	 * @param demo
	 * @param debug
	 * @return
	 */
	public static double[][] cannyProfileAnalyzer(ImagePlus imp1, double dimPixel, String title, boolean showProfiles,
			boolean step, boolean vertical, boolean fast, boolean verbose, int timeout) {

//		IJ.log("cannyProfileAnalyzer riceve: showprofiles= " + showProfiles + " step= " + step + " vertical= "
//				+ vertical + " fast= " + fast + " verbose= " + verbose);
//		ACRlog.waitThere("chiamata canny");
//		ACRlog.waitHere("step= " + step + " fast= " + fast + " verbose= " + verbose);

		double[][] profi3 = ACRgraphic.lineDecomposer(imp1);
		if (profi3 == null) {
			ACRlog.waitHere("profi3 == null");
			return null;
		}
		int count1 = 0;
		boolean ready1 = false;
		double max1 = 0;
		for (int i1 = 0; i1 < profi3[0].length; i1++) {

			if (profi3[2][i1] > max1) {
				max1 = profi3[2][i1];
				ready1 = true;
			}
			if ((profi3[2][i1] == 0) && ready1) {
				max1 = 0;
				count1++;
				ready1 = false;
			}
		}
		// devo ora contare i pixel a 255 che ho trovato, ne accettero' solo 2,
		if (count1 != 2) {
			if (true)
				ACRlog.waitHere("" + title + " trovati un numero di punti diverso da 2, count= " + count1
						+ " scartiamo questi risultati");
			return null;
		}
		// peaks1 viene utilizzato in un altra routine, per cui gli elementi 0,
		// 1 e 2 sono utilizzati per altro, li lascio a 0
		double[][] peaks1 = new double[6][count1];
		int count2 = 0;
		boolean ready2 = false;
		double max2 = 0;
		for (int i1 = 0; i1 < profi3[0].length; i1++) {
			if (profi3[2][i1] > max2) {
				peaks1[3][count2] = profi3[0][i1];
				peaks1[4][count2] = profi3[1][i1];
				max2 = profi3[2][i1];
				peaks1[5][count2] = max2;
				ready2 = true;
			}
			if ((profi3[2][i1] == 0) && ready2) {
				max2 = 0;
				count2++;
				ready2 = false;
			}
		}

// ----------------------------------------
// QUI AGGIUNGEVO ARBITRARIAMENTE 1 AI PUNTI TROVATI 
// E PARE CHE AD OCCHIO VADA MEGLIO (BOHHHHHHH)????
// ---------------------------------------

		for (int i1 = 0; i1 < peaks1.length; i1++) {
			for (int i2 = 0; i2 < peaks1[0].length; i2++)
				if (peaks1[i1][i2] > 0)
					peaks1[i1][i2] = peaks1[i1][i2] + 1;
		}

		if (showProfiles) {
			double[] bx = new double[profi3[2].length];
			for (int i1 = 0; i1 < profi3[2].length; i1++) {
				bx[i1] = (double) i1;
			}

			double[] xPoints = new double[peaks1[0].length];
			double[] yPoints = new double[peaks1[0].length];
			double[] zPoints = new double[peaks1[0].length];
			for (int i1 = 0; i1 < peaks1[0].length; i1++) {
				xPoints[i1] = peaks1[3][i1];
				yPoints[i1] = peaks1[4][i1];
				zPoints[i1] = peaks1[5][i1];
			}
			Plot plot2 = ACRgraphic.basePlot2(profi3, title, Color.GREEN, vertical);
			plot2.draw();
			plot2.setColor(Color.red);
			if (vertical)
				// plot2.addPoints(yPoints, zPoints, PlotWindow.CIRCLE);
				plot2.addPoints(yPoints, zPoints, Plot.CIRCLE);
			else
				// plot2.addPoints(xPoints, zPoints, PlotWindow.CIRCLE);
				plot2.addPoints(xPoints, zPoints, Plot.CIRCLE);
			plot2.show();

			Frame lw = WindowManager.getFrame(title);
			if (lw != null)
				lw.setLocation(10, 10);
			ACRlog.waitHere("Analizzando il profilo del segnale lungo la linea si ricavano \n"
					+ "le coordinate delle due intersezioni con la circonferenza.", debug, timeout, fast);
			if (WindowManager.getFrame("orizzontale") != null) {
				IJ.selectWindow("orizzontale");
				IJ.run("Close");
			}

		}
		if (WindowManager.getFrame("Profile") != null) {
			IJ.selectWindow("Profile");
			IJ.run("Close");
		}
		// verifico di avere trovato un max di 2 picchi
		if (peaks1[2].length > 2)
			ACRlog.waitHere(
					"Attenzione trovate troppe intersezioni col cerchio, cioe' " + peaks1[2].length + "  VERIFICARE");
		if (peaks1[2].length < 2)
			ACRlog.waitHere(
					"Attenzione trovata una sola intersezione col cerchio, cioe' " + peaks1[2].length + "  VERIFICARE");
		// logMatrix(peaks1, "peaks1 " + title);
		return peaks1;
	}

	public static boolean compareVectors(double[] vect1, double[] vect2, double precision, String msg) {
		if ((vect1 == null) || (vect2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning vector = null");
			}
			return false;
		}
		if (vect1.length != vect2.length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Vectors with different length");
			}
			return false;
		}
		for (int i1 = 0; i1 < vect1.length; i1++) {
			double diff = vect1[i1] - vect2[i1];
			if (Math.abs(diff) > precision) {
				return false;
			}
		}
		return true;
	}

	public static boolean compareVectors(float[] vect1, float[] vect2, float precision, String msg) {
		if ((vect1 == null) || (vect2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning vector = null");
			}
			return false;
		}
		if (vect1.length != vect2.length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Vectors with different length");
			}
			return false;
		}
		for (int i1 = 0; i1 < vect1.length; i1++) {
			double diff = vect1[i1] - vect2[i1];
			if (Math.abs(diff) > precision) {
				return false;
			}
		}
		return true;
	}

	public static boolean compareVectors(int[] vect1, int[] vect2, String msg) {
		if ((vect1 == null) || (vect2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning vector = null");
			}
			return false;
		}
		if (vect1.length != vect2.length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Vectors with different length");
			}
			return false;
		}
		for (int i1 = 0; i1 < vect1.length; i1++) {
			if (vect1[i1] != vect2[i1]) {
				return false;
			}
		}
		return true;
	}

	public static boolean compareVectors(long[] vect1, long[] vect2, String msg) {
		if ((vect1 == null) || (vect2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning vector = null");
			}
			return false;
		}
		if (vect1.length != vect2.length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Vectors with different length");
			}
			return false;
		}
		for (int i1 = 0; i1 < vect1.length; i1++) {
			if (vect1[i1] != vect2[i1]) {
				return false;
			}
		}
		return true;
	}

	public static boolean compareVectors(String[] vect1, String[] vect2, String msg) {
		if ((vect1 == null) || (vect2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning vector = null");
			}
			return false;
		}
		if (vect1.length != vect2.length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Vectors with different length");
			}
			return false;
		}
		for (int i1 = 0; i1 < vect1.length; i1++) {
			if (!vect1[i1].equals(vect2[i1])) {
				if (msg.length() > 0) {
					IJ.log(msg + " At pos " + i1 + " " + vect1[i1] + " differ " + vect2[i1]);
				}
				return false;
			}
		}
		return true;
	}

	public static boolean compareDoublesWithTolerance(double aa, double bb, double tolerance) {
		if (Double.compare(aa, bb) == 0)
			return true;
		return Math.abs(aa - bb) < tolerance;
	}

	/**
	 * Write preferences into IJ_Prefs.txt
	 * 
	 * @param boundingRectangle
	 */
	public static void writeStoredRoiData(Rectangle boundingRectangle) {

		Prefs.set("prefer.ACRrmnDiamFantoc", Integer.toString(boundingRectangle.width));
		Prefs.set("prefer.ACRrmnXRoi1", Integer.toString(boundingRectangle.x));
		Prefs.set("prefer.ACRrmnYRoi1", Integer.toString(boundingRectangle.y));
	}

	/**
	 * Calcolo della distanza tra un punto ed una circonferenza
	 * 
	 * @param x1 coord. x punto
	 * @param y1 coord. y punto
	 * @param x2 coord. x centro
	 * @param y2 coord. y centro
	 * @param r2 raggio
	 * @return distanza
	 */
	public static double pointCirconferenceDistance(int x1, int y1, int x2, int y2, int r2) {

		double dist = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) - r2;
		return dist;
	}

	/**
	 * Zoom max immagine
	 * 
	 * @param imp1 immagine di input
	 */
	public static void zoom(ImagePlus imp1) {
		imp1 = IJ.getImage();
		ImageWindow win = imp1.getWindow();
		// double magnification = win.getCanvas().getMagnification();
		win.maximize();
		return;
	}

	/**
	 * Zoom min immagine
	 * 
	 * @param imp1
	 */
	public static void unzoom(ImagePlus imp1) {
		imp1 = IJ.getImage();
		ImageWindow win = imp1.getWindow();
		// double magnification = win.getCanvas().getMagnification();
		win.minimize();
		return;
	}

	/**
	 * Attenzione che in questa versione la struttura della matrice fu completamente
	 * cambiata
	 * 
	 * @param profile
	 * @param title
	 * @param color
	 * @return
	 */
	public static Plot ACRplot(double[] profileY, String title, Color color, boolean vertical) {

		Plot plot1 = new Plot(title, "pixel", "valore");
		plot1.add("LINE", profileY);
		plot1.setColor(color);
		// plot1.setLineWidth(3);
		plot1.show();
		return plot1;
	}

//	/**
//	 * Acquisisce un profilo lungo una linea, non necessariamente orizzontale o
//	 * verticale, ed anche wideline
//	 * 
//	 * @param imp1
//	 * @param x1
//	 * @param y1
//	 * @param x2
//	 * @param y2
//	 * @param width
//	 * @return
//	 */
//
//	public static double[] getLinePixels(ImagePlus imp1, int x1, int y1, int x2, int y2, int width) {
//
//		Line.setWidth(width);
//		imp1.setRoi(new Line(x1, y1, x2, y2));
//		imp1.updateAndDraw();
//		Roi roi1 = imp1.getRoi();
//		double[] profiM1 = ((Line) roi1).getPixels();
//		return profiM1;
//	}

	/**
	 * Restituisce le coordinate x, y e z dei punti appartenenti alla linea, in cui
	 * x ed y sono le coordinate del pixel (relative all'immagine di partenza) e z
	 * e' il valore di segnale
	 * 
	 * @param imp1
	 * @return
	 */
	public static double[][] decomposer3v(ImagePlus imp1) {

		Line line = (Line) imp1.getRoi();
		if (line == null) {
			IJ.log(ACRlog.qui() + "  line==null");
			ACRlog.waitHere(ACRlog.qui() + "  line==null");
		}
		double[] profiZ = line.getPixels();
		double[] profiW = new double[profiZ.length];
		for (int i1 = 0; i1 < profiZ.length; i1++) {
			profiW[i1] = i1;
		}
		FloatPolygon fp1 = imp1.getRoi().getInterpolatedPolygon();
		int len = fp1.npoints - 1;
//		if (profiZ.length != len )
//			ACRlog.waitHere("vettori di lunghezza differente Line= " + profiZ.length + " Polygon= " + fp1.npoints);
		double[] profiX = new double[len];
		double[] profiY = new double[len];
		for (int i1 = 0; i1 < len; i1++) {
			profiX[i1] = (double) fp1.xpoints[i1];
			profiY[i1] = (double) fp1.ypoints[i1];
		}
		double[][] out1 = new double[4][profiX.length];
		out1[0] = profiX;
		out1[1] = profiY;
		out1[2] = profiZ;
		out1[3] = profiW;
		return out1;
	}

	/**
	 * Esegue l'interpolazione lineare tra due punti dati, conoscendo la x
	 * 
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @param cx
	 * @return
	 */
	public static double yLinearInterpolation(double ax, double ay, double bx, double by, double cx) {

		double cy = by - ((by - ay) * (bx - cx) / (bx - ax));
		IJ.log("yinterpolato cy= " + cx + " ax= " + ax + " ay=" + ay + " bx= " + bx + " by= " + by + " cx=" + cx);

		return cy;
	}

	/**
	 * Esegue l'interpolazione lineare tra due punti dati, conoscendo il valore di y
	 * "a mezza altezza" del punto interpolato
	 * 
	 * @param ay coordinata y del primo punto
	 * @param ax coordinata x del primo punto
	 * @param bx coordinata x del secondo punto
	 * @param by coordinata y del secondo punto
	 * @param cy cordinata y del punto da interpolare
	 * @return coordinata x del punto interpolato
	 */
	public static double xLinearInterpolation(double ax, double ay, double bx, double by, double cy) {
		// ax coordinata x su plot del primo punto
		// ay coordinata y su plot del primo punto
		// bx coordinata x su plot del secondo punto
		// by coordinata y su plot del secondo punto
		// cy cordinata y del punto da trovare

		double cx = bx - ((bx - ax) * (by - cy) / (by - ay));
		// IJ.log("xinterpolato cx= " + cx + " ax= " + ax + " ay=" + ay + " bx= " + bx +
		// " by= " + by + " cy=" + cy);
		// coordinata x su immagine calcolata
		return cx;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1, int size) {

		float[] xPoints = new float[1];
		float[] yPoints = new float[1];

		xPoints[0] = (float) xPoints1;
		yPoints[0] = (float) yPoints1;
		// logVector(xPoints, "xPoints");
		// logVector(yPoints, "yPoints");
		// ACRutils.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(2);
		pr1.setSize(size);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		return;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1, int type, int size,
			Color color, boolean border) {

//		float[] xPoints = new float[1];
//		float[] yPoints = new float[1];
//
//		xPoints[0] = (float) xPoints1;
//		yPoints[0] = (float) yPoints1;
		// logVector(xPoints, "xPoints");
		// logVector(yPoints, "yPoints");
		// ACRutils.waitHere();

		Roi pr1 = new Roi(xPoints1, yPoints1, 1, 1);
//		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
//		pr1.setPointType(type);
//		pr1.setSize(size);

		imp1.setRoi(pr1);
		if (border)
			imp1.getRoi().setStrokeColor(color);
		else
			imp1.getRoi().setFillColor(color);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		return;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, double xPoint, double yPoint, int type, int size,
			Color color, boolean border) {

//		float[] xPoints = new float[1];
//		float[] yPoints = new float[1];
//
//		xPoints[0] = (float) xPoints1;
//		yPoints[0] = (float) yPoints1;
//		// logVector(xPoints, "xPoints");
//		// logVector(yPoints, "yPoints");
//		// ACRutils.waitHere();

//		PointRoi pr1 = new PointRoi(xPoint, yPoint);
//		pr1.setPointType(type);
//		pr1.setSize(size);

		Roi pr1 = new Roi(xPoint, yPoint, 1, 1);

		imp1.setRoi(pr1);
		if (border)
			imp1.getRoi().setStrokeColor(color);
		else
			imp1.getRoi().setFillColor(color);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		return;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1) {

		float[] xPoints = new float[1];
		float[] yPoints = new float[1];

		xPoints[0] = (float) xPoints1;
		yPoints[0] = (float) yPoints1;
		// logVector(xPoints, "xPoints");
		// logVector(yPoints, "yPoints");
		// ACRutils.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(2);
		pr1.setSize(3);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		return;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1, Color color) {

		float[] xPoints = new float[1];
		float[] yPoints = new float[1];

		xPoints[0] = (float) xPoints1;
		yPoints[0] = (float) yPoints1;
		// logVector(xPoints, "xPoints");
		// logVector(yPoints, "yPoints");
		// ACRutils.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(2);
		pr1.setSize(4);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(color);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		return;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1, Color color, int type,
			int size) {

		float[] xPoints = new float[1];
		float[] yPoints = new float[1];

		xPoints[0] = (float) xPoints1;
		yPoints[0] = (float) yPoints1;
		// logVector(xPoints, "xPoints");
		// logVector(yPoints, "yPoints");
		// ACRutils.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(type);
		pr1.setSize(size);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(color);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
		return;
	}

	private static final int PIXEL_DATA = 0x7FE00010;
	private static final int NON_IMAGE = 0x7FE10010;
	private static final int BINARY_DATA = 0x7FE11010;

	/**
	 * La seguente routine, che si occupa di estrarre dati dall'header delle
	 * immagini fu copiata dal QueryDicomHeader.java di Anthony Padua & Daniel
	 * Barboriak - Duke University Medical Center. *** modified version *** Alberto
	 * Duina - Spedali Civili di Brescia - Servizio di Fisica Sanitaria 2006
	 * 
	 * @param imp       immagine di cui leggere l'header
	 * @param userInput stringa di 9 caratteri contenente "group,element"
	 *                  esempio:"0020,0013"
	 * @return stringa col valore del parametro
	 */
	public static String readDicomParameter(ImagePlus imp, String userInput) {
		// N.B. userInput => 9 characs [group,element] in format: xxxx,xxxx (es:
		// "0020,0013")
		// boolean bAbort;
		String attribute = "???";
		String value = "???";
		if (imp == null)
			return ("");
		int currSlice = imp.getCurrentSlice();
		ImageStack stack = imp.getStack();
		// int sSize = stack.getSize();
		// String sLabel = stack.getSliceLabel(currSlice);
		// String iLabel = (String) imp.getProperty("Info");
		// IJ.log("-------------------------");
		// IJ.log("sSize= "+sSize);
		// IJ.log("sLabel= "+sLabel);
		// IJ.log("iLabel= "+iLabel);
		// MyLog.waitHere();

		String header = stack.getSize() > 1 ? stack.getSliceLabel(currSlice) : (String) imp.getProperty("Info");

		if (header != null) {
			int idx1 = header.indexOf(userInput);
			int idx2 = header.indexOf(":", idx1);
			int idx3 = header.indexOf("\n", idx2);
			if (idx1 >= 0 && idx2 >= 0 && idx3 >= 0) {
				try {
					attribute = header.substring(idx1 + 9, idx2);
					attribute = attribute.trim();
					value = header.substring(idx2 + 1, idx3);
					value = value.trim();
					return (value);
				} catch (Throwable e) { // Anything else
					ACRlog.here("value PROBLEM");
					return (value);
				}
			} else {
				attribute = "MISSING";
				return (attribute);
			}
		} else {
			String aux1 = (String) imp.getProperty("Info");
			// MyLog.waitThere("aux1= " + aux1);

			IJ.log("" + imp.getTitle());
			ACRlog.trace("readDicomParameter WARNING!! Header is null.", true);
			ACRlog.trace("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n" + "file="
					+ Thread.currentThread().getStackTrace()[3].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[3].getLineNumber(), true);

//			IJ.error("readDicomParameter WARNING!! Header is null.");
			attribute = null;
			return (attribute);
		}
	}

	public static String readDicomParameter(String header, String userInput) {
		// N.B. userInput => 9 characs [group,element] in format: xxxx,xxxx (es:
		// "0020,0013")
		// boolean bAbort;
		String attribute = "???";
		String value = "???";
		if (header != null) {
			int idx1 = header.indexOf(userInput);
			int idx2 = header.indexOf(":", idx1);
			int idx3 = header.indexOf("\n", idx2);
			if (idx1 >= 0 && idx2 >= 0 && idx3 >= 0) {
				try {
					attribute = header.substring(idx1 + 9, idx2);
					attribute = attribute.trim();
					value = header.substring(idx2 + 1, idx3);
					value = value.trim();
					return (value);
				} catch (Throwable e) { // Anything else
					ACRlog.here("value PROBLEM");
					return (value);
				}
			} else {
				attribute = "MISSING";
				return (attribute);
			}
		} else {
			return (null);
		}
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
			// IJ.error("readDouble >> invalid floating point number");
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
			// IJ.error("readFloat >> invalid floating point number");
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
			// IJ.error(" readInt >> invalid integer number ");
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

	// public static void waitHere1(String str, boolean debug, final int milli)
	// {
	// if (milli != 0) {
	//
	// String where = "file="
	// + Thread.currentThread().getStackTrace()[2].getFileName()
	// + " " + " line="
	// + Thread.currentThread().getStackTrace()[2].getLineNumber();
	//
	// JFrame f = new JFrame();
	// JTextArea myarea = new JTextArea("\n " + where
	// + " \n" + "\n " + str + " \n");
	// final JDialog dialog = new JDialog(f, "Auto cancel msg", true);
	// dialog.setLocation(500, 500);
	// dialog.add(myarea);
	// dialog.pack();
	// ScheduledExecutorService s = Executors
	// .newSingleThreadScheduledExecutor();
	//
	// s.schedule(new Runnable() {
	// public void run() {
	// dialog.setVisible(false);
	// dialog.dispose();
	// }
	// }, milli, TimeUnit.MILLISECONDS);
	// dialog.setVisible(true);
	// } else {
	// waitThere(str, debug);
	//
	// }
	// }

	/**
	 * Cerca tutte me occorrenze del valore nell'elemento selezionato con key
	 * 
	 * @param vetIn matrice di ingreso
	 * @param value valore cercato
	 * @param key   selezione
	 * @return matrice occorrenze trovate
	 */
	public static int[][] searchValue(int[][] vetIn, int value, int key) {
		ArrayList<Integer> outX = new ArrayList<>();
		ArrayList<Integer> outY = new ArrayList<>();
		for (int i1 = 0; i1 < vetIn.length; i1++) {
			if (vetIn[i1][key] == value) {
				outX.add(vetIn[i1][0]);
				outY.add(vetIn[i1][1]);
			}
		}
		int[] myX = ACRcalc.arrayListToArrayInt(outX);
		int[] myY = ACRcalc.arrayListToArrayInt(outY);
		int[][] out1 = new int[myX.length][2];
		for (int i1 = 0; i1 < myX.length; i1++) {
			out1[i1][0] = myX[i1];
			out1[i1][1] = myY[i1];
		}
		return out1;
	}

	/**
	 * estrae dalla matrice uno degli array, selezionato da key
	 * 
	 * @param selectedPoints matrice input
	 * @param key            selezione
	 * @return array output
	 */
	public static int[] matExtractor(int[][] selectedPoints, int key) {
		int[] out1 = new int[selectedPoints.length];
		for (int i1 = 0; i1 < selectedPoints.length; i1++) {
			out1[i1] = selectedPoints[i1][key];
		}
		return out1;
	}

	public static int[][] matRotate(int[][] matin) {
		int[][] matrot = new int[matin[0].length][matin.length];
		for (int i1 = 0; i1 < matin.length; i1++) {
			for (int i2 = 0; i2 < matin[0].length; i2++) {
				matrot[i2][i1] = matin[i1][i2];
			}
		}
		return matrot;
	}

	/**
	 * ricerca del minimo
	 * 
	 * @param vetIn array input
	 * @return minimo ed indice del minimo
	 */
	public static int[] minsearch(int[] vetIn) {
		int min = Integer.MAX_VALUE;
		int index = 0;
		for (int i1 = 0; i1 < vetIn.length; i1++) {
			if (vetIn[i1] < min) {
				min = vetIn[i1];
				index = i1;
			}
		}
		int[] out1 = new int[2];
		out1[0] = min;
		out1[1] = index;

		return out1;
	}

	/**
	 * ricerca del massimo
	 * 
	 * @param vetIn array input
	 * @return massimo ed indice del massimo
	 */
	public static int[] maxsearch(int[] vetIn) {
		int max = Integer.MIN_VALUE;
		int index = 0;
		for (int i1 = 0; i1 < vetIn.length; i1++) {
			if (vetIn[i1] > max) {
				max = vetIn[i1];
				index = i1;
			}
		}
		int[] out1 = new int[2];
		out1[0] = max;
		out1[1] = index;
		return out1;
	}

	/**
	 * Trasforma un array da float a double
	 * 
	 * @param vetIn
	 * @return
	 */
	public static double[] toDouble(float[] vetIn) {
		double[] vetOut = new double[vetIn.length];
		for (int i1 = 0; i1 < vetIn.length; i1++) {
			vetOut[i1] = (double) vetIn[i1];
		}
		return vetOut;
	}

	/**
	 * arrotonda un array da double ad int
	 * 
	 * @param vetIn
	 * @return
	 */
	public static int[] toInt(double[] vetIn) {
		int[] vetOut = new int[vetIn.length];
		for (int i1 = 0; i1 < vetIn.length; i1++) {
			vetOut[i1] = (int) Math.round(vetIn[i1]);
		}
		return vetOut;
	}

	public static double dblTruncate(double in1, int decimals) {
		ACRlog.waitThere("");
		BigDecimal bd1 = new BigDecimal(Double.toString(in1));
		bd1 = bd1.setScale(decimals, RoundingMode.HALF_UP);

		return bd1.doubleValue();
	}

	public static boolean compareMatrix(double[][] mat1, double[][] mat2, String msg) {
		if ((mat1 == null) || (mat2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning matrix = null");
				return false;
			}
		}
		if (mat1.length != mat2.length || mat1[0].length != mat2[0].length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Matrices with different dimensions");
				return false;
			}
		}
		for (int i2 = 0; i2 < mat1[0].length; i2++) {
			for (int i1 = 0; i1 < mat1.length; i1++) {
				// IJ.log("compare
				// mat1["+i1+"]["+i2+"]="+mat1[i1][i2]+"\tmat2["+i1+"]["+i2+"]="+mat2[i1][i2]);
				if (mat1[i1][i2] != mat2[i1][i2]) {
					IJ.log("compareMatrix difference in " + i1 + "," + i2);
					return false;
				}
			}
		}
		return true;
	}

	public static boolean compareMatrix(float[][] mat1, float[][] mat2, String msg) {
		if ((mat1 == null) || (mat2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning matrix = null");
				return false;
			}
		}
		if (mat1.length != mat2.length || mat1[0].length != mat2[0].length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Matrices with different dimensions");
				return false;
			}
		}
		for (int i2 = 0; i2 < mat1[0].length; i2++) {
			for (int i1 = 0; i1 < mat1.length; i1++) {
				// IJ.log("compare
				// mat1["+i1+"]["+i2+"]="+mat1[i1][i2]+"\tmat2["+i1+"]["+i2+"]="+mat2[i1][i2]);
				if (mat1[i1][i2] != mat2[i1][i2]) {
					IJ.log("compareMatrix difference in " + i1 + "," + i2);
					return false;
				}
			}
		}
		return true;
	}

	public static boolean compareMatrix(int[][] mat1, int[][] mat2, String msg) {
		if ((mat1 == null) || (mat2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning matrix = null");
				return false;
			}
		}
		if (mat1.length != mat2.length || mat1[0].length != mat2[0].length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Matrices with different dimensions");
				return false;
			}
		}
		for (int i2 = 0; i2 < mat1[0].length; i2++) {
			for (int i1 = 0; i1 < mat1.length; i1++) {
				// IJ.log("compare
				// mat1["+i1+"]["+i2+"]="+mat1[i1][i2]+"\tmat2["+i1+"]["+i2+"]="+mat2[i1][i2]);
				if (mat1[i1][i2] != mat2[i1][i2]) {
					IJ.log("compareMatrix difference in " + i1 + "," + i2);
					return false;
				}
			}
		}
		return true;
	}

	public static boolean compareMatrix(String[][] mat1, String[][] mat2, String msg) {
		if ((mat1 == null) || (mat2 == null)) {
			if (msg.length() > 0) {
				IJ.log(msg + " Warning matrix = null");
				return false;
			}
		}
		if (mat1.length != mat2.length || mat1[0].length != mat2[0].length) {
			if (msg.length() > 0) {
				IJ.log(msg + " Matrices with different dimensions");
				return false;
			}
		}
		for (int i2 = 0; i2 < mat1[0].length; i2++) {
			for (int i1 = 0; i1 < mat1.length; i1++) {
				// IJ.log("compare
				// mat1["+i1+"]["+i2+"]="+mat1[i1][i2]+"\tmat2["+i1+"]["+i2+"]="+mat2[i1][i2]);
				if (!mat1[i1][i2].equals(mat2[i1][i2])) {
					IJ.log("compareMatrix difference in " + i1 + "," + i2);
					return false;
				}
			}
		}
		return true;
	}

}
