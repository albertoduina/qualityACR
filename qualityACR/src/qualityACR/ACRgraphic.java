package qualityACR;

import java.awt.Color;
import java.awt.Polygon;
import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.io.Opener;
import ij.measure.Measurements;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.Tools;

public class ACRgraphic {

	/***
	 * Liang-Barsky function by Daniel White
	 * http://www.skytopia.com/project/articles/compsci/clipping.html .This function
	 * inputs 8 numbers, and outputs 4 new numbers (plus a boolean value to say
	 * whether the clipped line is drawn at all). //
	 * 
	 * @param edgeLeft   lato sinistro, coordinata minima x = 0
	 * @param edgeRight  lato destro, coordinata max x = width
	 * @param edgeBottom lato inferiore, coordinata max y = height
	 * @param edgeTop    lato superiore, coordinata minima y = 0
	 * @param x0src      punto iniziale segmento
	 * @param y0src      punto iniziale segmento
	 * @param x1src      punto finale segmento
	 * @param y1src      punto finale segmento
	 * @return
	 */
	public static double[] liangBarsky(double edgeLeft, double edgeRight, double edgeBottom, double edgeTop,
			double x0src, double y0src, double x1src, double y1src) {

		double t0 = 0.0;
		double t1 = 1.0;
		double xdelta = x1src - x0src;
		double ydelta = y1src - y0src;
		double p = 0;
		double q = 0;
		double r = 0;
		double[] clips = new double[4];

		for (int edge = 0; edge < 4; edge++) { // Traverse through left, right,
												// bottom, top edges.
			if (edge == 0) {
				p = -xdelta;
				q = -(edgeLeft - x0src);
			}
			if (edge == 1) {
				p = xdelta;
				q = (edgeRight - x0src);
			}
			if (edge == 2) {
				p = -ydelta;
				q = -(edgeBottom - y0src);
			}
			if (edge == 3) {
				p = ydelta;
				q = (edgeTop - y0src);
			}
			r = q / p;
			if (p == 0 && q < 0) {
				IJ.log("null 001");
				return null; // Don't draw line at all. (parallel line outside)
			}
			if (p < 0) {
				if (r > t1) {
					IJ.log("null 002");
					return null; // Don't draw line at all.
				} else if (r > t0)
					t0 = r; // Line is clipped!
			} else if (p > 0) {
				if (r < t0) {
					IJ.log("null 003");
					return null; // Don't draw line at all.
				} else if (r < t1)
					t1 = r; // Line is clipped!
			}
		}

		double x0clip = x0src + t0 * xdelta;
		double y0clip = y0src + t0 * ydelta;
		double x1clip = x0src + t1 * xdelta;
		double y1clip = y0src + t1 * ydelta;

		clips[0] = x0clip;
		clips[1] = y0clip;
		clips[2] = x1clip;
		clips[3] = y1clip;

		return clips;
	}

	/**
	 * Disegna una serie di punti nell'overlay di una immagine
	 * 
	 * @param imp1
	 * @param over1
	 * @param peaks1
	 */
	public static void plotPoints(ImagePlus imp1, Overlay over1, double[][] peaks1) {

		float[] xPoints = new float[peaks1[0].length];
		float[] yPoints = new float[peaks1[0].length];

		for (int i1 = 0; i1 < peaks1[0].length; i1++) {
			xPoints[i1] = (float) peaks1[0][i1];
			yPoints[i1] = (float) peaks1[1][i1];
		}

		// MyLog.logVector(xPoints, "xPoints");
		// MyLog.logVector(yPoints, "yPoints");
		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(2);
		pr1.setSize(4);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1) {

		float[] xPoints = new float[1];
		float[] yPoints = new float[1];

		xPoints[0] = (float) xPoints1;
		yPoints[0] = (float) yPoints1;
		// MyLog.logVector(xPoints, "xPoints");
		// MyLog.logVector(yPoints, "yPoints");
		// MyLog.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(2);
		pr1.setSize(4);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());
		return;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1, Color color) {

		float[] xPoints = new float[1];
		float[] yPoints = new float[1];

		xPoints[0] = (float) xPoints1;
		yPoints[0] = (float) yPoints1;
		// MyLog.logVector(xPoints1, "xPoints1");
		// MyLog.logVector(yPoints1, "yPoints1");
		// MyLog.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(2);
		pr1.setSize(4);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(color);
		over1.addElement(imp1.getRoi());
		return;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int xPoints1, int yPoints1, Color color,
			double width) {

		float[] xPoints = new float[1];
		float[] yPoints = new float[1];

		xPoints[0] = (float) xPoints1;
		yPoints[0] = (float) yPoints1;
		// MyLog.logVector(xPoints, "xPoints");
		// MyLog.logVector(yPoints, "yPoints");
		// MyLog.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(2);
		pr1.setSize(2);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(color);
		imp1.getRoi().setStrokeWidth(width);
		over1.addElement(imp1.getRoi());
		return;
	}

	public static void plotPoints(ImagePlus imp1, Overlay over1, int[] xPoints1, int[] yPoints1) {

		if (xPoints1 == null)
			return;

		ACRlog.logVector(xPoints1, "xPoints1");
		ACRlog.logVector(yPoints1, "yPoints1");

		float[] xPoints = new float[xPoints1.length];
		float[] yPoints = new float[yPoints1.length];

		for (int i1 = 0; i1 < xPoints1.length; i1++) {
			xPoints[i1] = (float) xPoints1[i1];
			yPoints[i1] = (float) yPoints1[i1];
		}
		ACRlog.logVector(xPoints, "xPoints");
		ACRlog.logVector(yPoints, "yPoints");
		ACRlog.waitHere();

		PointRoi pr1 = new PointRoi(xPoints, yPoints, xPoints.length);
		pr1.setPointType(2);
		pr1.setSize(4);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(Color.green);
		over1.addElement(imp1.getRoi());
		return;
	}

	public static void addOverlayRoi(ImagePlus imp1, Color color, double width) {
		Roi roi1 = imp1.getRoi();
		roi1.setStrokeColor(color);
		roi1.setStrokeWidth(width);
		Overlay over1 = imp1.getOverlay();
		if (over1 == null) {
			ACRlog.waitThere("overlay null!!");
			return;
		}
		over1.add(roi1);
	}

	public static void drawCenter(ImagePlus imp1, Overlay over1, double xCenterCircle, double yCenterCircle,
			Color color1) {
		// imp1.setOverlay(over1);
		imp1.setRoi(new OvalRoi(xCenterCircle - 2, yCenterCircle - 2, 4, 4));
		Roi roi1 = imp1.getRoi();
		roi1.setFillColor(color1);
		roi1.setStrokeColor(color1);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
	}

	/**
	 * Imposta una Roi circolare diametro 4 in corrispondenza delle coordinate
	 * passate, importa la Roi nell'Overlay. La routine e' utilizzata per disegnare
	 * il centro di un cerchio su di un overlay.
	 * 
	 * @param imp1
	 * @param over1
	 * @param xCenterCircle
	 * @param yCenterCircle
	 * @param color1
	 */
	public static void drawCenter(ImagePlus imp1, Overlay over1, int xCenterCircle, int yCenterCircle, Color color1) {
		// imp1.setOverlay(over1);
		imp1.setRoi(new OvalRoi(xCenterCircle - 2, yCenterCircle - 2, 4, 4));
		Roi roi1 = imp1.getRoi();
		roi1.setFillColor(color1);
		roi1.setStrokeColor(color1);
		over1.addElement(imp1.getRoi());
		imp1.killRoi();
	}

	/***
	 * Questo e' il fitCircle preso da ImageJ (ij.plugins.Selection.java, con
	 * sostituito imp.setRoi a IJ.makeOval
	 * 
	 * if selection is closed shape, create a circle with the same area and
	 * centroid, otherwise use<br>
	 * the Pratt method to fit a circle to the points that define the line or
	 * multi-point selection.<br>
	 * Reference: Pratt V., Direct least-squares fitting of algebraic surfaces",
	 * Computer Graphics, Vol. 21, pages 145-152 (1987).<br>
	 * Original code: Nikolai Chernov's MATLAB script for Newton-based Pratt
	 * fit.<br>
	 * (http://www.math.uab.edu/~chernov/cl/MATLABcircle.html)<br>
	 * Java version:
	 * https://github.com/mdoube/BoneJ/blob/master/src/org/doube/geometry
	 * /FitCircle.java<br>
	 * 
	 * authors: Nikolai Chernov, Michael Doube, Ved Sharma
	 */
	public static void fitCircle(ImagePlus imp) {
		Roi roi = imp.getRoi();

		if (roi == null) {
			IJ.error("Fit Circle", "Selection required");
			return;
		}

		if (roi.isArea()) { // create circle with the same area and centroid
			ImageProcessor ip = imp.getProcessor();
			ip.setRoi(roi);
			ImageStatistics stats = ImageStatistics.getStatistics(ip, Measurements.AREA + Measurements.CENTROID, null);
			double r = Math.sqrt(stats.pixelCount / Math.PI);
			imp.killRoi();
			int d = (int) Math.round(2.0 * r);
			imp.setRoi(new OvalRoi((int) Math.round(stats.xCentroid - r), (int) Math.round(stats.yCentroid - r), d, d));

			// IJ.makeOval((int) Math.round(stats.xCentroid - r),
			// (int) Math.round(stats.yCentroid - r), d, d);
			return;
		}

		Polygon poly = roi.getPolygon();
		int n = poly.npoints;
		int[] x = poly.xpoints;
		int[] y = poly.ypoints;
		if (n < 3) {
			IJ.error("Fit Circle", "At least 3 points are required to fit a circle.");
			return;
		}

		// calculate point centroid
		double sumx = 0, sumy = 0;
		for (int i = 0; i < n; i++) {
			sumx = sumx + poly.xpoints[i];
			sumy = sumy + poly.ypoints[i];
		}
		double meanx = sumx / n;
		double meany = sumy / n;

		// calculate moments
		double[] X = new double[n], Y = new double[n];
		double Mxx = 0, Myy = 0, Mxy = 0, Mxz = 0, Myz = 0, Mzz = 0;
		for (int i = 0; i < n; i++) {
			X[i] = x[i] - meanx;
			Y[i] = y[i] - meany;
			double Zi = X[i] * X[i] + Y[i] * Y[i];
			Mxy = Mxy + X[i] * Y[i];
			Mxx = Mxx + X[i] * X[i];
			Myy = Myy + Y[i] * Y[i];
			Mxz = Mxz + X[i] * Zi;
			Myz = Myz + Y[i] * Zi;
			Mzz = Mzz + Zi * Zi;
		}
		Mxx = Mxx / n;
		Myy = Myy / n;
		Mxy = Mxy / n;
		Mxz = Mxz / n;
		Myz = Myz / n;
		Mzz = Mzz / n;

		// calculate the coefficients of the characteristic polynomial
		double Mz = Mxx + Myy;
		double Cov_xy = Mxx * Myy - Mxy * Mxy;
		double Mxz2 = Mxz * Mxz;
		double Myz2 = Myz * Myz;
		double A2 = 4 * Cov_xy - 3 * Mz * Mz - Mzz;
		double A1 = Mzz * Mz + 4 * Cov_xy * Mz - Mxz2 - Myz2 - Mz * Mz * Mz;
		double A0 = Mxz2 * Myy + Myz2 * Mxx - Mzz * Cov_xy - 2 * Mxz * Myz * Mxy + Mz * Mz * Cov_xy;
		double A22 = A2 + A2;
		double epsilon = 1e-12;
		double ynew = 1e+20;
		int IterMax = 20;
		double xnew = 0;
		int iterations = 0;

		// Newton's method starting at x=0
		for (int iter = 1; iter <= IterMax; iter++) {
			iterations = iter;
			double yold = ynew;
			ynew = A0 + xnew * (A1 + xnew * (A2 + 4. * xnew * xnew));
			if (Math.abs(ynew) > Math.abs(yold)) {
				if (IJ.debugMode)
					IJ.log("Fit Circle: wrong direction: |ynew| > |yold|");
				xnew = 0;
				break;
			}
			double Dy = A1 + xnew * (A22 + 16 * xnew * xnew);
			double xold = xnew;
			xnew = xold - ynew / Dy;
			if (Math.abs((xnew - xold) / xnew) < epsilon) {
				// qui e'dove se le cose vanno bene, viene fatta terminare
				// l'iterazione
				break;
			}
			if (iter >= IterMax) {
				if (IJ.debugMode)
					IJ.log("Fit Circle: will not converge");
				xnew = 0;
			}
			if (xnew < 0) {
				if (IJ.debugMode)
					IJ.log("Fit Circle: negative root:  x = " + xnew);
				xnew = 0;
			}
		}
		if (IJ.debugMode)
			IJ.log("Fit Circle: n=" + n + ", xnew=" + IJ.d2s(xnew, 2) + ", iterations=" + iterations);

		// calculate the circle parameters
		double DET = xnew * xnew - xnew * Mz + Cov_xy;
		double CenterX = (Mxz * (Myy - xnew) - Myz * Mxy) / (2 * DET);
		double CenterY = (Myz * (Mxx - xnew) - Mxz * Mxy) / (2 * DET);
		double radius = Math.sqrt(CenterX * CenterX + CenterY * CenterY + Mz + 2 * xnew);
		if (Double.isNaN(radius)) {
			IJ.error("Fit Circle", "Points are collinear.");
			return;
		}

		CenterX = CenterX + meanx;
		CenterY = CenterY + meany;
		imp.deleteRoi();

		// messo imp.setRoi anziche' IJ.makeOval perche' permette di non
		// mostrare
		// l'immagine
		imp.setRoi(new OvalRoi((int) Math.round(CenterX - radius), (int) Math.round(CenterY - radius),
				(int) Math.round(2 * radius), (int) Math.round(2 * radius)));
	}

	/***
	 * Questo e' il fitCircle preso da ImageJ (ij.plugins.Selection.java, con
	 * sostituito imp.setRoi a IJ.makeOval. La versione NEW, oltre a impostare una
	 * ROI circolare sull'immagine, ne restituisce anche i valori numerici:
	 * coordinate del centro e raggio.
	 * 
	 * if selection is closed shape, create a circle with the same area and
	 * centroid, otherwise use<br>
	 * the Pratt method to fit a circle to the points that define the line or
	 * multi-point selection.<br>
	 * Reference: Pratt V., Direct least-squares fitting of algebraic surfaces",
	 * Computer Graphics, Vol. 21, pages 145-152 (1987).<br>
	 * Original code: Nikolai Chernov's MATLAB script for Newton-based Pratt
	 * fit.<br>
	 * (http://www.math.uab.edu/~chernov/cl/MATLABcircle.html)<br>
	 * Java version:
	 * https://github.com/mdoube/BoneJ/blob/master/src/org/doube/geometry
	 * /FitCircle.java<br>
	 * 
	 * authors: Nikolai Chernov, Michael Doube, Ved Sharma
	 */
	public static double[] fitCircleNew(ImagePlus imp) {
		Roi roi = imp.getRoi();

		if (roi == null) {
			IJ.error("Fit Circle", "Selection required");
			return null;
		}

		if (roi.isArea()) { // create circle with the same area and centroid
			ImageProcessor ip = imp.getProcessor();
			ip.setRoi(roi);
			ImageStatistics stats = ImageStatistics.getStatistics(ip, Measurements.AREA + Measurements.CENTROID, null);
			double r = Math.sqrt(stats.pixelCount / Math.PI);
			imp.killRoi();
			int d = (int) Math.round(2.0 * r);
			imp.setRoi(new OvalRoi((int) Math.round(stats.xCentroid - r), (int) Math.round(stats.yCentroid - r), d, d));

			// IJ.makeOval((int) Math.round(stats.xCentroid - r),
			// (int) Math.round(stats.yCentroid - r), d, d);
			return null;
		}

		Polygon poly = roi.getPolygon();
		int n = poly.npoints;
		int[] x = poly.xpoints;
		int[] y = poly.ypoints;
		if (n < 3) {
			IJ.error("Fit Circle", "At least 3 points are required to fit a circle.");
			return null;
		}

		// calculate point centroid
		double sumx = 0, sumy = 0;
		for (int i = 0; i < n; i++) {
			sumx = sumx + poly.xpoints[i];
			sumy = sumy + poly.ypoints[i];
		}
		double meanx = sumx / n;
		double meany = sumy / n;

		// calculate moments
		double[] X = new double[n], Y = new double[n];
		double Mxx = 0, Myy = 0, Mxy = 0, Mxz = 0, Myz = 0, Mzz = 0;
		for (int i = 0; i < n; i++) {
			X[i] = x[i] - meanx;
			Y[i] = y[i] - meany;
			double Zi = X[i] * X[i] + Y[i] * Y[i];
			Mxy = Mxy + X[i] * Y[i];
			Mxx = Mxx + X[i] * X[i];
			Myy = Myy + Y[i] * Y[i];
			Mxz = Mxz + X[i] * Zi;
			Myz = Myz + Y[i] * Zi;
			Mzz = Mzz + Zi * Zi;
		}
		Mxx = Mxx / n;
		Myy = Myy / n;
		Mxy = Mxy / n;
		Mxz = Mxz / n;
		Myz = Myz / n;
		Mzz = Mzz / n;

		// calculate the coefficients of the characteristic polynomial
		double Mz = Mxx + Myy;
		double Cov_xy = Mxx * Myy - Mxy * Mxy;
		double Mxz2 = Mxz * Mxz;
		double Myz2 = Myz * Myz;
		double A2 = 4 * Cov_xy - 3 * Mz * Mz - Mzz;
		double A1 = Mzz * Mz + 4 * Cov_xy * Mz - Mxz2 - Myz2 - Mz * Mz * Mz;
		double A0 = Mxz2 * Myy + Myz2 * Mxx - Mzz * Cov_xy - 2 * Mxz * Myz * Mxy + Mz * Mz * Cov_xy;
		double A22 = A2 + A2;
		double epsilon = 1e-12;
		double ynew = 1e+20;
		int IterMax = 20;
		double xnew = 0;
		int iterations = 0;

		// Newton's method starting at x=0
		for (int iter = 1; iter <= IterMax; iter++) {
			iterations = iter;
			double yold = ynew;
			ynew = A0 + xnew * (A1 + xnew * (A2 + 4. * xnew * xnew));
			if (Math.abs(ynew) > Math.abs(yold)) {
				if (IJ.debugMode)
					IJ.log("Fit Circle: wrong direction: |ynew| > |yold|");
				xnew = 0;
				break;
			}
			double Dy = A1 + xnew * (A22 + 16 * xnew * xnew);
			double xold = xnew;
			xnew = xold - ynew / Dy;
			if (Math.abs((xnew - xold) / xnew) < epsilon) {
				// qui e'dove se le cose vanno bene, viene fatta terminare
				// l'iterazione
				break;
			}
			if (iter >= IterMax) {
				if (IJ.debugMode)
					IJ.log("Fit Circle: will not converge");
				xnew = 0;
			}
			if (xnew < 0) {
				if (IJ.debugMode)
					IJ.log("Fit Circle: negative root:  x = " + xnew);
				xnew = 0;
			}
		}
		if (IJ.debugMode)
			IJ.log("Fit Circle: n=" + n + ", xnew=" + IJ.d2s(xnew, 2) + ", iterations=" + iterations);

		// calculate the circle parameters
		double DET = xnew * xnew - xnew * Mz + Cov_xy;
		double CenterX = (Mxz * (Myy - xnew) - Myz * Mxy) / (2 * DET);
		double CenterY = (Myz * (Mxx - xnew) - Mxz * Mxy) / (2 * DET);
		double radius = Math.sqrt(CenterX * CenterX + CenterY * CenterY + Mz + 2 * xnew);
		if (Double.isNaN(radius)) {
			IJ.error("Fit Circle", "Points are collinear.");
			return null;
		}

		CenterX = CenterX + meanx;
		CenterY = CenterY + meany;
		imp.deleteRoi();

		// messo imp.setRoi anziche' IJ.makeOval perche' permette di non
		// mostrare
		// l'immagine
		imp.setRoi(new OvalRoi((int) Math.round(CenterX - radius), (int) Math.round(CenterY - radius),
				(int) Math.round(2 * radius), (int) Math.round(2 * radius)));

		double[] out = new double[4];
		out[0] = CenterX;
		out[1] = CenterY;
		out[2] = radius;
		out[3] = DET;
		return out;
	}

	/**
	 * assegna un profilo su cui effettuare i calcoli
	 * 
	 * @param imp1 immagine su cui effettuare il profilo
	 * @param x1   coordinata x start profilo
	 * @param y1   coordinata y start profilo
	 * @param x2   coordinata x end profilo
	 * @param y2   coordinata y end profilo
	 * @return profilo
	 */
	public static double[] getLinePixels(ImagePlus imp1, int x1, int y1, int x2, int y2) {
		imp1.setRoi(new Line(x1, y1, x2, y2));
		imp1.updateAndDraw();
		Roi roi1 = imp1.getRoi();
		double[] profi1 = ((Line) roi1).getPixels();
		ACRlog.vetPrint(profi1, "profi1");
		ACRlog.waitHere("getLinePixels");
		return profi1;
	}

	/**
	 * assegna un profilo su cui effettuare i calcoli wideline
	 * 
	 * @param imp1  immagine su cui effettuare il profilo
	 * @param x1    coordinata x start profilo
	 * @param y1    coordinata y start profilo
	 * @param x2    coordinata x end profilo
	 * @param y2    coordinata y end profilo
	 * @param width spessore linea
	 * @return
	 */
	public static double[] getLinePixels(ImagePlus imp1, int x1, int y1, int x2, int y2, int width) {

		imp1.show();
		int oldWidth = Line.getWidth();
		Line.setWidth(1);
		imp1.setRoi(new Line(x1, y1, x2, y2));
		imp1.updateAndDraw();
		Roi roi1 = imp1.getRoi();
		double[] profi1 = ((Line) roi1).getPixels();
		Line.setWidth(oldWidth);
		ACRlog.vetPrint(profi1, "profi1");
		ACRlog.waitHere("getLinePixels");
		return profi1;

	}

	/**
	 * Restituisce le coordinate x, y e z dei punti appartenenti alla linea, in cui
	 * x ed y sono le coordinate del pixel (relative all'immagine di partenza) e z
	 * e' il valore di segnale
	 * 
	 * @param imp1
	 * @return
	 */
	public static double[][] lineDecomposer(ImagePlus imp1) {

		Line line = (Line) imp1.getRoi();
		if (line == null || !(line.isLine())) {
			IJ.error("Line selection required.");
			return null;
		}
		double[] profiZ = line.getPixels();
		FloatPolygon fp1 = line.getInterpolatedPolygon();
		int len = fp1.npoints - 1;
		double[] profiX = new double[len];
		double[] profiY = new double[len];
		for (int i1 = 0; i1 < len; i1++) {
			profiX[i1] = (double) fp1.xpoints[i1];
			profiY[i1] = (double) fp1.ypoints[i1];
		}
		double[][] out1 = new double[3][profiX.length];
		out1[0] = profiX;
		out1[1] = profiY;
		out1[2] = profiZ;
		return out1;
	}

	public static double[][] lineDecomposer3(ImagePlus imp1) {

		Line line = (Line) imp1.getRoi();
		if (line == null || !(line.isLine())) {
			IJ.error("Line selection required.");
			return null;
		}
		double[] profiZ = line.getPixels();
		IJ.run(imp1, "Interpolate", "interval=1.0");
		FloatPolygon fp1 = line.getFloatPolygon();
		int len = fp1.xpoints.length;
		double[] profiX = new double[len];
		double[] profiY = new double[len];
		for (int i1 = 0; i1 < len; i1++) {
			profiX[i1] = (double) fp1.xpoints[i1];
			profiY[i1] = (double) fp1.ypoints[i1];
		}
		if (profiX.length != profiZ.length) {
			IJ.log("different length, profi2x= " + profiX.length + " profi2z= " + profiZ.length);
		}
		double[][] out1 = new double[3][profiX.length];
		out1[0] = profiX;
		out1[1] = profiY;
		out1[2] = profiZ;
		return out1;
	}

	public static Plot basePlot(double[] profilex, double[] profiley, String title, Color color) {
		double[] a = Tools.getMinMax(profilex);
		double[] b = Tools.getMinMax(profiley);

		Plot plot = new Plot(title, "pixel", "valore", profilex, profiley);
		plot.setLimits(a[0], a[1], b[0], b[1] * 1.1);
		plot.setColor(color);
		return plot;
	}

	public static Plot basePlot(double[] profiley, String title, Color color) {
		double[] profilex = new double[profiley.length];
		for (int j = 0; j < profiley.length; j++)
			profilex[j] = j;
		double[] a = Tools.getMinMax(profilex);
		double[] b = Tools.getMinMax(profiley);

		Plot plot = new Plot(title, "pixel", "valore", profilex, profiley);
		plot.setLimits(a[0], a[1], b[0], b[1] * 1.1);

		plot.setColor(color);
		return plot;
	}

	public static Plot basePlot(double[][] profile, String title, Color color) {

		double[] profilex = new double[profile.length];
		double[] profiley = new double[profile.length];

		for (int i1 = 0; i1 < profile.length; i1++) {
			profilex[i1] = profile[i1][0];
			profiley[i1] = profile[i1][1];
		}

		double[] a = Tools.getMinMax(profilex);
		double[] b = Tools.getMinMax(profiley);

		Plot plot = new Plot(title, "pixel", "valore", profilex, profiley);
		plot.setLimits(a[0], a[1], b[0], b[1] * 1.1);
		plot.setColor(color);
		return plot;
	}

	/**
	 * Attenzione che in questa versione la struttura della matrice � completamente
	 * cambiata
	 * 
	 * @param profile
	 * @param title
	 * @param color
	 * @return
	 */
	public static Plot basePlot2(double[][] profile, double[] vetXpoints, String title, Color color) {

		ACRlog.logMatrix(profile, "profile");
		double[] profileX = new double[profile[0].length];
		double[] profileZ = new double[profile[0].length];

		for (int i1 = 0; i1 < profile[0].length; i1++) {
			profileX[i1] = profile[0][i1];
			profileZ[i1] = profile[2][i1];
		}

		double[] a = Tools.getMinMax(profileX);
		double[] b = Tools.getMinMax(profileZ);

		Plot plot = new Plot(title, "pixel", "valore", profileX, profileZ);
		plot.setLimits(a[0], a[1], b[0], b[1] * 1.1);
		plot.setColor(color);

		double[] vetYpoints = new double[vetXpoints.length];
		for (int i1 = 0; i1 < vetXpoints.length; i1++) {
			for (int i2 = 0; i2 < profileX.length; i2++) {

				// public static boolean compareDoublesWithTolerance(double aa, double bb,
				// int digits) {

				if (ACRcalc.compareDoublesWithTolerance(vetXpoints[i1], profileX[i2], 1)) {
					vetYpoints[i1] = profileZ[i2];
				}
			}
		}

		ACRlog.logVector(vetXpoints, "vetXpoints");
		ACRlog.logVector(vetYpoints, "vetYpoints");
		ACRlog.waitHere();

		plot.addPoints(vetXpoints, vetYpoints, PlotWindow.CIRCLE);

		return plot;
	}

	/**
	 * Attenzione che in questa versione la struttura della matrice � completamente
	 * cambiata
	 * 
	 * @param profile
	 * @param title
	 * @param color
	 * @return
	 */
	public static Plot basePlot2(double[][] profile, String title, Color color, boolean vertical) {

		double[] profilex = new double[profile[0].length];
		double[] profileZ = new double[profile[0].length];

		for (int i1 = 0; i1 < profile[0].length; i1++) {
			if (vertical)
				profilex[i1] = profile[1][i1];
			else
				profilex[i1] = profile[0][i1];
			profileZ[i1] = profile[2][i1];
		}

		double[] a = Tools.getMinMax(profilex);
		double[] b = Tools.getMinMax(profileZ);

		Plot plot = new Plot(title, "pixel", "valore", profilex, profileZ);
		plot.setLimits(a[0], a[1], b[0], b[1] * 1.1);
		plot.setColor(color);
		return plot;
	}

	/**
	 * apre e mostra una immagine ingrandita
	 * 
	 * @param path path dell'immagine da mostrare
	 * @return puntatore ImagePlus all'immagine
	 */
	public static ImagePlus openImageMaximized(String path) {

		ImagePlus imp = new Opener().openImage(path);
		if (imp == null) {
			ACRlog.waitThere("Immagine " + path + " inesistente o non visualizzabile");
			return null;
		}
		// IJ.log("OpenImageEnlarged");
		imp.show();
		ACRutils.zoom(imp);
		// showImageMaximized(imp);
		return imp;
	} // openImageEnlarged

	/**
	 * apre un immagine senza display
	 * 
	 * @param path path dell'immagine
	 * @return puntatore ImagePlus all'immagine
	 */
	public static ImagePlus openImageNoDisplay(File nome, boolean verbose) {

		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(nome.getPath());
		if (imp == null) {
			if (verbose)
				ACRlog.waitThere("Immagine " + nome.getPath() + " inesistente o non visualizzabile");
			return null;
		}
		return imp;
	}

	/**
	 * apre un immagine senza display
	 * 
	 * @param path path dell'immagine
	 * @return puntatore ImagePlus all'immagine
	 */
	public static ImagePlus openImageNoDisplay(String path, boolean verbose) {

		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(path);
		if (imp == null) {
			if (verbose)
				ACRlog.waitThere("Immagine " + path + " inesistente o non visualizzabile");
			return null;
		}
		return imp;
	}

	/**
	 * apre e mostra una immagine
	 * 
	 * @param path path dell'immagine da mostrare
	 * @return puntatore ImagePlus all'immagine
	 */
	public static ImagePlus openImageNormal(String path) {

		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(path);
		if (imp == null) {
			ACRlog.waitThere("Immagine " + path + " inesistente o non visualizzabile");
			return null;
		}
		// IJ.log("OpenImageNormal");
		imp.show();
		return imp;
	} // openImageNormal

}
