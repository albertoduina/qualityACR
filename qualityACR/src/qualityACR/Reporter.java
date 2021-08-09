package qualityACR;

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

		mainReporter();
	}

	public static void mainReporter() {
		ACRlog.waitHere();

	}
}
