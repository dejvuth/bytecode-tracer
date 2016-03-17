package bt;

import java.util.Arrays;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

public class NodePrinter {

public static String print(AbstractInsnNode node) {
		
		switch (node.getType()) {
		case AbstractInsnNode.INSN:
			return Printer.OPCODES[node.getOpcode()];
		case AbstractInsnNode.INT_INSN:
			return Printer.OPCODES[node.getOpcode()] + " " + ((IntInsnNode) node).operand;
		case AbstractInsnNode.VAR_INSN:
			return Printer.OPCODES[node.getOpcode()] + " " + ((VarInsnNode) node).var;
		case AbstractInsnNode.TYPE_INSN:
			return Printer.OPCODES[node.getOpcode()] + " " + ((TypeInsnNode) node).desc;
		case AbstractInsnNode.FIELD_INSN: {
			FieldInsnNode fnode = (FieldInsnNode) node;
			return Printer.OPCODES[node.getOpcode()] + " " + fnode.owner + " " + fnode.name + " " + fnode.desc;
		}
		case AbstractInsnNode.METHOD_INSN: {
			MethodInsnNode mnode = (MethodInsnNode) node;
			return Printer.OPCODES[node.getOpcode()] + " " + mnode.owner + " " + mnode.name + " " + mnode.desc;
		}
		case AbstractInsnNode.INVOKE_DYNAMIC_INSN: {
			IDynamicInsnNode idnode = (IDynamicInsnNode) node;
			return Printer.OPCODES[node.getOpcode()] + " " + idnode.name + " " + idnode.desc + " " + idnode.bsm + " " + idnode.bsmArgs;
		}
		case AbstractInsnNode.JUMP_INSN: {
			IfInsnNode inode = (IfInsnNode) node;
			return Printer.OPCODES[node.getOpcode()] + " " + inode.jumpLabel + ((inode.nextLabel == null) ? "" : " " + inode.nextLabel);
		}
		case AbstractInsnNode.LABEL: {
			TraceLabelNode lnode = (TraceLabelNode) node;
			return "LABEL " + lnode.getTraceLabel(); // + " " + lnode.getSpecialType();
		}
		case AbstractInsnNode.LDC_INSN:
			return Printer.OPCODES[node.getOpcode()] + " " + ((LdcInsnNode) node).cst;
		case AbstractInsnNode.IINC_INSN: {
			IincInsnNode inode = (IincInsnNode) node;
			return Printer.OPCODES[node.getOpcode()] + " " + inode.var + " " + inode.incr;
		}
		case AbstractInsnNode.TABLESWITCH_INSN: {
			TSwitchNode tsnode = (TSwitchNode) node;
			return Printer.OPCODES[node.getOpcode()] + " " + tsnode.min 
					+ " " + tsnode.max + " " + tsnode.dflt + " " + tsnode.labels;
		}
		case AbstractInsnNode.LOOKUPSWITCH_INSN: {
			LSwitchNode lsnode = (LSwitchNode) node;
			return Printer.OPCODES[node.getOpcode()] + " " + lsnode.dflt 
					+ " " + lsnode.keys + " " + lsnode.labels;
		}
		case AbstractInsnNode.MULTIANEWARRAY_INSN: {
			MultiANewArrayInsnNode mananode = (MultiANewArrayInsnNode) node;
			return Printer.OPCODES[node.getOpcode()] + " " + mananode.desc + " " + mananode.dims;
		}
//		case BeginInsnNode.BEGIN:
//			return ((BeginInsnNode) node).name;
		default:
//			return opcode;
			throw new IllegalArgumentException("AbstractInsnNode type: " + node.getType());	
		}
	}
}
