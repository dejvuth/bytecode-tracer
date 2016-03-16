package bt;


import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);

//	public static String[] run(String className, String methodName, Class<?>[] methodParams, Object... args) 
//			throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, 
//			InvocationTargetException, NoSuchMethodException, SecurityException {
//		
//		// Creates a new class loader and uses it to load TraceCollector
//		InjectedClassLoader cl = new InjectedClassLoader();
//		Class<?> tracerClass = cl.loadClass("bt.TraceCollector");
//		
//		// Runs the test method
//		Class<?> runningClass = cl.loadClass(className);
//		try {
//			runningClass.getMethod(methodName, methodParams).invoke(null, args);
//		} catch (Exception e) {
//			System.err.println("Argument " + Arrays.toString(args) + " caused an exception: ");
//			e.printStackTrace();
//		}
//		
//		// Logs the execution trace and remaining labels
//		if (logger.isDebugEnabled()) {
//			tracerClass.getMethod("log").invoke(null);
//		}
//
//		return (String[]) tracerClass.getMethod("getInstructions").invoke(null);
//	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
