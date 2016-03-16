package test.bt;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class MultianewarrayTest {
	
	public static void multianewarrayInt(int x) {
		int[][] a = new int[x][x];
	}

	public static void multianewarrayObject(int x) {
		Object[][] b = new Object[x][x];
	}
	
	String cname;
	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		cname = this.getClass().getName();
		tracer = new Tracer(cname);
	}

	@Test
	public void testInt() throws Exception {
		// Sets up
		String mname = "multianewarrayInt";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 5;
		String lname = cname.replace('.', '/') + "." + mname + "(I)V";

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ILOAD 0",
				"ILOAD 0",
				"MULTIANEWARRAY [[I 2",
				"ASTORE 1",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}

	@Test
	public void testObject() throws Exception {
		// Sets up
		String mname = "multianewarrayObject";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 4;
		String lname = cname.replace('.', '/') + "." + mname + "(I)V";

		// Runs
		String[] is = tracer.run(mname, params, arg);
		TestUtils.print(is, lname);

		// Checks
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"ILOAD 0",
				"ILOAD 0",
				"MULTIANEWARRAY [[Ljava/lang/Object; 2",
				"ASTORE 1",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
}
