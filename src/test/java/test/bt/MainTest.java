package test.bt;

import org.junit.Test;

import bt.Main;

public class MainTest {

	@Test
	public void testSimple() throws Exception {
		Main.main(new String[] {"test.bt.SimpleTest", "b"});
	}
	
	@Test
	public void testSimpleFib() throws Exception {
		Main.main(new String[] {"test.bt.SimpleTest", "fib", "(I)I", "2"});
	}
}
