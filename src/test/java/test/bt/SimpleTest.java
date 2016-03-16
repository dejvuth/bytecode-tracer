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

	@Test
	public void test() throws Exception {
		Tracer tracer = new Tracer("test.bt.SimpleTest");
		String[] is = tracer.run("a", new Class<?>[] { int.class }, 3);
		Assert.assertArrayEquals(new String[] {
			"LABEL test/bt/SimpleTest.a(I)V0",
			"ILOAD 0",
			"ICONST_3",
			"IF_ICMPNE test/bt/SimpleTest.a(I)V3 test/bt/SimpleTest.a(I)V1",
			"LABEL test/bt/SimpleTest.a(I)V1",
			"IINC 0 1",
			"LABEL test/bt/SimpleTest.a(I)V2",
			"GOTO test/bt/SimpleTest.a(I)V4",
			"LABEL test/bt/SimpleTest.a(I)V4",
			"RETURN"
		}, is);
		
		is = tracer.run("a", new Class<?>[] { int.class }, 42);
		Assert.assertArrayEquals(new String[] {
			"LABEL test/bt/SimpleTest.a(I)V0",
			"ILOAD 0",
			"ICONST_3",
			"IF_ICMPNE test/bt/SimpleTest.a(I)V3 test/bt/SimpleTest.a(I)V1",
			"LABEL test/bt/SimpleTest.a(I)V3",
			"IINC 0 -1",
			"LABEL test/bt/SimpleTest.a(I)V4",
			"RETURN"
		}, is);
	}
}
