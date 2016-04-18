package bt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * A ClassLoader that injects tracing bytecode instructions.
 * 
 * @author suwimont
 *
 */
public class InjectedClassLoader extends ClassLoader {
	
	private String traceCollector;
	
	private Collection<String> ignoredPrefixes;
	
	public static final String STUB_PREFIX = "stub";
	
	/**
	 * Constructs a class loader with the given trace collector.
	 * 
	 * @param traceCollector the collector name.
	 */
	public InjectedClassLoader(String traceCollector, boolean traceJava) {
		this.traceCollector = traceCollector;

		ignoredPrefixes = new ArrayList<String>();
		if (!traceJava)
			ignoredPrefixes.add("java.");
		ignoredPrefixes.add("javax.");
		ignoredPrefixes.add("org.slf4j.");
		ignoredPrefixes.add("ch.qos.logback.");
		ignoredPrefixes.add("org.objectweb.asm.");
		ignoredPrefixes.add("org.xml.");
		ignoredPrefixes.add("com.sun.");
		ignoredPrefixes.add("bt.");
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// Ignores packages
		if (ignoredPrefixes.stream().anyMatch(s -> name.startsWith(s)))
			return super.loadClass(name);
		
		// Loads stub
		if (name.startsWith(STUB_PREFIX))
			return loadStub(name);
		
		// Injects tracing instructions
		try {
			ClassReader cr = new ClassReader(name);
			ClassWriter cw = new ClassWriter(cr, 0);
			TraceClassVisitor tcv = new TraceClassVisitor(cw, 
					new PrintWriter(System.getProperty("java.io.tmpdir") 
							+ File.separator + name.replace('/', '.') + ".jbc"));
			TraceClassAdapter tca = new TraceClassAdapter(
					tcv, traceCollector.replace('.', '/'));
			cr.accept(tca, 0);

			byte[] b = cw.toByteArray();
			return defineClass(name, b, 0, b.length);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ClassNotFoundException(e.getMessage());
		}
	}
	
	private Class<?> loadStub(String name) throws ClassNotFoundException {
		String origName = name.replace(STUB_PREFIX + ".", "");
		
		try {
			ClassReader cr = new ClassReader(origName);
			ClassWriter cw = new ClassWriter(cr, 0);
			TraceClassVisitor tcv = new TraceClassVisitor(cw, 
					new PrintWriter(System.getProperty("java.io.tmpdir") 
							+ File.separator + STUB_PREFIX + "." + origName + ".jbc"));
			ClassVisitor cv = new StubClassAdapter(tcv);
			cr.accept(cv, 0);

			byte[] b = cw.toByteArray();
			return defineClass(name, b, 0, b.length);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClassNotFoundException(e.getMessage());
		}
	}
	
	class StubClassAdapter extends ClassVisitor {
		
		private String className;

		public StubClassAdapter(ClassVisitor cv) {
			super(Opcodes.ASM5, cv);
		}
		
		@Override
		public void visit(int version, int access, String name, String signature,
	            String superName, String[] interfaces) {
			className = name;
			
			// Transforms superName
			if (superName == null)
				superName = "java/lang/Object";
			else if ((access & Opcodes.ACC_INTERFACE) == 0	// superName of interface must be java/lang/Object 
					&& !superName.equals("java/lang/Object")
					&& !superName.equals("java/lang/Throwable"))	// No stub for java/lang/Throwable
				superName = STUB_PREFIX + "/" + superName;
			
			// Transform interfaces
			if (interfaces != null) {
				for (int i = 0; i < interfaces.length; i++) {
					if (interfaces[i].startsWith("java/"))
						interfaces[i] = STUB_PREFIX + "/" + interfaces[i];
				}
			}
			cv.visit(version, access, STUB_PREFIX + "/" + name, signature, superName, interfaces);
		}
		
		@Override
		public void visitInnerClass(String name, String outerName,
	            String innerName, int access) {
			
			// FIXME
			if (name != null && name.startsWith("java/"))
				name = "stub/" + name;
			if (outerName != null && outerName.startsWith("java/"))
				outerName = "stub/" + outerName;
	        cv.visitInnerClass(name, outerName, innerName, access);
	    }
		
		@Override
		public FieldVisitor visitField(int access, String name, String desc,
	            String signature, Object value) {
			
			desc = TraceMethodAdapter.replaceStub(desc);
			return cv.visitField(access, name, desc, signature, value);
	    }
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
	            String signature, String[] exceptions) {
			
			// Transforms method descriptor
			desc = TraceMethodAdapter.replaceStub(desc);
			
			// FIXME
			if (exceptions != null) {
				for (int i = 0; i < exceptions.length; i++) {
					exceptions[i] = exceptions[i].replaceAll("java/", "stub/java/");
				}
			}
			
			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
			
			return new TraceMethodAdapter(access, name, desc, signature, exceptions, mv, 
					className, traceCollector.replace('.', '/'));
		}
	}
}

