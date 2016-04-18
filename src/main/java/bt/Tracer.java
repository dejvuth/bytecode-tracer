package bt;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tracer {
	
	private static final String DEFAULT_TRACE_COLLECTOR = "bt.TraceCollector";
	
	private InjectedClassLoader cl;
	private Class<?> tracerClass;
	private Class<?> runningClass;
	
	private boolean traceJava;
	
	private static Logger logger = LoggerFactory.getLogger(Tracer.class);

	/**
	 * Constructs a tracer.
	 * 
	 * @param className the class name to be traced.
	 * @throws ClassNotFoundException
	 */
	public Tracer(String className) throws ClassNotFoundException {
		this(className, DEFAULT_TRACE_COLLECTOR, false);
	}
	
	/**
	 * Constructs a tracer with the specified collector.
	 * 
	 * @param className the class name to be traced.
	 * @param traceCollector the collector name.
	 * @throws ClassNotFoundException
	 */
	public Tracer(String className,  String traceCollector, boolean traceJava) 
			throws ClassNotFoundException {
		this.traceJava = traceJava;
		
		// Creates a new class loader
		cl = new InjectedClassLoader(traceCollector, traceJava);
		
		// Uses the class loader to load the tracer and the traced class
		tracerClass = cl.loadClass(traceCollector);
		runningClass = cl.loadClass(className);
	}
	
	/**
	 * Traces the specified method.
	 * 
	 * @param methodName the method name.
	 * @param methodParams the method parameters (for example <code>new Class<?>[] { int.class }</code>)
	 * @param args the method arguments (for example <code>42</code>)
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public String[] run(String methodName, Class<?>[] methodParams, Object... args) 
			throws IllegalAccessException, IllegalArgumentException, 
			InvocationTargetException, NoSuchMethodException, SecurityException {
		// Resets the tracer
		tracerClass.getMethod("clear").invoke(null);
		
		// Runs!
		runningClass.getMethod(methodName, methodParams).invoke(null, args);
		
		// Logs the execution trace and remaining labels
		if (logger.isDebugEnabled()) {
			tracerClass.getMethod("log").invoke(null);
		}

		return (String[]) tracerClass.getMethod("getInstructions").invoke(null);
	}
}
