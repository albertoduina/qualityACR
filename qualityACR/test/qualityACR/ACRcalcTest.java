package qualityACR;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.IJ;
import ij.ImageJ;

public class ACRcalcTest {

	@Before
	public void setUp() throws Exception {
		new ImageJ(ImageJ.NORMAL);
	}

	@After
	public void tearDown() throws Exception {
		// new WaitForUserDialog("Do something, then click OK.").show();

	}

	@Test
	public final void testMinsort() {

		int[][] matin = { { 11, 44, 22, 77, 88, 33, 0, 9, 11, 8 }, { 3, 99, 134, 11, 22, 33, 77, 88, 3, 0 },
				{ 13, 9, 13, 10, 2, 3, 7, 8, 3, 11 } };

		ACRlog.logMatrix(matin, ACRlog.qui() + "matin");
		int key = 2;
		int[][] matout = ACRcalc.minsort(matin, key);

		int[][] matexpected = { { 88, 33, 11, 0, 9, 44, 77, 8, 22, 11 }, { 22, 33, 3, 77, 88, 99, 11, 0, 134, 3 },
				{ 2, 3, 3, 7, 8, 9, 10, 11, 13, 13 } };

//		ACRlog.logMatrix(matout, ACRlog.qui() + "matout");
//		ACRlog.waitHere();

		assertTrue(ACRutils.compareMatrix(matout, matexpected, ""));

	}

	@Test
	public final void testRemoveDuplicate() {
		int[][] matin = { { 11, 44, 22, 77, 88, 33, 77, 44, 11, 77 }, { 3, 99, 134, 11, 22, 33, 10, 99, 3, 10 },
				{ 13, 9, 13, 10, 2, 3, 11, 8, 13, 11 } };
		ACRlog.logMatrix(matin, ACRlog.qui() + "matin");
		int[][] matout = ACRcalc.removeDuplicate(matin);
		ACRlog.logMatrix(matout, ACRlog.qui() + "matout");

		int[][] matexpected = { { 11, 22, 33, 44, 44, 77, 77, 88 }, { 3, 134, 33, 99, 99, 11, 10, 22 },
				{ 13, 13, 3, 8, 9, 10, 11, 2 } };

//		boolean ok = ACRutils.compareMatrix(matout, matexpected, ACRlog.qui() + "MESSAGGIO");
//		ACRlog.waitHere("OK= " + ok);
		assertTrue(ACRutils.compareMatrix(matout, matexpected, ""));
//		ACRlog.waitHere("FINE, tutto ok");

	}

}
