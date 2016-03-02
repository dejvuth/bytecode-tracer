package bt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceCollector {

	public static List<AbstractInsnNode> instructions = new ArrayList<AbstractInsnNode>();
	
	public static Set<String> allLabels = new HashSet<>();
	public static Set<String> removedLabels = new HashSet<>();
	
	static Logger logger = LoggerFactory.getLogger(TraceCollector.class);
	
	public static void initialize() {
		instructions = new ArrayList<AbstractInsnNode>();
	}
	
	public static void clear() {
		if (instructions == null)
			instructions = new ArrayList<AbstractInsnNode>();
		else
			instructions.clear();
	}
	
	public static void log(Logger logger) {
		System.out.println("log tc");
		
		int nodeIndex = 0;
        for (AbstractInsnNode node : instructions) {
        	logger.debug("[{}] {}", nodeIndex++, NodePrinter.print(node));
        }
	}
	
	public static void log() {
		int nodeIndex = 0;
        for (AbstractInsnNode node : instructions) {
        	logger.debug("[{}] {}", nodeIndex++, NodePrinter.print(node));
        }
	}
	
	public static void logLabels(Logger logger) {
		if (allLabels.isEmpty()) {
			logger.debug("No remaining labels");
			return;
		}
		
		logger.debug("Remaining labels:");
        for (String label: allLabels)
        	logger.debug(label);
	}
	
	public static void visitInsn(final int opcode) {
		instructions.add(new InsnNode(opcode));
	}
	
	public static void visitIntInsn(final int opcode, final int operand) {
        instructions.add(new IntInsnNode(opcode, operand));
    }
	
    public static void visitVarInsn(final int opcode, final int var) {
        instructions.add(new VarInsnNode(opcode, var));
    }
    
    public static void visitTypeInsn(final int opcode, final String type) {
        instructions.add(new TypeInsnNode(opcode, type));
    }
    
    public static void visitFieldInsn(final int opcode, final String owner,
            final String name, final String desc) {
        instructions.add(new FieldInsnNode(opcode, owner, name, desc));
    }
    
    public static void visitMethodInsn(int opcode, String owner, String name,
            String desc, boolean itf) {
        instructions.add(new MethodInsnNode(opcode, owner, name, desc, itf)); 
    }
    
    public static void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        instructions.add(new InvokeDynamicInsnNode(name, desc, bsm, bsmArgs));
    }
    
    public static void visitIfInsn(final int value, final int opcode, final String jumpLabel, final String nextLabel) {
        instructions.add(new IfInsnNode(opcode, value, jumpLabel, nextLabel));
    }
    
    public static void visitIfIcmpInsn(final int value1, final int value2, final int opcode, final String jumpLabel, final String nextLabel) {
        instructions.add(new IfInsnNode(opcode, value1, value2, jumpLabel, nextLabel));
    }
    
    public static void visitIfAcmpInsn(final Object value1, final Object value2, final int opcode, final String jumpLabel, final String nextLabel) {
        instructions.add(new IfInsnNode(opcode, value1, value2, jumpLabel, nextLabel));
    }
    
    public static void visitIfInsn(final Object value, final int opcode, final String jumpLabel, final String nextLabel) {
        instructions.add(new IfInsnNode(opcode, value, jumpLabel, nextLabel));
    }
    
    public static void visitLdcInsn(final int cst) {
        instructions.add(new LdcInsnNode(cst));
    }
    
    public static void visitLdcInsn(final long cst) {
        instructions.add(new LdcInsnNode(cst));
    }
    
    public static void visitLdcInsn(final float cst) {
        instructions.add(new LdcInsnNode(cst));
    }
    
    public static void visitLdcInsn(final double cst) {
        instructions.add(new LdcInsnNode(cst));
    }
    
    public static void visitLdcInsn(final String cst) {
        instructions.add(new LdcInsnNode(cst));
    }
    
    public static void visitLdcInsn(final Object cst) {
        instructions.add(new LdcInsnNode(cst));
    }
    
    public static void visitIincInsn(final int var, final int increment) {
        instructions.add(new IincInsnNode(var, increment));
    }
    
    public static void visitMultiANewArrayInsn(final String desc, final int dims) {
        instructions.add(new MultiANewArrayInsnNode(desc, dims));
    }
    
    public static void addLabel(String label) {
    	if (label.contains("lljvm/runtime/Memory.alignOffsetUp")
    			|| label.contains("genunit/runtime/ByteBuffer.order"))
    		return;
    	
    	if (removedLabels.contains(label))
    		return;
    	
    	allLabels.add(label);
    }
    
    public static void visitLabel(String label) {
    	removedLabels.add(label);
    	allLabels.remove(label);
    	instructions.add(new TraceLabelNode(label));
    }
}