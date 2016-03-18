package test.bt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class TypeTest {
	
	public static void newTest(Object o) {
		o = new Object();
	}
	
	public static void newarray(byte[] b) {
		b = new byte[2];
	}
	
	public static Long checkcast(Object o) {
		return (Long) o;
	}

	public static boolean instanceofTest(Object o) {
		return o instanceof Long;
	}

	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}
	
	@Test
	public void testNew() throws Exception {
		// Sets up
		String mname = "newTest";
		Class<?>[] params = new Class<?>[] { Object.class };
		Object arg = null;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"NEW java/lang/Object",
				"DUP",
				"INVOKESPECIAL java/lang/Object <init> ()V",
				"ASTORE 0",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}

	@Test
	public void testNewarray() throws Exception {
		// Sets up
		String mname = "newarray";
		Class<?>[] params = new Class<?>[] { byte[].class };
		Object arg = null;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ICONST_2",
				"NEWARRAY 8",
				"ASTORE 0",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
	
	@Test
	public void testCheckcast() throws Exception {
		// Sets up
		String mname = "checkcast";
		Class<?>[] params = new Class<?>[] { Object.class };
		Object arg = 1L;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ALOAD 0",
				"CHECKCAST java/lang/Long",
				"ARETURN"
		}, is);
	}
	
	@Test
	public void testInstanceof() throws Exception {
		// Sets up
		String mname = "instanceofTest";
		Class<?>[] params = new Class<?>[] { Object.class };
		Object arg = 1L;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ALOAD 0",
				"INSTANCEOF java/lang/Long",
				"IRETURN"
		}, is);
	}
}
