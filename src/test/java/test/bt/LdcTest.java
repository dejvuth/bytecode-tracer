package test.bt;


import java.lang.invoke.MethodType;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class LdcTest {
	
	public static void ldcInt(int x) {
		x = 1234567;
	}

	public static void ldcFloat(float x) {
		x = 1.234567f;
	}
	
	public static void ldcString(String x) {
		x = "Hello World!";
	}
	
	public static void ldcClass(Class<?> c) {
		c = String.class;
	}

	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}
	
	private void test(String mname, Class<?> param, Object arg, 
			String expectedLoad, String expectedStore) throws Exception {
		// Runs
		Class<?>[] params = new Class<?>[] { param };
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				expectedLoad,
				expectedStore,
				"LABEL " + lname + "1",
				"RETURN"
		}, is);	
	}

	@Test
	public void testLdcInt() throws Exception {
		test("ldcInt", int.class, 0, "LDC 1234567", "ISTORE 0");
	}

	@Test
	public void testLdcFloat() throws Exception {
		test("ldcFloat", float.class, 0f, "LDC 1.234567", "FSTORE 0");
	}
	
	@Test
	public void testLdcString() throws Exception {
		test("ldcString", String.class, "", "LDC Hello World!", "ASTORE 0");
	}
	
	@Test
	public void testLdcClass() throws Exception {
		test("ldcClass", Class.class, Integer.class, "LDC Ljava/lang/String;", "ASTORE 0");
	}
}
