package qualityACR;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class UniformityEliminati {
	private static final boolean debug = true;
	
	
	/**
	 * Calcolo dei puni min e max, secondo l'algoritmo dell'articolo. Genera anche
	 * una immagine con i pixel della statistica, che su fa, uso quelli? In tal caso
	 * devo ritornare un vettore di double, per inserire anche il vaore di min e
	 * max, oltre alle loro coordinate
	 * 
	 * @param imp1          immagine di input
	 * @param phantomCircle cerchio esterno fantoccio
	 * @param MROIcircle    cerchio MROI
	 * @param pixint100     dimensioni roi di calcolo (dispari) >100 mmq
	 * @param timeout1
	 * @param verbose
	 * @param fast
	 * @return
	 */
	public static double[] minmaxArticolo(ImagePlus imp1, int[] phantomCircle, int[] MROIcircle, int pixint100,
			boolean step, boolean verbose, int timeout1) {

		// ---------------------------------------------------------------------------------------------
		// siccome il manuale ACR e'orientato all'elaborazione manuale delle immagini,
		// per la elaborazione automatica mi ispiro all'articolo
		// jimaging-06-00111-v2.pdf
		// An Automated Method for Quality Control in MRI
		// Systems: Methods and Considerations
		// Angeliki C. Epistatou 1, Ioannis A. Tsalafoutas 2 and Konstantinos K.
		// Delibasis
		// Received: 28 August 2020; Accepted: 14 October 2020; Published: 18 October
		// 2020
		// Nell'articolo vengono descritte le procedure automatizzate del loro programma
		// ---------------------------------------------------------------------------------------------

		int xmroi = MROIcircle[0];
		int ymroi = MROIcircle[1];
		int dmroi = MROIcircle[2];

		int xphantom = phantomCircle[0];
		int yphantom = phantomCircle[1];
		int dphantom = phantomCircle[2];

		imp1.killRoi();
		ImagePlus imp2 = imp1.duplicate();

		// ============================================================================
		// Estraggo i vari vettori su cui faremo i calcoli
		// ============================================================================
		// inizio estraendo in imageVetDoublePixels tutti i pixel dell'immagine in
		// formato double, in modo da poter fare i calcoli con una certa precisione
		// ============================================================================
		double[] imageVetDoublePixels = doubleVetPixelsExtractor(imp2);
		double[] imageResultDoublePixels = new double[imageVetDoublePixels.length];
		// ora creo una mask in formato byte, in cui i pixel interni alla MROI valgono
		// 255 ed il fondo vale 0
		byte[] imageVetMask = byteVetMaskBuilder(imp2, xmroi, ymroi, dmroi, timeout1, verbose);

		int width = imp2.getWidth();
		int height = imp2.getHeight();

		if (verbose || step) {
			ACRlog.vetPrint(imageVetDoublePixels, "##### imageVetDoublePixels IMMAGINE SORGENTE");
			ACRlog.vetPrint(imageVetMask, "##### imageVetDoublePixels IMMAGINE SORGENTE");
			ACRlog.waitHere();
		}
		int sublato = pixint100;
		int halfsublato = sublato / 2;
		// Genero un kernel costituito da una immagine 8 bit di lato pixint100. Nella
		// immagine viene generata una Roi circolare con centro sul pixel centrale del
		// kernel e diametro pixint100. I pixel di questa ROI sono a 255 ed il fondo a 0
		ImagePlus impKernel = kernelBuilder(pixint100, timeout1, verbose);
		// estraggo il vettore contenente i pixel del kernel
		byte[] kernelVetMask = byteVetKernelExtractor(impKernel);
		// byte[] kernelTarocco = byteVetKernelExtractor(impKernelTarocco) // veniva
		// usato nei primi test;
		if (verbose == false)
			ACRlog.vetPrint(kernelVetMask, "#### kernelVetMask KERNEL CIRCOLARE");
		// vetPrint(kernelTarocco, impKernel.getWidth(), impKernel.getHeight(),
		// "kernelTarocco") // veniva usato nei primi test;
		// =================================================
		// =============== motore di calcolo ===============
		// =================================================
		boolean trigger1 = false;
		boolean trigger2 = false;
		// inizialmente scansiono l'intera immagine, tanto i pixel verranno considerati
		// solo se corrispondono ad un 255 nella immagine risultante dall' AND dei due
		// kernel
		for (int y1 = 0; y1 < height; y1++) {
			int offset1 = y1 * width;
			for (int x1 = 0; x1 < width; x1++) {
				if ((y1 == 30) && (x1 == 84)) {
					trigger2 = verbose; // usato per stampe di debug durante i test
				}
				// Estrazione dei pixel di una sottomatrice di dimensioni sublato (pixint100)
				// quindi delle stesse dimensioni del kernel, centrata su x1,y1 (e quindi scorre
				// gradualmente sull'intera immagine (subImage)
				double[] subImage = extractSubmatrix(imageVetDoublePixels, width, height, x1, y1, sublato, timeout1,
						trigger2, trigger2);
				if (subImage == null) {
					continue;
				}
				if (trigger2) {
					IJ.log("*************************");
					ACRlog.vetPrint(subImage, "**** subImage IMMAGINETTA");
					IJ.log("ATTENZIONE: va bene che vi siano zeri, se siamo sull'orlo del fantoccio, "
							+ "le mask precedenti possono aver messo a zero i pixel che stiamo considerando");
					IJ.log("*************************");
				}
				// IJ.log("submatrixCenter " + x1 + " , " + y1);
				// per ogni pixel >0 nella imageVetMask estraggo il quadrato di lato pixint100
				// sia della imageVetMask che della imageVetPixel
				// Analogamente a subImage, estraggo anche una subMask, delle stesse dimensioni
				// del kernel, centrata su x1,y1
				byte[] subMask = extractSubmatrix(imageVetMask, width, height, x1, y1, sublato, timeout1, trigger2,
						trigger2);
				if (subMask == null) {
					continue;
				}
				if (trigger2) {
					ACRlog.vetPrint(subMask, "**** subMask MASCHERETTA");
				}
				// Infine effettuo un AND tra la subMask ed il kernel circolare. Il risultato
				// contiene 255 solo quando i corrispondenti pixels sono ambedua a 255,
				// altrimenti 0. kernelVero
				byte[] kernelVero = kernelsAND(subMask, kernelVetMask, timeout1, trigger2, trigger2);
				if (trigger2) {
					ACRlog.vetPrint(kernelVero, "**** AND_KERNELS KERNEL");
				}
				trigger1 = false;
				if ((y1 == 30) && (x1 == 84)) {
					trigger1 = verbose;
				}
				int upperleftx = x1 - halfsublato - 1;
				int point = offset1 + upperleftx + x1;
				if (point > imageVetDoublePixels.length) {
					continue;
				}
				// ============================================================================
				// Il kernelVero (risultante dall AND) viene usato per il calcolo della media
				// dei pixel, si utilizzano solo i pixel che hanno il corrispondente kernel a
				// 255. La somma dei valori viene divisa per il numero di pixel col kernel a
				// 255. Il risultato scritto nel imageVetDoublePixel partecipa al calcolo dei
				// pixel adiacenti (alternativa=1) oppure viene scritto in
				// imageResultDoublePixels e non influisce sul calcolo dei pixel adiacenti
				// (alternativa=2)
				// ============================================================================
				double result1 = calcPixel(subImage, kernelVero, trigger1);
				imageResultDoublePixels[offset1 + x1] = result1;
				if (trigger2) {
					IJ.log("RISULTATO SCRITTO=  " + imageVetDoublePixels[offset1 + x1]);
				}

				trigger2 = false;
			}
		}
		// ==============================================
		if (verbose) {
			IJ.log("***************************************************");
			ACRlog.vetPrint(imageResultDoublePixels, "imageResultDoublePixels_ELABORATA");
			IJ.log("***************************************************");
		}
		ImagePlus impOutFloat = doubleVetPixelsInsertorFloat(imageResultDoublePixels, width, height);
		ImagePlus impOutShort = doubleVetPixelsInsertor(imageResultDoublePixels, width, height);

		ImagePlus imp12 = impOutFloat.duplicate();
		imp12.setTitle("ARTICOLO minmax");
		// effettuo ora la ricerca delle posizioni del massimo e minimo su questa
		// immagine risultato.
		// non so che fare se fossero in numero maggiore di uno, per adesso faccio finta
		// che non possa accadere
		imp12.setRoi(new OvalRoi(xphantom - dphantom / 2, yphantom - dphantom / 2, dphantom, dphantom));
		imp12.updateAndDraw();
		imp12.show();
		ACRutils.zoom(imp12);
		ImageProcessor ip12 = imp12.getProcessor();
// 	test generando massimi e minimi multipli per vedere se li trova		
//		ip12.putPixelValue(85, 80, 4486.44);
//		ip12.putPixelValue(95, 85, 4486.44);
//		ip12.putPixelValue(105, 95, 4486.44);
//		ip12.putPixelValue(65, 100, 86.22);
//		ip12.putPixelValue(75, 105, 86.22);
//		ip12.putPixelValue(80, 115, 86.22);
//		imp12.updateAndDraw();
//		ACRutils.waitHere();

		ImageProcessor mask12 = imp12.getMask();
		if (mask12 == null) {
			ACRlog.waitHere();
		}
		float[] pixelsroi12 = extractRoiPixelsFloatLocale(ip12);
		float amax = ACRcalc.vetMax(pixelsroi12);
		float amin = ACRcalc.vetMin(pixelsroi12);
		if (verbose == false)
			ACRlog.waitHere("max= " + amax + " min= " + amin);
		ArrayList<Integer> vetXmaxpos = new ArrayList<Integer>();
		ArrayList<Integer> vetYmaxpos = new ArrayList<Integer>();
		ArrayList<Integer> vetXminpos = new ArrayList<Integer>();
		ArrayList<Integer> vetYminpos = new ArrayList<Integer>();
		float[] pixels12 = (float[]) ip12.getPixels();
		for (int y2 = 0; y2 < ip12.getWidth(); y2++) {
			int offset = y2 * ip12.getWidth();
			for (int x2 = 0; x2 < ip12.getHeight(); x2++) {
				float pix12 = (float) pixels12[offset + x2];
				if (Float.compare(amax, pix12) == 0) {
					vetXmaxpos.add(x2);
					vetYmaxpos.add(y2);
				}
				if (Float.compare(amin, pix12) == 0) {
					vetXminpos.add(x2);
					vetYminpos.add(y2);
				}
			}
		}
		int[] vetxmax = ACRcalc.arrayListToArrayInt(vetXmaxpos);
		int[] vetymax = ACRcalc.arrayListToArrayInt(vetYmaxpos);
		int[] vetxmin = ACRcalc.arrayListToArrayInt(vetXminpos);
		int[] vetymin = ACRcalc.arrayListToArrayInt(vetYminpos);
		if (vetxmax.length != 1)
			ACRlog.waitHere("HOUSTON abbiamo un numero di massimi diverso da 1: CHE FAMO?", debug, timeout1 * 2);
		if (vetxmin.length != 1)
			ACRlog.waitHere("HOUSTON abbiamo un numero di massimi diverso da 1: CHE FAMO?", debug, timeout1 * 2);
		Overlay over12 = new Overlay();
		imp12.setOverlay(over12);
		for (int i1 = 0; i1 < vetxmax.length; i1++) {
			imp12.setRoi(vetxmax[i1] - 1, vetymax[i1] - 1, 3, 3);
			imp12.getRoi().setStrokeColor(Color.RED);
			over12.addElement(imp12.getRoi());
		}
		for (int i1 = 0; i1 < vetxmin.length; i1++) {
			imp12.setRoi(vetxmin[i1] - 1, vetymin[i1] - 1, 3, 3);
			imp12.getRoi().setStrokeColor(Color.BLUE);
			over12.addElement(imp12.getRoi());
		}
		imp12.killRoi();
		imp12.updateAndDraw();
		int xposmin = vetxmin[0];
		int yposmin = vetymin[0];
		int xposmax = vetxmax[0];
		int yposmax = vetymax[0];

		if (verbose)
			ACRlog.waitHere("TERMINE MAXMIN ARTICOLO", debug, timeout1);

		double[] vetOut = new double[6];
		vetOut[0] = (double) xposmin;
		vetOut[1] = (double) yposmin;
		vetOut[2] = (double) amin;
		vetOut[3] = (double) xposmax;
		vetOut[4] = (double) yposmax;
		vetOut[5] = (double) amax;

		return vetOut;
	}

	/**
	 * Estrae dall'immagine un vettore double, contenente il valore dei pixel
	 * 
	 * @param imp1
	 * @return
	 */
	public static double[] doubleVetPixelsExtractor(ImagePlus imp1) {

		ImageProcessor ip1 = imp1.getProcessor();
		short[] pixels1 = (short[]) ip1.getPixels();

		double[] dpixels1 = new double[pixels1.length];
		for (int i1 = 0; i1 < pixels1.length; i1++) {
			dpixels1[i1] = (double) pixels1[i1];
		}
		return dpixels1;
	}

	/**
	 * Restituisce una immagine 8 bit con a 255 i valori all'interno della Roi
	 * circolare definita in imp1
	 * 
	 * @param imp1
	 * @return
	 */
	public static byte[] byteVetMaskBuilder(ImagePlus imp1) {

		Roi roi1 = imp1.getRoi();
		if (roi1 == null) {
			ACRlog.waitHere("niente ROI che stai a fa?");
			return (null);
		}
		ImagePlus impMask = NewImage.createByteImage("Matrice", imp1.getWidth(), imp1.getHeight(), 1,
				NewImage.FILL_BLACK);
//		vado a leggere i dati di roi1 e li riporto sulla immagine Mask
		Rectangle rec1 = imp1.getProcessor().getRoi();
		int diamRoi1 = (int) (rec1.width);
		int xRoi1 = rec1.x + ((rec1.width - diamRoi1) / 2);
		int yRoi1 = rec1.y + ((rec1.height - diamRoi1) / 2);
		ImageProcessor ipMask = impMask.getProcessor();
		ipMask.setColor(Color.WHITE);
		Roi roi2 = new OvalRoi(xRoi1, yRoi1, diamRoi1, diamRoi1);
		ipMask.setMask(roi2.getMask());
		ipMask.setRoi(roi2.getBounds());
		ipMask.fill(ipMask.getMask());
		impMask.updateAndDraw();
		byte[] pixels = (byte[]) ipMask.getPixels();
		return (pixels);
	}

	/**
	 * Costruisce un kernel rettangolare a 0 ed inscrive una roi circolare a 255
	 * 
	 * @param dia1
	 * @return
	 */
	public static byte[] byteVetMaskBuilder(ImagePlus imp1, int xcenter, int ycenter, int dia1, int timeout,
			boolean over) {

		int width = imp1.getWidth();
		int height = imp1.getHeight();
		Overlay over1 = new Overlay(); // con questo definisco un overlay indipendente dal valore dei pixels
		// dell'imagine
		over1.setStrokeColor(Color.red);
		ImagePlus impKernel = NewImage.createByteImage("KERNEL", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ipKernel = impKernel.getProcessor();
		impKernel.setOverlay(over1);

		ipKernel.setColor(Color.WHITE);
		ipKernel.setRoi(new OvalRoi(xcenter - dia1 / 2, ycenter - dia1 / 2, dia1, dia1));
		ipKernel.fill(ipKernel.getMask());
		// so che questa parte risulta lenta e non serve a nu caxxo, ma POTA vogliamo
		// stupirvi con gli effetti speciali!
		if (over) {
			for (int y1 = 0; y1 < impKernel.getHeight(); y1++) {
				for (int x1 = 0; x1 < impKernel.getWidth(); x1++) {
					impKernel.setRoi(x1, y1, 1, 1);
					if (ipKernel.getPixel(x1, y1) > 0) {
						impKernel.getRoi().setStrokeColor(Color.RED);
					} else {
						impKernel.getRoi().setStrokeColor(Color.BLUE);
					}
					over1.addElement(impKernel.getRoi());
				}
			}
			impKernel.updateAndDraw();
			impKernel.show();
			ACRutils.zoom(impKernel);

			ACRlog.waitHere("byteVetMaskBuilder byte[] pixel  0 contorno blu,  pixel 255 contorno rosso", debug,
					timeout);
		}
		byte[] pixels = (byte[]) ipKernel.getPixels();
		return (pixels);
	}
	
	/**
	 * Restituisce una immagine 8 bit con a 255 i valori all'interno della Roi
	 * circolare definita da xcenter, ycenter, diam
	 * 
	 * @param imp1    image plus di riferimento
	 * @param xcenter centro roi costituente la mask
	 * @param ycenter centro roi costituente la mask
	 * @param diam    diametro roi
	 * @return byte[] pixels com la mask messa a 255
	 */

	public static ImagePlus kernelBuilder(int dia1, int timeout, boolean over) {

		ImagePlus impKernel = NewImage.createByteImage("KERNEL", dia1, dia1, 1, NewImage.FILL_BLACK);
		ImageProcessor ipKernel = impKernel.getProcessor();
		ipKernel.setColor(Color.WHITE);
		Roi roi2 = new OvalRoi(0, 0, dia1, dia1);
		ipKernel.setMask(roi2.getMask());
//		ipKernel.set(roi2.get);
		ipKernel.fill(ipKernel.getMask());
		Overlay over1 = new Overlay(); // con questo definisco un overlay indipendente dal valore dei pixels
		// dell'imagine
		impKernel.setOverlay(over1);

		if (over) {
			for (int y1 = 0; y1 < impKernel.getHeight(); y1++) {
				for (int x1 = 0; x1 < impKernel.getWidth(); x1++) {
					impKernel.setRoi(x1, y1, 1, 1);
					if (ipKernel.getPixel(x1, y1) > 0) {
						impKernel.getRoi().setStrokeColor(Color.RED);
					} else {
						impKernel.getRoi().setStrokeColor(Color.BLUE);
					}
					over1.addElement(impKernel.getRoi());
				}
			}
			impKernel.updateAndDraw();
			impKernel.show();
			ACRutils.zoom(impKernel);
			ACRlog.waitHere("impKernel byte[]  0 contorno blu,  pixel 255 contorno rosso", debug, timeout);
		}
		return (impKernel);
	}

	
	/**
	 * Estrae i pixel del kernel
	 * 
	 * @param imp1
	 * @return
	 */
	public static byte[] byteVetKernelExtractor(ImagePlus imp1) {
		ImageProcessor ip1 = imp1.getProcessor();
		byte[] pixels1 = (byte[]) ip1.getPixels();
		return (pixels1);
	}

	/**
	 * Estrae il vetSubmatrix dal vetMatrix.
	 * 
	 * @param vetByteMatrix
	 * @param bigwidth
	 * @param bigheight
	 * @param subcenterx
	 * @param subcentery
	 * @param sublato
	 * @param trigger
	 * @return
	 */
	public static byte[] extractSubmatrix(byte[] vetByteMatrix, int bigwidth, int bigheight, int subcenterx,
			int subcentery, int sublato, int timeout, boolean trigger, boolean over) {
		int upperleftx = subcenterx - sublato / 2;
		int upperlefty = subcentery - sublato / 2;
		int lowerrightx = subcenterx + sublato / 2;
		int lowerrighty = subcentery + sublato / 2;
		byte[] vetByteSubmatrix = new byte[sublato * sublato];
		int[] vetIndex1 = new int[sublato * sublato];
		if (upperleftx < 0 || upperlefty < 0) {
			return vetByteSubmatrix;
		}
		if (lowerrightx >= bigwidth || lowerrighty >= bigheight) {
			return vetByteSubmatrix;
		}
		int count1 = 0;
		int count2 = 0;
		int index1 = 0;
		for (int y1 = 0; y1 < sublato; y1++) {
			int offset1 = (upperlefty + y1) * bigwidth + upperleftx;
			for (int x1 = 0; x1 < sublato; x1++) {
				index1 = offset1 + x1;
				vetIndex1[count1++] = index1;
				if (count2 > vetByteSubmatrix.length) {
					return vetByteSubmatrix;
				}
				vetByteSubmatrix[count2++] = vetByteMatrix[index1];
			}
		}
		if (trigger) {
			ACRlog.vetPrint(vetIndex1, "vetIndex1");
		}
		Overlay over1 = new Overlay(); // con questo definisco un overlay indipendente dal valore dei pixels
		// dell'imagine
		if (over) {
			byte[] pixels = new byte[sublato * sublato];
			for (int i1 = 0; i1 < vetByteSubmatrix.length; i1++) {
				pixels[i1] = vetByteSubmatrix[i1];
			}
			ImageProcessor ip3 = new ByteProcessor(sublato, sublato, pixels, null);
			ImagePlus imp3 = new ImagePlus("subimage", ip3);
			imp3.setOverlay(over1);

			for (int y1 = 0; y1 < imp3.getHeight(); y1++) {
				for (int x1 = 0; x1 < imp3.getWidth(); x1++) {
					imp3.setRoi(x1, y1, 1, 1);
					if (ip3.getPixel(x1, y1) > 0) {
						imp3.getRoi().setStrokeColor(Color.RED);
					} else {
						imp3.getRoi().setStrokeColor(Color.BLUE);
					}
					over1.addElement(imp3.getRoi());
				}
			}
			imp3.updateAndDraw();
			imp3.show();
			ACRlog.waitHere("extractSubmatrix byte[] contorno pixel a 0 blu pixel a 255 rosso", debug, timeout);
		}
		return vetByteSubmatrix;
	}

	/**
	 * Estrae il vetSubmatrix dal vetMatrix.
	 * 
	 * @param vetDoubleMatrix
	 * @param bigwidth
	 * @param bigheight
	 * @param subcenterx
	 * @param subcentery
	 * @param sublato
	 * @param trigger
	 * @return
	 */
	public static double[] extractSubmatrix(double[] vetDoubleMatrix, int bigwidth, int bigheight, int subcenterx,
			int subcentery, int sublato, int timeout, boolean trigger, boolean over) {
		int upperleftx = subcenterx - sublato / 2;
		int upperlefty = subcentery - sublato / 2;
		int lowerrightx = subcenterx + sublato / 2;
		int lowerrighty = subcentery + sublato / 2;
		double[] vetDoubleSubmatrix = new double[sublato * sublato];
		int[] vetIndex1 = new int[sublato * sublato];
		if (upperleftx < 0 || upperlefty < 0) {
			return vetDoubleSubmatrix;
		}
		if (lowerrightx >= bigwidth || lowerrighty >= bigheight) {
			return vetDoubleSubmatrix;
		}
		int count1 = 0;
		int count2 = 0;
		int index1 = 0;
		for (int y1 = 0; y1 < sublato; y1++) {
			int offset1 = (upperlefty + y1) * bigwidth + upperleftx;
			for (int x1 = 0; x1 < sublato; x1++) {
				index1 = offset1 + x1;
				vetIndex1[count1++] = index1;
				if (count2 > vetDoubleSubmatrix.length) {
					ACRlog.waitHere(">length");
					return vetDoubleSubmatrix;
				}
				vetDoubleSubmatrix[count2++] = vetDoubleMatrix[index1];
			}
		}
		if (trigger) {
			ACRlog.vetPrint(vetIndex1, "vetIndex1");
		}
		Overlay over1 = new Overlay(); // con questo definisco un overlay indipendente dal valore dei pixels
		// dell'imagine
		if (over) {
			float[] pixels = new float[sublato * sublato];
			for (int i1 = 0; i1 < vetDoubleSubmatrix.length; i1++) {
				pixels[i1] = (float) vetDoubleSubmatrix[i1];
			}
			ImageProcessor ip3 = new FloatProcessor(sublato, sublato, pixels, null);
			ImagePlus imp3 = new ImagePlus("subimage", ip3);
			imp3.setOverlay(over1);

			for (int y1 = 0; y1 < imp3.getHeight(); y1++) {
				for (int x1 = 0; x1 < imp3.getWidth(); x1++) {
					imp3.setRoi(x1, y1, 1, 1);
					imp3.getRoi().setStrokeColor(Color.YELLOW);
					over1.addElement(imp3.getRoi());
				}
			}
			imp3.updateAndDraw();
			imp3.show();
			ACRutils.zoom(imp3);
			ACRlog.waitHere("extractSubmatrix double[] contorno pixels giallo", debug, timeout);
		}
		return vetDoubleSubmatrix;
	}

	/**
	 * Effettua l'operazione AND tra due kernel di dimensioni identiche
	 * 
	 * @param kernel1
	 * @param kernel2
	 * @param trigger
	 * @return
	 */
	public static byte[] kernelsAND(byte[] kernel1, byte[] kernel2, int timeout, boolean trigger, boolean over) {
		if (kernel1 == null || kernel1.length == 0) {
			ACRlog.waitHere("kernel1 problems");
			return null;
		}
		if (kernel2 == null || kernel2.length == 0) {
			ACRlog.waitHere("kernel2 problems");
			return null;
		}
		if (kernel1.length != kernel2.length) {
			ACRlog.waitHere("kernel diversa lunghezza");
			return null;
		}
		byte[] kernelout = new byte[kernel1.length];
		for (int i1 = 0; i1 < kernel1.length; i1++) {
			if (kernel1[i1] == kernel2[i1]) {
				kernelout[i1] = kernel1[i1];
			}
		}

		Overlay over1 = new Overlay(); // con questo definisco un overlay indipendente dal valore dei pixels
		// dell'imagine
		if (over) {
			int sublato = (int) Math.sqrt(kernel1.length);

			ImageProcessor ip3 = new ByteProcessor(sublato, sublato, kernelout, null);
			ImagePlus imp3 = new ImagePlus("subimage", ip3);
			imp3.setOverlay(over1);

			for (int y1 = 0; y1 < imp3.getHeight(); y1++) {
				for (int x1 = 0; x1 < imp3.getWidth(); x1++) {
					imp3.setRoi(x1, y1, 1, 1);
					if (ip3.getPixel(x1, y1) > 0) {
						imp3.getRoi().setStrokeColor(Color.RED);
					} else {
						imp3.getRoi().setStrokeColor(Color.BLUE);
					}
					over1.addElement(imp3.getRoi());
				}
			}
			imp3.updateAndDraw();
			imp3.show();
			ACRutils.zoom(imp3);
			ACRlog.waitHere("kernelsAND byte[] contorno pixel a 0 blu pixel a 255 rosso", debug, timeout);
		}

		return kernelout;
	}

	/**
	 * Effettua il calcolo del pixel medio secondo le indicazioni dell'articolo
	 * 
	 * @param subImage
	 * @param kernel
	 * @param trigger
	 * @return
	 */
	public static double calcPixel(double[] subImage, byte[] kernel, boolean trigger) {
		double sum = 0;
		int count = 0;
		for (int i1 = 0; i1 < subImage.length; i1++) {
			if (kernel[i1] != 0) {
				sum = sum + subImage[i1];
				count++;
			}
		}
		double out1 = 0;
		if (count == 0)
			count = 1;
		out1 = sum / (double) count;
		if (trigger) {
			IJ.log("sum= " + sum + " count= " + count + " out1= " + out1);
			ACRlog.waitHere();
		}
		return out1;
	}
	/**
	 * Crea la ImagePlus contenente i valori calcolati
	 * 
	 * @param imageVetPixels
	 * @param imp1
	 * @return
	 */
	public static ImagePlus doubleVetPixelsInsertorFloat(double[] imageVetPixels, int width, int height) {
		float[] pixels = new float[width * height];
		for (int i1 = 0; i1 < imageVetPixels.length; i1++) {
			pixels[i1] = (float) imageVetPixels[i1];
		}
		ImageProcessor ip3 = new FloatProcessor(width, height, pixels, null);
		ImagePlus imp3 = new ImagePlus("FLOATmediata", ip3);
		return imp3;
	}

	/**
	 * Crea una immagine, a partire da un vettore.
	 * 
	 * @param imageVetPixels
	 * @param imp1
	 * @return
	 */
	public static ImagePlus doubleVetPixelsInsertor(double[] imageVetPixels, int width, int height) {
		short[] pixels = new short[width * height];
		for (int i1 = 0; i1 < imageVetPixels.length; i1++) {
			pixels[i1] = (short) imageVetPixels[i1];
		}
		ImageProcessor ip3 = new ShortProcessor(width, height, pixels, null);
		ImagePlus imp3 = new ImagePlus("SHORTmediata", ip3);
		return imp3;

	}
	/**
	 * Estrae i valori dei pixel di una qualsivoglia ROI da un ImageProcessor. Si
	 * chiama locale per distinguerlo da uno nella libreria iw2ayv.jar
	 * 
	 * @param ip1 ImageProcessor in input
	 * @return
	 */
	public static float[] extractRoiPixelsFloatLocale(ImageProcessor ip1) {

		ArrayList<Float> arr1 = new ArrayList<Float>();
		Rectangle r1 = ip1.getRoi();
		ImageProcessor mask1 = ip1.getMask();
		for (int y1 = r1.y; y1 < (r1.y + r1.height); y1++) {
			for (int x1 = r1.x; x1 < (r1.x + r1.width); x1++) {
				if (mask1 == null || mask1.getPixel(x1 - r1.x, y1 - r1.y) > 0) {
					arr1.add((float) ip1.getPixelValue(x1, y1));
				}
			}
		}
		float[] out1 = ACRcalc.arrayListToArrayFloat(arr1);
		return out1;
	}

	/**
	 * minmaxAlberto effettua la media sull'intera immagine, utilizzando una ROI
	 * circolare e mettendo il valore della media nelle coordinate del pixel
	 * centrale della ROI nell'immagine di output I RISULTATI NON SONO SODDISFACENTI
	 * LASCIA UN BORDO SBIADITO
	 * 
	 * @param imp1          immagine di input
	 * @param phantomCircle cerchio esterno fantoccio
	 * @param MROIcircle    cerchio MROI
	 * @param pixint100     dimensioni roi di calcolo (dispari) >100 mmq
	 * @param timeout
	 * @param verbose
	 * @param fast
	 * @return
	 */
	public static double[] minmaxAlberto(ImagePlus imp1, int[] phantomCircle, int[] MROIcircle, int pixint100,
			boolean step, boolean fast, boolean verbose, int timeout) {
		// ============================================================================
		// QUESTA SOLUZIONE NON E'SODDISFACENTE, LASCIA UN BORDO SBIADITO DOVE VIENE
		// RILEVATO UN MINIMO FASULLO
		// ============================================================================

		int xmroi = MROIcircle[0];
		int ymroi = MROIcircle[1];
		int dmroi = MROIcircle[2];

		int xphantom = phantomCircle[0];
		int yphantom = phantomCircle[1];
		int dphantom = phantomCircle[2];

		int x1 = xphantom;
		int y1 = yphantom;
		int d1 = dphantom;

		Overlay over2 = new Overlay();
		imp1.killRoi();
		ImagePlus imp2 = imp1.duplicate();
		ImageProcessor ip2 = imp2.getProcessor();
		imp2.setOverlay(over2);
		if (verbose) {
			imp2.show();
			ACRutils.zoom(imp2);

			// ACRutils.zoom(imp2);
		}
		ImageProcessor ipOut = new FloatProcessor(imp1.getWidth(), imp1.getHeight());
		ImagePlus impOut = new ImagePlus("ALBERTO minmax", ipOut);
		Overlay overOut = new Overlay();
		impOut.setOverlay(overOut);
		// ora faccio la scansione dell'intera immagine, mettendo la media nel pixel
		// centrale della ROI circolare.
		int width = imp1.getWidth();
		int height = imp1.getHeight();
		int xstart = pixint100 / 2;
		int xend = width - pixint100 / 2;
		int ystart = pixint100 / 2;
		int yend = height - pixint100 / 2;
		float amax = Float.MIN_VALUE;
		float amin = Float.MAX_VALUE;
		int xmax = 0;
		int ymax = 0;
		int xmin = 0;
		int ymin = 0;
		// devo fare tre elaborazioni separate: prima quella della ROLLING BALL e
		// successivamente le ricerchje di MAX e MIN, facendola pero' solo all'interno
		// della MROI. POTA troe mia en alternativa!

		// Riempimento di ipOut con la media di un area circolare di diametro
		// diamSearch. La elaborazione viene fatta per la intera immagine.
		// Anzich?? ripetere il calcolo in seguito, posso impiegare il contenuto dei
		// singoli pixdell'immagine impOut per le successive elaborazioni di
		// unifpormita'

		int d2 = d1 + pixint100 / 2;
		imp2.setRoi(new OvalRoi(x1 - d2 / 2, y1 - d2 / 2, d2, d2));
		imp2.getRoi().setStrokeColor(Color.BLUE);
		over2.addElement(imp2.getRoi());
		if (verbose)
			ACRlog.waitHere("Area di scansione in BLU", debug, timeout);
		impOut.setRoi(new OvalRoi(x1 - d2 / 2, y1 - d2 / 2, d2, d2));
		Roi roiCircle = impOut.getRoi();
		int a = 0;
		int b = 0;
		float sum2 = 0;
		int count2 = 0;
		for (int x2 = xstart; x2 < xend; x2++) {
			for (int y2 = ystart; y2 < yend; y2++) {
				if (roiCircle.contains(x2, y2)) {
					imp2.killRoi();
					imp2.setRoi(new OvalRoi(x2 - pixint100 / 2, y2 - pixint100 / 2, pixint100, pixint100));
					Roi roi2 = imp2.getRoi();
					Point[] points2 = roi2.getContainedPoints();
					float[] pixelsroi2 = extractRoiPixelsFloatLocale(ip2);
					count2 = 0;
					sum2 = 0;
					for (int i1 = 0; i1 < pixelsroi2.length; i1++) {
						sum2 = sum2 + pixelsroi2[i1];
					}
					for (int i1 = 0; i1 < points2.length; i1++) {
						Point p2 = points2[i1];
						a = p2.x;
						b = p2.y;
						if (roiCircle.contains(a, b)) {
							count2++;
						}
					}
					float med1 = sum2 / count2;
					ipOut.putPixelValue(x2, y2, (float) med1);
					imp2.setRoi(x2, y2, 1, 1);
					imp2.getRoi().setFillColor(Color.YELLOW);
					over2.addElement(imp2.getRoi());
				}
			}
		}

		impOut.updateAndDraw();
		impOut.show();
		if (verbose) {
			ACRutils.zoom(impOut);
			ACRlog.waitHere("visualizzata impOut come MEAN", debug, timeout);
		}
		d2 = d1;
		if (verbose) {
			impOut.setRoi(new OvalRoi(x1 - d2 / 2, y1 - d2 / 2, d2, d2));
			impOut.getRoi().setStrokeColor(Color.GREEN);
			overOut.addElement(impOut.getRoi());
			impOut.killRoi();
			ACRlog.waitHere("applico MROI su immagine MEAN ed all'interno di questa cerco MINIMI e MAXIMI", debug,
					timeout);
		}
		impOut.setRoi(new OvalRoi(x1 - d2 / 2, y1 - d2 / 2, d2, d2));
		Roi roiOut1 = impOut.getRoi();
		impOut.killRoi();
		for (int x2 = xstart; x2 < xend; x2++) {
			for (int y2 = ystart; y2 < yend; y2++) {
				if (roiOut1.contains(x2, y2)) {
					float val1 = ipOut.getPixelValue(x2, y2);
					if (Float.compare(val1, amax) > 0) {
						amax = val1;
					}
					if (Float.compare(val1, amin) < 0) {
						amin = val1;
					}
				}
			}
		}
		ArrayList<Integer> vetXmaxpos = new ArrayList<Integer>();
		ArrayList<Integer> vetYmaxpos = new ArrayList<Integer>();
		ArrayList<Integer> vetXminpos = new ArrayList<Integer>();
		ArrayList<Integer> vetYminpos = new ArrayList<Integer>();
		for (int x2 = xstart; x2 < xend; x2++) {
			for (int y2 = ystart; y2 < yend; y2++) {
				if (roiOut1.contains(x2, y2)) {
					float val1 = ipOut.getPixelValue(x2, y2);
					if (Float.compare(val1, amax) == 0) {
						vetXmaxpos.add(x2);
						vetYmaxpos.add(y2);
					}
					if (Float.compare(val1, amin) == 0) {
						vetXminpos.add(x2);
						vetYminpos.add(y2);
					}
				}
			}
		}
		int[] vetxmax = ACRcalc.arrayListToArrayInt(vetXmaxpos);
		int[] vetymax = ACRcalc.arrayListToArrayInt(vetYmaxpos);
		int[] vetxmin = ACRcalc.arrayListToArrayInt(vetXminpos);
		int[] vetymin = ACRcalc.arrayListToArrayInt(vetYminpos);
		if (verbose && (fast == false)) {
			ACRlog.vetPrint(vetxmax, "vetxmax");
			ACRlog.vetPrint(vetymax, "vetymax");
			ACRlog.vetPrint(vetxmin, "vetxmin");
			ACRlog.vetPrint(vetymin, "vetymin");
		}
		impOut.updateAndDraw();
		imp2.killRoi();
		for (int i1 = 0; i1 < vetxmax.length; i1++) {
			impOut.setRoi(vetxmax[i1] - 1, vetymax[i1] - 1, 3, 3);
			impOut.getRoi().setStrokeColor(Color.RED);
			overOut.addElement(impOut.getRoi());
		}
		for (int i1 = 0; i1 < vetxmin.length; i1++) {
			impOut.setRoi(vetxmin[i1] - 1, vetymin[i1] - 1, 3, 3);
			impOut.getRoi().setStrokeColor(Color.BLUE);
			overOut.addElement(impOut.getRoi());
		}
		impOut.killRoi();
		impOut.updateAndDraw();
		if (verbose) {
			ACRlog.waitHere(
					"TROVATO max1= " + amax + " in " + xmax + "," + ymax + " min1= " + amin + " in" + xmin + "," + ymin,
					debug, timeout);
			ACRlog.waitHere("TERMINE MAX MIN VERBOSE ALBERTO", debug, timeout);
		}

		int xposmin = vetxmin[0];
		int yposmin = vetymin[0];
		int xposmax = vetxmax[0];
		int yposmax = vetymax[0];

		double[] vetOut = new double[6];
		vetOut[0] = (double) xposmin;
		vetOut[1] = (double) yposmin;
		vetOut[2] = (double) amin;
		vetOut[3] = (double) xposmax;
		vetOut[4] = (double) yposmax;
		vetOut[5] = (double) amax;

		return vetOut;
	}
	
	/**
	 * Display di un profilo.
	 * 
	 * @param profile1
	 * @param sTitolo
	 */

	public static Plot displayPlot1(double[] profile1, String sTitolo) {

		Plot plot1 = new Plot(sTitolo, "pixel", "valore");
		plot1.add("LINE", profile1);
		plot1.setColor(Color.red);
		return plot1;

		// notare che anche se restituisce plot, per chiuderlo occorre:
		// if (WindowManager.getFrame("Profile") != null) {
		// IJ.selectWindow("Profile");
		// IJ.run("Close");
		// }

	}
	
	
	/**
	 * Estrae il vetSubmatrix dal vetMatrix. Le prime prove le faccio con vettori
	 * short, in modo da poter assegnare ad ogni pixel un valore crescente, in modo
	 * da poter fare comodamente e sicuramente tutti i test che necessitano
	 * 
	 * @param vetShortMatrix vettore che contiene i pixel dela matrice immagine
	 * @param bigwidth       width immagine grande
	 * @param bigheight      height immagine grande
	 * @param subcenterx     coordinata x del centro della submatrice
	 * @param subcentery     coordinata y del centro della submatrice
	 * @param sublato        lato della submatrice
	 * @return vetShortSubmatrix risultato
	 */
	public static int[] extractSubmatrix(int[] vetIntMatrix, int bigwidth, int bigheight, int subcenterx,
			int subcentery, int sublato, int timeout, boolean trigger, boolean over, boolean fast) {
		// coordinate upper left pixel, teniamo conto che lavoriamo su interi e quindi
		// per i dispari ci affidiamo alla java-provvidenza!
		int upperleftx = subcenterx - (int) Math.ceil((double) sublato / 2.);
		int upperlefty = subcentery - sublato / 2;
		if (upperleftx < 0 || upperlefty < 0)
			return null;
		int[] vetIntSubmatrix = new int[sublato * sublato];
		for (int y1 = 0; y1 < sublato; y1++) {
			int offset1 = (upperlefty + y1) * bigwidth + upperleftx;
			int offset2 = y1 * sublato;
			for (int x1 = 0; x1 < sublato; x1++) {
				vetIntSubmatrix[offset2 + x1] = vetIntMatrix[offset1 + x1];
			}
		}
		Overlay over1 = new Overlay(); // con questo definisco un overlay indipendente dal valore dei pixels
		// dell'imagine
		if (over) {
			float[] pixels = new float[sublato * sublato];
			for (int i1 = 0; i1 < vetIntSubmatrix.length; i1++) {
				pixels[i1] = (float) vetIntSubmatrix[i1];
			}
			ImageProcessor ip3 = new FloatProcessor(sublato, sublato, pixels, null);
			ImagePlus imp3 = new ImagePlus("subimage", ip3);
			imp3.setOverlay(over1);

			for (int y1 = 0; y1 < imp3.getHeight(); y1++) {
				for (int x1 = 0; x1 < imp3.getWidth(); x1++) {
					imp3.setRoi(x1, y1, 1, 1);
					imp3.getRoi().setStrokeColor(Color.YELLOW);
					over1.addElement(imp3.getRoi());
				}
			}
			imp3.updateAndDraw();
			imp3.show();
			ACRlog.waitHere("extractSubmatrix int[] contorno pixel giallo");
		}

		return vetIntSubmatrix;
	}

	
	/**
	 * Test riempie la mask double[] con pixel valore crescente
	 * 
	 * @param vetDoubleMatrix vetdouble matrix
	 * @param bigwidth        lato
	 * @param bigheight       lato
	 */
	public static void fillDoubleMatrix(double[] vetDoubleMatrix, int bigwidth, int bigheight) {
		double val = 0;
		for (int y1 = 0; y1 < bigheight; y1++) {
			int offset = y1 * bigwidth;
			for (int x1 = 0; x1 < bigwidth; x1++) {
				vetDoubleMatrix[offset + x1] = val++;
			}
		}
		return;
	}
	/**
	 * Test riempie la mask int[] con pixel valore crescente
	 * 
	 * @param vetIntMatrix vetint matrice
	 * @param bigwidth     lato
	 * @param bigheight    lato
	 */
	public static void fillIntMatrix(int[] vetIntMatrix, int bigwidth, int bigheight) {
		int val = 0;
		for (int y1 = 0; y1 < bigheight; y1++) {
			int offset = y1 * bigwidth;
			for (int x1 = 0; x1 < bigwidth; x1++) {
				vetIntMatrix[offset + x1] = ++val;
			}
		}
		return;
	}

	
	/**
	 * Questo sarebbe il kernel circolare
	 * 
	 * @param dia1 diametro della roi a 255 da creare
	 * @return
	 */
	public static ImagePlus kernelTarocco(int dia1) {

		ImagePlus impKernel = NewImage.createByteImage("KERNEL", dia1, dia1, 1, NewImage.FILL_WHITE);
		return (impKernel);

	}

	public static ImagePlus newMaskBuilder(int xcenter, int ycenter, int dia1, boolean over) {

//		ImagePlus impKernel = NewImage.createByteImage("KERNEL", 192, 192, 1, NewImage.FILL_BLACK);
		ImageProcessor ipKernel = new ByteProcessor(192, 192);
		OvalRoi roi2 = new OvalRoi(90, 90, 40, 40);
		ipKernel.setMask(roi2.getMask());
		ipKernel.setValue(255);
		ImageProcessor m1 = ipKernel.getMask();
		IJ.log("Mask= " + m1);
		ipKernel.fill(m1);
		Overlay over1 = new Overlay(); // con questo definisco un overlay indipendente dal valore dei pixels
		// dell'imagine
		over1.setStrokeColor(Color.red);

		ImagePlus impKernel = new ImagePlus("kernel", ipKernel);
		impKernel.killRoi();
		impKernel.setOverlay(over1);
		if (over) {
			for (int y1 = 0; y1 < impKernel.getHeight(); y1++) {
				for (int x1 = 0; x1 < impKernel.getWidth(); x1++) {
					impKernel.setRoi(x1, y1, 1, 1);
					if (ipKernel.getPixel(x1, y1) > 0) {
						impKernel.getRoi().setStrokeColor(Color.RED);
					} else {
						impKernel.getRoi().setStrokeColor(Color.BLUE);
					}
					over1.addElement(impKernel.getRoi());
				}
			}
		}
		impKernel.updateAndDraw();
		impKernel.show();
		ACRutils.zoom(impKernel);
		ACRlog.waitHere("impKernel pixel  0 contorno blu,  pixel 255 contorno rosso");
		return (impKernel);
	}

	/**
	 * Estrae i valori dei pixel di un immagine 16 bit e restituisce una matrice
	 * double
	 * 
	 * @param imp1
	 * @return
	 */
	public int[][] ImageToMatrix16(ImagePlus imp1) {

		int width = imp1.getWidth();
		int height = imp1.getHeight();
		Calibration cal1 = imp1.getCalibration();
		int pixValue = 0;
		int[][] ret = new int[width][height];
		ImageProcessor ip1 = imp1.getProcessor();
		short[] sdata = (short[]) ip1.getPixels();
		for (int y1 = 0; y1 < height; y1++) {
			for (int x1 = 0; x1 < width; x1++) {
				// pixValue serve per leggere correttamente i valori delle
				// immagini signed (calibrate, GE)
				pixValue = (int) cal1.getRawValue(sdata[x1 + y1 * width]);
				ret[x1][y1] = pixValue;
			}
		}
		return (ret);
	}


}
