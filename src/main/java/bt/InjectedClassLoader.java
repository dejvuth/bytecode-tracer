package bt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

public class InjectedClassLoader extends ClassLoader {
	
	public static final String STUB_PREFIX = "stub";

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name.startsWith("java."))
			return super.loadClass(name);
		
		if (name.startsWith(STUB_PREFIX))
			return loadStub(name);
		
		try {
			ClassReader cr = new ClassReader(name);
			ClassWriter cw = new ClassWriter(cr, 0);
			TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.getProperty("java.io.tmpdir") + File.separator + name.replace('/', '.') + ".jbc"));
			TraceClassAdapter tca = new TraceClassAdapter(tcv);
			cr.accept(tca, 0);
			byte[] b = cw.toByteArray();
			
			return defineClass(name, b, 0, b.length);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ClassNotFoundException(e.getMessage());
		}
	}
	
	private Class<?> loadStub(String name) {
		String origName = name.replace(STUB_PREFIX + ".", "");
		
		ClassReader cr = null;
		try {
//			System.out.println("Reading " + origName);
			cr = new ClassReader(origName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ClassWriter cw = new ClassWriter(cr, 0);
		TraceClassVisitor tcv = null;
		try {
			tcv = new TraceClassVisitor(cw, new PrintWriter(System.getProperty("java.io.tmpdir") + File.separator + "stub" + origName + ".jbc"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ClassVisitor cv = new StubClassAdapter(tcv);
		cr.accept(cv, 0);
		
		byte[] b = cw.toByteArray();
		Class<?> c = null;
		try {
			c = defineClass(name, b, 0, b.length);
//			System.out.println("DONE origName: " + origName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
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
			
			// Transform superName
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
			
			// Transform method descriptor
			desc = TraceMethodAdapter.replaceStub(desc);
			
			if (exceptions != null) {
				for (int i = 0; i < exceptions.length; i++) {
					exceptions[i] = exceptions[i].replaceAll("java/", "stub/java/");
				}
			}
			
			MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
			
			return new TraceMethodAdapter(access, name, desc, signature, exceptions, mv, 
					className);
		}
	}
}

