package bt;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

public class IfInsnNode extends AbstractInsnNode {

	public String jumpLabel;
	public String nextLabel;
	
	public IfInsnNode(final int opcode, final String jumpLabel, final String nextLabel) {
		super(opcode);
		this.jumpLabel = jumpLabel;
		this.nextLabel = nextLabel;
	}
	
	public IfInsnNode(final int opcode, final String jumpLabel) {
		super(opcode);
		this.jumpLabel = jumpLabel;
		this.nextLabel = null;
	}
	
	@Override
	public void accept(MethodVisitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractInsnNode clone(Map arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getType() {
		return JUMP_INSN;
	}
	
	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}
	
	public IfInsnNode cloneReverse() {
		return new IfInsnNode(reverseOpcode(opcode), jumpLabel, nextLabel);
	}

	private static int reverseOpcode(int opcode) {
		switch (opcode) {
		case Opcodes.IFEQ:
			return Opcodes.IFNE;
		case Opcodes.IFNE:
			return Opcodes.IFEQ;
		case Opcodes.IFLT:
			return Opcodes.IFGE;
		case Opcodes.IFGE:
			return Opcodes.IFLT;
		case Opcodes.IFGT:
			return Opcodes.IFLE;
		case Opcodes.IFLE:
			return Opcodes.IFGT;
		case Opcodes.IF_ICMPEQ:
			return Opcodes.IF_ICMPNE;
		case Opcodes.IF_ICMPNE:
			return Opcodes.IF_ICMPEQ;
		case Opcodes.IF_ICMPLT:
			return Opcodes.IF_ICMPGE;
		case Opcodes.IF_ICMPGE:
			return Opcodes.IF_ICMPLT;
		case Opcodes.IF_ICMPGT:
			return Opcodes.IF_ICMPLE;
		case Opcodes.IF_ICMPLE:
			return Opcodes.IF_ICMPGT;
		case Opcodes.IF_ACMPEQ:
			return Opcodes.IF_ACMPNE;
		case Opcodes.IF_ACMPNE:
			return Opcodes.IF_ACMPEQ;
		case Opcodes.IFNULL:
			return Opcodes.IFNONNULL;
		case Opcodes.IFNONNULL:
			return Opcodes.IFNULL;
		}
		
		throw new UnsupportedOperationException("Cannot reverse opcode " + opcode);
	}

}
