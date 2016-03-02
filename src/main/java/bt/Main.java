package bt;


import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
//	static Logger logger = BtLogger.getLogger(Main.class);
	
	static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void run(String className, String methodName, Class<?>[] methodParams, Object... args) 
			throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		// Creates a new class loader and uses it to load TraceCollector
		InjectedClassLoader cl = new InjectedClassLoader();
		Class<?> tracerClass = cl.loadClass("bt.TraceCollector");
		
		// Run the test method
		Class<?> runningClass = cl.loadClass(className);
		try {
			runningClass.getMethod(methodName, methodParams).invoke(null, args);
		} catch (Exception e) {
			System.err.println("Argument " + Arrays.toString(args) + " caused an exception: ");
			e.printStackTrace();
		}
		
		// Log the execution trace and remaining labels
		if (logger.isDebugEnabled()) {
//			tracerClass.getMethod("log", Logger.class).invoke(null, logger);
			tracerClass.getMethod("log").invoke(null);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
