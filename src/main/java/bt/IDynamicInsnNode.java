package bt;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

public class IDynamicInsnNode extends AbstractInsnNode {
	
	public String name;
	public String desc;
	public String bsm;
	public String bsmArgs;
	
	public IDynamicInsnNode(final String name, final String desc,
			final String bsm, final String bsmArgs) {
		super(Opcodes.INVOKEDYNAMIC);
		this.name = name;
		this.desc = desc;
		this.bsm = bsm;
		this.bsmArgs = bsmArgs;
	}

	@Override
	public int getType() {
        return INVOKE_DYNAMIC_INSN;
	}

	@Override
	public void accept(MethodVisitor cv) {
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractInsnNode clone(Map labels) {
		// TODO Auto-generated method stub
		return null;
	}

}
