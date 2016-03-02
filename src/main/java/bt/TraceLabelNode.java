package bt;

import org.objectweb.asm.tree.LabelNode;

public class TraceLabelNode extends LabelNode {

	private int labelHash;
	private String traceLabel;
	
	public TraceLabelNode(int labelHash) {
		super();
		this.labelHash = labelHash;
	}
	
	public TraceLabelNode(String traceLabel) {
		super();
		this.traceLabel = traceLabel;
	}
	
	public int getLabelHash() {
		return labelHash;
	}
	
	public String getTraceLabel() {
		return traceLabel;
	}
}
