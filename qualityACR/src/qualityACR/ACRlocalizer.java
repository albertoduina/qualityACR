package qualityACR;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.NewImage;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.RotatedRectRoi;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.Thresholder;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.AutoThresholder;
import ij.process.AutoThresholder.Method;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class ACRlocalizer {

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
			boolean verbose, int timeout) {

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
//		int diamCircleMan = 0;
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
			ACRlog.waitHere("L'immagine in input viene processata con il Canny Edge Detector", ACRutils.debug, timeout);
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
			ACRlog.waitHere("L'immagine risultante e' una immagine ad 8 bit, con i soli valori \n"
					+ "0 e 255. Lo spessore del perimetro del cerchio e' di 1 pixel", ACRutils.debug, timeout);
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

		int count = -1;
		boolean vertical = false;
		boolean valido = true;
		String motivo = "";
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
			boolean profilo = false;
			if (showProfiles)
				profilo = true;

			myPeaks = ACRutils.cannyProfileAnalyzer(imp12, dimPixel, vetTitle[i1], profilo, step, vertical, verbose,
					timeout);

			String direction1 = ACRutils.readDicomParameter(imp11, ACRconst.DICOM_IMAGE_ORIENTATION);

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
						if (imp12.isVisible()) {
							ImageWindow aa = imp12.getWindow();
							aa.toFront();
						}
					}
				}
			}
		}
		if (verbose)
			ACRlog.waitHere("Tracciate tutte le linee", ACRutils.debug, timeout);
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
			ACRlog.waitHere("La circonferenza risultante dal fit e' mostrata in rosso", ACRutils.debug, timeout);
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
			ACRlog.waitHere("Il centro del fantoccio e' contrassegnato dal pallino rosso", ACRutils.debug, timeout);
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
					false, false, 1);
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
					false, false, 1);
			double gapOrizz = 0;
			if (peaks10 != null) {
//				ImageUtils.plotPoints(imp12, over12, peaks10);
				gapOrizz = diamCircle / 2 - (xCenterCircle - peaks10[3][0]);
			}
			if (verbose)
				ACRlog.waitHere("Si determina ora l'eventuale dimensione delle bolla d'aria, \n"
						+ "essa viene automaticamente compensata entro il limite del \n" + "\"maxBubbleGapLimit\"= "
						+ maxBubbleGapLimit, ACRutils.debug, timeout);
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
				ACRlog.waitHere("DiamCircle= " + diamCircle, ACRutils.debug, timeout);
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
					false, false, false, 1);
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
					false, false, false, 1);
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
					timeout);
			ACRgraphic.drawCenter(imp12, over12, xCenterCircle + xcorr, yCenterCircle + ycorr, colore2);
			ACRlog.waitHere("Il centro della MROI e' contrassegnato dal pallino verde", ACRutils.debug, timeout);
			IJ.beep();
			ACRlog.waitHere("TERMINE PHANTOM SEARCH VERBOSE", ACRutils.debug, timeout);
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
	 * @param imp2        immagine da analizzare
	 * @param maxFitError limite errore sul fit
	 * @param step        esegue passo passo
	 * @param fast        preme automaticamente ok ai messaggi, dopo timeout
	 *                    millisecondi
	 * @param verbose     massimo livello di grafica
	 * @param timeout     millisecondi per OK
	 * @return vettore contenente i dati del fantoccio circolare localizzato
	 */
	public static int[] positionSearch2(ImagePlus imp2, double maxFitError, boolean step, boolean verbose,
			int timeout) {

		if (verbose)
			IJ.log("<ACRlocalizer.positionSearch2 START>  step= " + step + " verbose= " + verbose);

		if (step)
			ACRlog.waitHere(">>> 01 - RICERCA POSIZIONE E DIAMETRO FANTOCCIO <<<", ACRutils.debug, timeout);

		// ricerca posizione e diametro fantoccio

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
			// estrazione matrice con segnale pixels e loro coordinate su immagine
			double[][] decomposed3v = ACRutils.decomposer3v(imp2);
			// decomposed3v[0] = profiX coordinata x del pixel su immagine
			// decomposed3v[1] = profiY coordinata y del pixel su immagine
			// decomposed3v[2] = profiZ valore segnale nei pixels per plottaggio
			// decomposed3v[3] = profiW coordinata x per plottaggio
			if (verbose)
				ACRlog.logMatrix(decomposed3v, ACRlog.qui() + " decomposed3v");
			// azzera segnale primo ed ultimo pixel del profilo
			double[][] zeropadded3v = ACRutils.zeropadProfile3v(decomposed3v);
			if (verbose)
				ACRlog.logMatrix(zeropadded3v, ACRlog.qui() + " zeropadded3v INPUT FWHMpoints3vNEW");
			// ACRlog.waitHere();
			// cerca i due punti sopra e i due punti sotto mezza altezza

			if (verbose)
				IJ.log(ACRlog.qui() + " ==== PROFILO " + vetTitle[i1] + " ====");
			int pseudomaxlen = 3;
			double[][] vetout = ACRlocalizer.FWHManalyzer(zeropadded3v, pseudomaxlen, "PROFILO " + vetTitle[i1], step,
					verbose);

			// vetout[0][0] = leftx nature;___ vetout[1][0] = lefty nature
			// vetout[0][1] = rightx nature;__ vetout[1][1] = rightx nature
			// vetout[0][2] = leftx interp;___ vetout[1][2] = lefty interp
			// vetout[0][3] = rightx interp;__ vetout[1][3] = rightx interp
			if (verbose) {
				IJ.log(ACRlog.qui() + " ==========NOW=========");
				IJ.log(ACRlog.qui() + " OUTPUT DI FWHManalyzer");
				ACRlog.logMatrix(vetout, ACRlog.qui() + " vetout");
				IJ.log(ACRlog.qui() + " ======================");
			}

			int xpoint1 = (int) Math.round(vetout[0][2]); // vediamo gli interpolati
			int ypoint1 = (int) Math.round(vetout[1][2]); // vediamo gli interpolati
			int xpoint2 = (int) Math.round(vetout[0][3]); // vediamo gli interpolati
			int ypoint2 = (int) Math.round(vetout[1][3]); // vediamo gli interpolati
			ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, Color.GREEN);
			ACRutils.plotPoints(imp2, over2, xpoint2, ypoint2, Color.GREEN);
			if (verbose) {
				IJ.log(ACRlog.qui() + "PUNTO INIZIALE " + xpoint1 + " , " + ypoint1);
				IJ.log(ACRlog.qui() + "PUNTO FINALE " + xpoint2 + " , " + ypoint2);
				ACRlog.waitHere("PROFILO " + vetTitle[i1] + " con punti trovati, riportati anche su immagine",
						ACRutils.debug, timeout);
			}

			if (verbose) {
				IJ.log(ACRlog.qui() + " prima point= " + vetout[0][0] + "  " + vetout[1][0]);
				IJ.log(ACRlog.qui() + " diventa point= " + vetout[0][2] + "  " + vetout[1][0]);
			}

			xcircle[count1] = (float) vetout[0][2];
			ycircle[count1] = (float) vetout[1][0];
			vetx[count1] = (int) xpoint1;
			vety[count1] = (int) ypoint1;
			count1++;

			xcircle[count1] = (float) vetout[0][3];
			ycircle[count1] = (float) vetout[1][1];
			count1++;

			if (step) {
				ACRlog.waitHere("<ACRlocalizer.positionSearch2> Punti plottati VERDE su immagine con coordinate "
						+ xpoint1 + "," + ypoint1 + "   " + xpoint2 + "," + ypoint2, Geometric_Accuracy.debug, timeout);
			}
			imp2.updateAndDraw();
			// iw2ayv

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
			ACRlog.waitHere("La circonferenza risultante dal fit e' mostrata in verde", Geometric_Accuracy.debug,
					timeout);
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
	 * @param imp3    immagine da analizzare
	 * @param step    passo a passo
	 * @param fast    animazione
	 * @param timeout con fast preme automaticamente ok dopo timeout, si ha una
	 *                pseudo animazione
	 * @return array contenente i dati del cerchio localizzato ed i punti delle
	 *         quattro misure eseguite
	 */
	public static double[] positionSearch3(ImagePlus imp1, int[] phantomCircle, boolean step, boolean verbose,
			int timeout) {

		// al contrario di positionSearch2, dove non conoscevamo posizione e diametro
		// del fantoccio, in questo caso questi dati ci sono noti, ora vogliamo fare una
		// misura precisa dei diametri, presi in modo che passino correttamente per il
		// centro reale del fantoccio circolare

		if (verbose)
			IJ.log(ACRlog.qui() + "START>  step= " + step + " verbose= " + verbose);

		if (step)
			ACRlog.waitHere(">>> 02 - MISURA PRECISA DIAMETRI FANTOCCIO <<<", ACRutils.debug, timeout);

//		ImagePlus imp1 = imp3.duplicate();
//		imp1.show();
//		ImageWindow iw2 = imp1.getWindow();
//
//		ACRutils.zoom(imp1);

		Overlay over1 = new Overlay();
		imp1.setOverlay(over1);
		int height = imp1.getHeight();
		int width = imp1.getWidth();
		// estraggo i dati da phantomCircle
		int xcenter = phantomCircle[0];
		int ycenter = phantomCircle[1];
		int diam = phantomCircle[2];
		//
		// per tracciare le diagonali, centrate sul cerchio e non sulla immagine, devo
		// calcolare correttamente le nuove intersezioni della diagonale
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
		;
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
		over1.clear();
		// effettuo in tutto 4 profili
		int[] vetXstart = new int[4];
		int[] vetYstart = new int[4];
		int[] vetXend = new int[4];
		int[] vetYend = new int[4];
		int[] xcoord = new int[2];
		int[] ycoord = new int[2];
		// ---- orizzontale
		// per verifica grafica vado a plottare i punti di clipping delle diagonali,
		// punti che mi servono per impostare in ImageJ la Line
		vetXstart[0] = 0;
		vetYstart[0] = ycenter;
//		ACRutils.plotPoints(imp2, over2, vetXstart[0], vetYstart[0], Color.RED, 2, 6);
		vetXend[0] = width;
		vetYend[0] = ycenter;
//		ACRutils.plotPoints(imp2, over2, vetXend[0], vetYend[0], Color.BLUE, 2, 6);
		// ---- verticale
		vetXstart[1] = xcenter;
		vetYstart[1] = 0;
//		ACRutils.plotPoints(imp2, over2, vetXstart[1], vetYstart[1], Color.RED, 2, 6);
		vetXend[1] = xcenter;
		vetYend[1] = height;
//		ACRutils.plotPoints(imp2, over2, vetXend[1], vetYend[1], Color.BLUE, 2, 6); // ---- diagonale sx
		vetXstart[2] = (int) clippings1[0];
		vetYstart[2] = (int) clippings1[1];
//		ACRutils.plotPoints(imp2, over2, vetXstart[2], vetYstart[2], Color.RED, 2, 6);
		vetXend[2] = (int) clippings1[2];
		vetYend[2] = (int) clippings1[3];
//		ACRutils.plotPoints(imp2, over2, vetXend[2], vetYend[2], Color.BLUE, 2, 6); // ----- diagonale dx
		vetXstart[3] = (int) clippings2[0];
		vetYstart[3] = (int) clippings2[1];
//		ACRutils.plotPoints(imp2, over2, vetXstart[3], vetYstart[3], Color.RED, 2, 6);
		vetXend[3] = (int) clippings2[2];
		vetYend[3] = (int) clippings2[3];
//		ACRutils.plotPoints(imp2, over2, vetXend[3], vetYend[3], Color.BLUE, 2, 6); // -----

		String[] vetTitle = { "ORIZZONTALE", "VERTICALE", "DIAGONALE_SX", "DIAGONALE_DX" };
		// line analyzer
		double[] out1 = new double[4];
		for (int i1 = 0; i1 < 4; i1++) {
			if (step || verbose)
				IJ.log(ACRlog.qui() + "PROFILO " + vetTitle[i1]);
			// viene ripetuto per ogni profilo
			xcoord[0] = vetXstart[i1];
			ycoord[0] = vetYstart[i1];
			xcoord[1] = vetXend[i1];
			ycoord[1] = vetYend[i1];
			imp1.setRoi(new Line(xcoord[0], ycoord[0], xcoord[1], ycoord[1]));
			imp1.getRoi().setStrokeColor(Color.RED);
			over1.addElement(imp1.getRoi());
			int pseudomaxlen = 3;
			//
			double[][] decomposed3v = ACRutils.decomposer3v(imp1);
			double[][] zeropadded3v = ACRutils.zeropadProfile3v(decomposed3v);
			double[][] vetout = ACRlocalizer.FWHManalyzer(zeropadded3v, pseudomaxlen, "PROFILO " + vetTitle[i1], step,
					verbose);
			// vetout[0][0] = leftx nature;___ vetout[1][0] = lefty nature
			// vetout[0][1] = rightx nature;__ vetout[1][1] = rightx nature
			// vetout[0][2] = leftx interp;___ vetout[1][2] = lefty interp
			// vetout[0][3] = rightx interp;__ vetout[1][3] = rightx interp
			if (verbose) {
				IJ.log(ACRlog.qui() + " ==========NOW=========");
				IJ.log(ACRlog.qui() + " OUTPUT DI FWHManalyzer");
				ACRlog.logMatrix(vetout, ACRlog.qui() + " vetout");
				IJ.log(ACRlog.qui() + " ======================");
			}

			int xpoint1 = (int) Math.round(vetout[0][0]);
			int ypoint1 = (int) Math.round(vetout[1][0]);
			if (verbose) {
				IJ.log(ACRlog.qui() + "PUNZONA punto sinistro su immagine x= " + xpoint1 + " y= " + ypoint1);
			}
			ACRutils.plotPoints(imp1, over1, xpoint1, ypoint1, Color.RED, 2, 2);

			int xpoint2 = (int) Math.round(vetout[0][1]);
			int ypoint2 = (int) Math.round(vetout[1][1]);
			if (verbose) {
				IJ.log(ACRlog.qui() + "PUNZONA punto destro su immagine x= " + xpoint2 + " y= " + ypoint2);
			}
			ACRutils.plotPoints(imp1, over1, xpoint2, ypoint2, Color.RED, 2, 2);

			imp1.updateAndDraw();

			if (step)
				ACRlog.waitHere("PROFILO " + vetTitle[i1] + " con punti trovati, riportati anche su immagine",
						ACRutils.debug, timeout);

			double dist = Math
					.sqrt(Math.pow((vetout[0][2] - vetout[0][3]), 2) + Math.pow((vetout[1][2] - vetout[1][3]), 2));

			double diameter = dist; // IN PIXEL
			if (verbose)
				IJ.log(ACRlog.qui() + "END> diametro " + i1 + " " + diameter);

			out1[i1] = diameter;
		}

		return out1;
	}

	/**
	 * Ricerca di oggetti nell'immagine con la scansione orizzontale e verticale.
	 * Non elimina i doppioni.
	 * 
	 * @param imp1
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static double[] gridLocalizerOLD(ImagePlus imp1, boolean step, boolean fast, boolean verbose, int timeout) {

		boolean verbose2 = false;

		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);
		IJ.log(ACRlog.qui());
		int latoROI = 11;
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);
		//
		// Ricerca posizione del massimo con una roi quadrata di lato dispari 11x11,
		// restituisce le coordinate del centro
		//
		double[] water = maxPositionGeneric(imp2, latoROI);
		//
		// uso il segnale max trovato per definire il threshold
		//
		int threshold = (int) water[2] * 3 / 4;
		//
		// ora scansiono l'immagine, riga per riga, evito volutamente il bordo di 1
		// pixel attorno ai bordi, area dove di solito succedono COSSE BRUUTTE ASSAI!
		//
		int[] out2 = new int[4];
		double[] out3 = new double[4];
		double xpoint1 = 0;
		double ypoint1 = 0;
		double xpoint2 = 0;
		double ypoint2 = 0;

		ArrayList<Float> arrIntX = new ArrayList<>();
		ArrayList<Float> arrIntY = new ArrayList<>();
		ArrayList<Integer> arrX = new ArrayList<>();
		ArrayList<Integer> arrY = new ArrayList<>();

		// scansione per righe
		for (int y1 = 1; y1 < height - 1; y1++) {

			out2 = horizontalSearch(imp2, threshold, y1, verbose2);
			// qui intendo plottare sull'overlay, per vedere i risultati ottenibili,
			if (out2 != null) {
				xpoint1 = out2[0];
				ypoint1 = y1;
				arrX.add(out2[0]);
				arrY.add(y1);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, type, size, Color.YELLOW, false);
				xpoint1 = out2[1];
				ypoint1 = y1;
				arrX.add(out2[1]);
				arrY.add(y1);
				ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, type, size, Color.YELLOW, false);
			}
		}
		if (step || verbose)
			ACRlog.waitHere(ACRlog.qui() + "scansione orizzontale su interi colore giallo");

		// scansione per colonne
		for (int x1 = 1; x1 < width - 1; x1++) {

			out2 = verticalSearch(imp2, threshold, x1, verbose2);
			if (out2 != null) {
				// qui intendo plottare sull'overlay, per vedere i risultati ottenibili
				xpoint1 = x1;
				ypoint1 = out2[0];
				arrX.add(x1);
				arrY.add(out2[0]);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, type, size, Color.BLUE, false);
				xpoint1 = x1;
				ypoint1 = out2[1];
				xpoint1 = x1;
				ypoint1 = out2[1];
				ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, type, size, Color.BLUE, false);
			}
		}
		if (step || verbose)
			ACRlog.waitHere(ACRlog.qui() + "scansione verticale su interi colore blu");

		// scansione per righe
		for (int y1 = 1; y1 < height - 1; y1++) {

			out3 = horizontalSearchInterpolated(imp2, threshold, y1, verbose2);
			if (out3 != null) {
				xpoint2 = out3[0];
				ypoint2 = (double) y1;
				arrIntX.add((float) out3[0]);
				arrIntY.add((float) y1);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp2, over2, xpoint2, ypoint2, type, size, Color.RED, true);
				xpoint2 = out3[1];
				ypoint2 = y1;
				arrIntX.add((float) out3[1]);
				arrIntY.add((float) y1);
				ACRutils.plotPoints(imp2, over2, xpoint2, ypoint2, type, size, Color.RED, true);
			}
		}
		if (step || verbose)
			ACRlog.waitHere(ACRlog.qui() + "scansione orizzontale interpolata colore rosso");

		// scansione per colonne
		for (int x1 = 1; x1 < width - 1; x1++) {

			out3 = verticalSearchInterpolated(imp2, threshold, x1, verbose2);
			if (out3 != null) {
				xpoint2 = (double) x1;
				ypoint2 = out3[0];
				arrIntX.add((float) x1);
				arrIntY.add((float) out3[0]);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp2, over2, xpoint2, ypoint2, type, size, Color.GREEN, true);
				xpoint2 = (double) x1;
				ypoint2 = out3[1];
				arrIntX.add((float) x1);
				arrIntY.add((float) out3[1]);
				ACRutils.plotPoints(imp2, over2, xpoint2, ypoint2, type, size, Color.GREEN, true);
			}
		}
		if (step || verbose) {
			ACRlog.waitHere(ACRlog.qui() + "scansione verticale interpolata colore verde");

			ACRlog.waitHere(
					ACRlog.qui() + "ora ripuliamo gli overlay e disegnamo il cerchio calcolato con le interpolazioni");
		}
		over2.clear();

		float[] vetX = ACRcalc.arrayListToArrayFloat(arrIntX);
		float[] vetY = ACRcalc.arrayListToArrayFloat(arrIntY);

		PointRoi pr12 = new PointRoi(vetX, vetY);
		pr12.setPointType(2);
		pr12.setSize(4);
		imp2.updateAndDraw();
		// ---------------------------------------------------
		// eseguo ora fitCircle per trovare centro e dimensione grossolana del
		// fantoccio. FitCircle è copiato da ImageJ ed era a sua volta derivato dal
		// programma BoneJ
		// ---------------------------------------------------
		imp2.setRoi(new PointRoi(vetX, vetY, vetX.length));
		double[] out5 = ACRgraphic.fitCircleNew(imp2);
		if (verbose)
			ACRlog.logVector(out5, ACRlog.qui() + "out5");

		double xCenterCircle = out5[0];
		double yCenterCircle = out5[1];
		double diamCircle = out5[2];

		imp2.setRoi(
				new OvalRoi(xCenterCircle - diamCircle / 2, yCenterCircle - diamCircle / 2, diamCircle, diamCircle));

		imp2.getRoi().setStrokeColor(Color.YELLOW);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();

		if (step) {
			ACRlog.waitHere(ACRlog.qui() + "La circonferenza risultante dal fit non interpolato e' mostrata in GIALLO",
					true, timeout);
		}

		float[] vetX3 = ACRcalc.arrayListToArrayFloat(arrIntX);
		float[] vetY3 = ACRcalc.arrayListToArrayFloat(arrIntY);
		double[][] points3 = new double[vetX3.length][2];
		for (int i1 = 0; i1 < vetX3.length; i1++) {
			points3[i1][0] = (double) vetX3[i1];
			points3[i1][1] = (double) vetY3[i1];
		}

		double[] out4 = new double[3];
		out4[0] = xCenterCircle;
		out4[1] = yCenterCircle;
		out4[2] = diamCircle;
		imp2.close();
		return out4;

	}

	/**
	 * Ricerca rapida fantoccio circolare nell'imagine, con scansioni verticale ed
	 * orizzontale. Versione basata sugli integer e che non utilizza le
	 * interpolazioni.
	 * 
	 * @param imp1
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static double[] phantomLocalizerAdvanced(ImagePlus imp1, boolean step, boolean verbose, int timeout) {

		boolean verbose2 = false;

		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);

		int latoROI = 11;
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);
		//
		// Ricerca posizione del massimo con una roi quadrata di lato dispari 11x11,
		// restituisce le coordinate del centro
		//
		double[] water = maxPositionGeneric(imp2, latoROI);
		//
		// uso il segnale max trovato per definire il threshold
		//
		int threshold = (int) water[2] * 1 / 2;
		//
		// ora scansiono l'immagine, riga per riga, obbligo il segnale ad andare basso,
		// prima
		//
		int[] out2 = new int[4];
		double xpoint1 = 0;
		double ypoint1 = 0;

		ArrayList<Integer> arrX = new ArrayList<>();
		ArrayList<Integer> arrY = new ArrayList<>();

		// scansione per righe
		for (int y1 = 1; y1 < height - 1; y1++) {

			out2 = horizontalSearchAdvanced(imp2, threshold, y1, verbose2);
			// qui intendo plottare sull'overlay, per vedere i risultati ottenibili,
			if (out2 != null) {
				xpoint1 = out2[0];
				ypoint1 = y1;
				arrX.add(out2[0]);
				arrY.add(y1);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				if (verbose || step)
					ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, type, size, Color.YELLOW, false);
				xpoint1 = out2[1];
				ypoint1 = y1;
				arrX.add(out2[1]);
				arrY.add(y1);
				if (verbose || step)
					ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, type, size, Color.YELLOW, false);
			}
		}
		if (step || verbose)
			ACRlog.waitHere("scansione orizzontale su interi colore giallo", ACRutils.debug, timeout);

		// scansione per colonne
		for (int x1 = 1; x1 < width - 1; x1++) {

			out2 = verticalSearchAdvanced(imp2, threshold, x1, verbose2);
			if (out2 != null && (verbose || step)) {
				// qui intendo plottare sull'overlay, per vedere i risultati ottenibili
				xpoint1 = x1;
				ypoint1 = out2[0];
				arrX.add(x1);
				arrY.add(out2[0]);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				if (verbose || step)
					ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, type, size, Color.BLUE, false);
				xpoint1 = x1;
				ypoint1 = out2[1];
				xpoint1 = x1;
				ypoint1 = out2[1];
				if (verbose || step)
					ACRutils.plotPoints(imp2, over2, xpoint1, ypoint1, type, size, Color.BLUE, false);
			}
		}

		// ACRlog.waitHere();

		if (step || verbose)
			ACRlog.waitHere("scansione verticale su interi colore blu", ACRutils.debug, timeout);

		int[] vetX = ACRcalc.arrayListToArrayInt(arrX);
		int[] vetY = ACRcalc.arrayListToArrayInt(arrY);

		PointRoi pr12 = new PointRoi(vetX, vetY, vetX.length);
		pr12.setPointType(2);
		pr12.setSize(4);
		imp2.updateAndDraw();
		// ---------------------------------------------------
		// eseguo ora fitCircle per trovare centro e dimensione grossolana del
		// fantoccio. FitCircle è copiato da ImageJ ed era a sua volta derivato dal
		// programma BoneJ
		// ---------------------------------------------------
		imp2.setRoi(new PointRoi(vetX, vetY, vetX.length));
		double[] out5 = ACRgraphic.fitCircleNew(imp2);
		if (verbose)
			ACRlog.logVector(out5, ACRlog.qui() + "out5");

		double xCenterCircle = out5[0];
		double yCenterCircle = out5[1];
		double diamCircle = out5[2];

		imp2.setRoi(
				new OvalRoi(xCenterCircle - diamCircle / 2, yCenterCircle - diamCircle / 2, diamCircle, diamCircle));

		imp2.getRoi().setStrokeColor(Color.YELLOW);
		over2.addElement(imp2.getRoi());
		imp2.killRoi();

		if (verbose || step) {
			ACRlog.waitHere("La circonferenza risultante dal fit non interpolato e' mostrata in GIALLO", true, timeout);
		}

		imp2.close();

		double[] out4 = new double[3];
		out4[0] = xCenterCircle;
		out4[1] = yCenterCircle;
		out4[2] = diamCircle;
		return out4;

	}

	/**
	 * Ricerca posizione del massimo di segnale, sull'intera immagine, con una roi
	 * programmabile di lato dispari, che scorre su tutta l'immagine. Calcola il
	 * segnale medio della ROI e ne memorizziamo valore max e posizione in cui lo si
	 * incontra. Restituisce valore max e le coordinate del centro ROI in cui si
	 * trova (una sola posizione prevista)
	 * 
	 * @param imp1
	 * @param latoROI
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
		// restituiamo il valore massimo tra le medie delle ROI e le sue coordinate
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
	 * supera od eguaglia il threshold.
	 * 
	 * @param profi1
	 * @param threshold
	 * @return
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
	 * Ricerca lungo i pixel di un profilo, in pratica una specie di FWHM. Per
	 * prevenire strani scherzi da effetti digitali sui bordi, attende che il
	 * segnale scenda sotto al valore di threshold, prima di considerarlo valido.
	 * Restituisce il valore del pixel che supera od eguaglia il threshold.
	 * 
	 * @param profi1
	 * @param threshold
	 * @param stampa
	 * @return
	 */
	public static int[] profileSearchAdvanced(double[] profi1, double threshold, boolean stampa) {

		boolean valido = false;
		int startpoint = -1;
		// testo
		valido = false;
		for (int j1 = 0; j1 < profi1.length; j1++) {
			if (Double.compare(profi1[j1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(profi1[j1], threshold) >= 0) {
				startpoint = j1;
				break;
			}
		}

		int endpoint = -1;
		valido = false;
		for (int j1 = profi1.length - 1; j1 >= 0; j1--) {
			if (Double.compare(profi1[j1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(profi1[j1], threshold) >= 0) {
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
	 * dovuti a difetti digitali delle immagini. Restituisce il valore del pixel che
	 * supera od eguaglia il threshold.
	 * 
	 * @param profi1
	 * @param threshold
	 * @param stampa
	 * @return
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
	 * @param profi1
	 * @param threshold
	 * @param stampa
	 * @return
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
	 * @param profi1    profilo da elaborare
	 * @param threshold frazione del presunto segnale dell'acqua
	 * @param stampa
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
	 * Calcolo della FWHM di un profilo
	 * 
	 * @param line1
	 * @param pseudomaxlen
	 * @param titolo
	 * @return
	 */
	public static double FWHMcalc(double[] line1, int pseudomaxlen, String titolo, String pathname, String pathReport,
			String codice) {

		double pmax = ACRutils.pseudomax(line1, pseudomaxlen);
		double threshold = pmax / 2;
		double[] xVetLineHalf = new double[2];
		double[] yVetLineHalf = new double[2];
		xVetLineHalf[0] = 0;
		xVetLineHalf[1] = line1.length;
		yVetLineHalf[0] = threshold;
		yVetLineHalf[1] = threshold;
		String title = titolo;
		Plot plot1 = ACRutils.ACRplot(line1, title, Color.GREEN, false);
		plot1.addPoints(xVetLineHalf, yVetLineHalf, Plot.LINE);
		plot1.draw();
		int[] quattropunti = ACRlocalizer.findFWHMpoints(line1, threshold);
		double[] duepuntix = new double[2];
		duepuntix[0] = (double) quattropunti[0];
		duepuntix[1] = (double) quattropunti[2];
		double[] duepuntiy = new double[2];
		duepuntiy[0] = line1[quattropunti[0]];
		duepuntiy[1] = line1[quattropunti[2]];
		plot1.setLineWidth(7);
		plot1.setColor(Color.RED);
		plot1.addPoints(duepuntix, duepuntiy, Plot.DOT);
		duepuntix[0] = (double) quattropunti[1];
		duepuntix[1] = (double) quattropunti[3];
		duepuntiy[0] = line1[quattropunti[1]];
		duepuntiy[1] = line1[quattropunti[3]];
//		plot1.setLineWidth(6);
		plot1.setColor(Color.BLUE);
		plot1.addPoints(duepuntix, duepuntiy, Plot.DOT);

		double xa1 = (double) quattropunti[0];
		double ya1 = (double) line1[quattropunti[0]];
		double xb1 = (double) quattropunti[1];
		double yb1 = (double) line1[quattropunti[1]];
		double yc1 = threshold;
		double xc1 = ACRutils.xLinearInterpolation(xa1, ya1, xb1, yb1, yc1);

		double xa2 = (double) quattropunti[2];
		double ya2 = (double) line1[quattropunti[2]];
		double xb2 = (double) quattropunti[3];
		double yb2 = (double) line1[quattropunti[3]];
		double yc2 = threshold;
		double xc2 = ACRutils.xLinearInterpolation(xa2, ya2, xb2, yb2, yc2);

		double fwhmpixel1 = quattropunti[3] - quattropunti[0]; // PIXEL
		double fwhmpixel2 = xc2 - xc1;

		ImagePlus imp10 = plot1.getImagePlus();

		ACRlog.appendLog(pathReport, ACRlog.qui() + "imageName2: " + codice + pathname);
		IJ.saveAs(imp10, "jpg", pathname);
		imp10.close();
		// PIXEL
		// ho fatto delle prove ed ho visto che i quattro punti sono sempre in pixel
		// adiacenti

//		ACRlog.waitHere(ACRlog.qui() + "fwhmpixel1= " + fwhmpixel1 + " fwhmpixel2= " + fwhmpixel2, true, 200);
//
//		IJ.log("----------------");
//		for (int i1 = 0; i1 < quattropunti.length; i1++) {
//			IJ.log("punto " + i1 + " x=" + quattropunti[i1] + " y= " + IJ.d2s(line1[quattropunti[i1]], 2));
//		}
//		IJ.log("----------------");
//
//		ACRlog.waitHere(ACRlog.qui() + "PUNTI DISPONIBILI", true, 200);

		return fwhmpixel2;
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
	 * Riceve un array con i valori dei pixel e le loro coordinate, analizza il
	 * profilo e restituisce i punti della FWHM sia come coordinate intere, che
	 * interpolati
	 * 
	 * @param line1        array col contenuto e le coordinate dei pixel
	 * @param pseudomaxlen lunghezza per mediare lo pseudomassimo per poi fate max/2
	 * @param titolo       titolo per i plot
	 * @return matrice coi risultati
	 */
	public static double[][] FWHMpoints3vNEW(double[][] line3, int pseudomaxlen, String titolo, boolean step,
			boolean verbose) {
		//
		// [][] line3 ha il formato:
		// line3[0] = profiX coordinata x del pixel sulla immagine
		// line3[1] = profiY coordinata y del pixel sulla immagine
		// line3[2] = profiZ valore segnale nel pixels per plottaggio profilo
		// line3[3] = profiW coordinata W "ipotenusa", la x del profilo
		// dobbiamo fare in modo che la coordinata W sia sempre nella stessa direzione
		//
		int trunc = 4;

		if (verbose) {
			IJ.log(ACRlog.qui() + " START>  step= " + step + " verbose= " + verbose);
			IJ.log(ACRlog.qui() + " START>  analisi del profilo ricevuto per ottenere la FWHM");
			IJ.log(ACRlog.qui() + "  step= " + step + " verbose= " + verbose);
			ACRlog.logMatrix(line3, ACRlog.qui() + "line3");
		}

		Plot plot1 = null;
		double[] line1 = ACRutils.signal1vfrom3v(line3);
		if (line1 == null)
			ACRlog.waitHere(ACRlog.qui() + " line1==null");
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
			plot1 = ACRutils.ACRplot(line1, ACRlog.qui() + titolo, Color.GREEN, true);
			plot1.addPoints(xVetLineHalf, yVetLineHalf, Plot.LINE);
			plot1.draw();
		}
		// ricerca due punti superiori al fwhmlevel, potrebbero bastare questi, ma se
		// vogliamo fare di piu' possiamo anche trovare i due punti sotto (che avranno
		// la coordinata x del grafico inferiore di 1) ed alla fine fare una
		double[][] duepuntisopra = ACRlocalizer.findPointsopra3v(line3, (int) fwhmlevel);
		// interpolazione lineare per trovare la frazione di pixel
		// duepuntisopra[0][0] = leftx; duepuntisopra[1][0] = rightx;
		// duepuntisopra[0][1] = lefty; duepuntisopra[1][1] = righty;
		// duepuntisopra[0][2] = leftz; duepuntisopra[1][2] = rightz;
		// duepuntisopra[0][3] = leftw; duepuntisopra[1][3] = rightw;
		double[] duepuntiwa = new double[2];
		double[] duepuntiza = new double[2];
		duepuntiwa[0] = duepuntisopra[1][2];
		duepuntiwa[1] = duepuntisopra[1][3];
		duepuntiza[0] = duepuntisopra[0][2];
		duepuntiza[1] = duepuntisopra[0][3];
		if (verbose) {
			ACRlog.logVector(duepuntiwa, ACRlog.qui() + "duepuntiwa");
			ACRlog.logVector(duepuntiza, ACRlog.qui() + "duepuntiza");
			plot1.setLineWidth(4);
			plot1.setColor(Color.RED);
			plot1.addPoints(duepuntiwa, duepuntiza, Plot.DIAMOND);
			ACRlog.logVector(duepuntiwa, ACRlog.qui() + "duepuntiwa SOPRA");
			ACRlog.logVector(duepuntiza, ACRlog.qui() + "duepuntiza SOPRA");
//			ACRlog.waitHere();
		}
		// ricerca dei due punti inferiori al fwhmlevel
		double[][] duepuntisotto = ACRlocalizer.findPointsotto3v(line3, (int) fwhmlevel);
		// duepuntisotto[0][0] = leftx; duepuntisotto[1][0] = rightx;
		// duepuntisotto[0][1] = lefty; duepuntisotto[1][1] = righty;
		// duepuntisotto[0][2] = leftz; duepuntisotto[1][2] = rightz;
		// duepuntisotto[0][3] = leftw; duepuntisotto[1][3] = rightw;

		if (verbose) {
			IJ.log(ACRlog.qui() + "duepuntisotto leftx= " + duepuntisotto[0][0] + " lefty= " + duepuntisotto[0][1]
					+ " leftz= " + duepuntisotto[0][2] + " leftw= " + duepuntisotto[0][3]);
			IJ.log(ACRlog.qui() + "duepuntisotto rightx= " + duepuntisotto[1][0] + " righty= " + duepuntisotto[1][1]
					+ " rightz= " + duepuntisotto[1][2] + " rightw= " + duepuntisotto[1][3]);
		}

		double[] duepuntiwb = new double[2];
		double[] duepuntizb = new double[2];
		duepuntiwb[0] = duepuntisotto[0][3];
		duepuntiwb[1] = duepuntisotto[1][3];
		duepuntizb[0] = duepuntisotto[0][2];
		duepuntizb[1] = duepuntisotto[1][2];
		if (verbose) {
			plot1.setColor(Color.BLUE);
			plot1.addPoints(duepuntiwb, duepuntizb, Plot.DIAMOND);
			ACRlog.logVector(duepuntiwb, ACRlog.qui() + "duepuntiwb SOTTO");
			ACRlog.logVector(duepuntizb, ACRlog.qui() + "duepuntizb SOTTO");
		}
		// per complicarci la vita facciamo la interpolazione lineare tra il pixel sopra
		// e quello sotto (distanza 1 pixel cioe' 0.625 mm) otterremo dei double
		// interpolazione lineare coordinata X del punto sinistra
		double wa1 = (double) duepuntiwa[0];
		double za1 = (double) duepuntiza[0];
		double wb1 = (double) duepuntiwb[0];
		double zb1 = (double) duepuntizb[0];
		double zc1 = pmax / 2.;
		if (verbose)
			IJ.log(ACRlog.qui() + "interpolazione lineare");
		double wc1 = ACRutils.xLinearInterpolation(wa1, za1, wb1, zb1, zc1);
		// interpolazione lineare coordinata X del punto destra
		double wa2 = (double) duepuntiwa[1];
		double za2 = (double) duepuntiza[1];
		double wb2 = (double) duepuntiwb[1];
		double zb2 = (double) duepuntizb[1];
		double zc2 = pmax / 2.;
		if (verbose)
			IJ.log(ACRlog.qui() + "interpolazione lineare");
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
			ACRlog.logVector(duepuntiwc, ACRlog.qui() + "duepuntiwc INTERPOLATI");
			ACRlog.logVector(duepuntizc, ACRlog.qui() + "duepuntizc INTERPOLATI");
		}
		//
		// cerco ora di ricavare le coordinate immagine x ed y del punto interpolato
		// poiche' solo in seguito le arrotondo ad intero, sperando di risolvere
		// il problema dell'apparente erore di un pixel fuori per alcuni fitting
		//
		int len = line3[0].length;
		// ACRlog.waitHere("" + line3.length + " " +line3[0].length);
		double y2 = line3[1][len - 2];
		double y1 = line3[1][1];
		double x2 = line3[0][len - 2];
		double x1 = line3[0][1];

//		x1 = ACRutils.dblTruncate(x1, trunc);
//		x2 = ACRutils.dblTruncate(x2, trunc);
//		y1 = ACRutils.dblTruncate(y1, trunc);
//		y2 = ACRutils.dblTruncate(y2, trunc);
		if (verbose) {
			IJ.log(ACRlog.qui() + " x1= " + x1);
			IJ.log(ACRlog.qui() + " y1= " + y1);
			IJ.log(ACRlog.qui() + " x2= " + x2);
			IJ.log(ACRlog.qui() + " y2= " + y2);
		}

		double slope = 0;
		double xinterpsx = 0;
		double yinterpsx = 0;
		double xinterpdx = 0;
		double yinterpdx = 0;

		// Calcolo dello slope della retta su cui abbiamo ricavato il profilo:
		// escludiamo i casi particolari di retta orizzontale e verticale in cui lo
		// slope vale 0 ed infinito
		//
		// wc1 e wc2 sono i due punti interpolati sul grafico, di cui vogliamo calcolare
		// le coordinate x ed y per riportarli su immagine ed usarli nei calcoli
		//

		if (Double.compare(x1, x2) == 0) {
			// linea orizzontale caso particolare
			if (verbose)
				IJ.log(ACRlog.qui() + "caso particolare linea orizzontale");
			xinterpsx = x1;
			yinterpsx = wc1;
			xinterpdx = x1;
			yinterpdx = wc2;
		} else if (Double.compare(y1, y2) == 0) {
			if (verbose)
				IJ.log(ACRlog.qui() + "caso particolare linea verticale");
			xinterpsx = wc1;
			yinterpsx = y1;
			xinterpdx = wc2;
			yinterpdx = y1;
		} else {
			if (verbose)
				IJ.log(ACRlog.qui() + "calcolo per linea inclinata");
			// wc1 e wc2 sono i due punti interpol

			slope = (y2 - y1) / (x2 - x1);
//			slope = ACRutils.dblTruncate(slope, trunc);
			double xoffset = line3[0][0];
			double yoffset = line3[1][0];
			double q = xoffset - slope * yoffset;
//			q = ACRutils.dblTruncate(q, trunc);

			// lo slope sarebbe la tangente dell'angolo per cui possiamo ricavare seno e
			// coseno, necessari per ricavare i cateti del triangolo rettangolo data
			// l'ipotenusa, ricavando cosi'le coordinate x ed y interpolate (speriamo!!!)
			// NOTA, il dblTruncate alla fine viene tolto di torno, serve solo per non
			// vedere numeri luuuuunghi
			double sin = slope / Math.sqrt(1 + slope * slope);
//			sin = ACRutils.dblTruncate(sin, trunc);
			double cos = 1 / Math.sqrt(1 + slope * slope);
//			cos = ACRutils.dblTruncate(cos, trunc);
			if (verbose) {
				IJ.log(ACRlog.qui() + "xoffset= " + xoffset);
				IJ.log(ACRlog.qui() + "yoffset= " + yoffset);
				IJ.log(ACRlog.qui() + "slope= " + slope);
				IJ.log(ACRlog.qui() + "q= " + q);
				IJ.log(ACRlog.qui() + "sin= " + sin);
				IJ.log(ACRlog.qui() + "cos= " + cos);
			}
//			wc1 = ACRutils.dblTruncate(wc1, trunc);
//			wc2 = ACRutils.dblTruncate(wc2, trunc);

			if (slope > 0) {
				if (verbose)
					IJ.log(ACRlog.qui() + "slope > 0  slope= " + slope);
				// punto sx
				xinterpsx = Math.abs(wc1 * sin) + xoffset;
				yinterpsx = Math.abs(wc1 * cos) + yoffset;
				// punto dx
				xinterpdx = Math.abs(wc2 * sin) + xoffset;
				yinterpdx = Math.abs(wc2 * cos) + yoffset;
			} else {
				// punto sx
				if (verbose)
					IJ.log(ACRlog.qui() + "slope < 0  slope= " + slope);
				yinterpsx = Math.abs(wc1 * sin) + xoffset;
				xinterpsx = Math.abs(wc1 * cos) + yoffset;
				// punto dx
				yinterpdx = Math.abs(wc2 * sin) + xoffset;
				xinterpdx = Math.abs(wc2 * cos) + yoffset;
			}

//			xinterpsx = ACRutils.dblTruncate(xinterpsx, trunc);
//			yinterpsx = ACRutils.dblTruncate(yinterpsx, trunc);
//			xinterpdx = ACRutils.dblTruncate(xinterpdx, trunc);
//			yinterpdx = ACRutils.dblTruncate(yinterpdx, trunc);

			if (verbose) {
				IJ.log(ACRlog.qui() + "wc1= " + wc1);
				IJ.log(ACRlog.qui() + "wc2= " + wc2);
				IJ.log(ACRlog.qui() + "slope= " + slope);
				IJ.log(ACRlog.qui() + "sin= " + sin + " cos= " + cos);
				IJ.log(ACRlog.qui() + "xinterpsx= " + xinterpsx);
				IJ.log(ACRlog.qui() + "yinterpsx= " + yinterpsx);
				IJ.log(ACRlog.qui() + "xinterpdx= " + xinterpdx);
				IJ.log(ACRlog.qui() + "yinterpdx= " + yinterpdx);
				IJ.log(ACRlog.qui() + "punto uno= " + xinterpsx + " , " + yinterpsx);
				IJ.log(ACRlog.qui() + "punto due= " + xinterpdx + " , " + yinterpdx);
			}

		}

		if (verbose)

		{
			IJ.log(ACRlog.qui() + "SX punto interpolato su IMMAGINE= " + xinterpsx + " , " + yinterpsx);
			IJ.log(ACRlog.qui() + "DX punto interpolato su IMMAGINE= " + xinterpdx + " , " + yinterpdx);
		}

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
			ACRlog.logMatrix(vetout, ACRlog.qui() + "vetout");
//			ACRlog.waitHere("attenzione: le coordinate precise sono in sx(vetout[0][2]) e dx(vetout[0][3])");
		}
		return vetout;
	}

	/**
	 * Riceve un array con i valori dei pixel e le loro coordinate, analizza il
	 * profilo e restituisce i punti della FWHM sia come coordinate intere, che
	 * interpolati
	 * 
	 * @param line1        array col contenuto e le coordinate dei pixel
	 * @param pseudomaxlen lunghezza per mediare lo pseudomassimo per poi fate max/2
	 * @param titolo       titolo per i plot
	 * @return matrice coi risultati
	 */
	public static double[][] FWHManalyzer(double[][] line3, int pseudomaxlen, String titolo, boolean step,
			boolean verbose) {

		// line3[0][] = profiX coordinata x del pixel sulla immagine
		// line3[1][] = profiY coordinata y del pixel sulla immagine
		// line3[2][] = profiZ valore segnale nel pixels, la ordinata del profilo
		// line3[3][] = profiW coordinata W "ipotenusa", la ascissa del profilo
		int trunc = 4; // usata solo durante i test, per troncare il numero dei decimali
		if (verbose) {
			IJ.log(ACRlog.qui() + " START>  step= " + step + " verbose= " + verbose);
			IJ.log(ACRlog.qui() + " START>  analisi del profilo ricevuto per ottenere la FWHM");
			IJ.log(ACRlog.qui() + "  step= " + step + " verbose= " + verbose);
			ACRlog.logMatrix(line3, ACRlog.qui() + "line3");
		}
		Plot plot1 = null;
		double[] line1 = ACRutils.signal1vfrom3v(line3);
		if (line1 == null)
			ACRlog.waitHere(ACRlog.qui() + " line1==null");
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
		// ricerca due punti superiori al fwhmlevel, rileva solo le coordinate
		double[][] duepuntisopra = ACRlocalizer.findPointsopraAYV(line3, (int) fwhmlevel);
		// out1[0][0] = leftx; out1[1][0] = rightx;
		// out1[0][1] = lefty; out1[1][1] = righty;
		// out1[0][2] = leftz; out1[1][2] = rightz;
		// out1[0][3] = leftw; out1[1][3] = rightw;

		if (verbose) {
			IJ.log(ACRlog.qui() + "===============");
			ACRlog.logMatrix(duepuntisopra, ACRlog.qui() + "duepuntisopra");
			IJ.log(ACRlog.qui() + "duepuntisopra leftx= " + duepuntisopra[0][0] + " lefty= " + duepuntisopra[0][1]
					+ " leftz= " + duepuntisopra[0][2] + " leftw= " + duepuntisopra[0][3]);
			IJ.log(ACRlog.qui() + "duepuntisopra rightx= " + duepuntisopra[1][0] + " righty= " + duepuntisopra[1][1]
					+ " rightz= " + duepuntisopra[1][2] + " rightw= " + duepuntisopra[1][3]);
		}
		double[] duepuntiwa = new double[2];
		double[] duepuntiza = new double[2];

		duepuntiwa[0] = duepuntisopra[1][2]; // NEW trasformato
		duepuntiwa[1] = duepuntisopra[1][3]; // NEW trasformato
		duepuntiza[0] = duepuntisopra[0][2]; // NEW trasformato
		duepuntiza[1] = duepuntisopra[0][3]; // NEW trasformato

		if (verbose) {
			plot1.setLineWidth(4);
			plot1.setColor(Color.RED);
			plot1.addPoints(duepuntiwa, duepuntiza, Plot.DIAMOND);
			IJ.log(ACRlog.qui() + "====verificare========");
			ACRlog.logVector(duepuntiwa, ACRlog.qui() + "duepuntiwa SOPRA");
			ACRlog.logVector(duepuntiza, ACRlog.qui() + "duepuntiza SOPRA");
		}
		// ricerca dei due punti inferiori al fwhmlevel
		double[][] duepuntisotto = ACRlocalizer.findPointsottoAYV(line3, (int) fwhmlevel);

		if (verbose) {
			ACRlog.logMatrix(duepuntisotto, ACRlog.qui() + "duepuntisotto");
			IJ.log(ACRlog.qui() + "duepuntisotto leftx= " + duepuntisotto[0][0] + " lefty= " + duepuntisotto[0][1]
					+ " leftz= " + duepuntisotto[0][2] + " leftw= " + duepuntisotto[0][3]);
			IJ.log(ACRlog.qui() + "duepuntisotto rightx= " + duepuntisotto[1][0] + " righty= " + duepuntisotto[1][1]
					+ " rightz= " + duepuntisotto[1][2] + " rightw= " + duepuntisotto[1][3]);
		}

		double[] duepuntiwb = new double[2];
		double[] duepuntizb = new double[2];

		duepuntiwb[0] = duepuntisotto[1][2]; // NEW trasformato
		duepuntiwb[1] = duepuntisotto[1][3]; // NEW trasformato
		duepuntizb[0] = duepuntisotto[0][2]; // NEW trasformato
		duepuntizb[1] = duepuntisotto[0][3]; // NEW trasformato
		if (verbose) {
			plot1.setColor(Color.BLUE);
			plot1.addPoints(duepuntiwb, duepuntizb, Plot.DIAMOND);
			ACRlog.logVector(duepuntiwb, ACRlog.qui() + "duepuntiwb SOTTO");
			ACRlog.logVector(duepuntizb, ACRlog.qui() + "duepuntizb SOTTO");
		}
		// per complicarci la vita facciamo la interpolazione lineare tra il pixel sopra
		// e quello sotto (distanza 1 pixel cioe' 0.625 mm) otterremo dei double
		// interpolazione lineare coordinata X del punto sinistra
		double wa1 = (double) duepuntiwa[0]; // OK
		double za1 = (double) duepuntiza[0]; // OK
		double wb1 = (double) duepuntiwb[0]; // OK
		double zb1 = (double) duepuntizb[0]; // OK
		double zc1 = pmax / 2.;
		if (verbose)
			IJ.log(ACRlog.qui() + "interpolazione lineare");
		double wc1 = ACRutils.xLinearInterpolation(wa1, za1, wb1, zb1, zc1);
		// interpolazione lineare coordinata X del punto destra
		double wa2 = (double) duepuntiwa[1];
		double za2 = (double) duepuntiza[1];
		double wb2 = (double) duepuntiwb[1];
		double zb2 = (double) duepuntizb[1];
		double zc2 = pmax / 2.;
		if (verbose)
			IJ.log(ACRlog.qui() + "interpolazione lineare");
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
			ACRlog.logVector(duepuntiwc, ACRlog.qui() + "duepuntiwc INTERPOLATI");
			ACRlog.logVector(duepuntizc, ACRlog.qui() + "duepuntizc INTERPOLATI");
		}
		//
		// cerco ora di ricavare le coordinate immagine x ed y del punto interpolato
		// poiche' solo in seguito le arrotondo ad intero, sperando di risolvere
		// il problema dell'apparente erore di un pixel fuori per alcuni fitting
		//
		int len = line3[0].length;
		// ACRlog.waitHere("" + line3.length + " " +line3[0].length);
		double y2 = line3[1][len - 2];
		double y1 = line3[1][1];
		double x2 = line3[0][len - 2];
		double x1 = line3[0][1];

//		x1 = ACRutils.dblTruncate(x1, trunc);
//		x2 = ACRutils.dblTruncate(x2, trunc);
//		y1 = ACRutils.dblTruncate(y1, trunc);
//		y2 = ACRutils.dblTruncate(y2, trunc);

//		IJ.log(ACRlog.qui() + " x1= " + x1);
//		IJ.log(ACRlog.qui() + " y1= " + y1);
//		IJ.log(ACRlog.qui() + " x2= " + x2);
//		IJ.log(ACRlog.qui() + " y2= " + y2);

		double slope = 0;
		double xinterpsx = 0;
		double yinterpsx = 0;
		double xinterpdx = 0;
		double yinterpdx = 0;

		// Calcolo dello slope della retta su cui abbiamo ricavato il profilo:
		// escludiamo i casi particolari di retta orizzontale e verticale in cui lo
		// slope vale 0 ed infinito
		//
		// wc1 e wc2 sono i due punti interpolati sul grafico, di cui vogliamo calcolare
		// le coordinate x ed y per riportarli su immagine ed usarli nei calcoli
		//

		if (Double.compare(x1, x2) == 0) {
			// linea orizzontale caso particolare
			if (verbose)
				IJ.log(ACRlog.qui() + "LINEA ORIZZONTALE");
			xinterpsx = x1;
			yinterpsx = wc1;
			xinterpdx = x1;
			yinterpdx = wc2;
		} else if (Double.compare(y1, y2) == 0) {
			if (verbose)
				IJ.log(ACRlog.qui() + "LINEA VERTICALE");
			xinterpsx = wc1;
			yinterpsx = y1;
			xinterpdx = wc2;
			yinterpdx = y1;
		} else {
			if (verbose) {
				IJ.log(ACRlog.qui() + "=================");
				IJ.log(ACRlog.qui() + "LINEA INCLINATA");
			}
			// wc1 e wc2 sono i due punti interpol

			slope = (y2 - y1) / (x2 - x1);
//			slope = ACRutils.dblTruncate(slope, trunc);
			double xoffset = line3[0][0];
			double yoffset = line3[1][0];
			double q = xoffset - slope * yoffset;
//			q = ACRutils.dblTruncate(q, trunc);

			// lo slope sarebbe la tangente dell'angolo per cui possiamo ricavare seno e
			// coseno, necessari per ricavare i cateti del triangolo rettangolo data
			// l'ipotenusa, ricavando cosi'le coordinate x ed y interpolate (speriamo!!!)
			// NOTA, il dblTruncate alla fine viene tolto di torno, serve solo per non
			// vedere numeri luuuuunghi
			double sin = slope / Math.sqrt(1 + slope * slope);
//			sin = ACRutils.dblTruncate(sin, trunc);
			double cos = 1 / Math.sqrt(1 + slope * slope);
//			cos = ACRutils.dblTruncate(cos, trunc);
//			IJ.log(ACRlog.qui() + "xoffset= " + xoffset);
//			IJ.log(ACRlog.qui() + "yoffset= " + yoffset);
//			IJ.log(ACRlog.qui() + "slope= " + slope);
//			IJ.log(ACRlog.qui() + "q= " + q);
//			IJ.log(ACRlog.qui() + "sin= " + sin);
//			IJ.log(ACRlog.qui() + "cos= " + cos);
//			wc1 = ACRutils.dblTruncate(wc1, trunc);
//			wc2 = ACRutils.dblTruncate(wc2, trunc);

//			if (slope > 0) {
			if (verbose)
				IJ.log(ACRlog.qui() + "slope > 0  slope= " + slope);
			// punto sx
			xinterpsx = wc1 * sin + xoffset;
			yinterpsx = wc1 * cos + yoffset;
			// punto dx
			xinterpdx = wc2 * sin + xoffset;
			yinterpdx = wc2 * cos + yoffset;
//			} else {
//				// punto sx
//				IJ.log(ACRlog.qui() + "slope < 0  slope= " + slope);
//				yinterpsx = Math.abs(wc1 * sin) + xoffset;
//				xinterpsx = Math.abs(wc1 * cos) + yoffset;
//				// punto dx
//				yinterpdx = Math.abs(wc2 * sin) + xoffset;
//				xinterpdx = Math.abs(wc2 * cos) + yoffset;
//			}

//			xinterpsx = ACRutils.dblTruncate(xinterpsx, trunc);
//			yinterpsx = ACRutils.dblTruncate(yinterpsx, trunc);
//			xinterpdx = ACRutils.dblTruncate(xinterpdx, trunc);
//			yinterpdx = ACRutils.dblTruncate(yinterpdx, trunc);

			if (verbose) {
				IJ.log(ACRlog.qui() + "------------------------");
				IJ.log(ACRlog.qui() + "wc1= " + wc1);
				IJ.log(ACRlog.qui() + "wc2= " + wc2);
				IJ.log(ACRlog.qui() + "slope= " + slope);
				IJ.log(ACRlog.qui() + "sin= " + sin + " cos= " + cos);
				IJ.log(ACRlog.qui() + "xinterpsx= " + xinterpsx);
				IJ.log(ACRlog.qui() + "yinterpsx= " + yinterpsx);
				IJ.log(ACRlog.qui() + "xinterpdx= " + xinterpdx);
				IJ.log(ACRlog.qui() + "yinterpdx= " + yinterpdx);
				IJ.log(ACRlog.qui() + "punto uno= " + xinterpsx + " , " + yinterpsx);
				IJ.log(ACRlog.qui() + "punto due= " + xinterpdx + " , " + yinterpdx);
				IJ.log(ACRlog.qui() + "------------------------");
				IJ.log(ACRlog.qui() + "==FINE LINEA INCLINATA==");
			}

		}

		if (verbose)

		{
			IJ.log(ACRlog.qui() + "SX punto interpolato su IMMAGINE= " + xinterpsx + " , " + yinterpsx);
			IJ.log(ACRlog.qui() + "DX punto interpolato su IMMAGINE= " + xinterpdx + " , " + yinterpdx);
			ACRlog.logMatrix(duepuntisopra, ACRlog.qui() + "duepuntisopra");
		}

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
			ACRlog.logMatrix(vetout, ACRlog.qui() + "vetout");
//			ACRlog.waitHere("attenzione: le coordinate precise sono in sx(vetout[0][2]) e dx(vetout[0][3])");
		}
		return vetout;
	}

	/**
	 * Analizza il profilo e ricerca punti sopra e sotto del livello di threshold
	 * 
	 * @param line1
	 * @param threshold
	 * @return
	 */
	public static int[] findFWHMpoints(double[] line1, double threshold) {

		int leftUp = 0;
		int rightUp = 0;
		int leftDw = 0;
		int rightDw = 0;
		boolean valido = false;
		for (int i1 = 0; i1 < line1.length; i1++) {
			if (Double.compare(line1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(line1[i1], threshold) >= 0) {
				leftUp = i1;
				break;
			}
		}
		leftDw = leftUp - 1;
		valido = false;
		for (int i1 = line1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(line1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(line1[i1], threshold) >= 0) {
				rightUp = i1;
				break;
			}
		}
		rightDw = rightUp + 1;
		int[] out1 = new int[4];
		out1[0] = leftUp;
		out1[1] = leftDw;
		out1[2] = rightUp;
		out1[3] = rightDw;

		return out1;
	}

	/**
	 * Ricerca punti sopra e sotto del livello di threshold autocalcolato
	 * 
	 * @param line1
	 * @param threshold
	 * @return
	 */
	public static int[] findFWHMpoints(double[] line1) {

		int leftUp = 0;
		int rightUp = 0;
		int leftDw = 0;
		int rightDw = 0;
		boolean valido = false;
		double[] smooth1 = ACRcalc.vetSmooth3x3(line1);
		double threshold = ACRcalc.vetMax(smooth1) / 2;

		ACRlog.logVector(line1, ACRlog.qui() + "line1");
		ACRlog.waitHere();
		for (int i1 = 0; i1 < line1.length; i1++) {
			if (Double.compare(line1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(line1[i1], threshold) >= 0) {
				leftUp = i1;
				break;
			}
		}
		leftDw = leftUp - 1;
		valido = false;
		for (int i1 = line1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(line1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(line1[i1], threshold) >= 0) {
				rightUp = i1;
				break;
			}
		}
		rightDw = rightUp + 1;
		int[] out1 = new int[4];
		out1[0] = leftUp;
		out1[1] = rightUp;
		out1[2] = leftDw;
		out1[3] = rightDw;

		return out1;
	}

	/**
	 * Ricerca punti al di sopra del livello di threshold in corso di sostituzione
	 * con findFWHMpoints
	 * 
	 * @param line1
	 * @param threshold
	 * @return
	 */
	@Deprecated(forRemoval = true)
	public static int[] findPointsopra(double[] line1, double threshold) {

		int left = 0;
		int right = 0;
		boolean valido = false;
		ACRlog.logVector(line1, ACRlog.qui() + "line1");
		ACRlog.waitHere();
		for (int i1 = 0; i1 < line1.length; i1++) {
			if (Double.compare(line1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(line1[i1], threshold) >= 0) {
				left = i1;
				break;
			}
		}
		valido = false;
		for (int i1 = line1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(line1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(line1[i1], threshold) >= 0) {
				right = i1;
				break;
			}
		}
		int[] out1 = new int[2];
		out1[0] = left;
		out1[1] = right;

		return out1;
	}

	/**
	 * Ricerca punti al di sopra livello threshold, partendo dai bordi del profilo e
	 * andando verso il centro
	 * 
	 * @param line3           matrice profilo
	 * @param thresholdvalore di threshold
	 * @return matrice di output
	 */
	public static double[][] findPointsopra3v(double[][] line3, double threshold) {

		// line3 [4][n]
		// line3[0] = coordinata X punto
		// line3[1] = coordinata Y punto
		// line3[2] = segnale Z punto;
		// line3[3] = coordinata W su profilo;

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
		boolean valido = false;
		for (int i1 = 0; i1 < z1.length; i1++) {
			if (Double.compare(z1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(z1[i1], threshold) >= 0) {
				leftx = x1[i1];
				lefty = y1[i1];
				leftz = z1[i1];
				leftw = w1[i1];
				break;
			}
		}
		valido = false;
		for (int i1 = z1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(z1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(z1[i1], threshold) >= 0) {
				rightx = x1[i1];
				righty = y1[i1];
				rightz = z1[i1];
				rightw = w1[i1];
				break;
			}
		}

		double[][] out1 = new double[2][4];
		out1[0][0] = leftx;
		out1[0][1] = rightx;
		out1[0][2] = leftz;
		out1[0][3] = rightz;
		out1[1][0] = lefty;
		out1[1][1] = righty;
		out1[1][2] = leftw;
		out1[1][3] = rightw;

		return out1;
	}

	/**
	 * 
	 * @param line3
	 * @param threshold
	 * @return
	 */
	public static double[][] findPointsopraAYV(double[][] line3, double threshold) {

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
		boolean valido = false;
		// cerco, partendo da sinistra del segmento il valore di pixel che supera od
		// uguaglia la meta'altezza del segnale
		for (int i1 = 0; i1 < z1.length; i1++) {
			if (Double.compare(z1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(z1[i1], threshold) >= 0) {
				leftx = x1[i1];
				lefty = y1[i1];
				leftz = z1[i1];
				leftw = w1[i1];
				break;
			}
		}
		valido = false;
		for (int i1 = z1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(z1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(z1[i1], threshold) >= 0) {
				rightx = x1[i1];
				righty = y1[i1];
				rightz = z1[i1];
				rightw = w1[i1];
				break;
			}
		}
		double[][] out1 = new double[2][4];
		out1[0][0] = leftx;
		out1[0][1] = rightx;
		out1[0][2] = leftz;
		out1[0][3] = rightz;
		out1[1][0] = lefty;
		out1[1][1] = righty;
		out1[1][2] = leftw;
		out1[1][3] = rightw;
		if (false) {
			IJ.log(ACRlog.qui() + "=============");
			ACRlog.logMatrix(out1, ACRlog.qui() + "out1");
		}
		return out1;
	}

	/**
	 * 
	 * @param line1
	 * @param threshold
	 * @return
	 */
	@Deprecated(forRemoval = true)
	public static int[] findPointsotto(double[] line1, double threshold) {

		int left = 0;
		int right = 0;
		boolean valido = false;
		for (int i1 = 0; i1 < line1.length; i1++) {
			if (Double.compare(line1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(line1[i1], threshold) >= 0) {
				left = i1 - 1;
				break;
			}
		}
		valido = false;
		for (int i1 = line1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(line1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(line1[i1], threshold) >= 0) {
				right = i1 + 1;
				break;
			}
		}
		int[] out1 = new int[2];
		out1[0] = left;
		out1[1] = right;

		return out1;
	}

	/**
	 * 
	 * @param line3
	 * @param threshold
	 * @return
	 */
	public static double[][] findPointsotto3v(double[][] line3, double threshold) {

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
		boolean valido = false;

		for (int i1 = 0; i1 < z1.length; i1++) {
			if (Double.compare(z1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(z1[i1], threshold) >= 0) {
				leftx = x1[i1 - 1];
				lefty = y1[i1 - 1];
				leftz = z1[i1 - 1];
				leftw = w1[i1 - 1];
				break;
			}
		}
		valido = false;
		for (int i1 = z1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(z1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(z1[i1], threshold) >= 0) {
				rightx = x1[i1 + 1];
				righty = y1[i1 + 1];
				rightz = z1[i1 + 1];
				rightw = w1[i1 + 1];
				break;
			}
		}
		double[][] out1 = new double[2][4];
		out1[0][0] = leftx;
		out1[0][1] = lefty;
		out1[0][2] = leftz;
		out1[0][3] = leftw;
		out1[1][0] = rightx;
		out1[1][1] = righty;
		out1[1][2] = rightz;
		out1[1][3] = rightw;

		return out1;
	}

	/**
	 * 
	 * @param line3
	 * @param threshold
	 * @return
	 */
	public static double[][] findPointsottoAYV(double[][] line3, double threshold) {

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
		boolean valido = false;
		for (int i1 = 0; i1 < z1.length; i1++) {
			if (Double.compare(z1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(z1[i1], threshold) >= 0) {
				leftx = x1[i1 - 1];
				lefty = y1[i1 - 1];
				leftz = z1[i1 - 1];
				leftw = w1[i1 - 1];
				break;
			}
		}
		valido = false;
		for (int i1 = z1.length - 1; i1 >= 0; i1--) {
			if (Double.compare(z1[i1], threshold) < 0)
				valido = true;
			if (valido && Double.compare(z1[i1], threshold) >= 0) {
				rightx = x1[i1 + 1];
				righty = y1[i1 + 1];
				rightz = z1[i1 + 1];
				rightw = w1[i1 + 1];
				break;
			}
		}
		double[][] out1 = new double[2][4];
		out1[0][0] = leftx;
		out1[0][1] = rightx;
		out1[0][2] = leftz;
		out1[0][3] = rightz;
		out1[1][0] = lefty;
		out1[1][1] = righty;
		out1[1][2] = leftw;
		out1[1][3] = rightw;
		if (false) {
			IJ.log(ACRlog.qui() + "=============");
			ACRlog.logMatrix(out1, ACRlog.qui() + "out1");
		}

		return out1;
	}

	/**
	 * 
	 * @param imp1
	 * @param water
	 * @param xposition
	 * @param verbose
	 * @return
	 */
	public static int[] verticalSearch(ImagePlus imp1, double threshold, int xposition, boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
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
			ACRlog.logVector(profi1, ACRlog.qui() + "verticalSearch profi1");
		int[] out1 = profileSearch(profi1, threshold, verbose);
		if (verbose)
			ACRlog.logVector(out1, ACRlog.qui() + "verticalSearch out1");
		return out1;
	}

	/**
	 * 
	 * @param imp1
	 * @param water
	 * @param xposition
	 * @param verbose
	 * @return
	 */
	public static int[] verticalSearchAdvanced(ImagePlus imp1, double threshold, int xposition, boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
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
			ACRlog.logVector(profi1, ACRlog.qui() + "verticalSearch profi1");
		int[] out1 = profileSearchAdvanced(profi1, threshold, verbose);
		if (verbose)
			ACRlog.logVector(out1, ACRlog.qui() + "verticalSearch out1");
		return out1;
	}

	/**
	 * 
	 * @param imp1
	 * @param water
	 * @param yposition
	 * @param verbose
	 * @return
	 */
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

	/**
	 * 
	 * @param imp1
	 * @param threshold
	 * @param yposition
	 * @param verbose
	 * @return
	 */
	public static int[] horizontalSearchAdvanced(ImagePlus imp1, double threshold, int yposition, boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//

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
		int[] out1 = profileSearchAdvanced(profi1, threshold, verbose);
		if (verbose)
			ACRlog.logVector(out1, "horizontalSearch out1");

		return out1;
	}

	/**
	 * 
	 * @param imp1
	 * @param threshold
	 * @param yposition
	 * @param verbose
	 * @return
	 */
	public static double[] horizontalSearchInterpolated(ImagePlus imp1, double threshold, int yposition,
			boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
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
		double[] out1 = profileSearchInterpolated(profi1, threshold, verbose);
		if (verbose)
			ACRlog.logVector(out1, "horizontalSearchInterpolated out1");
		return out1;
	}

	/**
	 * 
	 * @param imp1
	 * @param water
	 * @param xposition
	 * @param verbose
	 * @return
	 */
	public static double[] verticalSearchInterpolated(ImagePlus imp1, double threshold, int xposition,
			boolean verbose) {
		//
		// faccio la scansione verticale per localizzare il fantoccio
		//
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

		double[] out1 = profileSearchInterpolated(profi1, threshold, verbose);
		if (verbose)
			ACRlog.logVector(out1, "verticalSearchInterpolated out1");
		return out1;
	}

	/**
	 * Analizza il profilo di una linea, cercando di determinare le dimensioni
	 * dell'oggetto. La linea deve essere attiva come ROI sull'immagine passata
	 * 
	 * @param imp2
	 * @return
	 */
	public static double profAnal(ImagePlus imp2, boolean step, boolean verbose, int timeout) {
		// e come caspita potrei abbreviare profile analyzer? Ci sono 2 soluzioni:
		// analProf oppure profAnal ai posteri(ori) la scelta!!
		if (verbose)
			IJ.log("<GeometricAccuracy.profAnal START>");
		int pseudomaxlen = 3; // dimensioni roi di ricerca pseudomassimo
		double[][] decomposed3v = ACRutils.decomposer3v(imp2);
		double[][] zeropadded3v = ACRutils.zeropadProfile3v(decomposed3v);
		double[][] vetout = ACRlocalizer.FWHMpoints3vNEW(zeropadded3v, pseudomaxlen, "PROFILO LINEA", step, verbose);
//		if (step) ACRlog.waitHere("profilo analizzato");
		//
		// posso misurare il diametro "con precisione?" utilizzando i due punti
		// interpolati
		//
		if (verbose)
			ACRlog.logMatrix(vetout, "<GeometricAccuracy.profAnal> vetout");

		/// MA DEVO CALCOLARE LA DISTANZA !!!

//		vetout[0][0] = leftxup; // leftx non interpolato
//		vetout[0][1] = rightxup; // rightx non interpolato
//		vetout[0][2] = xinterpsx; // leftx interpolato
//		vetout[0][3] = xinterpdx; // rightx interpolato
//
//		vetout[1][0] = leftyup; // lefty non interpolato
//		vetout[1][1] = rightyup; // rightx non interpolato
//		vetout[1][2] = yinterpsx; // lefty interpolato
//		vetout[1][3] = yinterpdx; // rightx interpolato

		double dimension = Math
				.sqrt(Math.pow((vetout[0][2] - vetout[0][3]), 2) + Math.pow((vetout[1][2] - vetout[1][3]), 2)); // IN
																												// PIXEL
		return dimension;
	}

	/**
	 * Estrae dalla slice1 l'inserto centrale e lo analizza per trovarne i vertici
	 * 
	 * @param imp1
	 * @param phantomCircle
	 * @param step
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static double[][] phantomReferences(ImagePlus imp1, double[] phantomCircle, boolean step, boolean verbose,
			int timeout) {

		if (verbose || step)
			IJ.log(ACRlog.qui() + "START");
		boolean verbose2 = false;
		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);
//		int latoROI = 11;
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		Overlay over1 = new Overlay();
		imp2.setOverlay(over1);
		//
		// elaboro e filtro l'immagine, in modo da renderla binaria ed isolare i
		// particolari che in seguito andro'a rilevare
		//
		ImagePlus imp4 = phantomFilter(imp2, phantomCircle, step, verbose, timeout);
		Overlay over4 = new Overlay();
		imp4.setOverlay(over4);
		//
		// l'immagine di phantomElab1 e'binaria, per cui threshold=255;
		//
		int threshold = 255;
		int[] out2 = new int[4];
		double xpoint1 = 0;
		double ypoint1 = 0;
		double xpoint2 = 0;
		double ypoint2 = 0;
		int[] out4 = new int[4];
		ArrayList<Integer> arrX = new ArrayList<>();
		ArrayList<Integer> arrY = new ArrayList<>();
		// scansione per colonne
		for (int x1 = 1; x1 < width - 1; x1++) {
			out2 = verticalSearch(imp4, threshold, x1, verbose2);
			if (out2 != null) {
				xpoint1 = x1;
				ypoint1 = out2[0];
				arrX.add(x1);
				arrY.add(out2[0]);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp4, over4, xpoint1, ypoint1, type, size, Color.GREEN, false);
				xpoint1 = x1;
				ypoint1 = out2[1];
				arrX.add(x1);
				arrY.add(out2[1]);
				ACRutils.plotPoints(imp4, over4, xpoint1, ypoint1, type, size, Color.GREEN, false);
			}
		}
		for (int y1 = 1; y1 < width - 1; y1++) {
			verbose2 = false;
			out4 = horizontalSearch(imp4, threshold, y1, verbose2);
			if (out4 != null) {
				xpoint2 = out4[0];
				ypoint2 = y1;
				arrX.add(out4[0]);
				arrY.add(y1);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp4, over4, xpoint2, ypoint2, type, size, Color.GREEN, false);
				xpoint2 = out4[1];
				ypoint2 = y1;
				arrX.add(out4[1]);
				arrY.add(y1);
				ACRutils.plotPoints(imp4, over4, xpoint2, ypoint2, type, size, Color.GREEN, false);
			}
		}

		// tolgo i doppioni dove scansione orizzontale e verticale si sovrappongono

		int[] vetX = ACRcalc.arrayListToArrayInt(arrX);
		int[] vetY = ACRcalc.arrayListToArrayInt(arrY);
		int[][] matXY = new int[2][vetX.length];
		for (int i1 = 0; i1 < vetX.length; i1++) {
			matXY[0][i1] = vetX[i1];
			matXY[1][i1] = vetY[i1];
		}
		if (verbose)
			ACRlog.logMatrix(matXY, ACRlog.qui() + "matXY");
		int[][] matout1 = ACRcalc.removeDuplicate(matXY);
		if (verbose)
			ACRlog.logMatrix(matout1, ACRlog.qui() + "matout1");
		if (step)
			ACRlog.waitHere("INSERTO FILTRATO", ACRutils.debug, timeout);
		int[][] rotated = ACRutils.matRotate(matout1);
		// analizzo tutti i punti trovati, per ricavare i piu'prossimi agli angoli
		// dell'immagine
		int[][] matout2 = vertexFinder(rotated, width, height, verbose);
		if (verbose)
			ACRlog.logMatrix(matout2, ACRlog.qui() + "matout2");
		// andiamo a plottare i punti trovati
		// VERDE
		int AX = matout2[0][0];
		int AY = matout2[1][0];
		// GIALLO
		int BX = matout2[0][1];
		int BY = matout2[1][1];
		// ROSSO
		int CX = matout2[0][2];
		int CY = matout2[1][2];
		// AZZURRO
		int DX = matout2[0][3];
		int DY = matout2[1][3];
		if (step)
			ACRlog.waitHere("A= " + AX + " , " + AY + " B= " + BX + " , " + BY + " C= " + CX + " , " + CY + " D= " + DX
					+ " , " + DY, ACRutils.debug, timeout);

		ACRutils.plotPoints(imp4, over4, AX, AY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp4, over4, BX, BY, Color.YELLOW, 4, 4);
		ACRutils.plotPoints(imp4, over4, CX, CY, Color.BLUE, 4, 4);
		ACRutils.plotPoints(imp4, over4, DX, DY, Color.BLUE, 4, 4);

		Line l1 = new Line(DX, DY, CX, CY);

		// da qui in poi necessitiamo di maggior precisione degli integer, per cui
		// trasformo in double
		double[][] matout = new double[2][4];

		matout[0][0] = (double) AX;
		matout[1][0] = (double) AY;
		matout[0][1] = (double) BX;
		matout[1][1] = (double) BY;
		matout[0][2] = (double) CX;
		matout[1][2] = (double) CY;
		matout[0][3] = (double) DX;
		matout[1][3] = (double) DY;

		if (verbose) {
			ACRlog.logMatrix(matout, ACRlog.qui() + "matout");
		}
		imp4.close();
		imp2.close();
		if (verbose || step)
			IJ.log(ACRlog.qui() + "END");
		return matout;
	}

	/**
	 * Dati i vertici trovati, calcola l'angolo di rotazione del fantoccio
	 * 
	 * @param vertices
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	static double phantomRotation2(double[][] vertices, boolean step, boolean verbose, int timeout) {

		int CX = (int) Math.round(vertices[0][2]);
		int CY = (int) Math.round(vertices[1][2]);
		int DX = (int) Math.round(vertices[0][3]);
		int DY = (int) Math.round(vertices[1][3]);
		Line l1 = new Line(DX, DY, CX, CY);
		double angle = l1.getAngle();
		if (verbose) {
			ACRlog.logMatrix(vertices, ACRlog.qui() + "vertices");
			IJ.log(ACRlog.qui() + "angolo= " + angle + " gradi");
		}

		return angle;
	}

	static double phantomRotation(double[][] vertices, boolean step, boolean verbose, int timeout) {

		double CX = vertices[0][2];
		double CY = vertices[1][2];
		double DX = vertices[0][3];
		double DY = vertices[1][3];
		double angle = ACRgraphic.getAngle(CX, CY, DX, DY);
		if (verbose) {
			ACRlog.logMatrix(vertices, ACRlog.qui() + "vertices");
			IJ.log(ACRlog.qui() + "angolo= " + angle + " gradi");
		}

		return angle;
	}

	/**
	 * dati due punti di un segmento inclinato ed una distanza, traccia un segmento
	 * parallelo, restituendo gli offset da applicare ai punti del segmento per
	 * traslarlo correttamente USATO
	 * 
	 * @param punti
	 * @param distanza
	 * @return
	 */
	public static double[] parallela(double[][] punti, double distanza) {
		double ax = punti[0][2];
		double ay = punti[1][2];
		double bx = punti[0][3];
		double by = punti[1][3];
		double slope = (ax - bx) / (ay - by);
		double perpendicolar = -1 / slope;
		double sinperpendicolar = perpendicolar / Math.sqrt(1 + perpendicolar * perpendicolar);
		double cosperpendicolar = 1 / Math.sqrt(1 + perpendicolar * perpendicolar);
		// data l'ipotenusa, la distanza perpendicolare tra i due segmenti, devo trovare
		// i due cateti, che sono gli spostamenti X ed Y per i punti del segmento
		// parallelo
		double xoffset = distanza * sinperpendicolar;
		double yoffset = distanza * cosperpendicolar;

		if (false) {
			IJ.log(ACRlog.qui() + "input a= " + ax + " , " + ay + " b= " + bx + " , " + by);
			IJ.log(ACRlog.qui() + "slope= " + slope);
			IJ.log(ACRlog.qui() + "perp= " + perpendicolar);
			IJ.log(ACRlog.qui() + "sinperp= " + sinperpendicolar);
			IJ.log(ACRlog.qui() + "cosperp= " + cosperpendicolar);
			IJ.log(ACRlog.qui() + "xoffset= " + xoffset);
			IJ.log(ACRlog.qui() + "yoffset= " + yoffset);
		}

		double[] vetout = new double[2];
		vetout[0] = xoffset;
		vetout[1] = yoffset;
		return vetout;
	}

	/**
	 * Effettua il filtraggio, in modo da lasciare solo la silouette degli inserti
	 * interni della slice1 USATO DA THICKNESS E RESOLUTION
	 * 
	 * @param imp1          immagine da analizzare
	 * @param phantomCircle dati profilo circolare esterno fantoccio
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return ImagePlus con immagine filtrata
	 */
	public static ImagePlus phantomFilter(ImagePlus imp1, double[] phantomCircle, boolean step, boolean verbose,
			int timeout) {

		if (verbose || step)
			IJ.log(ACRlog.qui() + "START");
		double xcircle = phantomCircle[0];
		double ycircle = phantomCircle[1];
		double dcircle = phantomCircle[2] - 4;
		ImagePlus imp3 = imp1.duplicate();
		ImagePlus imp4 = applyThreshold1(imp3);
		imp4.setRoi(new OvalRoi(xcircle - dcircle / 2, ycircle - dcircle / 2, dcircle, dcircle));
		ImageProcessor ip4 = imp4.getProcessor();
		ip4.setColor(Color.WHITE);
		ip4.fillOutside(imp4.getRoi());
		imp4.updateAndDraw();
		imp4.show();
		ACRutils.zoom(imp4);
		int options = ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES + ParticleAnalyzer.SHOW_MASKS
				+ ParticleAnalyzer.INCLUDE_HOLES;
		int minCirc = 0;
		int maxCirc = 1;
		int minSizePixels = 3500;
		int maxSizePixels = 300000;
		ResultsTable rt1 = new ResultsTable();
		int measurements = Measurements.CENTER_OF_MASS + Measurements.AREA;
		ParticleAnalyzer pa1 = new ParticleAnalyzer(options, measurements, rt1, minSizePixels, maxSizePixels, minCirc,
				maxCirc);
		pa1.setHideOutputImage(false);
		pa1.analyze(imp4);
		ImagePlus imp5 = pa1.getOutputImage();
		if (imp5 == null)
			ACRlog.waitHere("imp5==null niente immagine da particle analyzer");
		imp5.setTitle("005");
		imp5.show();
		ACRutils.zoom(imp5);
		imp3.close();
		imp4.close();
		if (verbose || step)
			IJ.log(ACRlog.qui() + "END");
		return imp5;

	}

	/**
	 * Effettua il threshold dell'immagine
	 * 
	 * @param imp1
	 * @param phantomCircle
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static ImagePlus phantomFilter32(ImagePlus imp1, double[] phantomCircle, boolean step, boolean verbose,
			int timeout) {

		IJ.log(ACRlog.qui() + "START");
		if (imp1 == null)
			ACRlog.waitHere(ACRlog.qui() + "imp1==null!!!");
		ImagePlus imp3 = imp1.duplicate();
		imp3.setTitle("003");
//		int threshold = computeThreshold32(imp3);
		ImagePlus imp4 = applyThreshold32(imp3, "Otsu");
		imp4.setTitle("phFilter32/apThresh32");
		IJ.log(ACRlog.qui());
		imp4.updateAndDraw();
		imp4.show();
		ACRutils.zoom(imp4);
		ACRlog.waitHere("THRESHOLDED");
		int options = ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES + ParticleAnalyzer.SHOW_MASKS
				+ ParticleAnalyzer.INCLUDE_HOLES;
		int minCirc = 0;
		int maxCirc = 1;
		int minSizePixels = 3500;
		int maxSizePixels = 300000;
		IJ.log(ACRlog.qui());
		ResultsTable rt1 = new ResultsTable();
		int measurements = Measurements.CENTER_OF_MASS + Measurements.AREA;
		ParticleAnalyzer pa1 = new ParticleAnalyzer(options, measurements, rt1, minSizePixels, maxSizePixels, minCirc,
				maxCirc);
		IJ.log(ACRlog.qui());
		pa1.setHideOutputImage(false);
		pa1.analyze(imp4);
		ImagePlus imp5 = pa1.getOutputImage();
		IJ.log(ACRlog.qui());
		if (imp5 == null)
			ACRlog.waitHere("imp5==null niente immagine da particle analyzer");
		imp5.setTitle("005");
		imp5.show();
		ACRutils.zoom(imp5);
		IJ.log(ACRlog.qui() + "DOPO PARTICLEANALYZER");
		imp3.close();
		imp4.close();
		return imp5;
	}

	/**
	 * Compute the beginnig threshold value
	 * 
	 * @param imp1 raw image
	 * @return
	 */
	private static int computeThreshold32(ImagePlus imp1) {
		ImageStatistics stat1 = imp1.getStatistics();
		int[] tHisto = stat1.histogram;
		ACRlog.logVector(tHisto, ACRlog.qui() + tHisto);
		IJ.log(ACRlog.qui() + " imp1= " + imp1.getBitDepth());
		AutoThresholder autoThresholder = new AutoThresholder();

		String[] metodi = autoThresholder.getMethods();
		int threshold = 0;
		for (int i1 = 0; i1 < metodi.length; i1++) {
			String met = metodi[i1];
			threshold = autoThresholder.getThreshold(metodi[i1], tHisto);
			IJ.log(ACRlog.qui() + " methodo= " + metodi[i1] + " threshold= " + threshold);
		}

		return autoThresholder.getThreshold(Method.Huang, tHisto);
	}

	/**
	 * Applica correttamente l'operazione di Thresholding
	 * 
	 * @param imp1
	 * @return
	 */
	public static ImagePlus applyThreshold1(ImagePlus imp1) {
		int slices = 1;
		ImageProcessor ip1 = imp1.getProcessor();

		Calibration cal1 = imp1.getCalibration();

		short[] pixels1 = rawVector((short[]) ip1.getPixels(), cal1);

		int threshold = (int) cal1.getCValue(ip1.getAutoThreshold());
		IJ.log(ACRlog.qui() + "threshold= " + threshold);

//		short[] pixels1 = (short[]) ip1.getPixels();
//		int threshold=500;

		ImagePlus imp2 = NewImage.createByteImage("Thresholded", imp1.getWidth(), imp1.getHeight(), slices,
				NewImage.FILL_BLACK);
		ByteProcessor ip2 = (ByteProcessor) imp2.getProcessor();
		byte[] pixels2 = (byte[]) ip2.getPixels();
		for (int i1 = 0; i1 < pixels2.length; i1++) {
			if (pixels1[i1] >= threshold) {
				pixels2[i1] = (byte) 255;
			} else {
				pixels2[i1] = (byte) 0;
			}
		}
		ip2.setThreshold(0, 127);
		imp2.updateAndDraw();
		IJ.log(ACRlog.qui());
//		ip2.resetMinAndMax();
		return imp2;
	}

	/**
	 * 
	 * @param imp1
	 * @param method
	 * @return
	 */
	public static ImagePlus applyThreshold32(ImagePlus imp1, String method) {
		int slices = 1;
		ImageProcessor ip1 = imp1.getProcessor();
		Calibration cal1 = imp1.getCalibration();

		short[] pixels1 = rawVector((short[]) ip1.getPixels(), cal1);

		int threshold = (int) cal1.getCValue(ip1.getAutoThreshold());
		IJ.log(ACRlog.qui() + "threshold= " + threshold);

		ImagePlus imp2 = NewImage.createByteImage("Thresholded", imp1.getWidth(), imp1.getHeight(), slices,
				NewImage.FILL_BLACK);
		ByteProcessor ip2 = (ByteProcessor) imp2.getProcessor();
		byte[] pixels2 = (byte[]) ip2.getPixels();
		for (int i1 = 0; i1 < pixels2.length; i1++) {
			if (pixels1[i1] >= threshold) {
				pixels2[i1] = (byte) 0;
			} else {
				pixels2[i1] = (byte) 255;
			}
		}
		ip2.resetMinAndMax();
		return imp2;
	}

	/**
	 * Esperimenti per localizzare il pattern della risoluzione. La procedura mi
	 * pare sia: con autoThreshold applico un metodo di sogliatura ed ottengo un
	 * valore di threshold nel range 0-255. Questo vuol dire che ho bisogno di un
	 * aimmagine ad 8 bit. Ma il problema e' che per otenere la mask mi viene
	 * richiesta una gia' thresholded image. NON CI SI CAPISCE UN CAXXXXO
	 * 
	 * @param imp1
	 * @return
	 */
	public static ImagePlus applyThreshold32(ImagePlus imp1, int threshold) {

		ImagePlus imp2 = imp1.duplicate();
		ImageProcessor ip2 = imp2.getProcessor();
		boolean doScale = true;
		ImageProcessor ip3 = ip2.convertToByte(doScale);
		ImagePlus imp3 = new ImagePlus("8bit", ip3);
		byte[] pixels3 = (byte[]) ip3.getPixels();
//		// ora imp3 e'ad 8 bit
//		String[] metodi = AutoThresholder.getMethods();
//		for (String metodo : metodi) {
//			IJ.log(metodo);
//		}
//
//		Thresholder.setMethod("Otsu");
//		Thresholder.setBackground("Black");
//		
//	Calibration cal2 = imp2.getCalibration();

//		short[] pixels2 = rawVector((short[]) ip2.getPixels(), cal2);
//		IJ.log(ACRlog.qui());
//		int threshold1 = ACRlocalizer.computeThreshold32(imp1);
//		int threshold2 = (int) cal2.getCValue(ip2.getAutoThreshold());
//		IJ.log(ACRlog.qui() + "threshold1= " + threshold1+ " threshold2= " +threshold2);

		int slices = 1;
		ImagePlus imp4 = NewImage.createByteImage("Thresholded", imp1.getWidth(), imp1.getHeight(), slices,
				NewImage.FILL_BLACK);
		ByteProcessor ip4 = (ByteProcessor) imp4.getProcessor();
		byte[] pixels4 = (byte[]) ip4.getPixels();
		for (int i1 = 0; i1 < pixels4.length; i1++) {
			if (pixels3[i1] >= threshold) {
				pixels4[i1] = (byte) 0;
			} else {
				pixels4[i1] = (byte) 255;
			}
		}

		ip4.resetMinAndMax();
		imp4.updateAndDraw();

		IJ.log(ACRlog.qui());
		return imp4;
	}

	/**
	 * 
	 * @param pixels1
	 * @param cal1
	 * @return
	 */
	public static short[] rawVector(short[] pixels1, Calibration cal1) {
		short[] out2 = new short[pixels1.length];
		for (int i1 = 0; i1 < pixels1.length; i1++) {
			out2[i1] = (short) cal1.getRawValue(pixels1[i1]);
		}
		return out2;
	}

	/**
	 * Dalla lista dei punti del profilo di un possibile rettangolo, ricava i 4
	 * possibili vertici, calcolandone le distanze dai vertici dell'immagine
	 * 
	 * @param inpoints ATTENZIONE il formato e'routato inpoints[n][2]!!!!
	 * @param width
	 * @param height
	 * @return
	 */

	public static int[][] vertexFinder(int[][] inpoints, int width, int height, boolean verbose) {

		if (inpoints.length < inpoints[0].length) {
			IJ.log(ACRlog.qui() + "attenzione la matrice di input DEVE essere ruotata!!");
			ACRlog.waitHere("EHI, GUARDA NEL LOG, per sapere cosa hai combinato");
		}

		int[] vetX = new int[inpoints.length];
		int[] vetY = new int[inpoints.length];
		for (int i1 = 0; i1 < inpoints.length; i1++) {
			vetX[i1] = inpoints[i1][0];
			vetY[i1] = inpoints[i1][1];
		}
		int px = 0;
		int py = 0;
		int ax = 0;
		int ay = 0;
		int bx = width;
		int by = 0;
		int cx = width;
		int cy = height;
		int dx = 0;
		int dy = height;
		//
		// FORSE HO TROVATO IL MODO: PER OGNI PUNTO SI CALCOLA:
		// ABS(distanza da un vertice su X)+ABS(distanza da un vertice su Y)
		// si hanno cosi' 4 colonne, coincidenti con le coordinate X ed Y del punto.
		// Il minimo per ogni colonna rappresenta quel particolare vertice
		//
		// SE FUNZIONA E'UNA FIGATA, PURE ELEGANTE E LOGICA COME SOLUZIONE!
		//
		//
		int[][] metamatrix = new int[inpoints.length][6];

		for (int i1 = 0; i1 < inpoints.length; i1++) {
			px = inpoints[i1][0];
			py = inpoints[i1][1];
			metamatrix[i1][0] = inpoints[i1][0];
			metamatrix[i1][1] = inpoints[i1][1];
			metamatrix[i1][2] = Math.abs(px - ax) + Math.abs(py - ay); // vertice a
			metamatrix[i1][3] = Math.abs(px - bx) + Math.abs(py - by); // vertice b
			metamatrix[i1][4] = Math.abs(px - cx) + Math.abs(py - cy); // vertice c
			metamatrix[i1][5] = Math.abs(px - dx) + Math.abs(py - dy); // vertice d
//		STRANAMENTE il calcolo qui sopra da'risultati migliori del calcolo dell'ipotenusa, piu'complicato			
		}

//		if (verbose)
//			ACRlog.printMatrix(metamatrix, ACRlog.qui() + "metamatrix");

		// estraggo dalla matrice gli array con il calcolo per i vertici
		int[] vertexa = ACRutils.matExtractor(metamatrix, 2);
		int[] vertexb = ACRutils.matExtractor(metamatrix, 3);
		int[] vertexc = ACRutils.matExtractor(metamatrix, 4);
		int[] vertexd = ACRutils.matExtractor(metamatrix, 5);
		if (verbose) {
			ACRlog.logVector(vertexa, "vertexa");
			ACRlog.logVector(vertexb, "vertexb");
			ACRlog.logVector(vertexc, "vertexc");
			ACRlog.logVector(vertexd, "vertexd");
		}

		int[] posmina = ACRutils.minsearch(vertexa);
		int[] posminb = ACRutils.minsearch(vertexb);
		int[] posminc = ACRutils.minsearch(vertexc);
		int[] posmind = ACRutils.minsearch(vertexd);
		if (verbose) {
			ACRlog.logVector(posmina, "posmina [min][index]");
			ACRlog.logVector(posminb, "posminb [min][index]");
			ACRlog.logVector(posminc, "posminc [min][index]");
			ACRlog.logVector(posmind, "posmind [min][index]");
		}

		// VERDE
		int AX = metamatrix[posmina[1]][0];
		int AY = metamatrix[posmina[1]][1];
		// GIALLO
		int BX = metamatrix[posminb[1]][0];
		int BY = metamatrix[posminb[1]][1];
		// ROSSO
		int CX = metamatrix[posminc[1]][0];
		int CY = metamatrix[posminc[1]][1];
		// AZZURRO
		int DX = metamatrix[posmind[1]][0];
		int DY = metamatrix[posmind[1]][1];

		int[][] vetvertex = new int[2][4];
		vetvertex[0][0] = AX;
		vetvertex[1][0] = AY;
		vetvertex[0][1] = BX;
		vetvertex[1][1] = BY;
		vetvertex[0][2] = CX;
		vetvertex[1][2] = CY;
		vetvertex[0][3] = DX;
		vetvertex[1][3] = DY;
		return vetvertex;

	}

	/**
	 * 
	 * @param imp1
	 * @param phantomVertices
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static double[][] phantomResolutionHoles(ImagePlus imp1, double[][] phantomVertices, boolean step,
			boolean verbose, int timeout) {

//		phantomVertices[0][0] = AX; phantomVertices[0][2] = CX;
//		phantomVertices[1][0] = AY;	phantomVertices[1][2] = CY;
//		phantomVertices[0][1] = BX; phantomVertices[0][3] = DX;
//		phantomVertices[1][1] = BY; phantomVertices[1][3] = DY;

		boolean verbose2 = false;
		IJ.log(ACRlog.qui() + "START");

		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);
//		int latoROI = 11;
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		Overlay over1 = new Overlay();
		imp2.setOverlay(over1);

		ACRlog.logMatrix(phantomVertices, ACRlog.qui() + "phantomVertices");

		double A0X = phantomVertices[0][0];
		double A0Y = phantomVertices[1][0];
		double B0X = phantomVertices[0][1];
		double B0Y = phantomVertices[1][1];
		double C0X = phantomVertices[0][2];
		double C0Y = phantomVertices[1][2];
		double D0X = phantomVertices[0][3];
		double D0Y = phantomVertices[1][3];

//		double[][] punti = new double[2][2];
//		punti[0][0] = A0X;
//		punti[1][0] = A0Y;
//		punti[0][1] = B0X;
//		punti[1][1] = B0Y;

		double distanza = 15;

		double[] spostamento = ACRlocalizer.parallela(phantomVertices, distanza);

		double A1X = A0X + spostamento[0];
		double A1Y = A0Y + spostamento[1];

		double B1X = B0X + spostamento[0];
		double B1Y = B0Y + spostamento[1];

		imp2.setRoi(new RotatedRectRoi(A1X, A1Y, B1X, B1Y, distanza * 1.8));
		imp2.getRoi().setStrokeColor(Color.RED);
		over1.addElement(imp2.getRoi());
		ACRlog.waitHere("ROTATED RECTANGLE");

		ImageProcessor ip2 = imp2.getProcessor();
		ip2.setColor(Color.BLACK);
		ip2.fillOutside(imp2.getRoi());
		imp2.updateAndDraw();
		ImagePlus imp3 = imp2.duplicate();
		imp2.close();
		imp3.show();
		ACRutils.zoom(imp3);
		ACRlog.waitHere("ROTATED RECTANGLE PULITO FUORI");

		double[] phantomCircle = { 0 };

		// elaboro e filtro l'immagine, in modo da renderla binaria ed isolare i
		// particolari che in seguito andro'a rilevare
		//
//		ImagePlus imp4 = phantomFilter(imp2, phantomCircle, step, fast, verbose, timeout);
		ImagePlus imp4 = phantomFilter32(imp3, phantomCircle, step, verbose, timeout);
		Overlay over4 = new Overlay();
		imp4.setOverlay(over4);
		//
		// l'immagine di phantomElab1 e'binaria, per cui threshold=255;
		//
		int threshold = 255;
		int[] out2 = new int[4];
		double xpoint1 = 0;
		double ypoint1 = 0;
		double xpoint2 = 0;
		double ypoint2 = 0;
		int[] out4 = new int[4];
		ArrayList<Integer> arrX = new ArrayList<>();
		ArrayList<Integer> arrY = new ArrayList<>();
		// scansione per colonne
		for (int x1 = 1; x1 < width - 1; x1++) {
			verbose2 = false;
			out2 = verticalSearch(imp4, threshold, x1, verbose2);
			if (out2 != null) {
				xpoint1 = x1;
				ypoint1 = out2[0];
				arrX.add(x1);
				arrY.add(out2[0]);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp4, over4, xpoint1, ypoint1, type, size, Color.GREEN, false);
				xpoint1 = x1;
				ypoint1 = out2[1];
				arrX.add(x1);
				arrY.add(out2[1]);
				ACRutils.plotPoints(imp4, over4, xpoint1, ypoint1, type, size, Color.GREEN, false);
			}
		}
		for (int y1 = 1; y1 < width - 1; y1++) {
			verbose2 = false;
			out4 = horizontalSearch(imp4, threshold, y1, verbose2);
			if (out4 != null) {
				xpoint2 = out4[0];
				ypoint2 = y1;
				arrX.add(out4[0]);
				arrY.add(y1);
				int size = 1;
				int type = 2; // 0=hybrid, 1=cross, 2= point, 3=circle
				ACRutils.plotPoints(imp4, over4, xpoint2, ypoint2, type, size, Color.GREEN, false);
				xpoint2 = out4[1];
				ypoint2 = y1;
				arrX.add(out4[1]);
				arrY.add(y1);
				ACRutils.plotPoints(imp4, over4, xpoint2, ypoint2, type, size, Color.GREEN, false);
			}
		}

		if (arrX.size() == 0 || arrY.size() == 0) {
			IJ.log(ACRlog.qui() + "attenzione non trovato punti");
			ACRlog.waitHere(ACRlog.qui() + "attenzione non trovato punti");
		}
		// tolgo i doppioni dove scansione orizzontale e verticale si sovrappongono

		int[] vetX = ACRcalc.arrayListToArrayInt(arrX);
		int[] vetY = ACRcalc.arrayListToArrayInt(arrY);
		int[][] matXY = new int[2][vetX.length];
		for (int i1 = 0; i1 < vetX.length; i1++) {
			matXY[0][i1] = vetX[i1];
			matXY[1][i1] = vetY[i1];
		}
		if (verbose) {
			if (matXY == null)
				ACRlog.waitHere(ACRlog.qui() + "matXY==null");
			ACRlog.logMatrix(matXY, ACRlog.qui() + "matXY");
		}
		int[][] matout1 = ACRcalc.removeDuplicate(matXY);
		if (verbose)
			ACRlog.logMatrix(matout1, ACRlog.qui() + "matout1");
		if (step)
			ACRlog.waitHere();
		int[][] rotated = ACRutils.matRotate(matout1);
		// analizzo tutti i punti trovati, per ricavare i piu'prossimi agli angoli
		// dell'immagine
		int[][] matout2 = vertexFinder(rotated, width, height, verbose);
		if (verbose)
			ACRlog.logMatrix(matout2, ACRlog.qui() + "matout2");
		// andiamo a plottare i punti trovati
		// VERDE
		int AX = matout2[0][0];
		int AY = matout2[1][0];
		// GIALLO
		int BX = matout2[0][1];
		int BY = matout2[1][1];
		// ROSSO
		int CX = matout2[0][2];
		int CY = matout2[1][2];
		// AZZURRO
		int DX = matout2[0][3];
		int DY = matout2[1][3];
		if (step)
			ACRlog.waitHere("A= " + AX + " , " + AY + " B= " + BX + " , " + BY + " C= " + CX + " , " + CY + " D= " + DX
					+ " , " + DY);

		ACRutils.plotPoints(imp4, over4, AX, AY, Color.RED, 4, 4);
		ACRutils.plotPoints(imp4, over4, BX, BY, Color.YELLOW, 4, 4);
		ACRutils.plotPoints(imp4, over4, CX, CY, Color.BLUE, 4, 4);
		ACRutils.plotPoints(imp4, over4, DX, DY, Color.BLUE, 4, 4);

		Line l1 = new Line(DX, DY, CX, CY);

		// da qui in poi necessitiamo di maggior precisione degli integer, per cui
		// trasformo in double
		double[][] matout = new double[2][2];
		matout[0][0] = (double) DX;
		matout[1][0] = (double) DY;
		matout[0][1] = (double) CX;
		matout[1][1] = (double) CY;

//		double[] vetout = ACRlocalizer.parallela(matin, -18);
//		int EX = DX + (int) vetout[0];
//		int EY = DY + (int) vetout[1];
//		int FX = CX + (int) vetout[0];
//		int FY = CY + (int) vetout[1];

//		ACRutils.plotPoints(imp4, over4, EX, EY, Color.CYAN, 4, 4);
//		ACRutils.plotPoints(imp4, over4, FX, FY, Color.CYAN, 4, 4);
//
//		double angle = l1.getAngle();

		return matout;
	}

	/**
	 * Effettua la rototraslazione di una matrice di punti
	 * 
	 * @param imp1
	 * @param matin
	 * @param phantomCircle
	 * @param angle
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static double[][] rototrasla(ImagePlus imp1, double[][] matin, double[] phantomCircle, double angle,
			boolean step, boolean verbose, int timeout) {

		double[][] matout = new double[matin.length][matin[0].length];
		ImagePlus imp2 = imp1.duplicate();
		imp2.setTitle("rototrasla");

		/// ATTENZIONE, DEVO CAMBIARE DI SEGNO L'ANGOLO, MA PECCCHHEEEEEEE???? SPIEGONE,
		/// PLEASE !!!! (credo che basti scambiare tra loro inizie e fine della Line su
		/// cui viene calcolato l'angolo)
		angle = angle * (-1);
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		double xcenter = phantomCircle[0];
		double ycenter = phantomCircle[1];
		//
		// il riferimento per la traslazione del centro fantoccio e' dato, nelle mie
		// simulazioni, dal centro dell'immagine, quindi vado a fare la differenza
		// rispetto a height/2 e width/2. Quanto ala rotazione facciamo riferimento a 0
		// gradi
		//
		double xoffset = width / 2 - xcenter;
		double yoffset = height / 2 - ycenter;
		IJ.log(ACRlog.qui() + "xoffset= " + xoffset + " yoffset= " + yoffset + " angle= " + angle);
		//
		// ACRlog.waitHere("angolo ricevuto= " + angle + "°");
		// and only for breaking browns chestnuts .... use radians ROMPICOJONI!
		//
		double radangle = Math.toRadians(angle);
		double cos1 = Math.cos(radangle);
		double sin1 = Math.sin(radangle);
		for (int i1 = 0; i1 < matin[0].length; i1++) {
			double FX = matin[0][i1] - width / 2;
			double FY = matin[1][i1] - height / 2;
			double FX1 = FX * cos1 - FY * sin1;
			double FY1 = FX * sin1 + FY * cos1;
			double FX2 = FX1 - xoffset + width / 2;
			double FY2 = FY1 - yoffset + height / 2;
			matout[0][i1] = FX2;
			matout[1][i1] = FY2;
		}
		imp2.close();
		return matout;
	}

	/**
	 * Effettua la rototraslazione di una matrice di punti
	 * 
	 * @param imp1
	 * @param matin
	 * @param phantomCircle
	 * @param angle
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 */
	public static double[][] rototrasla(double[][] matin, double width, double height, double[] phantomCircle,
			double angle, boolean step, boolean verbose, int timeout) {

		double[][] matout = new double[matin.length][matin[0].length];

		/// ATTENZIONE, DEVO CAMBIARE DI SEGNO L'ANGOLO, MA PECCCHHEEEEEEE???? SPIEGONE,
		/// PLEASE !!!! (credo che basti scambiare tra loro inizie e fine della Line su
		/// cui viene calcolato l'angolo)
		angle = angle * (-1);
		double xcenter = phantomCircle[0];
		double ycenter = phantomCircle[1];
		//
		// il riferimento per la traslazione del centro fantoccio e' dato, nelle mie
		// simulazioni, dal centro dell'immagine, quindi vado a fare la differenza
		// rispetto a height/2 e width/2. Quanto ala rotazione facciamo riferimento a 0
		// gradi
		//
		double xoffset = width / 2 - xcenter;
		double yoffset = height / 2 - ycenter;
		IJ.log(ACRlog.qui() + "xoffset= " + xoffset + " yoffset= " + yoffset + " angle= " + angle);
		//
		// ACRlog.waitHere("angolo ricevuto= " + angle + "°");
		// and only for breaking browns chestnuts .... use radians ROMPICOJONI!
		//
		double radangle = Math.toRadians(angle);
		double cos1 = Math.cos(radangle);
		double sin1 = Math.sin(radangle);

		IJ.log("radangle= " + radangle + " cos= " + cos1 + " sin= " + sin1);

		for (int i1 = 0; i1 < matin[0].length; i1++) {
			double FX = matin[0][i1] - width / 2;
			double FY = matin[1][i1] - height / 2;
			double FX1 = FX * cos1 - FY * sin1;
			double FY1 = FX * sin1 + FY * cos1;
			double FX2 = FX1 - xoffset + width / 2;
			double FY2 = FY1 - yoffset + height / 2;
			matout[0][i1] = FX2;
			matout[1][i1] = FY2;
		}
		return matout;
	}

	/**
	 * posiziona le roi (quasi) in corrispondenza con i fori del fantoccio
	 * risoluzione, in maniera statica
	 * 
	 * @param imp1
	 * @param phantomCircle
	 * @param angle
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 */
	public static void staticGridMatrix66(ImagePlus imp1, double[] phantomCircle, double angle, boolean step,
			boolean fast, boolean verbose, int timeout) {

		ImagePlus imp2 = imp1.duplicate();
		imp2.show();
		ACRutils.zoom(imp2);
//		int latoROI = 11;
		int width = imp2.getWidth();
		int height = imp2.getHeight();
		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);

		// innanzitutto vado a tracciare una struttura di punti, che potrebbe essere
		// fatta coincidere con la matrice di fori della risoluzione
		double lato = 2.2;
		double k1 = 0.0;
		double k2 = 0.2;

		double[] primo = new double[2];
		primo[0] = 64.2;
		primo[1] = 62.1;
		double[] secondo = new double[2];
		secondo[0] = 57;
		secondo[1] = 70;

		staticSubMatrix(imp2, over2, primo, secondo, lato, k1, k2);

		lato = 2.4;
		double[] terzo = new double[2];
		terzo[0] = 96.5;
		terzo[1] = 62;
		double[] quarto = new double[2];
		quarto[0] = 88;
		quarto[1] = 70.5;
		k1 = 0;
		k2 = 0.2;

		staticSubMatrix(imp2, over2, terzo, quarto, lato, k1, k2);

		lato = 2.6;
		double[] quinto = new double[2];
		quinto[0] = 129.5;
		quinto[1] = 61.5;
		double[] sesto = new double[2];
		sesto[0] = 119.5;
		sesto[1] = 71.5;
		k1 = 0;
		k2 = 0.2;

		staticSubMatrix(imp2, over2, quinto, sesto, lato, k1, k2);

	}

	public static double[][] movableGridMatrix(ImagePlus imp1, double[] phantomCircle, double angle, boolean step,
			boolean fast, boolean verbose, int timeout) {

		ImagePlus imp2 = imp1.duplicate();
//		imp2.show();
//		ACRutils.zoom(imp2);
////		int latoROI = 11;

		int width = imp2.getWidth();
		int height = imp2.getHeight();
		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp2, ACRconst.DICOM_PIXEL_SPACING), 1));

		Overlay over2 = new Overlay();
		imp2.setOverlay(over2);
		// calcoliamo l'offset del centro fantoccio, rispetto al centro immagine
//		double[] offset = new double[2];
//		offset[0] = phantomCircle[0] - width / 2;
//		offset[1] = phantomCircle[1] - height / 2;

		// innanzitutto vado a calcolare le tre strutture di punti di una immagine
		// perfetta

		double[] primo = new double[2];
		primo[0] = 63.5;
		primo[1] = 70;
		double hole = 0.7 / dimPixel;
		double[][] punti1 = ACRlocalizer.perfectSubMatrix(primo, hole, width, height);
		double[] secondo = new double[2];
		secondo[0] = 95.5;
		secondo[1] = 70;
		hole = 0.8 / dimPixel;
		double[][] punti2 = ACRlocalizer.perfectSubMatrix(secondo, hole, width, height);
		double[] terzo = new double[2];
		terzo[0] = 128.5;
		terzo[1] = 70;
		hole = 0.9 / dimPixel;
		double[][] punti3 = ACRlocalizer.perfectSubMatrix(terzo, hole, width, height);
		double[][] punti = new double[2][96];
		int conta = 0;
		for (int i1 = 0; i1 < punti1[0].length; i1++) {
			punti[0][conta] = punti1[0][i1];
			punti[1][conta] = punti1[1][i1];
			conta++;
		}
		for (int i1 = 0; i1 < punti2[0].length; i1++) {
			punti[0][conta] = punti2[0][i1];
			punti[1][conta] = punti2[1][i1];
			conta++;
		}
		for (int i1 = 0; i1 < punti3[0].length; i1++) {
			punti[0][conta] = punti3[0][i1];
			punti[1][conta] = punti3[1][i1];
			conta++;
		}

		double[][] matout = ACRlocalizer.rototrasla(imp2, punti, phantomCircle, angle, step, verbose, timeout);
		imp2.changes = false;
		imp2.close();
		return matout;

	}

	/**
	 * 
	 * @param imp2
	 * @param over2
	 * @param second
	 * @param first
	 * @param lato
	 * @param k1
	 * @param k2
	 */
	public static void staticSubMatrix(ImagePlus imp2, Overlay over2, double[] second, double[] first, double lato,
			double k1, double k2) {
		double ax = first[0];
		double ay = first[1];
		double bx = second[0];
		double by = second[1];

		double fx = 0;
		double fy = 0;

		for (int i1 = 0; i1 < 4; i1++) {
			fx = bx + i1 * lato;
			for (int i2 = 0; i2 < 4; i2++) {
				fx = fx - k2 * lato;
				fy = by + i2 * lato; // + i2 * k1 * lato;
				Roi r1 = new Roi(fx, fy, lato, lato);
				IJ.log("bx= " + fx + " by= " + fy + " VERDE");
				imp2.setRoi(r1);
				imp2.getRoi().setStrokeColor(Color.GREEN);
				over2.addElement(imp2.getRoi());
				imp2.killRoi();
				// ACRlog.waitHere();
			}
//			bx = bx + k1 * lato;
//			by= by+ k2*lato;
		}

		fx = 0;
		fy = 0;

		for (int i1 = 0; i1 < 4; i1++) {

			for (int i2 = 0; i2 < 4; i2++) {
				fx = ax + i1 * lato;
				fy = ay + i2 * lato; // + i2 * k1 * lato;
				IJ.log("bx= " + fx + " by= " + fy + " ROSSO");
				Roi r1 = new Roi(fx, fy, lato, lato);
				imp2.setRoi(r1);
				imp2.getRoi().setStrokeColor(Color.RED);
				over2.addElement(imp2.getRoi());
				imp2.killRoi();
				// ACRlog.waitHere();
			}
//			ax = ax + k1 * lato;
			ay = ay - k2 * lato;
		}

	}

	/**
	 * SUPERATO, VEDI movableGridMatrix
	 * 
	 * @param imp1
	 * @param over1
	 * @param commonpoint
	 * @param hole
	 * @param offset
	 * @param angle
	 * @param lato
	 * @param k1
	 * @param k2
	 */
	public static void movableSubMatrix22(ImagePlus imp1, Overlay over1, double[] commonpoint, double hole,
			double[] offset, double angle, double lato, double k1, double k2) {
		double ax = commonpoint[0];
		double ay = commonpoint[1];
//		double bx = first[0];
//		double by = first[1];

		double fx = 0;
		double fy = 0;

		double step = 3 * hole;

		//
		// i tre diversi gruppi di griglie differiscono per il diametro dei fori, che
		// vale 0.9 per la prima a dx, 0.8 per la centrale e 0.7 per la sx
		// le due griglie superiore ed inferiore hanno in comune un foro COMMONPOINT che
		// possiamo utilizzare come riferimento
		//
		// griglia superiore, con righe orizzontali e colonne scalate di frazioni di
		// pixel con anglo di inclinazione circa 97 gradi
		//

		PointRoi pr1 = new PointRoi(ax, ay);
		pr1.setPointType(2);
		pr1.setSize(4);

		imp1.setRoi(pr1);
		imp1.getRoi().setStrokeColor(Color.GREEN);
		over1.addElement(imp1.getRoi());
		double width = imp1.getWidth();
		double height = imp1.getHeight();
//		imp1.killRoi();
		// applico la formula della rototraslazione
		double radangle = Math.toRadians(-angle);
		double cos1 = Math.cos(radangle);
		double sin1 = Math.sin(radangle);

		fx = ax - offset[0] - width / 2;
		fy = ay - offset[1] - height / 2;
		double fx1 = fx * cos1 - fy * sin1;
		double fy1 = fx * sin1 + fy * cos1;
		double fx2 = fx1 + width / 2;
		double fy2 = fy1 + height / 2;
		PointRoi pr2 = new PointRoi(fx2, fy2);
		pr2.setPointType(2);
		pr2.setSize(4);

		imp1.setRoi(pr2);
		imp1.getRoi().setStrokeColor(Color.RED);
		over1.addElement(imp1.getRoi());
//		imp1.killRoi();

		ACRlog.waitHere();

	}

	/**
	 * La perfectSubMatrix sarebbe quella posizionata da me medesimo, perfettamente
	 * a bolla ed al centro dell'immagine. PFUI, dilettanti non ci riuscirete mai!
	 * UTILIZZATO
	 * 
	 * @param commonpoint punto comune tra le due matrici di fori, inf e sup
	 * @param hole        diametro del foro, espresso in pixel!!
	 * @param width1      larghezza immagine
	 * @param height1     altezza immagine
	 * @return matrice con le coordinate x ed y di tutti i 32*3 fori
	 */
	public static double[][] perfectSubMatrix(double[] commonpoint, double hole, int width1, int height1) {

		//
		// i tre diversi gruppi di griglie differiscono per il diametro dei fori, che
		// vale 0.9 mm per la prima a dx, 0.8 mm per la centrale e 0.7 mm per la sx
		// le due griglie superiore ed inferiore hanno in comune un foro COMMONPOINT che
		// possiamo utilizzare come riferimento
		//
		// utilizziamo le formule trigonometriche di rototraslazione all'interno dei
		// loop
		//
		double ax = commonpoint[0];
		double ay = commonpoint[1];
		double step = 2 * hole;
		double angle1 = -6;
		double radangle1 = Math.toRadians(-angle1);
		double cos1 = Math.cos(radangle1);
		double sin1 = Math.sin(radangle1);
		double width = (double) width1;
		double height = (double) height1;
		double fx = ax - width / 2;
		double fy = ay - height / 2;
		double fx1 = 0;
		double fy1 = 0;
		double fx2 = 0;
		double fy2 = 0;
		double[][] punti = new double[2][32];
		int conta = 0;
		//
		// GRIGLIA INFERIORE PER COLONNE VERTICALI E RIGHE SCALATE DI 6 gradi
		//
		for (int i1 = 0; i1 < 4; i1++) {
			for (int i2 = 0; i2 < 4; i2++) {
				fx1 = fx - (step * i1);
				fy1 = fy + step * i1 * sin1 + step * i2 * cos1;
				fx2 = fx1 + width / 2;
				fy2 = fy1 + height / 2;
				punti[0][conta] = fx2;
				punti[1][conta] = fy2;
				conta++;
			}
		}
		double angle2 = 6;
		double radangle2 = Math.toRadians(-angle2);
		double cos2 = Math.cos(radangle2);
		double sin2 = Math.sin(radangle2);
		//
		// GRIGLIA SUPERIORE PER RIGHE ORIZZONTALI E COLONNE SCALATE DI 6 gradi
		//
		for (int i1 = 0; i1 < 4; i1++) {
			for (int i2 = 0; i2 < 4; i2++) {
				fx1 = fx + step * i1 * cos2 - step * i2 * sin2;
				fy1 = fy - step * i2;
				fx2 = fx1 + width / 2;
				fy2 = fy1 + height / 2;
				punti[0][conta] = fx2;
				punti[1][conta] = fy2;
				conta++;
			}
		}

		return punti;
	}
	/**
	 * SOSTITUITO DA PHANTOM GRID LOCALIZER ADVANCED
	 * 
	 * @param path1
	 * @param slice
	 * @param step
	 * @param fast
	 * @param verbose
	 * @param timeout
	 * @return
	 * 
	 * @Deprecated usare phantomGridLocalizerAdvanced
	 */
	@Deprecated
	public double[] phantomPositionSearch(String path1, int slice, boolean step, boolean verbose, int timeout) {

		double maxFitError = +20;
		double maxBubbleGapLimit = 2;

		ImagePlus imp1 = ACRgraphic.openImageNoDisplay(path1, false);
//		if (fast) {

//			}
		// Ricerca delle coordinate e diametro del fantoccio su slice 5
		double[] out2 = ACRlocalizer.positionSearch1(imp1, maxFitError, maxBubbleGapLimit, step, verbose, timeout);
		//
		// per rendere le cose piu' interessanti durante il debug disegno un buco nel
		// fantoccio riempiendolo con segnale a 1.0.n ed un altro buco con segnale 3000.
		// RICORDARSI DI COMMENTARE PRIMA DELL' IMPIEGO EFFETIVO
		//
//		IJ.run(imp1, "Specify...", "width=20 height=20 x=96 y=96 oval");
//		IJ.run(imp1, "Set...", "value=1");
//		IJ.run(imp1, "Specify...", "width=20 height=20 x=96 y=50 oval");
//		IJ.run(imp1, "Set...", "value=3000");
//		imp1.updateAndDraw();

		double dimPixel = ACRutils
				.readDouble(ACRutils.readSubstring(ACRutils.readDicomParameter(imp1, ACRconst.DICOM_PIXEL_SPACING), 1));

		int xphantom = (int) out2[0];
		int yphantom = (int) out2[1];
		int dphantom = (int) out2[2];
// =========================================
		Overlay over1 = new Overlay(); // con questo definisco un overlay trasparente per i disegni
		imp1.setOverlay(over1);
// -----------------------------------------------------------------
// Visualizzo sull'immagine il posizionamento, ricevuto da  positionSearch1, che verra' utilizzato: 
// cerchio esterno fantoccio in rosso
// -----------------------------------------------------------------

		if (true) {
			if (!imp1.isVisible())
				imp1.show();
			// zoom(imp1);
			imp1.setRoi(new OvalRoi(xphantom - dphantom / 2, yphantom - dphantom / 2, dphantom, dphantom));
			imp1.getRoi().setStrokeColor(Color.RED);
			over1.addElement(imp1.getRoi());
			imp1.killRoi();
			ACRlog.waitHere("MainUnifor> cerchio esterno rosso, fantoccio rilevato da positionSearch1", true, timeout);
		}

		return out2;

	}


}
