package qualityACR;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class ReporterTest {
	
	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}

	@Test
	public final void testReporter() {

		Reporter reporter = new Reporter();
//		reporter.mainReporter();
		// assertTrue(UtilAyv.compareVectors(expected, result, ""));
	}

	@Test
	public final void testReporterEngine() {

		String htmlfile2[] = {"ReportSlice1_T3.html"};
		String resultfile2[] = {"ReportThick.txt"};		
		
		Reporter reporter = new Reporter();
		reporter.reporterEngine(htmlfile2, resultfile2, true);
		// assertTrue(UtilAyv.compareVectors(expected, result, ""));
	}

	
	
}
