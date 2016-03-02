package bt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

public class TraceMethodAdapter extends MethodNode implements Opcodes {

private String className;
	
	private Map<Integer, String> labels = new HashMap<>();
	
	public TraceMethodAdapter(int access, String name, String desc,
			String signature, String[] exceptions, MethodVisitor mv, 
			String className) {
		super(ASM5, access, name, desc, signature, exceptions);
		this.mv = mv;
		this.className = className;
	}
	
	@Override
    public void visitTryCatchBlock(Label start, Label end,
            Label handler, String type) {
		if (type != null && type.startsWith("java/")) {
			if (!type.equals("java/lang/Throwable"))
				type = "stub/" + type;
		}
		super.visitTryCatchBlock(start, end, handler, type);
    }
	
	@Override
	public void visitEnd() {
		preprocess();
		
		if (isNoTrace()) {
			accept(mv);
			return;
		}
		
		AbstractInsnNode node = instructions.getFirst();
		
		if (name.equals("<clinit>")) {
			InsnList list = new InsnList();
			list.add(new LdcInsnNode(INVOKESTATIC));
			list.add(new LdcInsnNode(className));
			list.add(new LdcInsnNode("<clinit>"));
			list.add(new LdcInsnNode("()V"));
			list.add(new InsnNode(ICONST_0));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitMethodInsn",
					"(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", false));
			
			instructions.insert(list);
		}
		
		while (node != null) {
			InsnList insertedInsnList = null;
			switch (node.getType()) {
			case AbstractInsnNode.INSN:
				insertedInsnList = visitInsn((InsnNode) node);
				break;
			case AbstractInsnNode.INT_INSN:
				insertedInsnList = visitIntInsn((IntInsnNode) node);
				break;
			case AbstractInsnNode.VAR_INSN:
				insertedInsnList = visitVarInsn((VarInsnNode) node);
				break;
			case AbstractInsnNode.TYPE_INSN:
				node = visitTypeInsn((TypeInsnNode) node);
				break;
			case AbstractInsnNode.FIELD_INSN:
				node = visitFieldInsn((FieldInsnNode) node);
				break;
			case AbstractInsnNode.METHOD_INSN:
				insertedInsnList = visitMethodInsn((MethodInsnNode) node);
				break;
			case AbstractInsnNode.JUMP_INSN:
				node = visitJumpInsn((JumpInsnNode) node);
				break;
			case AbstractInsnNode.LABEL:
				node = visitLabel((LabelNode) node);
				break;
			case AbstractInsnNode.LDC_INSN:
				insertedInsnList = visitLdc((LdcInsnNode) node);
				break;
			case AbstractInsnNode.IINC_INSN:
				insertedInsnList = visitIincInsn((IincInsnNode) node);
				break;
			}
			
			if (insertedInsnList != null) {
				AbstractInsnNode previous = node.getPrevious();
				if (previous == null)
					instructions.insert(insertedInsnList);
				else
					instructions.insert(previous, insertedInsnList);
			}
			
			if (node != null)
				node = node.getNext();
		}
		
		maxStack += 5;
		
		accept(mv);
	}
	
	private void preprocess() {
		ListIterator<AbstractInsnNode> itr = instructions.iterator();
		while (itr.hasNext()) {
			AbstractInsnNode node = itr.next();
			switch (node.getType()) {
			case AbstractInsnNode.LABEL:
				addTraceLabel(System.identityHashCode(((LabelNode) node).getLabel()));
				break;
				
			case AbstractInsnNode.INSN: {
				// Hack the heap
//				if (node.getOpcode() == RETURN 
//						&& className.equals("bt/runtime/ByteBuffer")
//						&& name.equals("<init>")
//						&& heap != null) {
//					
//					InsnList list = new InsnList();
//					for (int i = 0; i < heap.length; i++) {
//						int index = heap[i][0] - 13;
//						if (index < 0 || index >= 65536)
//							continue;
//						
//						list.add(new VarInsnNode(ALOAD, 0));
//						list.add(new FieldInsnNode(GETFIELD, "bt/runtime/ByteBuffer", "in", "[I"));
//						list.add(new LdcInsnNode(index));
//						list.add(new LdcInsnNode(heap[i][1]));
//						list.add(new InsnNode(IASTORE));
//					}
//					if (list.size() > 0)
//						instructions.insertBefore(node, list);
//				}
				break;
			}
			
			// Handle stubs
			case AbstractInsnNode.TYPE_INSN:
				TypeInsnNode tiNode = (TypeInsnNode) node;
				tiNode.desc = replaceStub(tiNode.desc);
				break;
			case AbstractInsnNode.FIELD_INSN:
				FieldInsnNode fiNode = (FieldInsnNode) node;
				fiNode.owner = replaceStub(fiNode.owner);
				fiNode.desc = replaceStub(fiNode.desc);
				break;
			case AbstractInsnNode.METHOD_INSN:
				MethodInsnNode miNode = (MethodInsnNode) node;
				miNode.owner = replaceStub(miNode.owner);
				miNode.desc = replaceStub(miNode.desc);
				break;
			case AbstractInsnNode.FRAME:
				visitFrame((FrameNode) node);
				break;
			}
		}
	}
	
//	private void insertTrace(AbstractInsnNode node) {
////		if (node.getOpcode() > 0)
////			System.out.println(Printer.OPCODES[node.getOpcode()]);
//		
//		if (name.equals("arraycopyInternal") && desc.equals("([II[III)V")) {
//			if (node.getType() == AbstractInsnNode.LABEL)
//				System.out.println("LABEL " + System.identityHashCode(((LabelNode) node).getLabel()));
//			else
//				if (node.getOpcode() > 0)
//					System.out.println(Printer.OPCODES[node.getOpcode()]);
//		}
//		
//		
//	}
	
	private boolean isNoTrace() {
		if (className.equals("java/lang/Math") && name.equals("<clinit>"))
			return true;
		
		if (className.equals("java/lang/Math") && name.equals("powerOfTwoD") && desc.equals("(I)D"))
			return true;
		
		return false;
	}
	
	private InsnList visitInsn(InsnNode node) {
		InsnList list = new InsnList();
		
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitInsn",
                "(I)V", false));
		return list;
	}
	
	private InsnList visitIntInsn(IntInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.operand));
		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitIntInsn",
                "(II)V", false));
		return list;
	}
	
	private InsnList visitVarInsn(VarInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.var));
		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitVarInsn",
                "(II)V", false));
		return list;
	}
	
	private AbstractInsnNode visitTypeInsn(TypeInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.desc));
		AbstractInsnNode lastNode = new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitTypeInsn",
				"(ILjava/lang/String;)V", false);
		list.add(lastNode);
		
		AbstractInsnNode prevNode = node.getPrevious();
		if (prevNode != null 
				&& prevNode.getOpcode() == INVOKESTATIC 
				&& ((MethodInsnNode) prevNode).name.equals("visitLabel")) {
			// If the previous node is INVOKESTATIC visitLabel, we need to reorganize the list
			// to prevent StackMapError because NEW must immediately follow LABEL
			// From: LABEL, LDC LabelHash, INVOKESTATIC visitLabel, NEW
			// To: LABEL, NEW, LDC LabelHash, INVOKESTATIC visitLabel
			instructions.remove(node);
			instructions.insertBefore(prevNode.getPrevious(), node);
			instructions.insert(prevNode, list);
		} else {
			// Insert after the node (not before the node) 
			// This is to prevent the error with frame that must point to NEW
			instructions.insert(node, list);
		}
		
		return lastNode;
	}
	
	private AbstractInsnNode visitFieldInsn(FieldInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.owner));
		list.add(new LdcInsnNode(node.name));
		list.add(new LdcInsnNode(node.desc));
		AbstractInsnNode lastNode = new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitFieldInsn",
				"(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
		list.add(lastNode);
		
		instructions.insert(node, list);
		return lastNode;
	}
	
	private InsnList visitMethodInsn(MethodInsnNode node) {	
		if (node.owner.startsWith("bt") && !node.owner.startsWith("bt/runtime/"))
			return null;
		
		if (node.owner.equals("java/lang/Object")
				&& node.name.equals("<init>")
				&& node.desc.equals("()V")) {
			InsnList list = new InsnList();
			list.add(new LdcInsnNode(POP));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitInsn",
	                "(I)V", false));
			return list;
		}
		
		if (node.owner.equals("java/lang/Object")
				&& node.name.equals("getClass")
				&& node.desc.equals("()Ljava/lang/Class;")) {
			InsnList list = new InsnList();
			list.add(new LdcInsnNode(BIPUSH));
			list.add(new LdcInsnNode(-2));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitIntInsn",
	                "(II)V", false));
			list.add(new LdcInsnNode(IALOAD));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitInsn",
	                "(I)V", false));
			return list;
		}
		
		if (node.owner.equals("java/lang/Class")
				&& node.name.equals("desiredAssertionStatus")
				&& node.desc.equals("()Z")) {
			InsnList list = new InsnList();
			list.add(new LdcInsnNode(POP));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitInsn",
	                "(I)V", false));
			list.add(new LdcInsnNode(ICONST_0));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitInsn",
	                "(I)V", false));
			return list;
		}
		
		if (node.owner.equals("java/lang/Integer")
				&& node.name.equals("valueOf")
				&& node.desc.equals("(I)Ljava/lang/Integer;")) {
			return null;
		}
		
		if (node.owner.equals("java/lang/Integer")
				&& node.name.equals("intValue")
				&& node.desc.equals("()I")) {
			return null;
		}
		
		if (node.owner.equals("java/lang/System")
				&& node.name.equals("arraycopy")
				&& node.desc.equals("(Ljava/lang/Object;ILjava/lang/Object;II)V")) {
			node.owner = "bt/runtime/System";
		}
		
		if (node.owner.equals("java/lang/String")
				&& node.name.equals("getBytes")
				&& node.desc.equals("()[B")) {
			InsnList list = new InsnList();
//			list.add(new LdcInsnNode(POP));
//			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitInsn",
//	                "(I)V", false));
//			list.add(new LdcInsnNode(ICONST_0));
//			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitInsn",
//	                "(I)V", false));
			list.add(new LdcInsnNode(NEWARRAY));
			list.add(new LdcInsnNode(T_BYTE));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitIntInsn",
	                "(II)V", false));
			return list;
		}
		
//		if (node.owner.equals("java/nio/ByteBuffer")
//				&& node.name.equals("allocateDirect")
//				&& node.desc.equals("(I)Ljava/nio/ByteBuffer;")) {
//			node.owner = "bt/runtime/ByteBuffer";
//			node.desc = "(I)Lbt/runtime/ByteBuffer;";
//		}
		
//		if (node.owner.equals("lljvm/lib/c")
//				&& node.name.equals("malloc")
//				&& node.desc.equals("(I)I")) {
//		}
		
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.owner));
		list.add(new LdcInsnNode(node.name));
		list.add(new LdcInsnNode(node.desc));
		list.add(new InsnNode((node.itf) ? ICONST_1 : ICONST_0));
		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitMethodInsn",
				"(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", false));
		return list;
	}
	
	private static final Set<String> NO_STUBS = new HashSet<String>();
	
	static {
		NO_STUBS.add("java/lang/Object");
		NO_STUBS.add("java/lang/Class");
		NO_STUBS.add("java/lang/Throwable");
		NO_STUBS.add("java/lang/String");
		NO_STUBS.add("java/lang/Boolean");
		NO_STUBS.add("java/lang/Character");
		NO_STUBS.add("java/lang/Byte");
		NO_STUBS.add("java/lang/Short");
		NO_STUBS.add("java/lang/Integer");
		NO_STUBS.add("java/lang/Long");
		NO_STUBS.add("java/lang/Float");
		NO_STUBS.add("java/lang/Double");
		NO_STUBS.add("java/lang/System");
	}
	
	public static String replaceStub(String name) {
		name = name.replaceAll("java/nio/ByteBuffer", "bt/runtime/ByteBuffer");
		name = name.replaceAll("java/nio/ByteOrder", "bt/runtime/ByteOrder");
		name = name.replaceAll("lljvm/lib/c", "bt/runtime/c");
		
		if (NO_STUBS.contains(name))
			return name;
		
		name = name.replaceAll("java/", "stub/java/");
		for (String noStub : NO_STUBS) {
			name = name.replaceAll("Lstub/" + noStub + ";", "L" + noStub + ";");
		}
		return name;
	}
	
	private AbstractInsnNode visitJumpInsn(JumpInsnNode node) {
		// Do not trace GOTO
		if (node.getOpcode() == GOTO) {
			return node;
		}
		
		AbstractInsnNode retNode = node;
		
		// Jump label
		LabelNode jumpLabelNode = node.label;
		int jumpLabelHash = System.identityHashCode(jumpLabelNode.getLabel());
		
		// If the next instruction after IF is not a label, create one
		AbstractInsnNode nextLabelNode = node.getNext();
		int nextLabelHash;
		if (nextLabelNode.getType() == AbstractInsnNode.LABEL) {
			nextLabelHash = System.identityHashCode(((LabelNode) nextLabelNode).getLabel());
		} else {
			nextLabelNode = new LabelNode();
			nextLabelHash = System.identityHashCode(((LabelNode) nextLabelNode).getLabel());
			
			InsnList l = new InsnList();
			l.add(nextLabelNode);
//			l.add(new LdcInsnNode(System.identityHashCode(((LabelNode) nextLabelNode).getLabel())));
//			retNode = new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLabel",
//	                "(I)V", false);
			l.add(new LdcInsnNode(addTraceLabel(nextLabelHash)));
			retNode = new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLabel",
	                "(Ljava/lang/String;)V", false);
			l.add(retNode);
			
			// Insert new label after the node
			instructions.insert(node, l);
		}
		
		// Add both jump label and next label to TraceCollector
		InsnList list = new InsnList();
//		list.add(new LdcInsnNode(jumpLabelHash));
//		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "addLabel",
//                "(I)V", false));
//		list.add(new LdcInsnNode(nextLabelHash));
//		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "addLabel",
//                "(I)V", false));
		list.add(new LdcInsnNode(labels.get(jumpLabelHash)));
		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "addLabel",
                "(Ljava/lang/String;)V", false));
		list.add(new LdcInsnNode(labels.get(nextLabelHash)));
		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "addLabel",
                "(Ljava/lang/String;)V", false));
		
		// Trace if-statements
		switch (node.getOpcode()) {
        case Opcodes.IFEQ:
        case Opcodes.IFNE:
        case Opcodes.IFLT:
        case Opcodes.IFGE:
        case Opcodes.IFGT:
        case Opcodes.IFLE:
        	list.add(new InsnNode(DUP));
        	list.add(new LdcInsnNode(node.getOpcode()));
        	list.add(new LdcInsnNode(labels.get(jumpLabelHash)));
        	list.add(new LdcInsnNode(labels.get(nextLabelHash)));
        	list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitIfInsn",
    				"(IILjava/lang/String;Ljava/lang/String;)V", false));
            break;

        case Opcodes.IF_ICMPEQ:
        case Opcodes.IF_ICMPNE:
        case Opcodes.IF_ICMPLT:
        case Opcodes.IF_ICMPGE:
        case Opcodes.IF_ICMPGT:
        case Opcodes.IF_ICMPLE:
        	list.add(new InsnNode(DUP2));
        	list.add(new LdcInsnNode(node.getOpcode()));
        	list.add(new LdcInsnNode(labels.get(jumpLabelHash)));
        	list.add(new LdcInsnNode(labels.get(nextLabelHash)));
        	list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitIfIcmpInsn",
    				"(IIILjava/lang/String;Ljava/lang/String;)V", false));
            break;
            
        case Opcodes.IF_ACMPEQ:
        case Opcodes.IF_ACMPNE:
        	list.add(new InsnNode(DUP2));
        	list.add(new LdcInsnNode(node.getOpcode()));
        	list.add(new LdcInsnNode(labels.get(jumpLabelHash)));
        	list.add(new LdcInsnNode(labels.get(nextLabelHash)));
        	list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitIfAcmpInsn",
    				"(Ljava/lang/Object;Ljava/lang/Object;ILjava/lang/String;Ljava/lang/String;)V", false));
            break;
            
        case Opcodes.IFNULL:
        case Opcodes.IFNONNULL:
        	list.add(new InsnNode(DUP));
        	list.add(new LdcInsnNode(node.getOpcode()));
        	list.add(new LdcInsnNode(labels.get(jumpLabelHash)));
        	list.add(new LdcInsnNode(labels.get(nextLabelHash)));
        	list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitIfInsn",
    				"(Ljava/lang/Object;ILjava/lang/String;Ljava/lang/String;)V", false));
        	break;

        case Opcodes.JSR:	// TODO
            break;
        
        default:
            throw new IllegalArgumentException();
        }
		
		// Insert trace
		instructions.insertBefore(node, list);
		
		return retNode;
	}
	
	private AbstractInsnNode visitLabel(LabelNode node) {
		// Finds next bytecode instruction
		AbstractInsnNode nextInsnNode = node;
		do {
			nextInsnNode = nextInsnNode.getNext();
			if (nextInsnNode != null && nextInsnNode.getType() == AbstractInsnNode.FRAME)
				visitFrame((FrameNode) nextInsnNode);
		} while (nextInsnNode != null && nextInsnNode.getOpcode() < 0);
		
		// If no instructions follow the label, do nothing
		if (nextInsnNode == null)
			return null;
		
		// Statically collects the label
		int labelHash = System.identityHashCode(((LabelNode) node).getLabel());
		
		// Creates new instructions that will dynamically remove the label
		InsnList list = new InsnList();
//		list.add(new LdcInsnNode(labelHash));
//		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLabel",
//                "(I)V", false));
		list.add(new LdcInsnNode(labels.get(labelHash)));
		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLabel",
                "(Ljava/lang/String;)V", false));
		
		// Inserts new instructions just before the next instruction
		instructions.insertBefore(nextInsnNode, list);
		
		return nextInsnNode.getPrevious();
	}
	
	private String addTraceLabel(int labelHash) {
		String l = className + "." + name + desc + labels.size();
		labels.put(labelHash, l);
		return l;
	}
	
	private InsnList visitLdc(LdcInsnNode node) {
		InsnList list = new InsnList();
		
		
		if (node.cst instanceof Integer) {
			list.add(new LdcInsnNode(node.cst));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLdcInsn",
	                "(I)V", false));
		} else if (node.cst instanceof Long) {
			list.add(new LdcInsnNode(node.cst));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLdcInsn",
	                "(J)V", false));
		} else if (node.cst instanceof Float) {
			list.add(new LdcInsnNode(node.cst));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLdcInsn",
	                "(F)V", false));
		} else if (node.cst instanceof Double) {
			list.add(new LdcInsnNode(node.cst));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLdcInsn",
	                "(D)V", false));
		} else if (node.cst instanceof Type) {
			list.add(new LdcInsnNode(node.cst));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLdcInsn",
	                "(Ljava/lang/Object;)V", false));
			
//            int sort = ((Type) node.cst).getSort();
//            if (sort == Type.OBJECT || sort == Type.ARRAY) {
//                return newValue(Type.getObjectType("java/lang/Class"));
//            } else if (sort == Type.METHOD) {
//                return newValue(Type
//                        .getObjectType("java/lang/invoke/MethodType"));
//            } else {
//                throw new IllegalArgumentException("Illegal LDC constant "
//                        + node.cst);
//            }
        } else if (node.cst instanceof String) {
			list.add(new LdcInsnNode(node.cst));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitLdcInsn",
	                "(Ljava/lang/String;)V", false));
		} else {
			// TODO Support other types of constant
			list.add(new LdcInsnNode(ICONST_0));
			list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitInsn",
	                "(I)V", false));
		}
		return list;
	}
	
	private InsnList visitIincInsn(IincInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.var));
		list.add(new LdcInsnNode(node.incr));
		list.add(new MethodInsnNode(INVOKESTATIC, "bt/TraceCollector", "visitIincInsn",
                "(II)V", false));
		return list;
	}
	
	void print(int opcode) {
		InsnList list = new InsnList();
		list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;"));
		list.add(new LdcInsnNode(Printer.OPCODES[opcode]));
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                "(Ljava/lang/String;)V", false));
		
//		instructions.insert(node.getPrevious(), list);
	}
	
	private void visitFrame(FrameNode node) {
		node.local = replaceStubs(node.local);
		node.stack = replaceStubs(node.stack);
	}
	
	private static List replaceStubs(List list) {
		if (list == null)
			return list;
		
		ListIterator itr = list.listIterator();
		outer: while (itr.hasNext()) {
			Object o = itr.next();
			if (!(o instanceof String))
				continue;
			String s = (String) o;
			s = s.replaceAll("java/nio/ByteBuffer", "bt/runtime/ByteBuffer");
			s = s.replaceAll("java/nio/ByteOrder", "bt/runtime/ByteOrder");
			s = s.replaceAll("lljvm/lib/c", "bt/runtime/c");
			
			if (s.contains("stub/java"))
				continue;
			if (NO_STUBS.contains(s))
				continue;
			for (String noStub : NO_STUBS) {
				if (s.contains("L" + noStub + ";"))
					continue outer;
			}
			
			itr.set(s.replaceAll("java/", "stub/java/"));
		}
		return list;
	}
}
