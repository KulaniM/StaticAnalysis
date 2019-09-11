package km.kulani.javadf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.TreeVisitor;

public class TreeAnalysis extends TreeVisitor {

	private static HashMap<String, Node> nodeMap = new HashMap<String, Node>();

	@Override
	public void process(Node node) {
		// System.out.println("Tree Visitor: " + node + " is ");
		nodeMap.put(node.toString(), node);
	}

	public HashMap<String, Node> getNodeList() {
		return nodeMap;
	}

	public void setNodeList(HashMap<String, Node> nodeList) {
		this.nodeMap = nodeList;
	}

	public static List<Node> getSibilings(Node node) {
		List<Node> sibilings = new ArrayList<>();
		Node parent = node.getParentNode().get();
		List<Node> parentsChildren = parent.getChildNodes();
		System.out.println("All children.............. =" + parentsChildren.size());
		for (Node child : parentsChildren) {
			if (child != node) {
				sibilings.add(child);
			}
		}
		System.out.println("All siblings.............. =" + sibilings.size());
		return sibilings;
	}

}
