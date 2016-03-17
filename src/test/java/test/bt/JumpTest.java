package test.bt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class JumpTest {
	
	public static void ifeq(int x) {
		if (x != 0)
			x = 1;
	}
	
	public static void if_icmpne(int x) {
		if (x == 7)
			x = 1;
	}
	
	public static void if_acmpeq(Object o1, Object o2) {
		if (o1 != o2)
			o1 = null;
	}

	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}
	
	@Test
	public void testIfeq() throws Exception {
		// Sets up
		String mname = "ifeq";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 0;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ILOAD 0",
				"IFEQ " + lname + "2 " + lname + "1",
				"LABEL " + lname + "2",
				"RETURN"
		}, is);
	}

	@Test
	public void testIficmpne() throws Exception {
		// Sets up
		String mname = "if_icmpne";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 0;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ILOAD 0",
				"BIPUSH 7",
				"IF_ICMPNE " + lname + "2 " + lname + "1",
				"LABEL " + lname + "2",
				"RETURN"
		}, is);
	}
	
	@Test
	public void testIfacmpeq() throws Exception {
		// Sets up
		String mname = "if_acmpeq";
		Class<?>[] params = new Class<?>[] { Object.class, Object.class };

		// Runs
		String[] is = tracer.run(mname, params, "a", null);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		TestUtils.print(is, lname);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ALOAD 0",
				"ALOAD 1",
				"IF_ACMPEQ " + lname + "2 " + lname + "1",
				"LABEL " + lname + "1",
				"ACONST_NULL",
				"ASTORE 0",
				"LABEL " + lname + "2",
				"RETURN"
		}, is);
	}
}
