package test.bt;

import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bt.Tracer;

public class InvokeDynamicTest {
	
	public static void invokedynamic(int x) {
		Consumer<String> c = System.out::println;
	}
	
	Tracer tracer;
	
	@Before
	public void setup() throws ClassNotFoundException {
		tracer = new Tracer(this.getClass().getName());
	}

	@Test
	public void testIinc() throws Exception {
		// Sets up
		String mname = "invokedynamic";
		Class<?>[] params = new Class<?>[] { int.class };
		Object arg = -1;

		// Runs
		String[] is = tracer.run(mname, params, arg);

		// Checks
		String lname = TestUtils.getLabelName(this.getClass(), mname, params);
		TestUtils.print(is, lname);
		Assert.assertArrayEquals(new String[] {
				"LABEL " + lname + "0",
				"GETSTATIC java/lang/System out Ljava/io/PrintStream;",
				"LDC accept",
				"LDC (Ljava/io/PrintStream;)Ljava/util/function/Consumer;",
				"LDC java/lang/invoke/LambdaMetafactory.metafactory"
				+ "(Ljava/lang/invoke/MethodHandles$Lookup;"
				+ "Ljava/lang/String;"
				+ "Ljava/lang/invoke/MethodType;"
				+ "Ljava/lang/invoke/MethodType;"
				+ "Ljava/lang/invoke/MethodHandle;"
				+ "Ljava/lang/invoke/MethodType;"
				+ ")Ljava/lang/invoke/CallSite; (6)",
				"LDC (Ljava/lang/Object;)V"
				+ "|java/io/PrintStream.println(Ljava/lang/String;)V (5)"
				+ "|(Ljava/lang/String;)V",
				"INVOKEDYNAMIC accept (Ljava/io/PrintStream;)Ljava/util/function/Consumer; "
				+ "java/lang/invoke/LambdaMetafactory.metafactory"
				+ "(Ljava/lang/invoke/MethodHandles$Lookup;"
				+ "Ljava/lang/String;"
				+ "Ljava/lang/invoke/MethodType;"
				+ "Ljava/lang/invoke/MethodType;"
				+ "Ljava/lang/invoke/MethodHandle;"
				+ "Ljava/lang/invoke/MethodType;"
				+ ")Ljava/lang/invoke/CallSite; (6) "
				+ "(Ljava/lang/Object;)V"
				+ "|java/io/PrintStream.println(Ljava/lang/String;)V (5)"
				+ "|(Ljava/lang/String;)V",
				"ASTORE 1",
				"LABEL " + lname + "1",
				"RETURN"
		}, is);
	}
}
