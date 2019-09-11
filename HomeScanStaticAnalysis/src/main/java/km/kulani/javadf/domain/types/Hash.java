package km.kulani.javadf.domain.types;

import java.util.HashMap;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;

import km.kulani.javadf.TreeAnalysis;

public class Hash extends DomainType{
	
	private static final String NAME = "hash";

	protected String instanceTypeString;
	protected String mode;
	protected Node referenceNode;	
	
	public Hash() {
		super();
	}
	public String getInstanceTypeString() {
		return instanceTypeString;
	}
	public void setInstanceTypeString(String instanceTypeString) {
		this.instanceTypeString = instanceTypeString;
		if (instanceTypeString.contains("SHA")) {
			setMode("sha");	
		} else if (instanceTypeString.contains("MD5")) {
			setMode("md5");			
		}				
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public Node getReferenceNode() {
		return referenceNode;
	}
	public void setReferenceNode(Node referenceNode) {
		this.referenceNode = referenceNode;
	}
	
	public static HashMap<String, DomainType> updateHashReferenceNode(MethodCallExpr mexpr, Node currentNode,
			HashMap<String, DomainType> domainHashMap) {

		//E.g: MessageDigest md = MessageDigest.getInstance("SHA-256");
		String instanceTypeString = mexpr.getArgument(0).toString();
		// Find the reference node which stores the instance
		List<Node> siblings = TreeAnalysis.getSibilings(currentNode);
		for (Node sib : siblings) {
			if (domainHashMap.containsKey(sib.toString())) {
				Hash hsh = (Hash) domainHashMap.get(sib.toString());
				hsh.setInstanceTypeString(instanceTypeString);
				hsh.setType(NAME+"(msg,"+hsh.getMode()+")");		
				
			}
		}
		return domainHashMap;
	}

	
}
