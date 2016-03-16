package bt;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Inject tracing instructions
 * 
 * @author suwimont
 *
 */
public class TraceClassAdapter extends ClassVisitor {
	
	private String className;
	private String traceCollector;

	public TraceClassAdapter(ClassVisitor cv, String traceCollector) {
		super(Opcodes.ASM5, cv);
		this.traceCollector = traceCollector;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
		this.className = name;
		
//		superName = TraceMethodAdapter.replaceStub(superName);
//		if (interfaces != null) {
//			for (int i = 0; i < interfaces.length; i++) {
//				interfaces[i] = TraceMethodAdapter.replaceStub(interfaces[i]);
//			}
//		}
		
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
//		desc = TraceMethodAdapter.replaceStub(desc);
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
//		desc = TraceMethodAdapter.replaceStub(desc);
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		
		return new TraceMethodAdapter(access, name, desc, signature, exceptions, mv, 
				className, traceCollector);
	}
}
