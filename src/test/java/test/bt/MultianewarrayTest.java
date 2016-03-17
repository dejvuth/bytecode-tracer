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
	
	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}

	@Test
	public void testMultianewarrayInt() throws Exception {
		// Sets up
		String mname = "multianewarrayInt";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 5;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
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
	public void testMultianewarrayObject() throws Exception {
		// Sets up
		String mname = "multianewarrayObject";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = 4;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
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
