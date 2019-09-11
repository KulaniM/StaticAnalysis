package km.kulani.javadf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;

import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.util.NormalizeType;

public class CrossExpressionAnalysis {	
	public static LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updateCrossExpressionNodeTypes(
			String methodSignature, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap) {

		List<TypeDetails> updateNodeList = analyseCrossExpressions(methodSignature, nodeMap);
		for (TypeDetails updateNode : updateNodeList) {
			Node n = updateNode.getNode();
			// update the allNodeMap
			for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
				LinkedHashMap<Node, Node> nodes = nmp.getKey();
				for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
					// Type primary type of both the nodes has to be normalized

					if (nAp.getKey().toString().equals(n.toString())
							&& NormalizeType.getNormalizedType(nmp.getValue().getPrimaryType())
									.equals(NormalizeType.getNormalizedType(updateNode.getPrimaryType()))) {
						System.out.println("*********************************** 5");
						System.out.println(" Node: " + n + "type: " + updateNode.getDomianType());
						if (!updateNode.getDomianType().equals("NA")) { // avoid if NA at update
							System.out.println(" Type pre: " + nmp.getValue().getDomianType());
							System.out.println(" Tyep post: " + updateNode.getDomianType());
							String preType = nmp.getValue().getDomianType();
							String postType = updateNode.getDomianType();
							if (!postType.equals(preType) && // avoid if update equal old
									!preType.contains(postType)) {// avoid if old contain update
								updateNode.setValueID(nmp.getValue().getValueID());
								if (!preType.equals("NA")) {
									updateNode.setDomianType(nmp.getValue().getDomianType() + " ->  " + postType);
									System.out.println(" Type pre: " + nmp.getValue().getDomianType());
									System.out.println(" Tyep post: " + updateNode.getDomianType());
								} else {
									updateNode.setDomianType(postType);
									System.out.println(" Type pre: " + nmp.getValue().getDomianType());
									System.out.println(" Tyep post: " + updateNode.getDomianType());
								}
								nodeMap.replace(nmp.getKey(), updateNode);
							}
						}
					}
				}
			}
		}
		return nodeMap;
	}

	public static List<TypeDetails> analyseCrossExpressions(String methodSignature,
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap) {

		List<TypeDetails> updatedNodeTypeList = new ArrayList<TypeDetails>();
		Node targetNode = null;
		HashMap<Node, Expression> allselectedExpr = MethodAnalyser.allselectedExprNodeMap.get(methodSignature);
		HashMap<String, String> nameExprType = MethodAnalyser.allnameExprTypeMap.get(methodSignature);
		// type of name expression
		HashMap<String, String> methodCallExprType = MethodAnalyser.allMethodCallExprTypeMap.get(methodSignature);
		// type of method expression

		TypeDetails updatedNodeType;
		for (Entry<Node, Expression> selectedExpr : allselectedExpr.entrySet()) {
			Node node = selectedExpr.getKey();
			System.out.println("----------------------- Selected Expression -------------------------" + node);
			Expression expr = selectedExpr.getValue();
			// L.H.S nameExpr
			HashMap<Node, String> newNodesAndTypes = new HashMap<>(); // <node,type>
			///////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////// param ----> prev expression type
			///////////////////////////////////////////////////////////////////////////////////////////
			if (expr.isAssignExpr()) {
				AssignExpr mexpr = (AssignExpr) expr; // key = sha.digest(key)
				for (Node nodeC : mexpr.getChildNodes()) {
					System.out.println(nodeC);// Children: key, sha.digest(key)
					if (nameExprType.containsKey(nodeC.toString())) {
						System.out.println("###################### @AssignExpr " + nameExprType.get(nodeC.toString()));
						targetNode = nodeC.getChildNodes().get(1);// sha.digest(key)
						newNodesAndTypes.put(targetNode, nameExprType.get(nodeC.toString()));// save node and type
					}
				}
			} else if (expr.isVariableDeclarationExpr()) {
				VariableDeclarationExpr vexpr = (VariableDeclarationExpr) expr;// byte[] hashkey =java.util.Arrays.copyOf(key, 16)
				for (Node nodeC : vexpr.getChildNodes()) { // Children: hashkey = java.util.Arrays.copyOf(key, 16)
					System.out.println(nodeC);
					for (Node nodeCC : nodeC.getChildNodes()) { // hashkey = java.util.Arrays.copyOf(key, 16)
						System.out.println(nodeCC); // Children: byte[], hashkey, java.util.Arrays.copyOf(key, 16)
						if (nameExprType.containsKey(nodeCC.toString())) {
							System.out.println("###################### @VariableDeclarationExpr "
									+ nameExprType.get(nodeCC.toString()));
							// target node
							targetNode = nodeC.getChildNodes().get(2);
							newNodesAndTypes.put(targetNode, nameExprType.get(nodeCC.toString()));
							//////////////////////////////////////////////////////////////////////
							if (targetNode.toString().contains("java.util.Arrays.copyOf")) { 
								// java.util.Arrays, copyOf, key, 16
								Node keyParamNode = targetNode.getChildNodes().get(2); // key
								newNodesAndTypes.put(keyParamNode, nameExprType.get(nodeCC.toString()));
							}
						}
					}
				}
				
				//////////////////////////// if the variable name node has type update variable declaration
				NodeList<VariableDeclarator> vdList = vexpr.getVariables();
				// e.g. final byte[] decode = Base64.decode(s2, 0);
				TypeDetails cTypeDetails = null;
				for (VariableDeclarator variableDeclarator : vdList) { // byte[] decode
					String variable = variableDeclarator.getNameAsString(); // decode
					// if the variable name node has type update variable declaration
					for (Entry<LinkedHashMap<Node, Node>, TypeDetails> entry : nodeMap.entrySet()) {
						LinkedHashMap<Node, Node> nodemp = entry.getKey();
						TypeDetails typeDetails = entry.getValue();
						// check for the current node
						Node cnode = null;

						// check equality of variable name node of the expression and node in the nodemap
						for (Map.Entry<Node, Node> nAp : nodemp.entrySet()) {
							cnode = nAp.getKey();
							if (cnode.toString().equals(variable.toString())) {
								cTypeDetails = typeDetails;
							}
						}
					}
					if (!cTypeDetails.getDomianType().equals("NA")) {
						List<Node> children = variableDeclarator.getChildNodes();
						newNodesAndTypes.put(vexpr,cTypeDetails.getDomianType());
						newNodesAndTypes.put(children.get(1),cTypeDetails.getDomianType());
						newNodesAndTypes.put(children.get(2),cTypeDetails.getDomianType());
		
					}
				}

				//////////////////////////////////////////////
				///////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////// expression --> param type
				///////////////////////////////////////////////////////////////////////////////////////////
			} else if (expr.isMethodCallExpr()) {
				MethodCallExpr mexpr = (MethodCallExpr) expr;// strToEncrypt.getBytes("UTF-8"),
																// java.util.Base64.getDecoder().decode(strToDecrypt)
				System.out.println("###################### @MethodCallExpr  " + mexpr.getNameAsString());
				////////////////////////////
				// get the current domain type of the node (method call expression)
				// e.g. KeyFactory.getInstance("RSA").generatePublic(new
				//////////////////////////// X509EncodedKeySpec(array)) --> publickey
				// if generatePublic then new X509EncodedKeySpec(array) --> publickey
				for (Entry<LinkedHashMap<Node, Node>, TypeDetails> entry : nodeMap.entrySet()) {
					LinkedHashMap<Node, Node> nodemp = entry.getKey();
					TypeDetails typeDetails = entry.getValue();
					// check for the current node
					Node cnode = null;
					TypeDetails cTypeDetails = null;

					for (Map.Entry<Node, Node> nAp : nodemp.entrySet()) {
						cnode = nAp.getKey();
						if (cnode.equals(expr)) {
							cTypeDetails = typeDetails;
							if (mexpr.getNameAsString().toString().equals("generatePublic")) {
								targetNode = mexpr.getArgument(0);// decode(strToDecrypt)
								newNodesAndTypes.put(targetNode, cTypeDetails.getDomianType());

								if (mexpr.getArgument(0).isObjectCreationExpr()) {
									ObjectCreationExpr oexpr = (ObjectCreationExpr) mexpr.getArgument(0);
									// new SecretKeySpec(hashkey, "AES")
									// new X509EncodedKeySpec(array)
									////////////////////////////////////// obtain type of the node --> assign in to
									// specific arg
									if (oexpr.getTypeAsString().equals("X509EncodedKeySpec")) {
										NodeList<Expression> x = oexpr.getArguments();
										targetNode = x.get(0);//
										newNodesAndTypes.put(targetNode, cTypeDetails.getDomianType());
									}
									/////////////////////
								}
							}
						}
					}
				}
				/////////////////////
				if (mexpr.getNameAsString().toString().equals("decode")) {
					targetNode = mexpr.getArgument(0);// decode(strToDecrypt)
					for (Entry<LinkedHashMap<Node, Node>, TypeDetails> entry : nodeMap.entrySet()) {
						LinkedHashMap<Node, Node> nodemp = entry.getKey();
						TypeDetails typeDetails = entry.getValue();
						// check for the current node
						Node cnode = null;
						TypeDetails cTypeDetails = null;

						for (Map.Entry<Node, Node> nAp : nodemp.entrySet()) {
							cnode = nAp.getKey();
							if (cnode.equals(mexpr)) {// get type from destNode --> srcNode
								cTypeDetails = typeDetails;
								newNodesAndTypes.put(targetNode,cTypeDetails.getDomianType());
							}
						}
					}					

				}
				// TODO
				if (mexpr.getNameAsString().toString().equals("arraycopy")) {
					//arraycopy(decode, 0, array, k.a.length, decode.length);
					// type of array (dest) ---> type of decode (src)
					Node srcNode = mexpr.getArgument(0);//
					Node destNode = mexpr.getArgument(2);
					for (Entry<LinkedHashMap<Node, Node>, TypeDetails> entry : nodeMap.entrySet()) {
						LinkedHashMap<Node, Node> nodemp = entry.getKey();
						TypeDetails typeDetails = entry.getValue();
						// check for the current node
						Node cnode = null;
						TypeDetails cTypeDetails = null;

						for (Map.Entry<Node, Node> nAp : nodemp.entrySet()) {
							cnode = nAp.getKey();
							if (cnode.equals(destNode)) {// get type from destNode --> srcNode
								cTypeDetails = typeDetails;
								newNodesAndTypes.put(srcNode,cTypeDetails.getDomianType());
							}
						}
					}					
				}
				if (mexpr.getNameAsString().toString().equals("getBytes")) {
					targetNode = mexpr.getChildNodes().get(0);// strToEncrypt (reference to getByte method call)
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ targetNode: " + targetNode);
					String methodExprType = methodCallExprType.get(node.toString()); // plaintext
					System.out.println(
							"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ methodExprType: " + methodExprType);
					if (methodExprType != null) {
						String domainType = methodExprType;
						newNodesAndTypes.put(targetNode, domainType);
					}
				}
			}
			
			else if (expr.isObjectCreationExpr()) {

			}
			

			for (Map.Entry<Node, String> entry : newNodesAndTypes.entrySet()) {
				Node targetNd = entry.getKey();
				String domainType = entry.getValue();
				for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
					LinkedHashMap<Node, Node> nodes = nmp.getKey();
					for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
						// Get the domain type of the assigned value
						if (nAp.getKey() == targetNd && nAp.getValue() == targetNd.getParentNode().get()) {
							System.out.println("*********************************** expression to expression "
									+ targetNode);
							System.out.println(
									"*********************************** expression to expression " + domainType);
							/// *Do not set the value id yet here. It should be the existing value id of the
							/// variable node
							updatedNodeType = new TypeDetails();
							updatedNodeType.setNode(targetNd);
							updatedNodeType.setPrimaryType(nmp.getValue().getPrimaryType());
							updatedNodeType.setDomianType(domainType);
							updatedNodeTypeList.add(updatedNodeType);
						}
					}
				}
			}
		}
		return updatedNodeTypeList;
	}

}
