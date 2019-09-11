package km.kulani.javadf.semantics;

import java.util.HashMap;
import com.github.javaparser.ast.Node;
import km.kulani.javadf.domain.types.TypeDetails;

/**
 * 
 * @author root The summary of the currently visited/analyzed method by the
 *         MethodAnalyser.java
 *
 */

public class MethodSummary {

	private String className;
	private Node methodNode;
	private String methodname;
	private TypeDetails returnStmt;
	private HashMap<String, TypeDetails> inputParameters = new HashMap<>();

	public Node getMethodNode() {
		return methodNode;
	}

	public void setMethodNode(Node methodNode) {
		this.methodNode = methodNode;
	}

	public String getMethodname() {
		return methodname;
	}

	public void setMethodname(String methodname) {
		this.methodname = methodname;
	}

	public TypeDetails getReturnStmt() {
		return returnStmt;
	}

	public void setReturnStmt(TypeDetails returnStmt) {
		this.returnStmt = returnStmt;
	}

	public HashMap<String, TypeDetails> getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(HashMap<String, TypeDetails> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
