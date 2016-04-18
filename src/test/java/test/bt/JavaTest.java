package test.bt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class JavaTest {
	
	public static void integer(int x) {
		Integer i = Integer.valueOf(x);
	}

	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}
	
	@Test
	public void testInteger() throws Exception {
		// Sets up
		String mname = "integer";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 2;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		TestUtils.print(is, lname);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ILOAD 0",
				"INVOKESTATIC java/lang/Integer valueOf (I)Ljava/lang/Integer;",
				"ASTORE 1",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}

}
