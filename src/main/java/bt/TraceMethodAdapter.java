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
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

public class TraceMethodAdapter extends MethodNode implements Opcodes {

	private String className;
	private String traceCollector;
	
	// Maps hash values to label strings (methodName + runningNumber)
	private Map<Integer, String> labels = new HashMap<>();
	
	private boolean tracesStub = false;
	
	public TraceMethodAdapter(int access, String name, String desc,
			String signature, String[] exceptions, MethodVisitor mv, 
			String className, String traceCollector) {
		super(ASM5, access, name, desc, signature, exceptions);
		this.mv = mv;
		this.className = className;
		this.traceCollector = traceCollector;
	}
	
	public void setTracesStub(boolean tracesStub) {
		this.tracesStub = tracesStub;
	}
	
	@Override
    public void visitTryCatchBlock(Label start, Label end,
            Label handler, String type) {
		if (tracesStub) {
			if (type != null && type.startsWith("java/")) {
				if (!type.equals("java/lang/Throwable"))
					type = "stub/" + type;
			}
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
		
		// Explicitly calls the static initializer
		if (name.equals("<clinit>")) {
			InsnList list = new InsnList();
			list.add(new LdcInsnNode(INVOKESTATIC));
			list.add(new LdcInsnNode(className));
			list.add(new LdcInsnNode("<clinit>"));
			list.add(new LdcInsnNode("()V"));
			list.add(new InsnNode(ICONST_0));
			list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitMethodInsn",
					"(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", false));
			
			instructions.insert(list);
		}
		
		// Injects tracing instructions
		AbstractInsnNode node = instructions.getFirst();
		while (node != null) {
			switch (node.getType()) {
			// 0
			case AbstractInsnNode.INSN:
				node = visitInsn((InsnNode) node);
				break;
			// 1
			case AbstractInsnNode.INT_INSN:
				node = visitIntInsn((IntInsnNode) node);
				break;
			// 2
			case AbstractInsnNode.VAR_INSN:
				node = visitVarInsn((VarInsnNode) node);
				break;
			// 3
			case AbstractInsnNode.TYPE_INSN:
				node = visitTypeInsn((TypeInsnNode) node);
				break;
			// 4
			case AbstractInsnNode.FIELD_INSN:
				node = visitFieldInsn((FieldInsnNode) node);
				break;
			// 5
			case AbstractInsnNode.METHOD_INSN:
				node = visitMethodInsn((MethodInsnNode) node);
				break;
			// 6
			case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
				node = visitInvokeDynamicInsn((InvokeDynamicInsnNode) node);
				break;
			// 7
			case AbstractInsnNode.JUMP_INSN:
				node = visitJumpInsn((JumpInsnNode) node);
				break;
			// 8
			case AbstractInsnNode.LABEL:
				node = visitLabel((LabelNode) node);
				break;
			// 9
			case AbstractInsnNode.LDC_INSN:
				node = visitLdc((LdcInsnNode) node);
				break;
			// 10
			case AbstractInsnNode.IINC_INSN:
				node = visitIincInsn((IincInsnNode) node);
				break;
			// 11
			case AbstractInsnNode.TABLESWITCH_INSN:
				node = visitTableSwitchInsn((TableSwitchInsnNode) node);
				break;
			// 12
			case AbstractInsnNode.LOOKUPSWITCH_INSN:
				node = visitLookupSwitchInsn((LookupSwitchInsnNode) node);
				break;
			// 13
			case AbstractInsnNode.MULTIANEWARRAY_INSN:
				node = visitMultiANewArrayInsn((MultiANewArrayInsnNode) node);
				break;
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
//			case AbstractInsnNode.TYPE_INSN:
//				TypeInsnNode tiNode = (TypeInsnNode) node;
//				tiNode.desc = replaceStub(tiNode.desc);
//				break;
//			case AbstractInsnNode.FIELD_INSN:
//				FieldInsnNode fiNode = (FieldInsnNode) node;
//				fiNode.owner = replaceStub(fiNode.owner);
//				fiNode.desc = replaceStub(fiNode.desc);
//				break;
//			case AbstractInsnNode.METHOD_INSN:
//				MethodInsnNode miNode = (MethodInsnNode) node;
//				miNode.owner = replaceStub(miNode.owner);
//				miNode.desc = replaceStub(miNode.desc);
//				break;
//			case AbstractInsnNode.FRAME:
//				visitFrame((FrameNode) node);
//				break;
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
	
	private AbstractInsnNode visitInsn(InsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitInsn",
                "(I)V", false));
		instructions.insertBefore(node, list);
		return node;
	}
	
	private AbstractInsnNode visitIntInsn(IntInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.operand));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitIntInsn",
                "(II)V", false));
		instructions.insertBefore(node, list);
		return node;
	}
	
	private AbstractInsnNode visitVarInsn(VarInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.var));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitVarInsn",
                "(II)V", false));
		instructions.insertBefore(node, list);
		return node;
	}
	
	// NEW, ANEWARRAY, CHECKCAST or INSTANCEOF
	private AbstractInsnNode visitTypeInsn(TypeInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.desc));
		AbstractInsnNode lastNode = new MethodInsnNode(INVOKESTATIC, traceCollector, "visitTypeInsn",
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
			// Inserts after the node (not before the node) 
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
		AbstractInsnNode lastNode = new MethodInsnNode(INVOKESTATIC, traceCollector, "visitFieldInsn",
				"(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
		list.add(lastNode);
		
		instructions.insert(node, list);
		return lastNode;
	}
	
	private AbstractInsnNode visitMethodInsn(MethodInsnNode node) {	
		if (node.owner.startsWith("bt") && !node.owner.startsWith("bt/runtime/"))
			return node;
		
//		if (node.owner.equals("java/lang/Object")
//				&& node.name.equals("<init>")
//				&& node.desc.equals("()V")) {
//			InsnList list = new InsnList();
//			list.add(new LdcInsnNode(POP));
//			list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitInsn",
//	                "(I)V", false));
//			return list;
//		}
//		
//		if (node.owner.equals("java/lang/Object")
//				&& node.name.equals("getClass")
//				&& node.desc.equals("()Ljava/lang/Class;")) {
//			InsnList list = new InsnList();
//			list.add(new LdcInsnNode(BIPUSH));
//			list.add(new LdcInsnNode(-2));
//			list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitIntInsn",
//	                "(II)V", false));
//			list.add(new LdcInsnNode(IALOAD));
//			list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitInsn",
//	                "(I)V", false));
//			return list;
//		}
//		
//		if (node.owner.equals("java/lang/Class")
//				&& node.name.equals("desiredAssertionStatus")
//				&& node.desc.equals("()Z")) {
//			InsnList list = new InsnList();
//			list.add(new LdcInsnNode(POP));
//			list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitInsn",
//	                "(I)V", false));
//			list.add(new LdcInsnNode(ICONST_0));
//			list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitInsn",
//	                "(I)V", false));
//			return list;
//		}
//		
//		if (node.owner.equals("java/lang/Integer")
//				&& node.name.equals("valueOf")
//				&& node.desc.equals("(I)Ljava/lang/Integer;")) {
//			return null;
//		}
//		
//		if (node.owner.equals("java/lang/Integer")
//				&& node.name.equals("intValue")
//				&& node.desc.equals("()I")) {
//			return null;
//		}
//		
//		if (node.owner.equals("java/lang/System")
//				&& node.name.equals("arraycopy")
//				&& node.desc.equals("(Ljava/lang/Object;ILjava/lang/Object;II)V")) {
//			node.owner = "bt/runtime/System";
//		}
//		
//		if (node.owner.equals("java/lang/String")
//				&& node.name.equals("getBytes")
//				&& node.desc.equals("()[B")) {
//			InsnList list = new InsnList();
//			list.add(new LdcInsnNode(NEWARRAY));
//			list.add(new LdcInsnNode(T_BYTE));
//			list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitIntInsn",
//	                "(II)V", false));
//			return list;
//		}
		
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.getOpcode()));
		list.add(new LdcInsnNode(node.owner));
		list.add(new LdcInsnNode(node.name));
		list.add(new LdcInsnNode(node.desc));
		list.add(new InsnNode((node.itf) ? ICONST_1 : ICONST_0));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitMethodInsn",
				"(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V", false));
		instructions.insert(node, list);
		return node;
	}
	
	private AbstractInsnNode visitInvokeDynamicInsn(InvokeDynamicInsnNode node) {	
		StringBuilder b = new StringBuilder();
		if (node.bsmArgs != null) {
			for (int i = 0; i < node.bsmArgs.length; i++) {
				if (i > 0)
					b.append('|');
				b.append(node.bsmArgs[i].toString());
			}
		}
		
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.name));
		list.add(new LdcInsnNode(node.desc));
		list.add(new LdcInsnNode(node.bsm.toString()));
		list.add(new LdcInsnNode(b.toString()));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitInvokeDynamicInsn",
				"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false));
		instructions.insert(node, list);
		return node;
	}
	
	// No stubs for these classes
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
		// Hashes jump label
		LabelNode jumpLabelNode = node.label;
		int jumpLabelHash = System.identityHashCode(jumpLabelNode.getLabel());
		
		// Adds jump label to trace collector
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(labels.get(jumpLabelHash)));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "addLabel",
		    "(Ljava/lang/String;)V", false));
		
		// No next label for GOTO
		if (node.getOpcode() == GOTO) {
			list.add(new LdcInsnNode(node.getOpcode()));
        	list.add(new LdcInsnNode(labels.get(jumpLabelHash)));
        	list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitIfInsn",
    				"(ILjava/lang/String;)V", false));
        	// Inserts trace
    		instructions.insertBefore(node, list);
    		return node;
		}

		// If the next instruction after IF is not a label, create one
		AbstractInsnNode lastNode = node;
		AbstractInsnNode nextLabelNode = node.getNext();
		int nextLabelHash;
		if (nextLabelNode != null && nextLabelNode.getType() == AbstractInsnNode.LABEL) {
			nextLabelHash = System.identityHashCode(((LabelNode) nextLabelNode).getLabel());
		} else {
			nextLabelNode = new LabelNode();
			nextLabelHash = System.identityHashCode(((LabelNode) nextLabelNode).getLabel());
			
			InsnList l = new InsnList();
			l.add(nextLabelNode);
			l.add(new LdcInsnNode(addTraceLabel(nextLabelHash)));
			lastNode = new MethodInsnNode(INVOKESTATIC, traceCollector, "visitLabel",
	                "(Ljava/lang/String;)V", false);
			l.add(lastNode);
			
			// Insert new label after the node
			instructions.insert(node, l);
		}
		
		// Adds next label
		list.add(new LdcInsnNode(labels.get(nextLabelHash)));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "addLabel",
                "(Ljava/lang/String;)V", false));
		
		// Traces if-statements
		list.add(new LdcInsnNode(node.getOpcode()));
    	list.add(new LdcInsnNode(labels.get(jumpLabelHash)));
    	list.add(new LdcInsnNode(labels.get(nextLabelHash)));
    	list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitIfInsn",
				"(ILjava/lang/String;Ljava/lang/String;)V", false));
    	
    	// Insert trace
    	instructions.insertBefore(node, list);

    	return lastNode;
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
		list.add(new LdcInsnNode(labels.get(labelHash)));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitLabel",
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
	
	private AbstractInsnNode visitLdc(LdcInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.cst.toString()));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitLdcInsn",
                "(Ljava/lang/String;)V", false));
		instructions.insertBefore(node, list);
		return node;
	}
	
	private AbstractInsnNode visitIincInsn(IincInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.var));
		list.add(new LdcInsnNode(node.incr));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitIincInsn",
                "(II)V", false));
		instructions.insertBefore(node, list);
		return node;
	}
	
	InsnList printInsnList(int opcode) {
		InsnList list = new InsnList();
		list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;"));
		list.add(new LdcInsnNode(Printer.OPCODES[opcode]));
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                "(Ljava/lang/String;)V", false));
		return list;
	}
	
	// TODO
	private AbstractInsnNode visitTableSwitchInsn(TableSwitchInsnNode node) {
		StringBuilder l = new StringBuilder();
		for (int i = 0; i < node.labels.size(); i++) {
			if (i > 0)
				l.append('|');
			l.append(labels.get(System.identityHashCode(((LabelNode)node.labels.get(i)).getLabel())));
		}
		
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.min));
		list.add(new LdcInsnNode(node.max));
		list.add(new LdcInsnNode(labels.get(System.identityHashCode(node.dflt.getLabel()))));
		list.add(new LdcInsnNode(l.toString()));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitTableSwitchInsn",
	                "(IILjava/lang/String;Ljava/lang/String;)V", false));
		instructions.insertBefore(node, list);
		return node;
	}
	
	private AbstractInsnNode visitLookupSwitchInsn(LookupSwitchInsnNode node) {
		StringBuilder k = new StringBuilder();
		StringBuilder l = new StringBuilder();
		for (int i = 0; i < node.keys.size(); i++) {
			if (i > 0) {
				k.append('|');
				l.append('|');
			}
			k.append(node.keys.get(i));
			l.append(labels.get(System.identityHashCode(((LabelNode)node.labels.get(i)).getLabel())));
		}

		InsnList list = new InsnList();
		list.add(new LdcInsnNode(labels.get(System.identityHashCode(node.dflt.getLabel()))));
		list.add(new LdcInsnNode(k.toString()));
		list.add(new LdcInsnNode(l.toString()));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitLookupSwitchInsn",
	                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false));
		instructions.insertBefore(node, list);
		return node;
	}
	
	private AbstractInsnNode visitMultiANewArrayInsn(MultiANewArrayInsnNode node) {
		InsnList list = new InsnList();
		list.add(new LdcInsnNode(node.desc));
		list.add(new LdcInsnNode(node.dims));
		list.add(new MethodInsnNode(INVOKESTATIC, traceCollector, "visitMultiANewArrayInsn",
	                "(Ljava/lang/String;I)V", false));
		instructions.insertBefore(node, list);
		return node;
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
