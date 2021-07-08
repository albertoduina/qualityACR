package qualityACR;

import java.math.BigDecimal;
import java.util.List;

public class ACRcalc {

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
	//		MyLog.waitHere("max= "+max);
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

}
