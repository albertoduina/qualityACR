package qualityACR;

/***
 * Questa classe definisce ed imposta le costanti utilizzate da ContMensili
 * 
 * @author alberto
 *
 */
public class ACRconst {
	// ------------- commons ------------------------
	public static final String TEST_DIRECTORY = "/test2/";
	public static final String TEST_FILE = "test2.jar";
	public static final String SEQUENZE_FILE = "iw2ayv.txt";
	public static final String XLS_FILE = "Result1.xls";
	public static final String TXT_FILE = "Result1.txt";
	public static final String CODE_FILE = "codiciNew.csv";
	public static final String LIMITS_FILE = "limiti.csv";
	public static final String EXPAND_FILE = "expand.csv";
	public static final String DEFAULT_PATH = "C:/Dati/";
//	public static final String[] CODE_GROUP = { "codiciNew.csv",
//			"codiciEsine.csv", "codiciManerbio.csv", "codiciDesenzano.csv",
//			"codiciRovato.csv", "codiciGavardo.csv" };
	public static final String[] CODE_GROUP = {"codici_.csv"};
	public static final int HMEAN = 2;
	public static final int HSTD_DEV = 4;
	public static final int TOKENS15 = 15;
	public static final int TOKENS4 = 4;
	public static final int TOKENS2 = 2;
	public static final int TOKENS1 = 1;
	public static String DICOM_ACQUISITION_DATE = "0008,0022";
	public static String DICOM_IMAGE_DATE = "0008,0023";
	// public static final String DICOM_ACQTIME = "0008,0032";
	public static final String DICOM_ACQUISITION_TIME = "0008,0032";
	public static final String DICOM_IMATIME = "0008,0033";
	public static final String DICOM_MANUFACTURER = "0008,0070";
	public static final String DICOM_SERIES_DESCRIPTION = "0008,103E";
	public static String DICOM_STATION_NAME = "0008,1010";
	public static final String DICOM_PATIENT_NAME = "0010,0010";
	public static final String DICOM_SLICE_THICKNESS = "0018,0050";
	public static final String DICOM_REPETITION_TIME = "0018,0080";
	public static final String DICOM_ECHO_TIME = "0018,0081";
	public static final String DICOM_INVERSION_TIME = "0018,0082";
	public static final String DICOM_IMAGING_FREQUENCY = "0018,0084";
	public static final String DICOM_SPACING_BETWEEN_SLICES = "0018,0088";
	public static final String DICOM_PHASE_ENCODING_STEPS = "0018,0089";
	public static final String DICOM_IMAGES_IN_MOSAIC = "0019,100A";
	public static final String DICOM_SERIES_NUMBER = "0020,0011";
	public static final String DICOM_ACQUISITION_NUMBER = "0020,0012";
	public static final String DICOM_IMAGE_NUMBER = "0020,0013";
	public static final String DICOM_IMAGE_POSITION = "0020,0032";
	public static final String DICOM_IMAGE_ORIENTATION = "0020,0037";
	public static final String DICOM_SLICE_LOCATION = "0020,1041";
	public static final String DICOM_ROWS = "0028,0010";
	public static final String DICOM_COLUMNS = "0028,0011";
	public static final String DICOM_PIXEL_SPACING = "0028,0030";
	public static final String DICOM_PIXEL_REPRESENTATION = "0028,0103";
	public static final String DICOM_COIL = "0051,100F";
	public static final double PLUS_20_PERC = 1.2;
	public static final double PLUS_10_PERC = 1.1;
	public static final double MINUS_10_PERC = 0.9;
	public static final double MINUS_20_PERC = 0.8;
	public static final double MINUS_30_PERC = 0.7;
	public static final double MINUS_40_PERC = 0.6;
	public static final double MINUS_50_PERC = 0.5;
	public static final double MINUS_60_PERC = 0.4;
	public static final double MINUS_70_PERC = 0.3;
	public static final double MINUS_80_PERC = 0.2;
	public static final double MINUS_90_PERC = 0.1;
	public static final int LEVEL_12 = 2500;
	public static final int LEVEL_11 = 2000;
	public static final int LEVEL_10 = 1800;
	public static final int LEVEL_9 = 1600;
	public static final int LEVEL_8 = 1400;
	public static final int LEVEL_7 = 1200;
	public static final int LEVEL_6 = 1000;
	public static final int LEVEL_5 = 800;
	public static final int LEVEL_4 = 600;
	public static final int LEVEL_3 = 400;
	public static final int LEVEL_2 = 200;
	public static final int LEVEL_1 = 5;
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final int TEMPO_VISUALIZZ = 20;
	// ------------ Sequenze ------------------
	public static final String PREFERENCES_1 = "prefer.string1";
	// ------------ p20rmn -----------------------
	// ULC = UpperLeftCorner
	public static final String P20_X_ROI_TESTSIEMENS = "97;148;42;94;144;196;39;91;142;193;88;139;235";
	public static final String P20_Y_ROI_TESTSIEMENS = "39;41;87;91;92;96;139;143;145;147;194;197;235";
	public static final String P20_X_ROI_TESTGE = "93;143;39;90;141;193;38;88;141;191;88;139;235";
	public static final String P20_Y_ROI_TESTGE = "24;25;73;74;76;77;124;125;127;128;176;178;235";
	public static final int P20_DIAM_ROI = 20;
	public static final int P20_N_GELS = 12;
	public static final String P20_DEFAULT = "0.2; 0.2; 0.2; 0.2; 0.2; 0.2; 0.2; 0.2; 0.2; 0.2; 0.2; 0.2; 0.2";
	public static final int P20_MAX_IMAGES = 24;
	public static final double P20_KMEDIA_FILTRO_FONDO = 3.0;
	public static final double P20_KDEVST_FILTRO_FONDO = 3.0;
	public static final int P20_ARRAY_DIMENSIONS = 4;
	public static final double P20_DIM_PIXEL_FOV_220 = 0.859375;
	public static String[] P20_vetName = { "mediaGel1", "devStandGel1",
			"mediaGel2", "devStandGel2", "mediaGel3", "devStandGel3",
			"mediaGel4", "devStandGel4", "mediaGel5", "devStandGel5",
			"mediaGel6", "devStandGel6", "mediaGel7", "devStandGel7",
			"mediaGel8", "devStandGel8", "mediaGel9", "devStandGel9",
			"mediaGel10", "devStandGel10", "mediaGel11", "devStandGel11",
			"mediaGel2", "devStandGel2" };
	// ------------ p3rmn -----------------------
	public static final int P3_DIAM_PHANTOM = 174;
	public static final int P3_X_ROI_TESTSIEMENS = 40;
	public static final int P3_Y_ROI_TESTSIEMENS = 26;
	public static final int P3_X_ROI_TESTGE = 39;
	public static final int P3_Y_ROI_TESTGE = 35;
	public static final double P3_AREA_PERC_80_DIAM = 0.89;
	public static final int P3_DIAM_FOR_450_PIXELS = 24; // area > 450 pixels
	public static final int P3_DIAM_ROI_GHOSTS = 20;
	public static final int P3_DIAM_ROI_BACKGROUND = 20;
	public static final int P3_ROI_LIMIT = 10;
	public static String[] P3_vetName = { "SEGNALE", "RUMORE", "SNR", "GHOST1",
			"GHOST2", "GHOST3", "GHOST4", "UNIF.INT.%", "BKG", "NUM_CLASS1",
			"NUM_CLASS2", "NUM_CLASS3", "NUM_CLASS4", "NUM_CLASS5" };
	// ------------ p13rmn -----------------------	
	public static final int P13_LATO_ROI_BACKGROUND = 20;
	public static final double P13_LATO_ROI = 100;
	// ------------ p12rmn -----------------------
	public static final int P12_DIAM_PHANTOM = 174;
	public static final int P12_X_ROI_TESTSIEMENS = 40;
	public static final int P12_Y_ROI_TESTSIEMENS = 26;
	public static final int P12_X_ROI_TESTGE = 39;
	public static final int P12_Y_ROI_TESTGE = 35;
	public static final double P12_AREA_PERC_80_DIAM = 0.89;
	public static final int P12_DIAM_FOR_450_PIXELS = 24; // area > 450 pixels
	public static final int P12_DIAM_ROI_GHOSTS = 20;
	public static final int P12_DIAM_ROI_BACKGROUND = 20;
	public static final int P12_ROI_LIMIT = 10;
	public static String[] P12_vetName = { "SEGNALE", "RUMORE", "SNR",
			"GHOST1", "GHOST2", "GHOST3", "GHOST4", "UNIF.INT.%", "BKG", "POS",
			"NUM_CLASS1", "NUM_CLASS2", "NUM_CLASS3", "NUM_CLASS4",
			"NUM_CLASS5" };
	// ------------ p4rmn -----------------------
	public static final double P4_X_START_REFLINE_TESTSIEMENS = 51.0;
	public static final double P4_Y_START_REFLINE_TESTSIEMENS = 112.0;
	public static final double P4_X_END_REFLINE_TESTSIEMENS = 141.0;
	public static final double P4_Y_END_REFLINE_TESTSIEMENS = 207.0;
	public static final double P4_X_START_REFLINE_TESTGE = 46.0;
	public static final double P4_Y_START_REFLINE_TESTGE = 114.0;
	public static final double P4_X_END_REFLINE_TESTGE = 153.0;
	public static final double P4_Y_END_REFLINE_TESTGE = 189.0;
	public static final double[] P4_X_ROI_POSITION = { 7, -18, 29, -10, 113,
			70, 70 };
	public static final double[] P4_Y_ROI_POSITION = { 57, 87, 62, 34, 64, 25,
			63 };
	public static final double[] P4_DIA_ROI = { 15, 12, 8, 5, 3, 10, 10 };
	public static String[] P4_vetName = { "MTF2.0", "MTF1.5", "MTF1.0",
			"MTF0.5", "MTF0.3" };
	// ------------ p5rmn -----------------------
	public static final int P5_GRID_NUMBER = 20;
	public static final int P5_X_ROI_TESTSIEMENS = 126;
	public static final int P5_Y_ROI_TESTSIEMENS = 222;
	public static final int P5_X_ROI_TESTGE = 176;
	public static final int P5_Y_ROI_TESTGE = 129;
	public static final int P5_NEA_11X11_PIXEL = 11; // Noise Evaluation Area
	public static final int P5_MROI_7X7_PIXEL = 7;
	public static final int P5_CHECK_PIXEL_MULTIPLICATOR = 5;
	public static final int P5_DIAM_ROI_BACKGROUND = 10;
	public static final int P5_X_ROI_BACKGROUND = 230;
	public static final int P5_Y_ROI_BACKGROUND = 10;
	public static String[] P5_vetName = { "simulata", "SIGNAL", "BACKNOISE",
			"SNRATIO", "FWHM", "BKG", "NUM_CLASS1", "NUM_CLASS2", "NUM_CLASS3",
			"NUM_CLASS4", "NUM_CLASS5", "NUM_CLASS6", "NUM_CLASS7",
			"NUM_CLASS8", "NUM_CLASS9", "NUM_CLASS10", "NUM_CLASS11",
			"NUM_CLASS12" };
	// ------------ p10rmn -----------------------
	public static final int P10_CIRCLE_NUMBER = 5;
	public static final int P10_DIAM_PHANTOM = 220;
	public static final int P10_AREA = 30;
	public static final int P10_X_ROI_TESTSIEMENS = 126;
	public static final int P10_Y_ROI_TESTSIEMENS = 222;
	public static final int P10_X_ROI_TESTGE = 176;
	public static final int P10_Y_ROI_TESTGE = 129;
	public static final int P10_NEA_11X11_PIXEL = 11; // Noise Evaluation Area
	public static final int P10_MROI_7X7_PIXEL = 7;
	public static final int P10_CHECK_PIXEL_MULTIPLICATOR = 5;
	public static final int P10_DIAM_ROI_BACKGROUND = 10;
	public static final int P10_X_ROI_BACKGROUND = 20;
	public static final int P10_Y_ROI_BACKGROUND = 10;
	public static final int P10_ROI_LIMIT = 10;
	public static String[] P10_vetName = { "simul", "SIGNAL", "BACKNOISE",
			"SNRATIO", "FWHM", "BKG", "POS", "NUM_CLASS1", "NUM_CLASS2",
			"NUM_CLASS3", "NUM_CLASS4", "NUM_CLASS5", "NUM_CLASS6",
			"NUM_CLASS7", "NUM_CLASS8", "NUM_CLASS9", "NUM_CLASS10",
			"NUM_CLASS11", "NUM_CLASS12" };
	// ------------ p11rmn -----------------------
	public static final int P11_GRID_NUMBER = 20;
	public static final int P11_X_ROI_TESTSIEMENS = 126;
	public static final int P11_Y_ROI_TESTSIEMENS = 222;
	public static final int P11_X_ROI_TESTGE = 176;
	public static final int P11_Y_ROI_TESTGE = 129;
	public static final int P11_NEA_11X11_PIXEL = 11; // Noise Evaluation Area
	public static final int P11_MROI_7X7_PIXEL = 7;
	public static final int P11_CHECK_PIXEL_MULTIPLICATOR = 5;
	public static final int P11_DIAM_ROI_BACKGROUND = 10;
	public static final int P11_X_ROI_BACKGROUND = 20;
	public static final int P11_Y_ROI_BACKGROUND = 10;
	public static String[] P11_vetName = { "simulata", "SIGNAL", "BACKNOISE",
			"SNRATIO", "FWHM", "BKG", "NUM_CLASS1", "NUM_CLASS2", "NUM_CLASS3",
			"NUM_CLASS4", "NUM_CLASS5", "NUM_CLASS6", "NUM_CLASS7",
			"NUM_CLASS8", "NUM_CLASS9", "NUM_CLASS10", "NUM_CLASS11",
			"NUM_CLASS12" };
	// ------------ p6rmn -----------------------
	public static final double[] P6_REFERENCE_LINE_SIEMENS = { 51, 36, 52, 192 };
	public static final double[] P6_REFERENCE_LINE_GE = { 40, 38, 38, 191 };
	public static String[] P6_vetName = { "slicePos2", "fwhmSlab1",
			"peak_slab1", "fwhm_slab2", "peak_slab2", "fwhm_cuneo3",
			"peak_cuneo3", " fwhm_cuneo4", " peak_cuneo4", "S1CorSlab",
			" S2CorSlab", "ErrSperSlab", " AccurSpesSlab", " S1CorCuneo",
			"S2CorCuneo", " ErrSperCuneo", " AccurSpesCuneo", " Accettab",
			" DimPix", "Thick", "Spacing" };
	// ------------ p7rmn -----------------------
	public static final int[] P7_X_POINTS_TESTSIEMENS = { 255, 250, 122, 127,
			251, 252, 383, 380, 187, 188, 316, 317, 88, 88, 169, 169, 334, 334,
			417, 416, 186, 187, 313, 317, 123, 122, 250, 251, 381, 381, 250,
			251, 257, 288, 254, 223 };
	public static final int[] P7_Y_POINTS_TESTSIEMENS = { 86, 105, 139, 155,
			176, 196, 138, 157, 202, 222, 202, 221, 267, 285, 267, 287, 266,
			286, 266, 286, 332, 351, 331, 351, 396, 415, 357, 377, 397, 414,
			448, 467, 247, 281, 312, 278 };
	public static final int[] P7_X_POINTS_TESTGE = { 247, 259, 131, 115, 241,
			254, 374, 383, 177, 193, 306, 319, 77, 88, 156, 169, 321, 332, 401,
			415, 173, 185, 300, 314, 107, 120, 237, 248, 375, 362, 234, 244,
			258, 281, 242, 217 };
	public static final int[] P7_Y_POINTS_TESTGE = { 52, 71, 100, 116, 141,
			160, 110, 127, 164, 184, 169, 187, 224, 242, 227, 246, 232, 252,
			236, 255, 292, 310, 296, 315, 352, 371, 319, 339, 364, 380, 408,
			427, 213, 251, 275, 232 };
	public static final int P7_NUM_RODS = 32;
	public static final int P7_TOTAL_NUM_POINTS = 36;
	public static final double P7_DIAM_ROI = 187.5;
	// ---------------- p8rmn -----------------------------------
	public static final int[] P8_X_POINT_TESTSIEMENS = { 52, 208, 208, 52 };
	public static final int[] P8_Y_POINT_TESTSIEMENS = { 61, 61, 215, 215 };
	public static final int[] P8_X_POINT_TESTGE = { 40, 194, 191, 37 };
	public static final int[] P8_Y_POINT_TESTGE = { 38, 42, 193, 190 };
	public static final int P8_NUM_POINTS4 = 4;
	public static final int P8_TOKENS4 = 4;
	public static final double P8_PHANTOM_SIDE = 120.0;
	public static final double P8_PHANTOM_DIAGONAL = 169.7;
	// ---------------- p19rmn -----------------------------------
	public static final int P19_GRID_NUMBER = 40;
	public static final int P19_NEA_11X11_PIXEL = 50; // Noise Evaluation Area
	public static final int P19_MROI_7X7_PIXEL = 40;
	public static final int P19_CHECK_PIXEL_MULTIPLICATOR = 5;
	public static final int P19_DIAM_ROI_BACKGROUND = 50;
	public static final int P19_X_ROI_BACKGROUND = 650;
	public static final int P19_Y_ROI_BACKGROUND = 650;
	public static final int P19_LATO_ROI_BACKGROUND = 50;
	public static final double P19_LATO_LUNGO_ROI = 300;
	public static final double P19_LATO_CORTO_ROI = 150;
	// ---------------------------------------------------

}
