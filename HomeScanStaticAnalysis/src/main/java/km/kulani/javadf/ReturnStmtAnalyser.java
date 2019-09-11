package km.kulani.javadf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.javaparser.ast.Node;

import km.kulani.javadf.domain.types.TypeDetails;

public class ReturnStmtAnalyser {

	public static LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updateReturnStatementType(
			String methodSignature, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap) {
		Node returnNode = null;
		TypeDetails updatedReturnNodeType = null;

		// return statement of the current method
		//// MAKE THIS WORK FOR A LIST OF LIST OF TYPE DETAILS FOR ALL RETURN NODES
		List<TypeDetails> returnStmtNode = MethodAnalyser.returnStmtMap.get(methodSignature);
		// if the method return type is void
		System.out.println(returnStmtNode);
		if (returnStmtNode!=null) {
			System.out.println(returnStmtNode.size());
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			if (returnStmtNode.size() > 0) {
				for (TypeDetails td : returnStmtNode) {
					returnNode = td.getNode();
					System.out.println(returnNode);
					if (returnNode != null) {
						// If the return statement is a method call expression
						// Get the forward children nodes if return node is a method call expression
						// the immediate child list always contains one child
						List<Node> childOfReturnStmt = returnNode.getChildNodes();
						if (childOfReturnStmt.size() > 0) {
							Node child = childOfReturnStmt.get(0);
							// filter the null returns
							if (!child.toString().equals("null")) {
								System.out.println("*********************************** 1");
								// Get the domain type of this immediate child
								updatedReturnNodeType = analyseReturnStmtWithMethodCallExpr(methodSignature, nodeMap,
										returnNode, child, returnStmtNode, td);

								// Update the type details of the return statement node in the nodeMap, and then
								// update the allNodeMap
								for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
									LinkedHashMap<Node, Node> nodes = nmp.getKey();
									for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
										if (nAp.getKey() == returnNode
												&& nAp.getValue() == returnNode.getParentNode().get()) {
											System.out.println("*********************************** 3");
											if (!updatedReturnNodeType.getDomianType().equals("NA")
													&& !updatedReturnNodeType.getDomianType()
															.equals(nmp.getValue().getDomianType())) {
												updatedReturnNodeType.setNode(returnNode);
												updatedReturnNodeType.setPrimaryType(nmp.getValue().getPrimaryType());
												nodeMap.replace(nmp.getKey(), updatedReturnNodeType);
											}

										}
									}
								}
							} else {
								return nodeMap;
							}
						} 
					}
				}
			} 
		}
		return nodeMap;
	}

	/*
	 * If the return statement is a method call expression and the corresponding
	 * method call is an library call, then finds the domain type of the method call
	 * expression
	 */
	public static TypeDetails analyseReturnStmtWithMethodCallExpr(String methodSignature,
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap, Node returnNode, Node child,
			List<TypeDetails> returnStmtNode, TypeDetails td) {
		TypeDetails returnNodeType = new TypeDetails();
		String domType = "";
		for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
			LinkedHashMap<Node, Node> nodes = nmp.getKey();
			for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
				if (nAp.getKey().equals(child) && nAp.getValue().equals(returnNode)) {
					String domainType = nmp.getValue().getDomianType();
					System.out.println(domainType);
					// If domain tye is assigned for the first child, propagate that to the return
					// statement
					if (!domainType.equals("NA")) {
						System.out.println("*********************************** 2(1)");
						// update the returnStmtMap
						returnStmtNode.remove(td);
						returnNodeType.setDomianType(domainType);
						returnStmtNode.add(returnNodeType);
						MethodAnalyser.returnStmtMap.put(methodSignature, returnStmtNode);
					} else {
						// If the domain type is NA for the immediate child, then check the method call
						// expressions of the child nodes of the immediate-child.
						List<Node> childNodeList = child.getChildNodes();
						for (int i = 0; i < childNodeList.size(); i++) {

							Node childNode = childNodeList.get(i);
							// Find the type using the expression of the node
							try {
								// For each childNode which is a methodcall expr or a variable, or other
								// traverse
								// through the nodeMap to retrieve its domain type.
								//// if(childNode.findFirst(MethodCallExpr.class).isPresent())// {
								System.out.println("*********************************** 2(2)");
								domainType = "";

								for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp2 : nodeMap.entrySet()) {
									// Find the corresponding child node in the map
									LinkedHashMap<Node, Node> nodes2 = nmp2.getKey();

									for (Map.Entry<Node, Node> nAp2 : nodes2.entrySet()) {
										// If the child node found
										if (nAp2.getKey() == childNode) {
											// Get its domain type
											String domainType2 = nmp2.getValue().getDomianType();

											if (!domainType2.equals("NA")) {
												domType = domType.concat(domainType2);
											}
											System.out.println(
													"========================domain type================================>" + domType);
										}
										
									}
									
								}
								

							} catch (Exception e) {
								System.out.println("NO EXPRESSION FOUND " + "JavaClassParser.class");
							}
						}
						returnStmtNode.remove(td);
						returnNodeType.setDomianType(domType);
						returnStmtNode.add(returnNodeType);
						MethodAnalyser.returnStmtMap.put(methodSignature, returnStmtNode);
						System.out.println("size 2(2): " + returnStmtNode.size());

					}
				}
			}
		}

		return returnNodeType;
	}

	/*
	 * If the return statement is a variable, which has assigned a value in a
	 * previous step. Find the variable declaration and the assignment expression
	 * which assign a value to that variable
	 */
	public static TypeDetails analyseReturnStmtWithVariable(String methodSignature,
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap, Node returnNode, Node child,
			List<TypeDetails> returnStmtNode, TypeDetails td) {
		TypeDetails returnNodeType = new TypeDetails();

		return returnNodeType;
	}

	/*
	 * If the return statement is a method call expression and that method is a
	 * implemented method in the source code (not an library call)
	 */
	public static TypeDetails analyseReturnStmtWithMethodCallOfSourceCode(String methodSignature,
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap, Node returnNode, Node child,
			List<TypeDetails> returnStmtNode, TypeDetails td) {
		TypeDetails returnNodeType = new TypeDetails();
		return returnNodeType;
	}
}
