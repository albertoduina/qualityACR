package qualityACR;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class ACRlocalizer {
	private static final boolean debug = true;
	private static final boolean big = true;

	/**
	 * Ricerca posizione e diametro Phantom per calcolo uniformita', utilizza un
	 * Canny Edge Detector
	 * 
	 * @param imp11      immagine in input
	 * @param info1      messaggio esplicativo
	 * @param autoCalled true se chiamato in automatico
	 * @param step       true se in modo passo passo
	 * @param verbose1   true se in modo verbose
	 * @param test       true se in test con junit, nessuna visualizzazione e
	 *                   richiesta conferma
	 * @param fast       true se in modo batch
	 * @return
	 */
	public static double[] positionSearch1(ImagePlus imp11, double maxFitError, double maxBubbleGapLimit, boolean step,
			boolean fast, boolean verbose, int timeout) {

		//
		// ================================================================================
		// Inizio calcoli geometrici
		// ================================================================================
		//

//		ACRutils.waitHere("demo= " + demo + " fast=" + fast + " timeout= " + timeout);

		Color colore1 = Color.red;
		Color colore2 = Color.green;
		Color colore3 = Color.red;
		boolean manual = false;
		int xCenterCircle = 0;
		int yCenterCircle = 0;
		int diamCircleMan = 0;
		int diamCircle = 0;
		int xCenterMROI = 0;
		int yCenterMROI = 0;
		int diamMROI = 0;
		int xcorr = 0;
		int ycorr = 0;
		boolean showProfiles = verbose;
		int height = imp11.getHeight();
		int width = imp11.getWidth();
		// per evitare che il cerchio della roi di default possa avere strane dimensioni
		int aux1 = 0;
		if (width >= height)
			aux1 = height;
		else
			aux1 = width;
		int[] roiData = ACRutils.readPreferences(width, height, width);
		int diamRoiMan = roiData[2];
		if (diamRoiMan > aux1)
			diamRoiMan = aux1;
		ImageWindow iw11 = null;
//		ImageWindow iw12 = null;
		if (verbose) {
			iw11 = imp11.getWindow();
		}
		Overlay over12 = new Overlay();
		double dimPixel = ACRutils.readDouble(
				ACRutils.readSubstring(ACRutils.readDicomParameter(imp11, ACRconst.DICOM_PIXEL_SPACING), 1));
		if (verbose) {
			imp11.show();
			ACRutils.zoom(imp11);

			ACRlog.waitHere("L'immagine in input viene processata con il Canny Edge Detector", ACRutils.debug, timeout,
					fast);

		}
		ACRcannyEdgeDetector mce = new ACRcannyEdgeDetector();
		mce.setGaussianKernelRadius(2.0f);
		mce.setLowThreshold(2.5f);
		mce.setHighThreshold(10.0f);
		mce.setContrastNormalized(false);
		ImagePlus imp12 = mce.process(imp11);
		imp12.setOverlay(over12);
		if (verbose) {
			imp12.show();
			ACRutils.zoom(imp12);
			ACRlog.waitHere(
					"L'immagine risultante e' una immagine ad 8 bit, con i soli valori \n"
							+ "0 e 255. Lo spessore del perimetro del cerchio e' di 1 pixel",
					ACRutils.debug, timeout, fast);
//			iw12 = imp12.getWindow();
		}
		double[][] peaks9 = new double[4][1];
		double[][] peaks10 = new double[4][1];
		double[][] peaks11 = new double[4][1];
		double[][] peaks12 = new double[4][1];
		// ------ riadattamento da p10
		double[][] myPeaks = new double[4][1];
		int[] myXpoints = new int[16];
		int[] myYpoints = new int[16];
		int[] xcoord = new int[2];
		int[] ycoord = new int[2];
		boolean manualOverride = false;
		int[] vetx0 = new int[8];
		int[] vetx1 = new int[8];
		int[] vety0 = new int[8];
		int[] vety1 = new int[8];
		// ----
		vetx0[0] = 0;
		vety0[0] = height / 2;
		vetx1[0] = width;
		vety1[0] = height / 2;
		// ----
		vetx0[1] = width / 2;
		vety0[1] = 0;
		vetx1[1] = width / 2;
		vety1[1] = height;
		// ----
		vetx0[2] = 0;
		vety0[2] = 0;
		vetx1[2] = width;
		vety1[2] = height;
		// -----
		vetx0[3] = width;
		vety0[3] = 0;
		vetx1[3] = 0;
		vety1[3] = height;
		// -----
		vetx0[4] = width / 4;
		vety0[4] = 0;
		vetx1[4] = width * 3 / 4;
		vety1[4] = height;
		// ----
		vetx0[5] = width * 3 / 4;
		vety0[5] = 0;
		vetx1[5] = width / 4;
		vety1[5] = height;
		// ----
		vetx0[6] = width;
		vety0[6] = height * 1 / 4;
		vetx1[6] = 0;
		vety1[6] = height * 3 / 4;
		// ----
		vetx0[7] = 0;
		vety0[7] = height * 1 / 4;
		vetx1[7] = width;
		vety1[7] = height * 3 / 4;
		// ----
		String[] vetTitle = { "orizzontale", "verticale", "diagonale sinistra", "diagonale destra", "inclinata 1",
				"inclinata 2", "inclinata 3", "inclinata 4" };
		// multipurpose line analyzer
		int count = -1;
		boolean vertical = false;
		boolean valido = true;
		String motivo = "";
		// boolean solouno = true;
		for (int i1 = 0; i1 < 8; i1++) {
			xcoord[0] = vetx0[i1];
			ycoord[0] = vety0[i1];
			xcoord[1] = vetx1[i1];
			ycoord[1] = vety1[i1];
			imp12.setRoi(new Line(xcoord[0], ycoord[0], xcoord[1], ycoord[1]));
			if (verbose) {
				imp12.getRoi().setStrokeColor(colore2);
				over12.addElement(imp12.getRoi());
				imp12.updateAndDraw();
			}
			if (i1 == 1)
				vertical = true;
			else
				vertical = false;
			// showProfiles = false;
			boolean profilo = false;
			if (showProfiles) {
				// solouno = false;
				profilo = true;
			}

//			cannyProfileAnalyzer(ImagePlus imp1, double dimPixel, String title, boolean showProfiles,
//					boolean step, boolean vertical, boolean fast, boolean verbose, int timeout)

			myPeaks = ACRutils.cannyProfileAnalyzer(imp12, dimPixel, vetTitle[i1], profilo, step, vertical, fast,
					verbose, timeout);
			// myPeaks = profileAnalyzer(imp12, dimPixel, vetTitle[i1],

			// showProfiles, vertical, timeout);
			String direction1 = ACRutils.readDicomParameter(imp11, ACRconst.DICOM_IMAGE_ORIENTATION);
			// String direction2 = "1\0\0\01\0";
			if (myPeaks != null) {
				// per evitare le bolle d'aria escludero' il punto in alto per
				// l'immagine assiale ed il punto a sinistra dell'immagine
				// sagittale. Considero punto in alto quello con coordinata y <
				// mat/2 e come punto a sinistra quello con coordinata x < mat/2
				for (int i2 = 0; i2 < myPeaks[0].length; i2++) {
					valido = true;
					// ACRutils.waitHere("direction1= " + direction1 + " i1= " +
					// i1);

					if ((direction1.compareTo("0\\1\\0\\0\\0\\-1") == 0) && (i1 == 0)) {
						// ACRutils.waitHere("interdizione 0");
						if (((int) (myPeaks[0][i2]) < width / 2)) {
							valido = false;
							// ACRutils.waitHere("linea orizzontale eliminato punto
							// sx");
						} else
							;
						// ACRutils.waitHere("linea orizzontale mantenuto punto
						// dx");
					}
					if ((direction1.compareTo("1\\0\\0\\0\\1\\0") == 0) && (i1 == 1)) {
						// ACRutils.waitHere("interdizione 1");
						if (((int) Math.round(myPeaks[1][i2]) < height / 2)) {
							valido = false;
							// ACRutils.waitHere("linea verticale eliminato punto
							// sup");
						} else
							;
						// ACRutils.waitHere("linea verticale mantenuto punto
						// inf");
					}
					if (valido) {
						count++;
						myXpoints[count] = (int) Math.round(myPeaks[3][i2]);
						myYpoints[count] = (int) Math.round(myPeaks[4][i2]);
						ACRgraphic.plotPoints(imp12, over12, (int) (myPeaks[3][i2]), (int) (myPeaks[4][i2]), colore1,
								8.1);
						imp12.updateAndDraw();
						if (imp12.isVisible())
							imp12.getWindow().toFront();
					}
				}
			}
		}
		if (verbose)
			ACRlog.waitHere("Tracciate tutte le linee", ACRutils.debug, timeout, fast);
		int[] xPoints3 = new int[1];
		int[] yPoints3 = new int[1];
		if (count >= 1) {
			xPoints3 = new int[count];
			yPoints3 = new int[count];
			count++;
			xPoints3 = new int[count];
			yPoints3 = new int[count];
			for (int i3 = 0; i3 < count; i3++) {
				xPoints3[i3] = myXpoints[i3];
				yPoints3[i3] = myYpoints[i3];
			}
		}
		over12.clear();
		// boolean positioned1 = false;
		PointRoi pr12 = new PointRoi(xPoints3, yPoints3, xPoints3.length);
		pr12.setPointType(2);
		pr12.setSize(4);
		imp12.setRoi(pr12);
		if (verbose) {
			ACRgraphic.addOverlayRoi(imp12, colore1, 8.1);
			pr12.setPointType(2);
			pr12.setSize(4);

		}
		// ---------------------------------------------------
		// eseguo ora fitCircle per trovare centro e dimensione del
		// fantoccio
		// ---------------------------------------------------
		ACRgraphic.fitCircle(imp12);
		if (verbose) {
			imp12.getRoi().setStrokeColor(colore1);
			over12.addElement(imp12.getRoi());
		}
		if (verbose)
			ACRlog.waitHere("La circonferenza risultante dal fit e' mostrata in rosso", ACRutils.debug, timeout, fast);
		Rectangle boundRec = imp12.getProcessor().getRoi();
		xCenterCircle = Math.round(boundRec.x + boundRec.width / 2);
		yCenterCircle = Math.round(boundRec.y + boundRec.height / 2);
		diamCircle = boundRec.width;
		if (!manualOverride)
			ACRutils.writeStoredRoiData(boundRec);
		ACRgraphic.drawCenter(imp12, over12, xCenterCircle, yCenterCircle, colore3);
		// ----------------------------------------------------------
		// Misuro l'errore sul fit rispetto ai punti imposti
		// -----------------------------------------------------------
		double[] vetDist = new double[xPoints3.length];
		double sumError = 0;
		for (int i1 = 0; i1 < xPoints3.length; i1++) {
			vetDist[i1] = ACRutils.pointCirconferenceDistance(xPoints3[i1], yPoints3[i1], xCenterCircle, yCenterCircle,
					diamCircle / 2);
			sumError += Math.abs(vetDist[i1]);
		}
		if (sumError > maxFitError) {
			// -------------------------------------------------------------
			// disegno il cerchio ed i punti, in modo da date un feedback
			// grafico al messaggio di eccessivo errore nel fit
			// -------------------------------------------------------------
			imp11.getWindow().toFront();
			over12.clear();
			imp11.setOverlay(over12);
			imp11.setRoi(new PointRoi(xPoints3, yPoints3, xPoints3.length));
			imp11.getRoi().setStrokeColor(Color.green);
			over12.addElement(imp11.getRoi());
			imp11.setRoi(new OvalRoi(xCenterCircle - diamCircle / 2, yCenterCircle - diamCircle / 2, diamCircle,
					diamCircle));
			imp11.getRoi().setStrokeColor(Color.red);
			imp11.getRoi().setStrokeWidth(1.1);
			ACRlog.waitHere("Distanza eccessiva tra i punti forniti ed il fit del cerchio ottenuto, CI RINUNCIO");
		}
		//
		// ----------------------------------------------------------
		// disegno la ROI del centro, a solo scopo dimostrativo !
		// ----------------------------------------------------------
		//
		if (verbose && !manual) {
			ACRgraphic.drawCenter(imp12, over12, xCenterCircle, yCenterCircle, colore1);
			ACRlog.waitHere("Il centro del fantoccio e' contrassegnato dal pallino rosso", ACRutils.debug, timeout,
					fast);
		}
		// =============================================================
		// COMPENSAZIONE PER EVENTUALE BOLLA D'ARIA NEL FANTOCCIO
		// ==============================================================
		// Traccio nuovamente le bisettrici verticale ed orizzontale, solo
		// che anziche' essere sul centro dell'immagine, ora sono poste sul
		// centro del cerchio circoscritto al fantoccio
		// BISETTRICE VERTICALE FANTOCCIO
		if (!manual) {
			imp12.setRoi(new Line(xCenterCircle, 0, xCenterCircle, height));
			if (verbose) {
				imp12.getRoi().setStrokeColor(colore2);
				over12.addElement(imp12.getRoi());
				imp12.updateAndDraw();
			}
			peaks9 = ACRutils.cannyProfileAnalyzer(imp12, dimPixel, "BISETTRICE VERTICALE FANTOCCIO", false, false,
					false, false, false, 1);
			// logMatrix(peaks9, "peaks9");
			// ACRutils.waitHere();
			// PLOTTAGGIO PUNTI
			double gapVert = 0;
			if (peaks9 != null) {
//				ImageUtils.plotPoints(imp12, over12, peaks9);
				gapVert = diamCircle / 2 - (yCenterCircle - peaks9[4][0]);
			}
			// BISETTRICE ORIZZONTALE FANTOCCIO
			imp12.setRoi(new Line(0, yCenterCircle, width, yCenterCircle));
			if (verbose) {
				imp12.getRoi().setStrokeColor(colore2);
				over12.addElement(imp12.getRoi());
				imp12.updateAndDraw();
			}
			peaks10 = ACRutils.cannyProfileAnalyzer(imp12, dimPixel, "BISETTRICE ORIZZONTALE FANTOCCIO", false, false,
					false, false, false, 1);
			double gapOrizz = 0;
			if (peaks10 != null) {
//				ImageUtils.plotPoints(imp12, over12, peaks10);
				gapOrizz = diamCircle / 2 - (xCenterCircle - peaks10[3][0]);
			}
			if (verbose)
				ACRlog.waitHere("Si determina ora l'eventuale dimensione delle bolla d'aria, \n"
						+ "essa viene automaticamente compensata entro il limite del \n" + "\"maxBubbleGapLimit\"= "
						+ maxBubbleGapLimit, ACRutils.debug, timeout, fast);
			// Effettuo in ogni caso la correzione, solo che in assenza di
			// bolla d'aria la correzione sara' irrisoria, in presenza di bolla la
			// correzione sara' apprezzabile
			if (gapOrizz > gapVert) {
				xcorr = (int) gapOrizz / 2;
			} else {
				ycorr = (int) gapVert / 2;
			}
			// ---------------------------------------
			// qesto e' il risultato della nostra correzione e saranno i
			// dati della MROI
			// ---------------------------------------
			// diamMROI = (int) Math.round(diamCircle * MyConst.P3_AREA_PERC_80_DIAM);
			if (verbose)
				ACRlog.waitHere("DiamCircle= " + diamCircle, ACRutils.debug, timeout, fast);
			diamMROI = (int) Math.round(84 / dimPixel);

			xCenterMROI = xCenterCircle + xcorr;
			yCenterMROI = yCenterCircle + ycorr;
			// ---------------------------------------
			// verifico ora che l'entita' della bolla non sia cosi' grande
			// da portare l'area MROI troppo a contatto del profilo
			// fantoccio calcolato sulle bisettrici
			// ---------------------------------------
			imp12.setRoi(new Line(xCenterMROI, 0, xCenterMROI, height));
			if (verbose) {
				imp12.getRoi().setStrokeColor(colore2);
				over12.addElement(imp12.getRoi());
				imp12.updateAndDraw();
			}
			// showProfiles = true;
			peaks11 = ACRutils.cannyProfileAnalyzer(imp12, dimPixel, "BISETTRICE VERTICALE MROI x VERIFICA", false,
					false, false, false, false, 1);
			if (peaks11 != null) {
				// ImageUtils.plotPoints(imp12, over12, peaks11);
			}
			imp12.setRoi(new Line(0, yCenterMROI, width, yCenterMROI));
			if (verbose) {
				imp12.getRoi().setStrokeColor(colore2);
				over12.addElement(imp12.getRoi());
				imp12.updateAndDraw();
			}
			peaks12 = ACRutils.cannyProfileAnalyzer(imp12, dimPixel, "BISETTRICE ORIZZONTALE MROI x VERIFICA", false,
					false, false, false, false, 1);
			if (peaks12 != null) {
				// ImageUtils.plotPoints(imp12, over12, peaks12);
			}
			// showProfiles = false;
			double d1 = maxBubbleGapLimit;
			double d2 = maxBubbleGapLimit;
			double d3 = maxBubbleGapLimit;
			double d4 = maxBubbleGapLimit;
			double dMin = 9999;
			// verticale
			if (peaks11 != null) {
				d1 = -(peaks11[4][0] - (yCenterMROI - diamMROI / 2));
				d2 = peaks11[4][1] - (yCenterMROI + diamMROI / 2);
			}
			// orizzontale
			if (peaks12 != null) {
				d3 = -(peaks12[3][0] - (xCenterMROI - diamMROI / 2));
				d4 = peaks12[3][1] - (xCenterMROI + diamMROI / 2);
			}
			dMin = Math.min(dMin, d1);
			dMin = Math.min(dMin, d2);
			dMin = Math.min(dMin, d3);
			dMin = Math.min(dMin, d4);
			if (dMin < maxBubbleGapLimit) {
				manual = true;
				motivo = "Spostamento automatico eccessivo per compensare la bolla d'aria presente nel fantoccio";
			} else {
				imp12.setRoi(new OvalRoi(xCenterMROI - diamMROI / 2, yCenterMROI - diamMROI / 2, diamMROI, diamMROI));
				Rectangle boundingRectangle2 = imp12.getProcessor().getRoi();
				diamMROI = (int) boundingRectangle2.width;
				xCenterMROI = boundingRectangle2.x + boundingRectangle2.width / 2;
				yCenterMROI = boundingRectangle2.y + boundingRectangle2.height / 2;
				// imp12.killRoi();
			}
		}
		imp12.setRoi(new OvalRoi(xCenterMROI - diamMROI / 2, yCenterMROI - diamMROI / 2, diamMROI, diamMROI));
		if (verbose) {
			imp12.updateAndDraw();
			imp12.getRoi().setStrokeColor(colore2);
			over12.addElement(imp12.getRoi());
			ACRlog.waitHere("Viene mostrata la circonferenza con area 80% del fantoccio, chiamata MROI", ACRutils.debug,
					timeout, fast);
			ACRgraphic.drawCenter(imp12, over12, xCenterCircle + xcorr, yCenterCircle + ycorr, colore2);
			ACRlog.waitHere("Il centro della MROI e' contrassegnato dal pallino verde", ACRutils.debug, timeout, fast);
			IJ.beep();
			ACRlog.waitHere("TERMINE PHANTOM SEARCH VERBOSE", ACRutils.debug, timeout, fast);
		}
		imp12.close();
		if (imp11.isVisible()) {
			ACRutils.unzoom(imp11);
		}
		double[] out2 = new double[6];
		out2[0] = xCenterCircle;
		out2[1] = yCenterCircle;
		out2[2] = diamCircle;
		out2[3] = xCenterMROI;
		out2[4] = yCenterMROI;
		out2[5] = diamMROI;
		return out2;
	}

	/**
	 * Ricerca posizione e diametro fantoccio, senza precedenti elaborazioni
	 * 
	 * @param imp1        immagine da analizzare
	 * @param maxFitError errore sul fit
	 * @param step        esecuzione passo passo
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return vettore contenente i dati del cerchio localizzato ed i punti delle
	 *         quattro misure eseguite
	 */

	/**
	 * Ricerca posizione e diametro fantoccio, senza precedenti elaborazioni
	 * 
	 * @param imp1        immagine da analizzare
	 * @param maxFitError limite errore sul fit
	 * @param step        esegue passo passo
	 * @param fast        preme automaticamente ok ai messaggi, dopo timeout
	 *                    millisecondi
	 * @param verbose     massimo livello di grafica
	 * @param timeout     millisecondi per OK
	 * @return vettore contenente i dati del fantoccio circolare localizzato
	 */
	public static int[] positionSearch2(ImagePlus imp1, double maxFitError, boolean step, boolean fast, boolean verbose,
			int timeout) {

		IJ.log("positionSearch2 >>  step= " + step + " fast= " + fast + " verbose= " + verbose);

		ACRlog.waitHere(">>> 01 - RICERCA POSIZIONE E DIAMETRO FANTOCCIO <<<", ACRutils.debug, timeout, fast);

		// ricerca posizione e diametro fantoccio
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ImageWindow iw2 = imp2.getWindow();
		if (ACRutils.big)
			ACRutils.zoom(imp2);

		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);
		int height = imp2.getHeight();
		int width = imp2.getWidth();
		// imposto i dati dei segmenti per i 4 profili
		int[] vetXstart = new int[4];
		int[] vetYstart = new int[4];
		int[] vetXend = new int[4];
		int[] vetYend = new int[4];
		int[] xcoord = new int[2];
		int[] ycoord = new int[2];
		// ----
		vetXstart[0] = 0;
		vetYstart[0] = height / 2;
		vetXend[0] = width;
		vetYend[0] = height / 2;
		// ----
		vetXstart[1] = width / 2;
		vetYstart[1] = 0;
		vetXend[1] = width / 2;
		vetYend[1] = height;
		// ----
		vetXstart[2] = 0;
		vetYstart[2] = 0;
		vetXend[2] = width;
		vetYend[2] = height;
		// -----
		vetXstart[3] = width;
		vetYstart[3] = 0;
		vetXend[3] = 0;
		vetYend[3] = height;
		// -----
		String[] vetTitle = { "ORIZZONTALE", "VERTICALE", "DIAGONALE_SX", "DIAGONALE_DX" };
		int count1 = 0;
		boolean now = false;
		float[] xcircle = new float[8];
		float[] ycircle = new float[8];
		int[] vetx = new int[8];
		int[] vety = new int[8];

		// esecuzione dei 4 profili
		for (int i1 = 0; i1 < 4; i1++) {
			now = true;
			// tracciamento linea profilo
			xcoord[0] = vetXstart[i1];
			ycoord[0] = vetYstart[i1];
			xcoord[1] = vetXend[i1];
			ycoord[1] = vetYend[i1];
			imp2.setRoi(new Line(xcoord[0], ycoord[0], xcoord[1], ycoord[1]));
			over2.addElement(imp2.getRoi());
			// if (now)
			// ACRlog.waitHere("linea");
			// estrazione matrice con segnale pixels e loro coordinate su immagine
			double[][] decomposed3v = ACRutils.decomposer3v(imp2);
			if (verbose)
				ACRlog.logMatrix(decomposed3v, "decomposed3v");
			// ACRlog.waitHere();

			// [][] decomposed3v ha il formato:
			// decomposed3v[0] = profiX coordinata x del pixel su immagine
			// decomposed3v[1] = profiY coordinata y del pixel su immagine
			// decomposed3v[2] = profiZ valore segnale nei pixels per plottaggio
			// decomposed3v[3] = profiW coordinata x per plottaggio

			// azzera segnale primo ed ultimo pixel del profilo
			double[][] zeropadded3v = ACRutils.zeropadProfile3v(decomposed3v);
			if (verbose)
				ACRlog.logMatrix(zeropadded3v, "zeropadded3v INPUT FWHMpoints3vNEW");
			// ACRlog.waitHere();
			// cerca i due punti sopra e i due punti sotto mezza altezza

			IJ.log("==== PROFILO " + vetTitle[i1] + " ====");
			int pseudomaxlen = 3;
			double[][] vetout = ACRlocalizer.FWHMpoints3vNEW(zeropadded3v, pseudomaxlen, "PROFILO " + vetTitle[i1],
					step, fast, verbose);
			// double[][] vetout ha il formato
			// vetout[0][0] = coordinata X sinistra interpolata
			// vetout[1][2] = segnale mezza altezza
			// vetout[0][3] = coordinata X destra interpolata
			// vetout[1][3] = segnale mezza altezza
			if (verbose) {
				IJ.log("==========NOW=========");
				IJ.log("OUTPUT DI FWHMpoints3vNEW");
				IJ.log("vetout da FWMpoints3vNEW");
				ACRlog.logMatrix(vetout, "vetout");
				IJ.log("======================");
			}

			int xpoint1 = (int) Math.round(vetout[0][2]);
			int ypoint1 = (int) vetout[1][0];
			if (verbose) {
				IJ.log("prima point= " + vetout[0][0] + "  " + vetout[1][0]);
				IJ.log("diventa point= " + vetout[0][2] + "  " + vetout[1][0]);
			}

			xcircle[count1] = (float) vetout[0][2];
			ycircle[count1] = (float) vetout[1][0];
			vetx[count1] = (int) xpoint1;
			vety[count1] = (int) ypoint1;
			count1++;
			int xpoint2 = (int) Math.round(vetout[0][3]);
			int ypoint2 = (int) vetout[1][1];

			xcircle[count1] = (float) vetout[0][3];
			ycircle[count1] = (float) vetout[1][1];
			count1++;
			ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, Color.GREEN);
			ACRutils.plotPoints(imp2, over2, xpoint2, ypoint2, Color.GREEN);

			if (verbose) {
				ACRlog.waitHere("Punti plottati VERDE su immagine con coordinate " + xpoint1 + "," + ypoint1 + "   "
						+ xpoint2 + "," + ypoint2);
			}
			imp2.updateAndDraw();
			iw2.toFront();
			// iw2ayv
			if (verbose) {
				ACRlog.waitHere("PROFILO " + vetTitle[i1] + " con punti trovati, riportati anche su immagine",
						ACRutils.debug, timeout, fast);
			}

		}
		imp2.killRoi();

		PointRoi pr12 = new PointRoi(vetx, vety, vetx.length);
		pr12.setPointType(2);
		pr12.setSize(4);
		imp2.updateAndDraw();
		// ---------------------------------------------------
		// eseguo ora fitCircle per trovare centro e dimensione grossolana del
		// fantoccio. FitCircle è copiato da ImageJ ed era a sua volta derivato dal
		// programma BoneJ
		// ---------------------------------------------------
		imp2.setRoi(new PointRoi(xcircle, ycircle));
		ACRgraphic.fitCircle(imp2);
		if (true) {
			imp2.getRoi().setStrokeColor(Color.GREEN);
			over2.addElement(imp2.getRoi());
		}
		if (verbose) {
			ACRlog.waitHere("La circonferenza risultante dal fit e' mostrata in verde", true, timeout, fast);
			ACRlog.waitHere();
		}
		Rectangle boundRec = imp2.getProcessor().getRoi();
		int xCenterCircle = Math.round(boundRec.x + boundRec.width / 2);
		int yCenterCircle = Math.round(boundRec.y + boundRec.height / 2);
		int diamCircle = boundRec.width;

		int[] out1 = new int[3];
		out1[0] = xCenterCircle;
		out1[1] = yCenterCircle;
		out1[2] = diamCircle;

		return out1;
	}

	/**
	 * Misura precisa diametri fantoccio, si presuppone di conoscere la posizione
	 * del centro del fantoccio (si conosce anche il diametro, ma vogliamo
	 * calcolarlo come scritto nel manuale ACR
	 * 
	 * @param imp1
	 * @param step
	 * @param fast
	 * @param timeout
	 * @return vettore contenente i dati del cerchio localizzato ed i punti delle
	 *         quattro misure eseguite
	 */
	public static double[] positionSearch3(ImagePlus imp1, int[] phantomCircle, boolean step, boolean fast,
			boolean verbose, int timeout) {

		boolean now = true;
		// al contrario di positionSearch2, dove non conoscevamo posizione e diametro
		// del fantoccio, in questo caso questi dati ci sono noti, ora vogliamo fare una
		// misura precisa dei diametri, presi in modo che passino correttamente per il
		// centro del fantoccio circolare

		IJ.log("positionSearch3 >>  step= " + step + " fast= " + fast + " verbose= " + verbose);

		ACRlog.waitHere(">>> 02 - MISURA PRECISA DIAMETRI FANTOCCIO <<<", ACRutils.debug, timeout, fast);

		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ImageWindow iw2 = imp2.getWindow();
		if (ACRutils.big)
			ACRutils.zoom(imp2);

		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);
		int height = imp2.getHeight();
		int width = imp2.getWidth();
		// estraggo i dati da phantomCircle
		int xcenter = phantomCircle[0];
		int ycenter = phantomCircle[1];
		int diam = phantomCircle[2];
		//
		// per tracciare diagonali a 45 gradi, centrate sul cerchio e non sulla
		// immagine, devo calcolare correttamente le nuove intersezioni della retta
		// con i bordi della immagine. Uso l'algoritmo Liang_Barsky. Introduco come
		// estremi del segmento due punti al di fuori dei bordi dell'immagine e
		// calcolo le intersezioni tra questa linea ed il bordo immagine
		//
		double edgeLeft = 0.;
		double edgeRight = width;
		double edgeBottom = 0.;
		double edgeTop = height;
		double x0src = xcenter - width;
		double y0src = ycenter - height;
		double x1src = xcenter + width;
		double y1src = ycenter + height;
		double[] clippings1 = ACRgraphic.liangBarsky(edgeLeft, edgeRight, edgeBottom, edgeTop, x0src, y0src, x1src,
				y1src);
		//
		edgeLeft = 0.;
		edgeRight = width;
		edgeBottom = 0.;
		edgeTop = height;
		x0src = xcenter + width;
		y0src = ycenter - width;
		x1src = xcenter - width;
		y1src = ycenter + width;
		double[] clippings2 = ACRgraphic.liangBarsky(edgeLeft, edgeRight, edgeBottom, edgeTop, x0src, y0src, x1src,
				y1src);
		//
		// effettuo in tutto 4 profili
		int[] vetXstart = new int[4];
		int[] vetYstart = new int[4];
		int[] vetXend = new int[4];
		int[] vetYend = new int[4];
		int[] xcoord = new int[2];
		int[] ycoord = new int[2];
		// ---- orizzontale
		vetXstart[0] = 0;
		vetYstart[0] = ycenter;
		vetXend[0] = width;
		vetYend[0] = ycenter;
		// ---- verticale
		vetXstart[1] = xcenter;
		vetYstart[1] = 0;
		vetXend[1] = xcenter;
		vetYend[1] = height;
		// ---- diagonale sx
		vetXstart[2] = (int) clippings1[0];
		vetYstart[2] = (int) clippings1[1];
		vetXend[2] = (int) clippings1[2];
		vetYend[2] = (int) clippings1[3];
		// ----- diagonale dx
		vetXstart[3] = (int) clippings2[0];
		vetYstart[3] = (int) clippings2[1];
		vetXend[3] = (int) clippings2[2];
		vetYend[3] = (int) clippings2[3];
		// -----
		String[] vetTitle = { "ORIZZONTALE", "VERTICALE", "DIAGONALE_SX", "DIAGONALE_DX" };
		// line analyzer
		double[] out1 = new double[4];
		for (int i1 = 0; i1 < 4; i1++) {
			// viene ripetuto per ogni profilo
			xcoord[0] = vetXstart[i1];
			ycoord[0] = vetYstart[i1];
			xcoord[1] = vetXend[i1];
			ycoord[1] = vetYend[i1];
			imp2.setRoi(new Line(xcoord[0], ycoord[0], xcoord[1], ycoord[1]));
			imp2.getRoi().setStrokeColor(Color.CYAN);
			over2.addElement(imp2.getRoi());
			int pseudomaxlen = 3;
			double[][] decomposed3v = ACRutils.decomposer3v(imp2);
			double[][] zeropadded3v = ACRutils.zeropadProfile3v(decomposed3v);
			double[][] vetout = ACRlocalizer.FWHMpoints3vNEW(zeropadded3v, pseudomaxlen, "PROFILO " + vetTitle[i1],
					step, fast, true);
			// ora vado a plottare i punti trovati dall'analizzatore di profilo,
			// sull'immagine reale 2D

			int xpoint1 = (int) Math.round(vetout[0][2]);
			int ypoint1 = (int) vetout[1][0];
			IJ.log("immagine punto sinistro x= " + vetout[0][2] + " y= " + vetout[1][0]);
			if (verbose) {
				ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1);
			}

			int xpoint2 = (int) vetout[0][3];
			int ypoint2 = (int) vetout[1][1];
			IJ.log("immagine punto destro x= " + vetout[0][2] + " y= " + vetout[1][0]);
			if (verbose) {
				ACRutils.plotPoints(imp2, over2, xpoint2, ypoint2);
			}
			imp2.updateAndDraw();
			iw2.toFront();

			if (step)
				ACRlog.waitHere("PROFILO " + vetTitle[i1] + " con punti trovati, riportati anche su immagine",
						ACRutils.debug, timeout, fast);

			// vetout[0][2] = coordinata X sinistra interpolata
			// vetout[0][3] = coordinata X destra interpolata

			//
			// posso misurare il diametro "con precisione?" utilizzando i due punti
			// interpolati
			//
			double diameter = (vetout[0][3] - vetout[0][2]); // IN PIXEL

			out1[i1] = diameter;
		}

		return out1;
	}

	/**
	 * Ricerca di oggetti nell'immagine con la scansione di una griglia. Se fosse
	 * sufficientemente veloce sarebbe interessante eseguirla per ogni riga ed ogni
	 * colonna.
	 */
	public static int[] gridLocalizer1(ImagePlus imp1, boolean step, boolean fast, boolean verbose, int timeout) {

		boolean verbose2 = false;

		// ricerca del valore massimo di segnale sull'intera immagine mediato su di una
		// ROI quadrata 11x11, utilizzo questo segnale come valore dell'acqua, per la
		// ricerca e localizzazione del fantoccio
		imp1.show();

		ACRutils.zoom(imp1);

//		imp1.setRoi(17, 18, 164, 160);
//		imp1.cut();
//		imp1.setRoi(3, 2, 164, 160);
//		imp1.paste();
//		imp1.killRoi();

		IJ.log("gridLocalizer1_001 > ");
		int latoROI = 11;
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);
		//
		// Ricerca posizione del massimo con una roi quadrata di lato dispari 11x11,
		// restituisce le coordinate del centro
		//
		double[] water = maxPositionGeneric(imp1, latoROI);
		//
		// uso il segnale max trovato per definire il threshold
		//
		int threshold = (int) water[2] * 3 / 4;
		//
		// ora scansiono l'immagine, riga per riga, evito volutamente il bordo di 1
		// pixel attorno ai bordi, area dove di solito succedono COSSE BRUUTTE ASSAI!
		//
		ImageProcessor ip1 = imp1.getProcessor();
		int[] rowpixels = new int[width];
		double[] out1 = new double[4];
		int[] out2 = new int[4];
		double[] out3 = new double[4];
		double xpoint1 = 0;
		double ypoint1 = 0;
		double xpoint2 = 0;
		double ypoint2 = 0;
		int xp = 0;
		int yp = 0;

		ArrayList<Integer> arrX = new ArrayList<>();
		ArrayList<Integer> arrY = new ArrayList<>();

		// scansione per righe
		for (int y1 = 1; y1 < height - 1; y1++) {
			verbose2 = false;
			out2 = horizontalSearch(imp1, threshold, y1, verbose2);
			// qui intendo plottare sull'overlay, per vedere i risultati ottenibili,
			if (out2 != null) {
				xpoint1 = out2[0];
				ypoint1 = y1;
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp1, over1, xpoint1, ypoint1, type, size, Color.YELLOW, false);
				xpoint1 = out2[1];
				ypoint1 = y1;
				ACRutils.plotPoints(imp1, over1, xpoint1, ypoint1, type, size, Color.YELLOW, false);
			}
		}
		ACRlog.waitHere("scansione orizzontale su interi colore giallo");

		// scansione per colonne
		for (int x1 = 1; x1 < width - 1; x1++) {
			verbose2 = false;
			out2 = verticalSearch(imp1, threshold, x1, verbose2);
			if (out2 != null) {
				// qui intendo plottare sull'overlay, per vedere i risultati ottenibili
				xpoint1 = x1;
				ypoint1 = out2[0];
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp1, over1, xpoint1, ypoint1, type, size, Color.BLUE, false);
				xpoint1 = x1;
				ypoint1 = out2[1];
				ACRutils.plotPoints(imp1, over1, xpoint1, ypoint1, type, size, Color.BLUE, false);
			}
		}
		ACRlog.waitHere("scansione verticale su interi colore blu");

		// scansione per righe
		for (int y1 = 1; y1 < height - 1; y1++) {
			verbose2 = false;
			out3 = horizontalSearchInterpolated(imp1, threshold, y1, verbose2);
			// qui intendo plottare sull'overlay, per vedere i risultati ottenibili ma
			// arrotondando all'intero per essere nelle stesse condizioni finali
			if (out3 != null) {
				xpoint2 = Math.round(out3[0]);
				ypoint2 = y1;
				arrX.add((int) Math.round(out3[0]));
				arrY.add(y1);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp1, over1, xpoint2, ypoint2, type, size, Color.RED, true);
				xpoint2 = Math.round(out3[1]);
				ypoint2 = y1;
				arrX.add((int) Math.round(out3[1]));
				arrY.add(y1);
				ACRutils.plotPoints(imp1, over1, xpoint2, ypoint2, type, size, Color.RED, true);
			}
		}
		ACRlog.waitHere("scansione orizzontale interpolata colore rosso");

		// scansione per colonne
		for (int x1 = 1; x1 < width - 1; x1++) {
			verbose2 = false;
			out3 = verticalSearchInterpolated(imp1, threshold, x1, verbose2);
			// qui intendo plottare sull'overlay, per vedere i risultati ottenibili ma
			// arrotondando all'intero per essere nelle stesse condizioni finali
			if (out3 != null) {
				xpoint2 = x1;
				ypoint2 = Math.round(out3[0]);
				arrX.add(x1);
				arrY.add((int) Math.round(out3[0]));
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp1, over1, xpoint2, ypoint2, type, size, Color.GREEN, true);
				xpoint2 = x1;
				ypoint2 = Math.round(out3[1]);
				arrX.add(x1);
				arrY.add((int) Math.round(out3[1]));
				ACRutils.plotPoints(imp1, over1, xpoint2, ypoint2, type, size, Color.GREEN, true);
			}
		}

		ACRlog.waitHere("scansione verticale interpolata colore verde");

		// ora raduno tutti i punti che abbiamo determinato, ad esempio con la ricerca
		// interpolata, e li passo al fitter
		int[] vetX = ACRutils.arrayListToArrayInt(arrX);
		int[] vetY = ACRutils.arrayListToArrayInt(arrY);
		
		PointRoi pr12 = new PointRoi(vetX, vetY, vetX.length);
		pr12.setPointType(2);
		pr12.setSize(4);
		imp1.updateAndDraw();
		
		
		
		// ---------------------------------------------------
		// eseguo ora fitCircle per trovare centro e dimensione grossolana del
		// fantoccio. FitCircle è copiato da ImageJ ed era a sua volta derivato dal
		// programma BoneJ
		// ---------------------------------------------------
		imp1.setRoi(new PointRoi(vetX, vetY, vetX.length));
		ACRgraphic.fitCircle(imp1);
		if (true) {
			imp1.getRoi().setStrokeColor(Color.GREEN);
			over1.addElement(imp1.getRoi());
		}
		if (verbose) {
			ACRlog.waitHere("La circonferenza risultante dal fit e' mostrata in verde", true, timeout, fast);
			ACRlog.waitHere();
		}
		Rectangle boundRec = imp1.getProcessor().getRoi();
		int xCenterCircle = Math.round(boundRec.x + boundRec.width / 2);
		int yCenterCircle = Math.round(boundRec.y + boundRec.height / 2);
		int diamCircle = boundRec.width;

		int[] out4 = new int[3];
		out4[0] = xCenterCircle;
		out4[1] = yCenterCircle;
		out4[2] = diamCircle;

		return out4;

	}

	/**
	 * Ricerca posizione del massimo con una roi programmabile di lato dispari,
	 * restituisce le coordinate del centro
	 * 
	 * @param imp1
	 * @return
	 */
	public static double[] maxPositionGeneric(ImagePlus imp1, int latoROI) {
		if ((latoROI & 1) == 0) {
			ACRlog.waitHere("GNARO, il lato del kernel deve essere dispari!!");
			return null;
		}

		int width = imp1.getWidth();
		int height = imp1.getHeight();
		long sum1 = 0;
		double mean1 = 0;
		double max1 = 0;
		int xmax1 = 0;
		int ymax1 = 0;
		int offset = 0;
		int address = 0;
		int count1 = 0;
		if (imp1.getBytesPerPixel() != 2)
			return null;
		ImageProcessor ip1 = imp1.getProcessor();
		short[] pixels1 = (short[]) ip1.getPixels();
		// scansione sulle coordinate del centro roi yxy
		int pip = (latoROI - 1) / 2; // distanza tra centro e lato del kernel
		// scansiono l'intera imagine muovendo il centro della ROI pixel per pixel
		for (int y1 = pip; y1 < height - pip; y1++) {
			for (int x1 = pip; x1 < width - pip; x1++) {
				sum1 = 0;
				count1 = 0;
				// mi posiziono sulla riga
				// qui sommo i pixel appartenenti alla ROI quadrata , utilizzando sum1 e count1
				// per accumulare i conteggi intermedi
				for (int y4 = -pip; y4 < pip + 1; y4++) {
					offset = (y1 + y4) * width + x1;
					for (int x4 = -pip; x4 < pip + 1; x4++) {
						address = offset + x4;
						sum1 = sum1 + (pixels1[address]);
						count1++;
					}
				}
				mean1 = sum1 / count1;
				if (count1 != latoROI * latoROI)
					ACRlog.waitHere("errore count= " + count1);
				if (mean1 > max1) {
					max1 = mean1;
					xmax1 = x1;
					ymax1 = y1;
				}
			}
		}
		if (max1 < 50.0) // filtro per evitare di restitruire il fondo, non so se serve a qualcosa
			return null;
		// restituiamo il vaolre massimo tra le medie delle ROI e le sue coordinate
		// sull'immagine
		double[] out = new double[3];
		out[0] = xmax1;
		out[1] = ymax1;
		out[2] = max1;
		return out;
	}

	/**
	 * Ricerca lungo i pixel di un profilo, in pratica una specie di FWHM non
	 * utilizzo il primo e l'ultimo pixel del profilo, per evitare pixel strani
	 * dovuti a difetti digitali delle immagini. Restituisce il valore del pixel che
	 * supera od eguaglia il threshold. Integer
	 * 
	 * @param profi1 profilo da elaborare
	 * @param water  presunto segnale dell'acqua
	 * @return int[] out1 con startpoint ed endpoint per il profilo[]
	 */
	public static int[] profileSearch(int[] profi1, int threshold) {

//		boolean valido1 = false;
		int startpoint = -1;
		// testo
		for (int j1 = 1; j1 < profi1.length - 1; j1++) {
			if (profi1[j1] >= threshold) {
				startpoint = j1;
				break;
			}
		}

		int endpoint = -1;
//		boolean valido2 = false;
		for (int j1 = profi1.length - 2; j1 >= 0; j1--) {
			if (profi1[j1] >= threshold) {
				endpoint = j1;
				break;
			}
		}
		if (startpoint < 1 || endpoint < 1 || endpoint > profi1.length - 2)
			return null;

		int[] out1 = new int[2];
		out1[0] = startpoint;
		out1[1] = endpoint;
		return out1;
	}

	/**
	 * Ricerca lungo i pixel di un profilo, in pratica una specie di FWHM non
	 * utilizzo il primo e l'ultimo pixel del profilo, per evitare pixel strani
	 * dovuti a difetti digitali delle immagini. Restituisce il valore del pixel che
	 * supera od eguaglia il threshold. DoublePrecision
	 * 
	 * @param profi1 profilo da elaborare
	 * @param water  presunto segnale dell'acqua
	 * @return int[] out1 con startpoint ed endpoint per il profilo[]
	 */
	public static int[] profileSearch(double[] profi1, double threshold, boolean stampa) {

//		boolean valido1 = false;
		int startpoint = -1;
		// testo
		for (int j1 = 1; j1 < profi1.length - 1; j1++) {
			if (Double.compare(profi1[j1], threshold) >= 0) {
				startpoint = j1;
				break;
			}
		}

		int endpoint = -1;
//		boolean valido2 = false;
		for (int j1 = profi1.length - 2; j1 >= 0; j1--) {
			if (Double.compare(profi1[j1], threshold) >= 0) {
				endpoint = j1;
				break;
			}
		}
		if (startpoint < 1 || endpoint < 1 || endpoint > profi1.length - 2)
			return null;
		if (stampa)
			IJ.log("profileSearch startpoint= " + startpoint + " endpoint= " + endpoint);

		int[] out1 = new int[2];
		out1[0] = startpoint;
		out1[1] = endpoint;
		return out1;
	}

	/**
	 * Ricerca lungo i pixel di un profilo, in pratica una specie di FWHM non
	 * utilizzo il primo e l'ultimo pixel del profilo, per evitare pixel strani
	 * dovuti a difetti digitali delle immagini. Restituisce il valore interpolato
	 * tra pixel che supera od uguaglia threshold ed il pixel precedente.
	 * 
	 * @param profi1 profilo da elaborare
	 * @param water  frazione del presunto segnale dell'acqua
	 * @return int[] out1 con startpoint ed endpoint per il profilo[]
	 */
	public static double[] profileSearchInterpolated(int[] profi1, int threshold, boolean stampa) {

		int startpoint = -1;
		for (int j1 = 1; j1 < profi1.length - 1; j1++) {
			if (profi1[j1] >= threshold) {
				startpoint = j1;
				break;
			}
		}
		if (startpoint < 1)
			return null;
		double value1 = (double) profi1[startpoint];
		double value2 = (double) profi1[startpoint - 1];
		double startmedpoint = ACRutils.xLinearInterpolation(startpoint, value1, startpoint - 1, value2, threshold);
		int endpoint = -1;
		for (int j1 = profi1.length - 2; j1 >= 0; j1--) {
			if (profi1[j1] >= threshold) {
				endpoint = j1;
				break;
			}
		}
		double value3 = (double) profi1[endpoint];
		double value4 = (double) profi1[endpoint + 1];
		double endmedpoint = ACRutils.xLinearInterpolation(endpoint, value3, endpoint + 1, value4, threshold);
		if (startmedpoint < 1 || endmedpoint >= profi1.length || endmedpoint > profi1.length - 2)
			return null;

		if (stampa) {
			IJ.log("profileSearchInterpolated startpoint= " + startpoint + " endpoint= " + endpoint);
			IJ.log("profileSearchInterpolated startmedpoint= " + startmedpoint + " endmedpoint= " + endmedpoint);
		}

		double[] out1 = new double[2];
		out1[0] = startmedpoint;
		out1[1] = endmedpoint;
		return out1;
	}

	/**
	 * Ricerca lungo i pixel di un profilo, in pratica una specie di FWHM non
	 * utilizzo il primo e l'ultimo pixel del profilo, per evitare pixel strani
	 * dovuti a difetti digitali delle immagini. Restituisce il valore interpolato
	 * tra pixel che supera od uguaglia threshold ed il pixel precedente.
	 * 
	 * @param profi1 profilo da elaborare
	 * @param water  frazione del presunto segnale dell'acqua
	 * @return int[] out1 con startpoint ed endpoint per il profilo[]
	 */
	public static double[] profileSearchInterpolated(double[] profi1, double threshold, boolean stampa) {

		int startpoint = -1;
		// determino quale sia il primo pixel a uguagliare o superare il threshold, si
		// trova a sinistra nel profilo
		for (int j1 = 1; j1 < profi1.length - 1; j1++) {
			if (Double.compare(profi1[j1], threshold) >= 0) {
				startpoint = j1;
				break;
			}
		}
		if (startpoint < 1)
			return null;
		double value1 = (double) profi1[startpoint];
		double value2 = (double) profi1[startpoint - 1];
		// faccio l'interpolazione lineare tra il pixel che supera il threshold ed il
		// pixel precedente
		double startmedpoint = ACRutils.xLinearInterpolation(startpoint, value1, startpoint - 1, value2, threshold);
		int endpoint = -1;
		// partendo dalla fine ed andando all'indietro determino quale sia il primo
		// pixel a uguagliare o superare il threshold, si trova a destra nel profilo
		for (int j1 = profi1.length - 2; j1 >= 0; j1--) {
			if (profi1[j1] >= threshold) {
				endpoint = j1;
				break;
			}
		}
		double value3 = (double) profi1[endpoint];
		double value4 = (double) profi1[endpoint + 1];
		// faccio l'interpolazione lineare tra il pixel che supera il threshold ed il
		// pixel precedente (ma andiampo all'indietro e sara' il seguente

		double endmedpoint = ACRutils.xLinearInterpolation(endpoint, value3, endpoint + 1, value4, threshold);
		if (startmedpoint < 1 || endmedpoint < 1 || endmedpoint > profi1.length - 2)
			return null;

		if (stampa) {
			IJ.log("profileSearchInterpolated(double) startpoint= " + startpoint + " endpoint= " + endpoint);
			IJ.log("profileSearchInterpolated(double) startmedpoint= " + startmedpoint + " endmedpoint= "
					+ endmedpoint);
		}

		double[] out1 = new double[2];
		out1[0] = startmedpoint;
		out1[1] = endmedpoint;
		return out1;
	}

	/**
	 * Calcolo della FWHM del diametro fantoccio
	 * 
	 * @param line1
	 * @param pseudomaxlen
	 * @param titolo
	 * @return
	 */
	public static double FWHMcalc(double[] line1, int pseudomaxlen, String titolo) {

		double pmax = ACRutils.pseudomax(line1, pseudomaxlen);
		double fwhmlevel = pmax / 4;
		double[] xVetLineHalf = new double[2];
		double[] yVetLineHalf = new double[2];
		xVetLineHalf[0] = 0;
		xVetLineHalf[1] = line1.length;
		yVetLineHalf[0] = fwhmlevel;
		yVetLineHalf[1] = fwhmlevel;
		String title = titolo;
		Plot plot1 = ACRutils.ACRplot(line1, title, Color.GREEN, true);
		plot1.addPoints(xVetLineHalf, yVetLineHalf, Plot.LINE);
		plot1.draw();
		int[] duepuntisopra = ACRlocalizer.findPointsopra(line1, (int) fwhmlevel);
		double[] duepuntix = new double[2];
		duepuntix[0] = (double) duepuntisopra[0];
		duepuntix[1] = (double) duepuntisopra[1];
		double[] duepuntiy = new double[2];
		duepuntiy[0] = line1[duepuntisopra[0]];
		duepuntiy[1] = line1[duepuntisopra[1]];
		// ACRutils.vetPrint(duepuntix, "duepuntix SOPRA");
		// ACRutils.vetPrint(duepuntiy, "duepuntiy SOPRA");
		plot1.setColor(Color.RED);
		plot1.addPoints(duepuntix, duepuntiy, Plot.CIRCLE);
		int[] duepuntisotto = ACRlocalizer.findPointsotto(line1, (int) fwhmlevel);
		duepuntix[0] = (double) duepuntisotto[0];
		duepuntix[1] = (double) duepuntisotto[1];
		duepuntiy[0] = line1[duepuntisotto[0]];
		duepuntiy[1] = line1[duepuntisotto[1]];
		// ACRutils.vetPrint(duepuntix, "duepuntix SOTTO");
		// ACRutils.vetPrint(duepuntiy, "duepuntiy SOTTO");
		plot1.setColor(Color.BLUE);
		plot1.addPoints(duepuntix, duepuntiy, Plot.CIRCLE);

		double fwhmPixel = (duepuntisopra[1] - duepuntisopra[0]); // PIXEL

		ACRlog.waitHere("FWHMpixel= " + fwhmPixel);

		return fwhmPixel;
	}

	/**
	 * Calcolo dei punti estremi della FWHM del diametro fantoccio
	 * <<<<<ATTENZIONE>>>>, non devo lavorare sui plots, ho bisogno delle coordinate
	 * x,y dei punti effettivi
	 * 
	 * 
	 * @param line1
	 * @param pseudomaxlen
	 * @param titolo
	 * @return
	 */
	public static double[] FWHMpoints(double[] line1, int pseudomaxlen, String titolo, boolean verbose) {

		double pmax = ACRutils.pseudomax(line1, pseudomaxlen);
		double fwhmlevel = pmax / 4;

		double[] xVetLineHalf = new double[2];
		double[] yVetLineHalf = new double[2];
		xVetLineHalf[0] = 0;
		xVetLineHalf[1] = line1.length;
		yVetLineHalf[0] = fwhmlevel;
		yVetLineHalf[1] = fwhmlevel;
		// ACRutils.vetPrint(xVetLineHalf, "xvetlinehalf");
		// ACRutils.vetPrint(yVetLineHalf, "yvetlinehalf");
		String title = titolo;
		Plot plot1 = ACRutils.ACRplot(line1, title, Color.GREEN, true);
		plot1.addPoints(xVetLineHalf, yVetLineHalf, Plot.LINE);
		plot1.draw();
		int[] duepuntisopra = ACRlocalizer.findPointsopra(line1, (int) fwhmlevel);
		double[] duepuntixa = new double[2];
		double[] duepuntiya = new double[2];
		duepuntixa[0] = (double) duepuntisopra[0];
		duepuntixa[1] = (double) duepuntisopra[1];
		duepuntiya[0] = line1[duepuntisopra[0]];
		duepuntiya[1] = line1[duepuntisopra[1]];
		// ACRutils.vetPrint(duepuntixa, "duepuntix SOPRA");
		// ACRutils.vetPrint(duepuntiya, "duepuntiy SOPRA");
		plot1.setColor(Color.RED);
		plot1.addPoints(duepuntixa, duepuntiya, Plot.CIRCLE);
		int[] duepuntisotto = ACRlocalizer.findPointsotto(line1, (int) fwhmlevel);
		double[] duepuntixb = new double[2];
		double[] duepuntiyb = new double[2];
		duepuntixb[0] = (double) duepuntisotto[0];
		duepuntixb[1] = (double) duepuntisotto[1];
		duepuntiyb[0] = line1[duepuntisotto[0]];
		duepuntiyb[1] = line1[duepuntisotto[1]];
		// ACRutils.vetPrint(duepuntixb, "duepuntix SOTTO");
		// ACRutils.vetPrint(duepuntiyb, "duepuntiy SOTTO");
		plot1.setColor(Color.BLUE);
		plot1.addPoints(duepuntixb, duepuntiyb, Plot.CIRCLE);
		double[] vetout = new double[8];
		int count = 0;
		for (int i1 = 0; i1 < duepuntisopra.length; i1++) {
			count = 0;
			vetout[count++] = (double) duepuntisopra[i1];
			vetout[count++] = line1[duepuntisopra[i1]];
			vetout[count++] = (double) duepuntisopra[i1];
			vetout[count++] = line1[duepuntisopra[i1]];
			vetout[count++] = (double) duepuntisotto[i1];
			vetout[count++] = line1[duepuntisotto[i1]];
			vetout[count++] = (double) duepuntisotto[i1];
			vetout[count++] = line1[duepuntisotto[i1]];
		}
		// ACRutils.vetPrint(vetout, "vetout");

		return vetout;
	}

	/**
	 * Calcolo dei punti estremi della FWHM del diametro fantoccio. Provo a
	 * interpolare per sinistra e destra la coordinata X del punto preciso.
	 * 
	 * @param line1
	 * @param pseudomaxlen
	 * @param titolo
	 * @return
	 */
	public static double[][] FWHMpoints3vNEW(double[][] line3, int pseudomaxlen, String titolo, boolean step,
			boolean fast, boolean verbose) {

		// [][] line3 ha il formato:
		// line3[0] = profiX coordinata x del pixel su immagine
		// line3[1] = profiY coordinata y del pixel su immagine
		// line3[2] = profiZ valore segnale nei pixels per plottaggio profilo
		// line3[3] = profiW coordinata x per plottaggio profilo

		if (verbose) {
			ACRlog.logMatrix(line3, "line3");
			IJ.log("FWHMpoints3v >>  step= " + step + " fast= " + fast + " verbose= " + verbose);
		}

		Plot plot1 = null;
		double[] line1 = ACRutils.signal1vfrom3v(line3);
		if (line1 == null)
			ACRlog.waitHere("line1==null");
		double pmax = ACRutils.pseudomax(line1, pseudomaxlen);
		double fwhmlevel = pmax / 2;
		// disegno linea verde a fwhmlevel (mezza altezza)
		double[] xVetLineHalf = new double[2];
		double[] yVetLineHalf = new double[2];
		xVetLineHalf[0] = 0;
		xVetLineHalf[1] = line1.length;
		yVetLineHalf[0] = fwhmlevel;
		yVetLineHalf[1] = fwhmlevel;
		if (verbose) {
			plot1 = ACRutils.ACRplot(line1, titolo, Color.GREEN, true);
			plot1.addPoints(xVetLineHalf, yVetLineHalf, Plot.LINE);
			plot1.draw();
		}
		// ricerca due punti superiori al fwhmlevel
		double[][] duepuntisopra = ACRlocalizer.findPointsopra3v(line3, (int) fwhmlevel);

		// duepuntisopra[0][0] = leftx;
		// duepuntisopra[1][0] = lefty;
		// duepuntisopra[2][0] = leftz;
		// duepuntisopra[3][0] = leftw;
		// duepuntisopra[0][1] = rightx;
		// duepuntisopra[1][1] = righty;
		// duepuntisopra[2][1] = rightz;
		// duepuntisopra[3][1] = rightw;

		IJ.log("duepuntisopra leftx= " + duepuntisopra[0][0] + " lefty= " + duepuntisopra[1][0] + " leftz= "
				+ duepuntisopra[2][0] + " leftw= " + duepuntisopra[3][0]);
		IJ.log("duepuntisopra rightx= " + duepuntisopra[0][1] + " righty= " + duepuntisopra[1][1] + " rightz= "
				+ duepuntisopra[2][1] + " rightw= " + duepuntisopra[3][1]);

		double[] duepuntiwa = new double[2];
		double[] duepuntiza = new double[2];
		duepuntiwa[0] = duepuntisopra[3][0];
		duepuntiwa[1] = duepuntisopra[3][1];
		duepuntiza[0] = duepuntisopra[2][0];
		duepuntiza[1] = duepuntisopra[2][1];
		if (verbose) {
			plot1.setLineWidth(4);
			plot1.setColor(Color.RED);
			plot1.addPoints(duepuntiwa, duepuntiza, Plot.DIAMOND);
			ACRlog.logVector(duepuntiwa, "duepuntiwa");
			ACRlog.logVector(duepuntiza, "duepuntiza");
		}
		// ricerca dei due punti inferiori al fwhmlevel
		double[][] duepuntisotto = ACRlocalizer.findPointsotto3v(line3, (int) fwhmlevel);

		IJ.log("duepuntisotto leftx= " + duepuntisotto[0][0] + " lefty= " + duepuntisotto[1][0] + " leftz= "
				+ duepuntisotto[2][0] + " leftw= " + duepuntisotto[3][0]);
		IJ.log("duepuntisotto rightx= " + duepuntisotto[0][1] + " righty= " + duepuntisotto[1][1] + " rightz= "
				+ duepuntisotto[2][1] + " rightw= " + duepuntisotto[3][1]);

		double[] duepuntiwb = new double[2];
		double[] duepuntizb = new double[2];
		duepuntiwb[0] = duepuntisotto[3][0];
		duepuntiwb[1] = duepuntisotto[3][1];
		duepuntizb[0] = duepuntisotto[2][0];
		duepuntizb[1] = duepuntisotto[2][1];
		if (verbose) {
			plot1.setColor(Color.BLUE);
			plot1.addPoints(duepuntiwb, duepuntizb, Plot.DIAMOND);
			ACRlog.logVector(duepuntiwb, "duepuntiwb");
			ACRlog.logVector(duepuntizb, "duepuntizb");
		}
		// per complicarci la vita facciamo la interpolazione lineare tra il pixel sopra
		// e quello sotto (distanza 1 pixel cioe' 0.625 mm) otterremo dei double
		// interpolazione lineare coordinata X del punto sinistra
		double wa1 = (double) duepuntiwa[0];
		double za1 = (double) duepuntiza[0];
		double wb1 = (double) duepuntiwb[0];
		double zb1 = (double) duepuntizb[0];
		double zc1 = pmax / 2.;
		IJ.log("interpolazione lineare");
		double wc1 = ACRutils.xLinearInterpolation(wa1, za1, wb1, zb1, zc1);
		// interpolazione lineare coordinata X del punto destra
		double wa2 = (double) duepuntiwa[1];
		double za2 = (double) duepuntiza[1];
		double wb2 = (double) duepuntiwb[1];
		double zb2 = (double) duepuntizb[1];
		double zc2 = pmax / 2.;
		IJ.log("interpolazione lineare");
		double wc2 = ACRutils.xLinearInterpolation(wa2, za2, wb2, zb2, zc2);
		// tentativo di plot dei due punti interpolati
		double[] duepuntiwc = new double[2];
		double[] duepuntizc = new double[2];
		// duepuntiwc[0] = wc1; IW2AYV test escluso lnterpolazione
		// duepuntiwc[1] = wc2;
		duepuntiwc[0] = wa1;
		duepuntiwc[1] = wa2;
		duepuntizc[0] = fwhmlevel;
		duepuntizc[1] = fwhmlevel;
		if (verbose) {
			plot1.setColor(Color.GREEN);
			plot1.addPoints(duepuntiwc, duepuntizc, Plot.DIAMOND);
		}
		ACRlog.logVector(duepuntiwc, "duepuntiwc");
		ACRlog.logVector(duepuntizc, "duepuntizc");
//		ACRlog.waitHere("plottato anche il punto NERO interpolato");
		//
		// cerco ora di ricavare le coordinate immagine x ed y del punto interpolato
		// (double poiche' solo in seguito le arrotondo ad intero, sperando di risolvere
		// il problema dell'apparente erore di un pixel fuori per alcuni fitting
		//
		int len = line3[0].length;
		// ACRlog.waitHere("" + line3.length + " " + line3[0].length);
		double y2 = line3[1][len - 2];
		double y1 = line3[1][1];
		double x2 = line3[0][len - 2];
		double x1 = line3[0][1];
		double slope = 0;
		double xinterpsx = 0;
		double yinterpsx = 0;
		double xinterpdx = 0;
		double yinterpdx = 0;

		// Calcolo dello slope della retta su cui abbiamo ricavato il profilo:
		// escludiamo i casi particolari di retta orizzontale e verticale in cui lo
		// slope vale 0 ed infinito
		if (x1 == x2) {
			// linea orizzontale caso particolare
			xinterpsx = x1;
			yinterpsx = wc1;
			xinterpdx = x1;
			yinterpdx = wc2;
		} else if (y1 == y2) {
			// linea verticale caso particolare
			xinterpsx = wc1;
			yinterpsx = y1;
			xinterpdx = wc2;
			yinterpdx = y1;
		} else {
			// linea inclinata
			slope = (y2 - y1) / (x2 - x1);
			if (verbose) {
				IJ.log("slope= " + slope);
				// ACRlog.waitHere("slope= " + slope);
			}
			double xoffset = line3[0][0];
			double yoffset = line3[1][0];

			// lo slope sarebbe la tangente dell'angolo
			// per cui possiamo scrivere

			double sin = slope / Math.sqrt(1 + slope * slope);
			double cos = 1 / Math.sqrt(1 + slope * slope);
			if (slope > 0) {
				// punto sx
				xinterpsx = wc1 * sin + xoffset;
				yinterpsx = wc1 * cos + yoffset;
				// punto dw
				xinterpdx = wc2 * sin + xoffset;
				yinterpdx = wc2 * cos + yoffset;
			} else {
				// punto sx
				xinterpsx = Math.abs((len - wc1) * sin);
				yinterpsx = Math.abs((len - wc1) * cos);
				// punto dx
				xinterpdx = Math.abs((len - wc2) * sin);
				yinterpdx = Math.abs((len - wc2) * cos);
			}
		}
		IJ.log("SX punto interpolato su IMMAGINE= " + xinterpsx + " , " + yinterpsx);
		IJ.log("DX punto interpolato su IMMAGINE= " + xinterpdx + " , " + yinterpdx);

		// ACRutils.waitHere("TRACCIATA UNA BISETTRICE IMMAGINE E TROVATI GLI ESTREMI
		// OGGETTO");

		double leftxup = duepuntisopra[0][0];
		double leftyup = duepuntisopra[1][0];
		double rightxup = duepuntisopra[0][1];
		double rightyup = duepuntisopra[1][1];

		double[][] vetout = new double[2][4];

		vetout[0][0] = leftxup; // leftx non interpolato
		vetout[0][1] = rightxup; // rightx non interpolato
		vetout[0][2] = xinterpsx; // leftx interpolato
		vetout[0][3] = xinterpdx; // rightx interpolato

		vetout[1][0] = leftyup; // lefty non interpolato
		vetout[1][1] = rightyup; // rightx non interpolato
		vetout[1][2] = yinterpsx; // lefty interpolato
		vetout[1][3] = yinterpdx; // rightx interpolato

		if (verbose) {
			ACRlog.logMatrix(vetout, "vetout");
//			ACRlog.waitHere("attenzione: le coordinate precise sono in sx(vetout[0][2]) e dx(vetout[0][3])");
		}
		return vetout;
	}

	/**
	 * Calcolo dei punti estremi della FWHM del diametro fantoccio
	 * 
	 * @param line1
	 * @param pseudomaxlen
	 * @param titolo
	 * @return
	 */
	public static double[][] FWHMpoints3v(double[][] line3, int pseudomaxlen, String titolo, boolean step, boolean fast,
			boolean verbose) {

		ACRlog.waitHere("CIAO");
		// line3[0] = coordinata X punto
		// line3[1] = coordinata Y punto
		// line3[2] = segnale Z punto;
		// line3[3] = coordinata W su profilo;
		if (verbose)
			IJ.log("FWHMpoints3v >>  step= " + step + " fast= " + fast + " verbose= " + verbose);
		// estraggo dalla matrice line3 l'array contenente i valori dei pixel
		Plot plot1 = null;
		double[] line1 = ACRutils.signal1vfrom3v(line3);
		if (line1 == null)
			ACRlog.waitHere("line1==null");
		// calcolo la mezza altezza con smooth su pxeudomaxlen pixels
		double pmax = ACRutils.pseudomax(line1, pseudomaxlen);
		double halfmax = pmax / 2;
		// traccio sul plot la linea verde a mezza altezza
		double[] xVetLineHalf = new double[2];
		double[] yVetLineHalf = new double[2];
		xVetLineHalf[0] = 0;
		xVetLineHalf[1] = line1.length;
		yVetLineHalf[0] = halfmax;
		yVetLineHalf[1] = halfmax;
		if (verbose) {
			// plotto la linea verde a mezza altezza
			plot1 = ACRutils.ACRplot(line1, titolo, Color.GREEN, true);
			plot1.addPoints(xVetLineHalf, yVetLineHalf, Plot.LINE);
			plot1.draw();
		}
		// ricavo i due punti sopra alla mezza altezza
		double[][] duepuntisopra = ACRlocalizer.findPointsopra3v(line3, (int) halfmax);

		// duepuntisopra[0][0] = leftx;
		// duepuntisopra[1][0] = lefty;
		// duepuntisopra[2][0] = leftz;
		// duepuntisopra[3][0] = leftw;
		// duepuntisopra[0][1] = rightx;
		// duepuntisopra[1][1] = righty;
		// duepuntisopra[2][1] = rightz;
		// duepuntisopra[3][1] = rightw;

		double[] duepuntiza = new double[2];
		double[] duepuntiwa = new double[2];
		duepuntiwa[0] = duepuntisopra[3][0];
		duepuntiwa[1] = duepuntisopra[3][1];
		duepuntiza[0] = duepuntisopra[2][0];
		duepuntiza[1] = duepuntisopra[2][1];
		if (verbose) {
			// plotto i due punti sopra, sx e dx
			plot1.setColor(Color.RED);
			plot1.addPoints(duepuntiwa, duepuntiza, Plot.DIAMOND);
		}
		// ricavo i due punti sotto alla mezza altezza
		double[][] duepuntisotto = ACRlocalizer.findPointsotto3v(line3, (int) halfmax);
		double[] duepuntizb = new double[2];
		double[] duepuntiwb = new double[2];
		duepuntiwb[0] = duepuntisotto[3][0];
		duepuntiwb[1] = duepuntisotto[3][1];
		duepuntizb[0] = duepuntisotto[2][0];
		duepuntizb[1] = duepuntisotto[2][1];
		if (verbose) {
			// plotto i due punti sotto sx e dx
			plot1.setColor(Color.BLUE);
			plot1.addPoints(duepuntiwb, duepuntizb, Plot.DIAMOND);
		}
		// per complicarci la vita facciamo la interpolazione lineare tra il pixel sopra
		// e quello sotto (distanza 1 pixel cioe' 0.625 mm) otterremo dei double
		// interpolazione lineare punto sinistra
		double wa1 = (double) duepuntiwa[0];
		double za1 = (double) duepuntiza[0];
		double wb1 = (double) duepuntiwb[0];
		double zb1 = (double) duepuntizb[0];
		double zc1 = pmax / 2.;
		double wc1 = ACRutils.xLinearInterpolation(wa1, za1, wb1, zb1, zc1);
		// interpolazione lineare punto destra
		double wa2 = (double) duepuntiwa[1];
		double za2 = (double) duepuntiza[1];
		double wb2 = (double) duepuntiwb[1];
		double zb2 = (double) duepuntizb[1];
		double zc2 = pmax / 2.;
		double wc2 = ACRutils.xLinearInterpolation(wa2, za2, wb2, zb2, zc2);

		// cerco ora di ricavare le coordinate (double) x ed y del punto interpolato

		if (verbose) {
			IJ.log("punto interpolato a sx= " + wc1 + " , " + zc1);
			IJ.log("punto interpolato a dx= " + wc2 + " , " + zc2);
		}

		// ACRutils.waitHere("TRACCIATA UNA BISETTRICE IMMAGINE E TROVATI GLI ESTREMI
		// OGGETTO");

		// duepuntisopra[0][0] = leftx;
		// duepuntisopra[1][0] = lefty;
		// duepuntisopra[0][1] = rightx;
		// duepuntisopra[1][1] = righty;

		double[][] vetout = new double[2][4];
		vetout[0][0] = duepuntisopra[0][0]; // coordinata X pixel sopra sinistra
		vetout[1][0] = duepuntisopra[1][0]; // coordinata Y pixel sopra sinistra
		vetout[0][1] = duepuntisopra[0][1]; // coordinata X pixel sopra destra
		vetout[1][1] = duepuntisopra[1][1]; // coordinata Y pixel sopra destra
		vetout[0][2] = wc1; // coordinata X sinistra interpolata
		vetout[1][2] = zc1; // segnale mezza altezza
		vetout[0][3] = wc2; // coordinata X destra interpolata
		vetout[1][3] = zc2; // segnale mezza altezza

		if (verbose) {
			ACRlog.logMatrix(vetout, "vetout");
			ACRlog.waitHere("attenzione: le coordinate precise sono in sx(vetout[0][2]) e dx(vetout[0][3])");
		}
		return vetout;
	}

	public static int[] findPointsopra(double[] line1, double halfmax) {

		int left = 0;
		int right = 0;

		for (int i1 = 0; i1 < line1.length; i1++) {
			if (Double.compare(line1[i1], halfmax) >= 0) {
				left = i1;
				break;
			}
		}
		for (int i1 = line1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(line1[i1], halfmax) >= 0) {
				right = i1;
				break;
			}
		}
		int[] out1 = new int[2];
		out1[0] = left;
		out1[1] = right;

		return out1;
	}

	public static double[][] findPointsopra3v(double[][] line3, double halfmax) {

		double leftx = 0;
		double lefty = 0;
		double leftz = 0;
		double leftw = 0;
		double rightx = 0;
		double righty = 0;
		double rightz = 0;
		double rightw = 0;

		double[] x1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			x1[i1] = line3[0][i1];
		}
		double[] y1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			y1[i1] = line3[1][i1];
		}
		double[] z1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			z1[i1] = line3[2][i1];
		}
		double[] w1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			w1[i1] = line3[3][i1];
		}
		// cerco, partendo da sinistra del segmento il valore di pixel che supera od
		// uguaglia la meta'altezza del segnale
		for (int i1 = 0; i1 < z1.length; i1++) {
			if (Double.compare(z1[i1], halfmax) >= 0) {
				leftx = x1[i1];
				lefty = y1[i1];
				leftz = z1[i1];
				leftw = w1[i1];
				break;
			}
		}
		for (int i1 = z1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(z1[i1], halfmax) >= 0) {
				rightx = x1[i1];
				righty = y1[i1];
				rightz = z1[i1];
				rightw = w1[i1];
				break;
			}
		}
		double[][] out1 = new double[4][2];
		out1[0][0] = leftx;
		out1[1][0] = lefty;
		out1[2][0] = leftz;
		out1[3][0] = leftw;
		out1[0][1] = rightx;
		out1[1][1] = righty;
		out1[2][1] = rightz;
		out1[3][1] = rightw;

		return out1;
	}

	public static int[] findPointsotto(double[] line1, double pseudomax) {

		int left = 0;
		int right = 0;
		for (int i1 = 0; i1 < line1.length; i1++) {
			if (Double.compare(line1[i1], pseudomax) >= 0) {
				left = i1 - 1;
				break;
			}
		}
		for (int i1 = line1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(line1[i1], pseudomax) >= 0) {
				right = i1 + 1;
				break;
			}
		}
		int[] out1 = new int[2];
		out1[0] = left;
		out1[1] = right;

		return out1;
	}

	public static double[][] findPointsotto3v(double[][] line3, double pseudomax) {

		double leftx = 0;
		double lefty = 0;
		double leftz = 0;
		double leftw = 0;
		double rightx = 0;
		double righty = 0;
		double rightz = 0;
		double rightw = 0;

		double[] x1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			x1[i1] = line3[0][i1];
		}
		double[] y1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			y1[i1] = line3[1][i1];
		}
		double[] z1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			z1[i1] = line3[2][i1];
		}
		double[] w1 = new double[line3[0].length];
		for (int i1 = 0; i1 < line3[0].length; i1++) {
			w1[i1] = line3[3][i1];
		}
		for (int i1 = 0; i1 < z1.length; i1++) {
			if (Double.compare(z1[i1], pseudomax) >= 0) {
				leftx = x1[i1 - 1];
				lefty = y1[i1 - 1];
				leftz = z1[i1 - 1];
				leftw = w1[i1 - 1];
				break;
			}
		}
		for (int i1 = z1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(z1[i1], pseudomax) >= 0) {
				rightx = x1[i1 + 1];
				righty = y1[i1 + 1];
				rightz = z1[i1 + 1];
				rightw = w1[i1 + 1];
				break;
			}
		}
		double[][] out1 = new double[4][2];
		out1[0][0] = leftx;
		out1[1][0] = lefty;
		out1[2][0] = leftz;
		out1[3][0] = leftw;
		out1[0][1] = rightx;
		out1[1][1] = righty;
		out1[2][1] = rightz;
		out1[3][1] = rightw;

		return out1;
	}

	public static int[] verticalSearch(ImagePlus imp1, double water, int xposition, boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
		double halfwater = water / 2;
		int spessore = 1;
		imp1.deleteRoi();
		int height = imp1.getHeight();
		Line.setWidth(spessore);
		int x1 = xposition;
		int y1 = 0;
		int x2 = xposition;
		int y2 = height;
		imp1.setRoi(new Line(x1, y1, x2, y2));
		imp1.updateAndDraw();
		Roi roi1 = imp1.getRoi();
		double[] profi1 = ((Line) roi1).getPixels();
		if (verbose)
			ACRlog.logVector(profi1, "verticalSearch profi1");
		int[] out1 = profileSearch(profi1, halfwater, verbose);
		if (verbose)
			ACRlog.logVector(out1, "verticalSearch out1");
		return out1;
	}

	public static double[] verticalSearchInterpolated(ImagePlus imp1, double water, int xposition, boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
		double halfwater = water / 2;
		int spessore = 1;
		imp1.deleteRoi();
		int height = imp1.getHeight();
		Line.setWidth(spessore);
		int x1 = xposition;
		int y1 = 0;
		int x2 = xposition;
		int y2 = height;
		imp1.setRoi(new Line(x1, y1, x2, y2));
		imp1.updateAndDraw();
		Roi roi1 = imp1.getRoi();
		double[] profi1 = ((Line) roi1).getPixels();
		if (verbose)
			ACRlog.logVector(profi1, "verticalSearchInterpolated profi1");

		double[] out1 = profileSearchInterpolated(profi1, halfwater, verbose);
		if (verbose)
			ACRlog.logVector(out1, "verticalSearchInterpolated out1");
		return out1;
	}

	public static int[] horizontalSearch(ImagePlus imp1, double water, int yposition, boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
		double halfwater = water / 2;
		int spessore = 1;
		imp1.deleteRoi();
		int width = imp1.getWidth();
		Line.setWidth(spessore);
		int x1 = 0;
		int y1 = yposition;
		int x2 = width;
		int y2 = yposition;
		imp1.setRoi(new Line(x1, y1, x2, y2));
		imp1.updateAndDraw();
		Roi roi1 = imp1.getRoi();
		double[] profi1 = ((Line) roi1).getPixels();
		if (verbose)
			ACRlog.logVector(profi1, "horizontalSearch profi1");
		int[] out1 = profileSearch(profi1, halfwater, verbose);
		if (verbose)
			ACRlog.logVector(out1, "horizontalSearch out1");

		return out1;
	}

	public static double[] horizontalSearchInterpolated(ImagePlus imp1, double water, int yposition, boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
		double halfwater = water / 2;
		int spessore = 1;
		imp1.deleteRoi();
		int width = imp1.getWidth();
		Line.setWidth(spessore);
		int x1 = 0;
		int y1 = yposition;
		int x2 = width;
		int y2 = yposition;
		imp1.setRoi(new Line(x1, y1, x2, y2));
		imp1.updateAndDraw();
		Roi roi1 = imp1.getRoi();
		double[] profi1 = ((Line) roi1).getPixels();
		if (verbose)
			ACRlog.logVector(profi1, "horizontalSearchInterpolated profi1");
		double[] out1 = profileSearchInterpolated(profi1, halfwater, verbose);
		if (verbose)
			ACRlog.logVector(out1, "horizontalSearchInterpolated out1");
		return out1;
	}

}
