package test.bt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class SimpleTest {
	
	public static void a(int x) {
		if (x == 3) {
			x++;
		} else {
			x--; 
		}
	}
	
	public static void b() {
		a(3);
	}
	
	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}

	@Test
	public void test() throws Exception {
		Tracer tracer = new Tracer("test.bt.SimpleTest");
		String[] is = tracer.run("a", new Class<?>[] { int.class }, 3);
		TestUtils.print(is);
		Assert.assertArrayEquals(new String[] {
			"LABEL test/bt/SimpleTest.a(I)V0",
			"ILOAD 0",
			"ICONST_3",
			"IF_ICMPNE test/bt/SimpleTest.a(I)V2 test/bt/SimpleTest.a(I)V1",
			"LABEL test/bt/SimpleTest.a(I)V1",
			"IINC 0 1",
			"GOTO test/bt/SimpleTest.a(I)V3",
			"LABEL test/bt/SimpleTest.a(I)V3",
			"RETURN"
		}, is);
		
		is = tracer.run("a", new Class<?>[] { int.class }, 42);
		TestUtils.print(is);
		Assert.assertArrayEquals(new String[] {
			"LABEL test/bt/SimpleTest.a(I)V0",
			"ILOAD 0",
			"ICONST_3",
			"IF_ICMPNE test/bt/SimpleTest.a(I)V2 test/bt/SimpleTest.a(I)V1",
			"LABEL test/bt/SimpleTest.a(I)V2",
			"IINC 0 -1",
			"LABEL test/bt/SimpleTest.a(I)V3",
			"RETURN"
		}, is);
	}
	
	public static int fib(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		if (n <= 1)
			return n;
		return fib(n-1) + fib(n-2);
	}
	
	@Test
	public void testFib() throws Exception {
		// Sets up
		String mname = "fib";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = -1;

		// Runs
		String[] is = tracer.run(mname, params, arg);
		
		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		TestUtils.print(is, lname);
		Assert.assertArrayEquals(new String[] {
			"LABEL " + lname + "0",
			"ILOAD 0",
			"IFGE " + lname + "2 " + lname + "1",
			"LABEL " + lname + "1",
			"NEW java/lang/IllegalArgumentException",
			"DUP",
			"INVOKESPECIAL java/lang/IllegalArgumentException <init> ()V",
			"ATHROW"
		}, is);
	}
}
