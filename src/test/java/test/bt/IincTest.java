package test.bt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class IincTest {
	
	public static void iinc(int x) {
		x++;
	}

	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}

	@Test
	public void testIinc() throws Exception {
		// Sets up
		String mname = "iinc";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = -1;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"IINC 0 1",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
}
