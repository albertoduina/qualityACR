package qualityACR;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.ImageJ;

public class ACRcalcTest {

	@Test
	public void testConvertToDoubleOK() {
		// Test case with the age is a numeric string
		String age = "1990";
		Double expAge = Double.valueOf(age);
		Double actual = Double.valueOf(age);

		assertAll("Do many assertions.", () -> {
			assertNotNull(actual);
			assertEquals(expAge, actual);
		});

	}

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
		ACRlog.logMatrix(matout, ACRlog.qui() + "matout");
		ACRlog.waitHere();

	}

	@Test
	public final void testRemoveDuplicate() {
		int[][] matin = { { 11, 44, 22, 77, 88, 33, 0, 44, 11, 8 }, { 3, 99, 134, 11, 22, 33, 44, 99, 3, 0 } };
		ACRlog.logMatrix(matin, ACRlog.qui() + "matin");
		int[][] matout = ACRcalc.removeDuplicate(matin);

		ACRlog.logMatrix(matout, ACRlog.qui() + "matout");
		ACRlog.waitHere();

	}

}
