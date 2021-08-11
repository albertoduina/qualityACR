package qualityACR;

import java.awt.Color;
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.gui.YesNoCancelDialog;

public class ACRlog {

	public static void deleteTmpFile(String completePath) {

		File f1 = new File(completePath);
		if (f1.exists()) {
			f1.delete();
		}
		return;
	}

	public static boolean initLog(String path) {
		File f1 = new File(path);
		if (f1.exists()) {
			GenericDialog gd1 = new GenericDialog("REPORT");
			gd1.addMessage("Per proseguire devo cancellare il report precedente, procedo?");
			gd1.enableYesNoCancel("Continua", "No");
			gd1.hideCancelButton();
			gd1.showDialog();
			if (gd1.wasOKed()) {
				// f1.delete();
				ACRinputOutput.purgeDirectory(f1);
			}
			if (gd1.wasCanceled())
				return false;
		}
		appendLog(path, "< calculated " + LocalDate.now() + " @ " + LocalTime.now() + " >");

		return true;
	}


	public static void appendLog(String completePath, String linea) {

		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(completePath, true));
			out.write(linea);
			out.newLine();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Stampa formattata quadrata di un vettore
	 * 
	 * @param imageVetPixels
	 * @param title
	 */

	public static void vetPrint(double[] imageVetPixels, String title) {
		String riga = "";

		int len = imageVetPixels.length;
		int lato = (int) Math.ceil((Math.sqrt(len)));
		IJ.log("----- " + title + " - " + len + " double[] elements -----");
		for (int y1 = 0; y1 < lato; y1++) {
			int offset = y1 * lato;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < lato; x1++) {
				if ((offset + x1) >= len) {
					riga = riga + "####,";
					continue;
				}
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + String.format("%04.0f", imageVetPixels[offset + x1]) + ";";
			}
			IJ.log(riga);
		}
		IJ.log("---------------------------");
	}

	/**
	 * Stampa formattata quadrata di un vettore
	 * 
	 * @param imageVetPixels
	 * @param title
	 */

	public static void vetPrint(float[] imageVetPixels, String title) {
		String riga = "";

		int len = imageVetPixels.length;
		int lato = (int) Math.ceil((Math.sqrt(len)));
		IJ.log("----- " + title + " - " + len + " float[] elements -----");
		for (int y1 = 0; y1 < lato; y1++) {
			int offset = y1 * lato;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < lato; x1++) {
				if ((offset + x1) >= len) {
					riga = riga + "####,";
					continue;
				}
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + String.format("%04.0f", imageVetPixels[offset + x1]) + ";";
			}
			IJ.log(riga);
		}
		IJ.log("---------------------------");
	}

	/**
	 * Stampa formattata quadrata di un vettore
	 * 
	 * @param imageVetPixels
	 * @param title
	 */

	public static void vetPrint(short[] imageVetPixels, String title) {
		String riga = "";
		int len = imageVetPixels.length;
		int lato = (int) Math.ceil((Math.sqrt(len)));
		IJ.log("----- " + title + " - " + len + " short[] elements -----");
		for (int y1 = 0; y1 < lato; y1++) {
			int offset = y1 * lato;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < lato; x1++) {
				if ((offset + x1) >= len) {
					riga = riga + "####,";
					continue;
				}
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + String.format("%04d", imageVetPixels[offset + x1]) + ";";
			}
			IJ.log(riga);
		}
		IJ.log("---------------------------");
		ACRlog.waitHere("vetPrint " + title);
	}

	/**
	 * Stampa formattata quadrata di un vettore
	 * 
	 * @param imageVetPixels
	 * @param title
	 */

	public static void vetPrint(int[] imageVetPixels, String title) {
		String riga = "";
		int len = imageVetPixels.length;
		int lato = (int) Math.ceil((Math.sqrt(len)));
		IJ.log("----- " + title + " - " + len + " int[] elements -----");
		for (int y1 = 0; y1 < lato; y1++) {
			int offset = y1 * lato;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < lato; x1++) {
				if ((offset + x1) >= len) {
					riga = riga + "####,";
					continue;
				}
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + String.format("%04d", imageVetPixels[offset + x1]) + ";";
			}
			IJ.log(riga);
		}
		IJ.log("---------------------------");
		ACRlog.waitHere("vetPrint " + title);
	}

	public static void vetPrint(byte[] imageVetPixels, String title) {
		String riga = "";
		int len = imageVetPixels.length;
		int lato = (int) Math.ceil((Math.sqrt(len)));
		IJ.log("----- " + title + " - " + len + " int[] elements -----");
		for (int y1 = 0; y1 < lato; y1++) {
			int offset = y1 * lato;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < lato; x1++) {
				if ((offset + x1) >= len) {
					riga = riga + "####,";
					continue;
				}
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + String.format("%04d", imageVetPixels[offset + x1]) + ";";
			}
			IJ.log(riga);
		}
		IJ.log("---------------------------");
		ACRlog.waitHere("vetPrint " + title);
	}

	public static void vetPrint(boolean[] imageVetPixels, String title) {
		String riga = "";
		int len = imageVetPixels.length;
		int lato = (int) Math.ceil((Math.sqrt(len)));
		IJ.log("----- " + title + " - " + len + " int[] elements -----");
		for (int y1 = 0; y1 < lato; y1++) {
			int offset = y1 * lato;
			riga = "riga " + String.format("%03d", y1) + " | ";
			for (int x1 = 0; x1 < lato; x1++) {
				if ((offset + x1) >= len) {
					riga = riga + "####,";
					continue;
				}
				// per facilitare la leggibilita' stampo tutto con formato fisso a 4 cifre
				riga = riga + imageVetPixels[offset + x1] + ";";
			}
		}
		IJ.log("---------------------------");
		IJ.log("vetPrint " + title);
	}

	public static void waitHere(String str, boolean debug) {
		if (debug) {
			IJ.beep();
			new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n \n" + str).show();
		} else {
			IJ.beep();
			new WaitForUserDialog(str).show();
		}
	}

	/***
	 * Il programma mostra un WaitForUserDialog ma, passato il timeout, chiude il
	 * dialogo, proprio come fosse stato premuto ok
	 * 
	 * @param str     messtaggio
	 * @param debug   switch di attivazione
	 * @param timeout millisecondi per il timeout
	 */

	public static void waitHere(String str, boolean debug, int timeout) {

		String where = "";
		if (debug)
			where = " \nfile=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " \n \n";

		if (timeout > 0) {
			final WaitForUserDialog wfud = new WaitForUserDialog(where + str);

			ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
			s.schedule(new Runnable() {
				public void run() {
					wfud.close();
					wfud.dispose();
				}
			}, timeout, TimeUnit.MILLISECONDS);
			wfud.setBackground(Color.yellow);
			wfud.show();
		} else {
			final WaitForUserDialog wfud = new WaitForUserDialog(where + str);

			wfud.show();
		}
	}

	/**
	 * Messaggio, con possibilità di atuotomazione del click
	 * 
	 * @param str     stringa da mostrare
	 * @param debug   se true stampa la linea in cui siamo
	 * @param timeout millisecondi di visualizzazione prima di ok automatico
	 * @param animate attivazione ok automatico
	 */
//	public static void waitHere(String str, boolean debug, int timeout, boolean animate) {
//
//		String where = "";
//		if (debug)
//			where = " \nfile=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
//					+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " \n \n";
//
//		if ((timeout > 0) && animate) {
//			final WaitForUserDialog wfud = new WaitForUserDialog(where + str);
//
//			ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
//			s.schedule(new Runnable() {
//				public void run() {
//					wfud.close();
//					wfud.dispose();
//				}
//			}, timeout, TimeUnit.MILLISECONDS);
//			wfud.setBackground(Color.yellow);
//			wfud.show();
//		} else {
//			final WaitForUserDialog wfud = new WaitForUserDialog(where + str);
//
//			wfud.show();
//		}
//	}

	public static void waitHere() {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber()).show();
	}

	public static void waitHere(String str) {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n \n" + str).show();
	}

	public static void here() {
		IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " class="
				+ Thread.currentThread().getStackTrace()[2].getClassName() + " method="
				+ Thread.currentThread().getStackTrace()[2].getMethodName());

	}

	public static void here(String str) {
		IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + "line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " class="
				+ Thread.currentThread().getStackTrace()[2].getClassName() + " method="
				+ Thread.currentThread().getStackTrace()[2].getMethodName() + " " + str);
	}

	public static void trace(boolean trok) {
		if (trok)
			IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[2].getLineNumber());
	}

	public static void trace(String str, boolean trok) {

		if (trok)
			// TextWindow tw = new TextWindow("Sequenze", "<-- INIZIO Sequenze -->", 300,
			// 200);

			IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "    " + str);
	}

	public static void trace2() {
		IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber());
	}

	public static void trace2(String str) {
		IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "    " + str);
	}

	public static void traceStart() {
		IJ.log("");
		Frame lw = WindowManager.getFrame("Log");
		if (lw == null)
			return;
		lw.setSize(600, 1000);
		lw.setLocation(10, 10);

	}

	public static void logArrayList(ArrayList<Double> arrList, String title) {
		if (arrList == null) {
			IJ.log("Warning vector " + title + " = null");
		} else {
			IJ.log("----------- " + title + "  [ " + arrList.size() + " ] -----------");
			String logRiga = "";
			for (int j1 = 0; j1 < arrList.size(); j1++) {
				logRiga += arrList.get(j1) + ",  ";
			}
			IJ.log(logRiga);
		}
	}

	public static void logArrayList(ArrayList<String> arrList) {
		if (arrList == null) {
			IJ.log("Warning vector = null");
		} else {
			IJ.log("----------- [ " + arrList.size() + " ] -----------");
			String logRiga = "";
			for (int j1 = 0; j1 < arrList.size(); j1++) {
				logRiga += arrList.get(j1) + ",  ";
			}
			IJ.log(logRiga);
		}
	}

	public static void logArrayListInteger(ArrayList<Integer> arrList, String title) {
		if (arrList == null) {
			IJ.log("Warning vector " + title + " = null");
		} else {
			IJ.log("----------- " + title + "  [ " + arrList.size() + " ] -----------");
			String logRiga = "";
			for (int j1 = 0; j1 < arrList.size(); j1++) {
				logRiga += arrList.get(j1) + ",  ";
			}
			IJ.log(logRiga);
		}
	}

	public static void logArrayListInteger(List<int[]> tmp, String title) {
		if (tmp == null) {
			IJ.log("Warning vector " + title + " = null");
		} else {
			IJ.log("----------- " + title + "  [ " + tmp.size() + " ] -----------");
			String logRiga = "";
			for (int j1 = 0; j1 < tmp.size(); j1++) {
				logRiga += tmp.get(j1) + ",  ";
			}
			IJ.log(logRiga);
		}
	}

	public static void logArrayListTable(ArrayList<ArrayList<Double>> matrixTable, String title) {
		// ArrayList<Double> row1 = new ArrayList<Double>();
		if (matrixTable == null) {
			IJ.log("fromArrayListToStringTable.matrixTable == null");
			return;
		}
		IJ.log("####### " + title + "[" + matrixTable.get(0).size() + " x " + matrixTable.size() + "] ########");
		// ArrayList<String> riga = matrixTable.get(0);
		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
			ArrayList<Double> arrayList = matrixTable.get(i1);
			String logRiga = "";
			for (int j1 = 0; j1 < matrixTable.get(i1).size(); j1++) {
				logRiga += arrayList.get(j1) + ",  ";
			}
			IJ.log(logRiga);
		}
		return;
	}

	public static void logArrayListTable3(ArrayList<ArrayList<Integer>> matrixTable, String title) {
		// ArrayList<Double> row1 = new ArrayList<Double>();
		if (matrixTable == null) {
			IJ.log("fromArrayListToStringTable.matrixTable == null");
			return;
		}
		IJ.log("####### " + title + "[" + matrixTable.get(0).size() + " x " + matrixTable.size() + "] ########");
		// ArrayList<String> riga = matrixTable.get(0);
		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
			ArrayList<Integer> arrayList = matrixTable.get(i1);
			String logRiga = "";
			for (int j1 = 0; j1 < matrixTable.get(i1).size(); j1++) {
				logRiga += arrayList.get(j1) + ",  ";
			}
			IJ.log(logRiga);
		}
		return;
	}

	public static void logArrayListTable4(ArrayList<ArrayList<Integer>> matrixTable, String title) {
		// ArrayList<Double> row1 = new ArrayList<Double>();
		if (matrixTable == null) {
			IJ.log("fromArrayListToStringTable.matrixTable == null");
			return;
		}

		for (ArrayList obj : matrixTable) {
			ArrayList<Integer> temp = obj;
			String aa = "";
			for (Integer num : temp) {
				aa = aa + (num + " , ");
			}
			IJ.log("" + aa);
		}

		return;
	}

	public static void logArrayListTable2(ArrayList<ArrayList<String>> matrixTable, String title) {
		// ArrayList<Double> row1 = new ArrayList<Double>();
		if (matrixTable == null) {
			IJ.log("logArrayListTable == null");
			return;
		}
		IJ.log("####### " + title + "[" + matrixTable.get(0).size() + " x " + matrixTable.size() + "] ########");
		// ArrayList<String> riga = matrixTable.get(0);
		for (int i1 = 0; i1 < matrixTable.size(); i1++) {
			ArrayList<String> arrayList = matrixTable.get(i1);
			String logRiga = "";
			for (int j1 = 0; j1 < matrixTable.get(0).size(); j1++) {
				logRiga += arrayList.get(j1) + ",  ";
			}
			IJ.log(logRiga);
		}
		return;
	}

	public static void logArrayListVertical(ArrayList<String> arrList) {
		if (arrList == null) {
			IJ.log("Warning vector = null");
		} else {
			IJ.log("----------- [ " + arrList.size() + " ] -----------");
			for (int j1 = 0; j1 < arrList.size(); j1++) {
				IJ.log(arrList.get(j1));
			}
		}
	}

//	public static void logDebug(int riga, String prg, String fileDir) {
//		String[][] table = new TableSequence().loadTable(fileDir + MyConst.SEQUENZE_FILE);
//		String tableRiga = TableSequence.getRow(table, riga);
//		String tablePath = TableSequence.getPath(table, riga);
//		File f1 = new File(tablePath);
//		String tableCode = TableSequence.getCode(table, riga);
//		String tableCoil = TableSequence.getCoil(table, riga);
//		ImagePlus imp1 = UtilAyv.openImageNoDisplay(tablePath, true);
//		String aux1 = ReadDicom.readDicomParameter(imp1, MyConst.DICOM_SERIES_DESCRIPTION);
//		String imaCode = "";
//		if (aux1.length() >= 5)
//			imaCode = aux1.substring(0, 5).trim();
//		else
//			imaCode = aux1.substring(0, aux1.length()).trim();
//		String imaCoil = ReadDicom.getAllCoils(imp1);
//		MyLog.appendLog(fileDir + "MyLog.txt",
//				prg + " > logDebug name= " + f1.getName() + "  tableRiga= " + tableRiga + " tableCode= " + tableCode
//						+ " imaCode= " + imaCode + " tableCoil=" + tableCoil + " imaCoil= " + imaCoil);
//	}

	public static void logMatrix(double mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			ACRlog.waitThere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				ACRlog.waitThere("Warning matrix " + nome + " length=0");
				return;
			}
			columns = mat[0].length;
			// MyLog.waitThere("rows=" + rows + " columns= " + columns);

			IJ.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {
					stri += mat[i1][i2] + ",  ";
				}
				IJ.log(stri);
			}
		}
		IJ.log("---------------------------------------------");
	}

	public static void logMatrix(float mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			ACRlog.waitThere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				ACRlog.waitThere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// IJ.log("rows=" + rows + " columns= " + columns);

			IJ.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {
					stri += mat[i1][i2] + ",  ";
				}
				IJ.log(stri);
			}
		}
		IJ.log("---------------------------------------------");
	}

	public static void logMatrix(int mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			ACRlog.waitThere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				ACRlog.waitThere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// IJ.log("rows=" + rows + " columns= " + columns);

			IJ.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {
					stri += mat[i1][i2] + ",  ";
				}
				IJ.log(stri);
			}
		}
		IJ.log("---------------------------------------------");
	}

	public static void printMatrix(int mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			ACRlog.waitThere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				ACRlog.waitThere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// IJ.log("rows=" + rows + " columns= " + columns);

			IJ.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {

					stri += String.format("%04d", mat[i1][i2]) + " ,  ";
				}
				IJ.log(stri);
			}
		}
		IJ.log("---------------------------------------------");
	}

	public static void logMatrix(String mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			ACRlog.waitThere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				ACRlog.waitThere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// IJ.log("rows=" + rows + " columns= " + columns);

			IJ.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < rows; i1++) {
				stri = "";
				for (int i2 = 0; i2 < columns; i2++) {
					stri += mat[i1][i2] + ",  ";
				}
				IJ.log(stri);
			}
		}
		IJ.log("---------------------------------------------");
	}

	public static void logMatrixDimensions(double[][] mat1, String nome) {
		IJ.log("matrice " + nome + " [ " + mat1.length + " ] x [ " + mat1[0].length + " ]");
	}

	public static void logMatrixDimensions(double[][][] mat1, String nome) {
		IJ.log("matrice " + nome + " [ " + mat1.length + " ] x [ " + mat1[0].length + " ] x [ " + mat1[0][0].length
				+ " ]");
	}

	public static void logMatrixDimensions(float[][] mat1, String nome) {
		IJ.log("matrice " + nome + " [ " + mat1.length + " ] x [ " + mat1[0].length + " ]");
	}

	public static void logMatrixDimensions(float[][][] mat1, String nome) {
		IJ.log("matrice " + nome + " [ " + mat1.length + " ] x [ " + mat1[0].length + " ] x [ " + mat1[0][0].length
				+ " ]");
	}

	public static void logMatrixDimensions(int[][] mat1, String nome) {
		IJ.log("matrice " + nome + " [ " + mat1.length + " ] x [ " + mat1[0].length + " ]");
	}

	public static void logMatrixDimensions(int[][][] mat1, String nome) {
		IJ.log("matrice " + nome + " [ " + mat1.length + " ] x [ " + mat1[0].length + " ] x [ " + mat1[0][0].length
				+ " ]");
	}

	public static void logMatrixDimensions(String[][] mat1, String nome) {
		IJ.log("matrice " + nome + " [ " + mat1.length + " ] x [ " + mat1[0].length + " ]");
	}

	public static void logMatrixDimensions(String[][][] mat1, String nome) {
		IJ.log("matrice " + nome + " [ " + mat1.length + " ] x [ " + mat1[0].length + " ] x [ " + mat1[0][0].length
				+ " ]");
	}

	public static void logMatrixVertical(double mat[][], String nome) {
		String stri = "";
		int rows = 0;
		int columns = 0;
		if (mat == null) {
			ACRlog.waitThere("Warning matrix " + nome + " = null");
			return;
		} else {
			rows = mat.length;
			if (rows == 0) {
				ACRlog.waitThere("Warning matrix " + nome + " length=0");
				return;
			}

			columns = mat[0].length;
			// IJ.log("rows=" + rows + " columns= " + columns);

			IJ.log("---- " + nome + " [ " + rows + "x" + columns + " ] ----");
			for (int i1 = 0; i1 < columns; i1++) {
				stri = "";
				for (int i2 = 0; i2 < rows; i2++) {
					stri += mat[i2][i1] + ",  ";
				}
				IJ.log(stri);
			}
		}
		IJ.log("---------------------------------------------");
	}

	public static void logVector(boolean vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {
			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				if (vect[i1])
					stri = stri + "T, ";
				else
					stri = stri + "F, ";
			}
			IJ.log(stri);
		}
		IJ.log("---------------------------------------------");
	}

	public static void logVector(byte vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {

			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");
			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			IJ.log(stri);
			IJ.log("---------------------------------------------");
		}
	}

	public static void logVector(double vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {
			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			IJ.log(stri);
		}
		IJ.log("---------------------------------------------");
	}

	public static void logVector(float vect[], String nome) {
		String stri = "";
		int init;
		int end;
		int remain;

		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {

			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");
			if (vect.length <= 255) {
				for (int i1 = 0; i1 < vect.length; i1++) {
					stri = stri + vect[i1] + ",  ";
				}
				IJ.log(stri);

			} else {
				init = 0;
				end = 255;
				remain = vect.length;
				while (remain > 255) {
					for (int i1 = init; i1 < end; i1++) {
						stri = stri + vect[i1] + ",  ";
					}
					IJ.log(stri);
					init = end + 1;
					end = end + 255;
					remain = vect.length - end;
				}
			}
			IJ.log("---------------------------------------------");
		}
	}

	public static void logVector(int vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {

			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");
			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			IJ.log(stri);
			IJ.log("---------------------------------------------");
		}
	}

	public static void logVector(short vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {

			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");
			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			IJ.log(stri);
			IJ.log("---------------------------------------------");
		}
	}

	public static void logVector(String vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {
			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + ",  ";
			}
			IJ.log(stri);
		}
		IJ.log("---------------------------------------------");
	}

	public static void logVectorVertical(double vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {
			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + "\n";
			}
			IJ.log(stri);
		}
		IJ.log("---------------------------------------------");
	}

	public static void logVectorVertical(int vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {
			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + "\n";
			}
			IJ.log(stri);
		}
		IJ.log("---------------------------------------------");
	}

	public static void logVectorVertical(short vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {
			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + vect[i1] + "\n";
			}
			IJ.log(stri);
		}
		IJ.log("---------------------------------------------");
	}

	public static void logVectorVertical(String vect[], String nome) {
		String stri = "";
		if (vect == null) {
			IJ.log("Warning vector " + nome + " = null");
		} else {
			IJ.log("----------- " + nome + "  [ " + vect.length + " ] -----------");

			for (int i1 = 0; i1 < vect.length; i1++) {
				stri = stri + i1 + "  " + vect[i1] + "\n";
			}
			IJ.log(stri);
		}
		IJ.log("---------------------------------------------");
	}

	public static void mark(String str) {
		IJ.log("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " ===> " + str);
	}

	public static String qui() {
		String out = ("<" + Thread.currentThread().getStackTrace()[2].getClassName() + "."
				+ Thread.currentThread().getStackTrace()[2].getMethodName()) + " line= "
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + ">  ";
		return out;
	}

	public static String quiOld() {
		String out = ("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + " class="
				+ Thread.currentThread().getStackTrace()[2].getClassName() + " method="
				+ Thread.currentThread().getStackTrace()[2].getMethodName());
		return out;
	}

	public static void waitThere(String str) {
		new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n" + "file="
				+ Thread.currentThread().getStackTrace()[3].getFileName() + " " + " line="
				+ Thread.currentThread().getStackTrace()[3].getLineNumber() + "\n \n" + str).show();
	}

	public static void waitThere(String str, boolean debug) {
		if (debug) {
			IJ.beep();

			new WaitForUserDialog("file=" + Thread.currentThread().getStackTrace()[2].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[2].getLineNumber() + "\n" + "file="
					+ Thread.currentThread().getStackTrace()[3].getFileName() + " " + " line="
					+ Thread.currentThread().getStackTrace()[3].getLineNumber() + "\n \n" + str).show();
		} else {
			IJ.beep();
			new WaitForUserDialog(str).show();
		}
	}

}
