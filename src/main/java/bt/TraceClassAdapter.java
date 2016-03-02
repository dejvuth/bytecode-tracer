package bt;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TraceClassAdapter extends ClassVisitor {
	
	private String className;

	public TraceClassAdapter(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}
	
	private static boolean notTraced(String name) {
//		if (name.startsWith("bt/") && !name.startsWith("bt/runtime/")
//				|| name.equals("lljvm/lib/c")
//				|| name.startsWith("org/objectweb/asm")
//				|| name.startsWith("com/microsoft/z3")
//				)
		
		// FIXME
		if (name.equals("bt/TraceCollector")
				|| name.equals("bt/TraceLabelNode")
				|| name.equals("bt/IfInsnNode")
				|| name.equals("bt/NodePrinter")
				|| name.startsWith("org/objectweb/asm")
				|| name.startsWith("org/slf4j")
				|| name.startsWith("ch/qos/logback")
				|| name.startsWith("org/xml")
				|| name.startsWith("javax/xml")
				|| name.startsWith("com/sun"))
			return true;
		
		return false;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
		this.className = name;
		
		// Do not transform transformer classes
		if (notTraced(className)) {
			super.visit(version, access, name, signature, superName, interfaces);
			return;
		}
		
		superName = TraceMethodAdapter.replaceStub(superName);
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				interfaces[i] = TraceMethodAdapter.replaceStub(interfaces[i]);
			}
		}
		
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
		// Do not transform transformer classes
		if (notTraced(className))
			return super.visitField(access, name, desc, signature, value);
		
		desc = TraceMethodAdapter.replaceStub(desc);
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
		
		// Do not transform transformer classes
		if (notTraced(className))
			return cv.visitMethod(access, name, desc, signature, exceptions);
			
		desc = TraceMethodAdapter.replaceStub(desc);
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		
		return new TraceMethodAdapter(access, name, desc, signature, exceptions, mv, 
				className);
	}
}
