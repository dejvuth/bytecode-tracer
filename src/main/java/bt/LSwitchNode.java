package bt;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

public class LSwitchNode extends AbstractInsnNode {

	public String dflt;
	public String keys;
	public String labels;
	
	public LSwitchNode(final String dflt, final String keys, final String labels) {
		super(Opcodes.LOOKUPSWITCH);
		this.dflt = dflt;
		this.keys = keys;
		this.labels = labels;
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
        return LOOKUPSWITCH_INSN;
    }

}
