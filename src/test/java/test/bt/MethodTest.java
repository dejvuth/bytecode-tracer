package test.bt;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class MethodTest {
	
	public static void invokestatic(int x) {
		Integer.valueOf(x);
	}
	
	public static void invokevirtual(String s) {
		s.length();
	}
	
	public static <E> void invokeinterface(List<E> l) {
		l.isEmpty();
	}
	
	public static void invokespecial(double d) {
		new Double(d);
	}

	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}
	
	@Test
	public void testInvokestatic() throws Exception {
		// Sets up
		String mname = "invokestatic";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 1;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ILOAD 0",
				"INVOKESTATIC java/lang/Integer valueOf (I)Ljava/lang/Integer;",
				"POP",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
	
	@Test
	public void testInvokevirtual() throws Exception {
		// Sets up
		String mname = "invokevirtual";
		Class<?>[] params = new Class<?>[] { String.class };
		Object arg = "Hello World!";

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ALOAD 0",
				"INVOKEVIRTUAL java/lang/String length ()I",
				"POP",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}

	@Test
	public void testInvokeinterface() throws Exception {
		// Sets up
		String mname = "invokeinterface";
		Class<?>[] params = new Class<?>[] { List.class };
		Object arg = Arrays.asList(1);

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ALOAD 0",
				"INVOKEINTERFACE java/util/List isEmpty ()Z",
				"POP",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
	
	@Test
	public void testInvokespecial() throws Exception {
		// Sets up
		String mname = "invokespecial";
		Class<?>[] params = new Class<?>[] { double.class };
		Object arg = 3.14;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		TestUtils.print(is, lname);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"NEW java/lang/Double",
				"DLOAD 0",
				"INVOKESPECIAL java/lang/Double <init> (D)V",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
}
