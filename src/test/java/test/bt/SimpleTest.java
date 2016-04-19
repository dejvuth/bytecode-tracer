package test.bt;

import org.junit.Assert;
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
}
