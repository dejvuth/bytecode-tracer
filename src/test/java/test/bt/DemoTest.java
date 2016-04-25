package test.bt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class DemoTest {

	public static int[] createIntArray(int size) {
		if (size < 0)
			throw new IllegalArgumentException();
		return new int[size];
	}
	
	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}
	
	@Test
	public void testDemoThrow() throws Exception {
		// Sets up
		String mname = "createIntArray";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = -1;

		// Runs
		String[] is = tracer.run(mname, params, arg);
		
		// Checks
		TestUtils.print(is);
		Assert.assertArrayEquals(new String[] {
			"LABEL test/bt/DemoTest.createIntArray(I)[I0",
			"ILOAD 0",
			"IFGE test/bt/DemoTest.createIntArray(I)[I2 test/bt/DemoTest.createIntArray(I)[I1",
			"LABEL test/bt/DemoTest.createIntArray(I)[I1",
			"NEW java/lang/IllegalArgumentException",
			"DUP",
			"INVOKESPECIAL java/lang/IllegalArgumentException <init> ()V",
			"ATHROW"
		}, is);
	}
	
	@Test
	public void testDemo() throws Exception {
		// Sets up
		String mname = "createIntArray";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 4;

		// Runs
		String[] is = tracer.run(mname, params, arg);
		
		// Checks
		TestUtils.print(is);
		Assert.assertArrayEquals(new String[] {
			"LABEL test/bt/DemoTest.createIntArray(I)[I0",
			"ILOAD 0",
			"IFGE test/bt/DemoTest.createIntArray(I)[I2 test/bt/DemoTest.createIntArray(I)[I1",
			"LABEL test/bt/DemoTest.createIntArray(I)[I2",
			"ILOAD 0",
			"NEWARRAY 10",
			"ARETURN"
		}, is);
	}
}
