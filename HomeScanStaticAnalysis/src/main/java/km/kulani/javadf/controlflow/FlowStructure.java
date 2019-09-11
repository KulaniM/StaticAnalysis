package km.kulani.javadf.controlflow;

import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author root FlowStructure is the actual method call flow of either backward
 *         or forward. It is a list of FlowEntries (methods).
 *         E.g.: for the setKey method the backward flow
 *         setKey(java.lang.String)=[km.kulani.javadf.controlflow.FlowEntry@3590fc5b,
 *         km.kulani.javadf.controlflow.FlowEntry@397fbdb]
 *         Meaningful e.g.:
 *         setKey(java.lang.String)=[encrypt(java.lang.String,java.lang.String)/simple2.Class,
 *         decrypt(java.lang.String,java.lang.String)/simple2.Class]
 */

public class FlowStructure {

	private HashMap<String, List<FlowEntry>> forwardflow;
	private HashMap<String, List<FlowEntry>> backwardflow;

	public HashMap<String, List<FlowEntry>> getForwardflow() {
		return forwardflow;
	}

	public void setForwardflow(HashMap<String, List<FlowEntry>> forwardflow) {
		this.forwardflow = forwardflow;
	}

	public HashMap<String, List<FlowEntry>> getBackwardflow() {
		return backwardflow;
	}

	public void setBackwardflow(HashMap<String, List<FlowEntry>> backwardflow) {
		this.backwardflow = backwardflow;
	}

}
