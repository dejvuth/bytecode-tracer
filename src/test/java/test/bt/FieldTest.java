package test.bt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class FieldTest {
		
	static int s;
	
	public static void getstatic(Object o) {
		o = System.out;
	}
	
	public static void putstatic(int x) {
		s = x;
	}
	
	public static void getfield(int x) {
		x = (new Inner()).f;
	}
	
	public static void putfield(int x) {
		new Inner(x);
	}
	
	private static class Inner {
		int f;
		public Inner() {}
		public Inner(int f) { this.f = f; }
	}
	
	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}
	
	@Test
	public void testGetstatic() throws Exception {
		// Sets up
		String mname = "getstatic";
		Class<?>[] params = new Class<?>[] { Object.class };
		Object arg = null;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"GETSTATIC java/lang/System out Ljava/io/PrintStream;",
				"ASTORE 0",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}

	@Test
	public void testPutstatic() throws Exception {
		// Sets up
		String mname = "putstatic";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = -2;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ILOAD 0",
				"PUTSTATIC test/bt/FieldTest s I",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
	
	@Test
	public void testGetfield() throws Exception {
		// Sets up
		String mname = "getfield";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 8; 

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"NEW test/bt/FieldTest$Inner",
				"DUP",
				"INVOKESPECIAL test/bt/FieldTest$Inner <init> ()V",
				"LABEL test/bt/FieldTest$Inner.<init>()V0",
				"ALOAD 0",
				"INVOKESPECIAL java/lang/Object <init> ()V",
				"RETURN",
				"GETFIELD test/bt/FieldTest$Inner f I",
				"ISTORE 0",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
	
	@Test
	public void testPutfield() throws Exception {
		// Sets up
		String mname = "putfield";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 23; 

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"NEW test/bt/FieldTest$Inner",
				"ILOAD 0",
				"INVOKESPECIAL test/bt/FieldTest$Inner <init> (I)V",
				"LABEL test/bt/FieldTest$Inner.<init>(I)V0",
				"ALOAD 0",
				"INVOKESPECIAL java/lang/Object <init> ()V",
				"ALOAD 0",
				"ILOAD 1",
				"PUTFIELD test/bt/FieldTest$Inner f I",
				"RETURN",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
}
