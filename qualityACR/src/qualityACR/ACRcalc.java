package qualityACR;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ij.IJ;

public class ACRcalc {

	
	/**
	 * Calcola il max di un vettore
	 * 
	 * @param in1
	 * @return
	 */
	public static double[] vetSmooth3x3(double[] in1) {
		double[] out1=new double[in1.length];
		for (int i1 = 1; i1 < in1.length-1; i1++) {
			out1[i1]= (in1[i1-1]+in1[i1]+in1[i1+1])/3;	
		}
		return out1;
	}
	
	/**
	 * Calcola il max di un vettore
	 * 
	 * @param data
	 * @return
	 */
	public static double vetMax(double[] data) {
		final int n = data.length;
		if (n < 1) {
			return Double.NaN;
		}
		double max = Double.MIN_VALUE;
		for (int i1 = 0; i1 < data.length; i1++) {
			if (data[i1] > max) {
				max = data[i1];
			}
		}
		return max;
	}

	public static float vetMax(float[] data) {
		final int n = data.length;
		if (n < 1) {
			return Float.NaN;
		}
		float max = Float.MIN_VALUE;
		for (int i1 = 0; i1 < data.length; i1++) {
			if (data[i1] > max) {
				max = data[i1];
			}
		}
		// MyLog.waitHere("max= "+max);
		return max;
	}

	public static int vetMax(int[] data) {
		final int n = data.length;
		if (n < 1) {
			return Integer.MIN_VALUE;
		}
		int max = Integer.MIN_VALUE;
		for (int i1 = 0; i1 < data.length; i1++) {
			if (data[i1] > max) {
				max = data[i1];
			}
		}
		return max;
	}

	public static short vetMax(short[] data) {
		final int n = data.length;
		if (n < 1) {
			return Short.MIN_VALUE;
		}
		short max = Short.MIN_VALUE;
		for (int i1 = 0; i1 < data.length; i1++) {
			if (data[i1] > max) {
				max = data[i1];
			}
		}
		return max;
	}

	public static double vetMean(byte[] data) {
		final int n = data.length;
		if (n < 1) {
			return Double.NaN;
		}
		double sum = 0;
		for (int i1 = 0; i1 < data.length; i1++) {
			sum += data[i1];
		}
		double mean = sum / data.length;
		return mean;
	}

	/**
	 * Calcola la media di un vettore
	 * 
	 * @param data
	 * @return
	 */

	public static double vetMean(double[] data) {
		final int n = data.length;
		if (n < 1) {
			return Double.NaN;
		}
		double sum = 0;
		for (int i1 = 0; i1 < data.length; i1++) {
			sum += data[i1];
		}
		double mean = sum / data.length;
		return mean;
	}

	public static double vetMean(float[] data) {
		final int n = data.length;
		if (n < 1) {
			return Float.NaN;
		}
		double sum = 0;
		for (int i1 = 0; i1 < data.length; i1++) {
			sum += data[i1];
		}
		double mean = sum / data.length;
		return mean;
	}

	public static double vetMean(int[] data) {
		final int n = data.length;
		if (n < 1) {
			return Double.NaN;
		}
		double sum = 0;
		for (int i1 = 0; i1 < data.length; i1++) {
			sum += data[i1];
		}
		double mean = sum / data.length;
		return mean;
	}

	public static double vetMean(short[] data) {
		final int n = data.length;
		if (n < 1) {
			return Double.NaN;
		}
		double sum = 0;
		for (int i1 = 0; i1 < data.length; i1++) {
			sum += data[i1];
		}
		double mean = sum / data.length;
		return mean;
	}

	public static double vetMedian(double[] data) {

		double[] sorted = ACRcalc.vetSort(data);
		double median = sorted[sorted.length / 2];
		return median;
	}

	public static int vetMedian(int[] data) {

		int[] sorted = ACRcalc.vetSort(data);
		int median = sorted[sorted.length / 2];
		return median;
	}

	/**
	 * Calcola il min di un vettore
	 * 
	 * @param data
	 * @return
	 */
	public static double vetMin(double[] data) {
		final int n = data.length;
		if (n < 1) {
			return Double.NaN;
		}
		double min = Double.MAX_VALUE;
		for (int i1 = 0; i1 < data.length; i1++) {
			if (data[i1] < min) {
				min = data[i1];
			}
		}
		return min;
	}

	public static float vetMin(float[] data) {
		final int n = data.length;
		if (n < 1) {
			return Float.NaN;
		}
		float min = Float.MAX_VALUE;
		for (int i1 = 0; i1 < data.length; i1++) {
			if (data[i1] < min) {
				min = data[i1];
			}
		}
		return min;
	}

	public static int vetMin(int[] data) {
		final int n = data.length;
		if (n < 1) {
			return Integer.MAX_VALUE;
		}
		int min = Integer.MAX_VALUE;
		for (int i1 = 0; i1 < data.length; i1++) {
			if (data[i1] < min) {
				min = data[i1];
			}
		}
		return min;
	}

	public static short vetMin(short[] data) {
		final int n = data.length;
		if (n < 1) {
			return Short.MAX_VALUE;
		}
		short min = Short.MAX_VALUE;
		for (int i1 = 0; i1 < data.length; i1++) {
			if (data[i1] < min) {
				min = data[i1];
			}
		}
		return min;
	}

	public static double[] vetSort(double[] source) {
		double[] sorted = new double[source.length];
		for (int i1 = 0; i1 < source.length; i1++) {
			sorted[i1] = source[i1];
		}
		// effettuo minsort su key, gli altri campi andranno in parallelo
		double aux1 = 0;
		for (int i1 = 0; i1 < sorted.length; i1++) {
			for (int i2 = i1 + 1; i2 < sorted.length; i2++) {
				if (sorted[i2] < sorted[i1]) {
					aux1 = sorted[i1];
					sorted[i1] = sorted[i2];
					sorted[i2] = aux1;
				}
			}
		}
		return sorted;
	}

	public static int[] vetSort(int[] source) {
		int[] sorted = new int[source.length];
		for (int i1 = 0; i1 < source.length; i1++) {
			sorted[i1] = source[i1];
		}
		// effettuo minsort su key, gli altri campi andranno in parallelo
		int aux1 = 0;
		for (int i1 = 0; i1 < sorted.length; i1++) {
			for (int i2 = i1 + 1; i2 < sorted.length; i2++) {
				if (sorted[i2] < sorted[i1]) {
					aux1 = sorted[i1];
					sorted[i1] = sorted[i2];
					sorted[i2] = aux1;
				}
			}
		}
		return sorted;
	}

	public static boolean compareDoublesWithTolerance(double aa, double bb, double tolerance) {
		if (Double.compare(aa, bb) == 0)
			return true;
		return Math.abs(aa - bb) < tolerance;
	}

	public static boolean compareDoublesWithTolerance(double aa, double bb, int digits) {

		// IJ.log ("aa= "+aa+" bb="+bb+" digits=" +digits);

		double uno = roundDoubleDecimals(aa, digits);
		double due = roundDoubleDecimals(bb, digits);
		double tre = Math.abs(roundDoubleDecimals(aa, digits) - roundDoubleDecimals(bb, digits));
		// IJ.log ("uno= "+uno+" due="+due+" tre=" +tre);
		// MyLog.waitHere();

		return tre == 0;
	}

	public static double roundDoubleDecimals(double x1, int decimalPlaces) {
		BigDecimal bd = new BigDecimal(x1);
		bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_DOWN);
		return bd.doubleValue();
	}

	public static double[] arrayListToArrayDouble(List<Double> inArrayList) {

		double[] outIntArr = new double[inArrayList.size()];
		int i1 = 0;
		for (Double n : inArrayList) {
			outIntArr[i1++] = n;
		}
		return outIntArr;
	}

	public static double[] arrayListToArrayDouble2(List<Float> inArrayList) {

		double[] outIntArr = new double[inArrayList.size()];
		int i1 = 0;
		for (Float n : inArrayList) {
			outIntArr[i1++] = (double) n;
		}
		return outIntArr;
	}

	public static double[] arrayListToArrayDouble3(List<Integer> inArrayList) {

		double[] outIntArr = new double[inArrayList.size()];
		int i1 = 0;
		for (Integer n : inArrayList) {
			outIntArr[i1++] = n;
		}
		return outIntArr;
	}

	public static float[] arrayListToArrayFloat(List<Float> inArrayList) {

		float[] outIntArr = new float[inArrayList.size()];
		int i1 = 0;
		for (Float n : inArrayList) {
			outIntArr[i1++] = n;
		}
		return outIntArr;
	}

	/**
	 * Conversion from arrayList<Integer> to int[]
	 * 
	 * @param inArrayList arrayList input
	 * @return String[] output
	 */
	public static int[] arrayListToArrayInt(ArrayList<Integer> inArrayList) {
		int[] outIntArr = new int[inArrayList.size()];
		int i1 = 0;
		for (Integer n : inArrayList) {
			outIntArr[i1++] = n;
		}
		return outIntArr;
	}

	/**
	 * Conversion from arrayList<Integer> to int[]
	 * 
	 * @param inArrayList arrayList input
	 * @return String[] output
	 */
	public static int[] arrayListToArrayInt(List<Integer> inArrayList) {
		int[] outIntArr = new int[inArrayList.size()];
		int i1 = 0;
		for (Integer n : inArrayList) {
			outIntArr[i1++] = n;
		}
		return outIntArr;
	}

	public static short[] arrayListToArrayShort(List<Short> inArrayList) {
		short[] outIntArr = new short[inArrayList.size()];
		int i1 = 0;
		for (Short n : inArrayList) {
			outIntArr[i1++] = n;
		}
		return outIntArr;
	}

	/**
	 * Conversion from arrayList<String> to String[]
	 * 
	 * @param inArrayList arrayList input
	 * @return String[] output
	 */
	public static String[] arrayListToArrayString(List<String> inArrayList) {
		Object[] objArr = inArrayList.toArray();
		String[] outStrArr = new String[objArr.length];
		for (int i1 = 0; i1 < objArr.length; i1++) {
			outStrArr[i1] = objArr[i1].toString();
		}
		return outStrArr;
	}

	public static double vetSd(double[] data) {
		final int n = data.length;
		if (n < 2) {
			return Double.NaN;
		}
		double avg = data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n));
	}

	public static double vetSd(float[] data) {
		final int n = data.length;
		if (n < 2) {
			return Float.NaN;
		}
		double avg = (double) data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n));
	}

	public static double vetSd(int[] data) {
		final int n = data.length;
		if (n < 2) {
			return Double.NaN;
		}
		double avg = (double) data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n));
	}

	public static double vetSd(short[] data) {
		final int n = data.length;
		if (n < 2) {
			return Double.NaN;
		}
		double avg = (double) data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n));
	}

	/**
	 * Calculates the standard deviation of an array of numbers. see Knuth's The Art
	 * Of Computer Programming Volume II: Seminumerical Algorithms This algorithm is
	 * slower, but more resistant to error propagation.
	 * 
	 * @param data Numbers to compute the standard deviation of. Array must contain
	 *             two or more numbers.
	 * @return standard deviation estimate of population ( to get estimate of
	 *         sample, use n instead of n-1 in last line )
	 */
	public static double vetSdKnuth(double[] data) {
		final int n = data.length;
		if (n < 2) {
			return Double.NaN;
		}
		double avg = data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n - 1));
	}

	public static double vetSdKnuth(float[] data) {
		final int n = data.length;
		if (n < 2) {
			return Float.NaN;
		}
		double avg = (double) data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n - 1));
	}

	public static double vetSdKnuth(int[] data) {
		final int n = data.length;
		if (n < 2) {
			return Double.NaN;
		}
		double avg = (double) data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n - 1));
	}

	public static double vetSdKnuth(short[] data) {
		final int n = data.length;
		if (n < 2) {
			return Double.NaN;
		}
		double avg = (double) data[0];
		double sum = 0;
		// yes, i1 below starts from 1
		for (int i1 = 1; i1 < data.length; i1++) {
			double newavg = avg + (data[i1] - avg) / (i1 + 1);
			sum += (data[i1] - avg) * (data[i1] - newavg);
			avg = newavg;
		}
		return Math.sqrt(sum / (n - 1));
	}

	/**
	 * Rimozione duplicati (ancora da testare)
	 * 
	 * @param matrix
	 * @return
	 */
	public static String[][] removeDuplicate(String[][] matrix) {
		String[][] newMatrix = new String[matrix.length][matrix[0].length];
		int newMatrixRow = 1;

		for (int i = 0; i < matrix[0].length; i++)
			newMatrix[0][i] = matrix[0][i];

		for (int j = 1; j < matrix.length; j++) {
			List<Boolean> list = new ArrayList<>();
			for (int i = 0; newMatrix[i][0] != null; i++) {
				boolean same = true;
				for (int col = 2; col < matrix[j].length; col++) {
					if (!newMatrix[i][col].equals(matrix[j][col])) {
						same = false;
						break;
					}
				}
				list.add(same);
			}

			if (!list.contains(true)) {
				for (int i = 0; i < matrix[j].length; i++) {
					newMatrix[newMatrixRow][i] = matrix[j][i];
				}
				newMatrixRow++;
			}
		}

		int i1;
		for (i1 = 0; newMatrix[i1][0] != null; i1++)
			;

		String finalMatrix[][] = new String[i1][newMatrix[0].length];
		for (i1 = 0; i1 < finalMatrix.length; i1++) {
			for (int j = 0; j < finalMatrix[i1].length; j++)
				finalMatrix[i1][j] = newMatrix[i1][j];
		}

		return finalMatrix;
	}

	/**
	 * effettua l'ordinamento di un array a due dimensioni, secondo la chiave
	 * fornita (0 o 1). LO SO CHE JAVA8 FA TUTTO CON UNA ISTRUZIONE, MA IO IGNORO E
	 * ME NE F8!
	 * 
	 * @param tableIn matrice da ordinare
	 * @param key     chiave di ordinamento
	 * @return matrice ordinata
	 */
	public static double[][] minsort(double[][] tableIn, int key) {

		double[][] tableOut = new double[tableIn.length][tableIn[0].length];
		for (int i1 = 0; i1 < tableIn.length; i1++) {
			for (int i2 = 0; i2 < tableIn[0].length; i2++) {
				tableOut[i1][i2] = tableIn[i1][i2];
			}
		}
		//
		// per mia lazzaronaggine creo un array con i valori di key ed inoltre un array
		// indice
		//
		double[] vetKey = new double[tableOut[0].length];
		for (int i1 = 0; i1 < tableOut[0].length; i1++) {
			vetKey[i1] = tableOut[key][i1];
		}
		int[] vetIndex = new int[tableOut[0].length];
		for (int i1 = 0; i1 < tableOut[0].length; i1++) {
			vetIndex[i1] = i1;
		}
		//
		// lo battezzo algoritmo di Tone&Batista
		//
		double aux1 = 0;
		int aux2 = 0;
		for (int i1 = 0; i1 < vetKey.length; i1++) {
			for (int i2 = i1 + 1; i2 < vetKey.length; i2++) {
				if (vetKey[i2] < vetKey[i1]) {
					aux1 = vetKey[i1];
					vetKey[i1] = vetKey[i2];
					vetKey[i2] = aux1;
					// ----
					aux2 = vetIndex[i1];
					vetIndex[i1] = vetIndex[i2];
					vetIndex[i2] = aux2;
				}
			}
		}

		// a questo punto usando il vetIndex di Tone&Batista, riordino tabella in un
		// unica passata
		for (int i1 = 0; i1 < tableOut[0].length; i1++) {
			for (int i2 = 0; i2 < vetIndex.length; i2++) {
				tableOut[i2][i1] = tableIn[vetIndex[i2]][i1];
			}
		}

		return tableOut;
	}

	/**
	 * minsort per matrice di interi
	 * 
	 * @param tableIn matrice da ordinare
	 * @param key     chiave ordinamento
	 * @return matrice ordinata
	 */
	public static int[][] minsort(int[][] tableIn, int key) {

		int[][] tableOut = new int[tableIn.length][tableIn[0].length];
		int[] vetKey = new int[tableIn[0].length];
		for (int i1 = 0; i1 < tableIn[0].length; i1++) {
			vetKey[i1] = tableIn[key][i1];
		}
		int[] vetIndex = new int[tableIn[0].length];
		for (int i1 = 0; i1 < tableIn[0].length; i1++) {
			vetIndex[i1] = i1;
		}
		int aux1 = 0;
		int aux2 = 0;
		for (int i1 = 0; i1 < vetKey.length; i1++) {
			for (int i2 = i1 + 1; i2 < vetKey.length; i2++) {
				if (vetKey[i2] < vetKey[i1]) {
					aux1 = vetKey[i1];
					vetKey[i1] = vetKey[i2];
					vetKey[i2] = aux1;
					// ----
					aux2 = vetIndex[i1];
					vetIndex[i1] = vetIndex[i2];
					vetIndex[i2] = aux2;
				}
			}
		}
		int aux3 = 0;
		// a questo punto usando il vetIndex di Tone&Batista, riordino tabella in un
		// unica passata
		for (int i1 = 0; i1 < tableOut.length; i1++) {
			for (int i2 = 0; i2 < tableOut[0].length; i2++) {
				// aux3 = tableIn[i1][vetIndex[i2]];
				tableOut[i1][i2] = tableIn[i1][vetIndex[i2]];
			}
		}

		return tableOut;
	}

	/**
	 * Rimozione dei duplicati all'interno di una matrice 2D, ricorre all'uso di
	 * arrayList 2D
	 * 
	 * @param matin matrice di interi
	 * @return matrice di interi coi duplicati rimossi
	 */
	public static int[][] removeDuplicate(int[][] matin) {

		ArrayList<ArrayList<Integer>> pippo = new ArrayList<>();

		boolean dup = false;
		int key = 0; // chiave per il sort della matrice
//		IJ.log(ACRlog.qui());
//		ACRlog.logMatrix(matin, ACRlog.qui()+"matin");
		int[][] matsort = minsort(matin, key);
		int dim1 = matsort.length;
		int dim2 = matsort[0].length;
//		IJ.log(ACRlog.qui() + " dim1= " + dim1 + " dim2= " + dim2);

		int count = 0;
		int aux1 = 0;
		int[] vetprec = new int[matsort.length];

//		ACRlog.logMatrix(matsort, ACRlog.qui() + "matsort");
		for (int i2 = 0; i2 < matsort[0].length; i2++) {
			dup = true;
			for (int i1 = 0; i1 < matsort.length; i1++) {
				if (matsort[i1][i2] != vetprec[i1]) {
					dup = false;
				}
			}
			if (!dup) {
				count++;
				for (int i1 = 0; i1 < matsort.length; i1++) {
					ArrayList<Integer> tempList = new ArrayList<>();
					aux1 = matsort[i1][i2];
					tempList.add(aux1);
					vetprec[i1] = aux1;
					if (i2 == 0) {
						pippo.add(tempList);
					} else {
						pippo.get(i1).add(aux1);
					}
				}
			}
		}

//		ACRlog.waitHere("count= " + count);
//
//		ACRlog.logArrayListTable4(pippo, ACRlog.qui() + "pippo");
//		ACRlog.waitHere();

		int[][] pluto = ACRcalc.convert2Darraylist(pippo);
		return pluto;
	}


	public static int[][] convert2Darraylist(ArrayList<ArrayList<Integer>> pippo) {
		int dim1 = pippo.size();
		int dim2 = pippo.get(0).size();
//		IJ.log(ACRlog.qui() + "dim1= " + dim1 + " dim2= " + dim2);

		int[][] matOut = new int[dim1][dim2];
		for (int i1 = 0; i1 < dim1; i1++) {
			ArrayList<Integer> temparray = pippo.get(i1);
			int[] vettemp = ACRcalc.arrayListToArrayInt(temparray);
			matOut[i1] = vettemp;
		}
		return matOut;
	}
}
