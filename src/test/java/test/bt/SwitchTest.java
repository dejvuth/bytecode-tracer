package test.bt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class SwitchTest {
	
	public static int lookup(int x) {
		switch (x) {
		case Integer.MIN_VALUE:
			return -1;
		case 0:
			return 0;
		default:
			return 1;
		}
	}
	
	public static int table(int x) {
		switch (x) {
		case -1:
			return 1;
		case 0:
			return 0;
		default:
			return 1;
		}
	}
	
	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}

	@Test
	public void testLookup() throws Exception {
		// Sets up
		String mname = "lookup";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 3;

		// Runs
		String[] is = tracer.run(mname, params, arg);
		
		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
			"LABEL " + lname + "0",
			"ILOAD 0",
			"LOOKUPSWITCH " + lname + "3 -2147483648|0 " + lname + "1|" + lname + "2",
			"LABEL " + lname + "3",
			"ICONST_1",
			"IRETURN"
		}, is);
	}

	@Test
	public void testTable() throws Exception {
		// Sets up
		String mname = "table";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = -1;

		// Runs
		String[] is = tracer.run(mname, params, arg);
		
		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
			"LABEL " + lname + "0",
			"ILOAD 0",
			"TABLESWITCH -1 0 " + lname + "3 " + lname + "1|" + lname + "2",
			"LABEL " + lname + "1",
			"ICONST_1",
			"IRETURN"
		}, is);
	}
}
