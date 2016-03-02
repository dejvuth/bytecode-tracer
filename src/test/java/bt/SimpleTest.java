package bt;

import static org.junit.Assert.*;

import org.junit.Test;

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
		Main.run("bt.SimpleTest", "a", new Class<?>[] { int.class }, new Integer(3));
	}

}
