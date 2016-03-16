package bt;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

public class TSwitchNode extends AbstractInsnNode {
	
	public int min;
	public int max;
	public String dflt;
	public String labels;
	
	public TSwitchNode(int min, int max, String dflt, String labels) {
		super(Opcodes.TABLESWITCH);
		this.min = min;
		this.max = max;
		this.dflt = dflt;
		this.labels = labels;
	}

	@Override
	public int getType() {
		return TABLESWITCH_INSN;
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
