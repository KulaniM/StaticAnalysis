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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;

import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.util.NormalizeType;

/*
 * The objective is to assign the domain type of the LHS of the expression to the variable on the RHS.
 */
public class VariableDeclarationAnalyser {

	public static LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updateVariableNameType(String methodSignature,
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap) {

		//DomainTypePopulater.printAllNodeMap(nodeMap, methodSignature);

		List<TypeDetails> updateVaraibleNodeList = analyseVariableDeclaration(methodSignature, nodeMap);
		for (TypeDetails updateVaraibleNode : updateVaraibleNodeList) {
			Node n = updateVaraibleNode.getNode();
			// update the allNodeMap
			for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
				LinkedHashMap<Node, Node> nodes = nmp.getKey();
				for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
					// Type primary type of both the nodes has to be normalized

					// if (nAp.getKey().toString().equals(n.toString())
					// && NormalizeType.getNormalizedType(nmp.getValue().getPrimaryType())
					// .equals(NormalizeType.getNormalizedType(updateVaraibleNode.getPrimaryType())))
					// {
					// System.out.println("*********************************** 5");
					// System.out.println(n + " Node");
					// if (!updateVaraibleNode.getDomianType().equals("NA")
					// &&
					// !updateVaraibleNode.getDomianType().equals(nmp.getValue().getDomianType())) {
					// updateVaraibleNode.setValueID(nmp.getValue().getValueID());
					// nodeMap.replace(nmp.getKey(), updateVaraibleNode);
					// System.out.println(" Type pre: " + nmp.getValue().getDomianType());
					// System.out.println(" Tyep post: " + updateVaraibleNode.getDomianType());
					// }
					// }
					if (nAp.getKey().toString().equals(n.toString())
							&& NormalizeType.getNormalizedType(nmp.getValue().getPrimaryType())
									.equals(NormalizeType.getNormalizedType(updateVaraibleNode.getPrimaryType()))) {
						System.out.println("*********************************** 5");
						System.out.println(" Node: " + n + "type: " + updateVaraibleNode.getDomianType());

						if (!updateVaraibleNode.getDomianType().equals("NA")) { // avoid if NA at update
							System.out.println(" Type pre: " + nmp.getValue().getDomianType());
							System.out.println(" Tyep post: " + updateVaraibleNode.getDomianType());
							String preType = nmp.getValue().getDomianType();
							String postType = updateVaraibleNode.getDomianType();
							if (!postType.equals(preType) && // avoid if update equal old
									!preType.contains(postType)) {// avoid if old contain update
								updateVaraibleNode.setValueID(nmp.getValue().getValueID());
								if (!preType.equals("NA")) {
									updateVaraibleNode
											.setDomianType(nmp.getValue().getDomianType() + " ->  " + postType);
								} else {
									updateVaraibleNode.setDomianType(updateVaraibleNode.getDomianType());
								}
								nodeMap.replace(nmp.getKey(), updateVaraibleNode);
							}
						}
					}
				}
			}
		}
		return nodeMap;

	}

	/*
	 * 1.) Get nodes which are variable declarations of the current method. Eg1. VD
	 * => Cipher localCipher = Cipher.getInstance("AES/ECB/PKCS5Padding") Eg2. VD =>
	 * String str3 = decrypt(str2, ssshhhhhhhhhhh!!!!) 2.) Get the declared value
	 * alone from the expression. Eg. value => decrypt(str2, "ssshhhhhhhhhhh!!!!")
	 * 3.) Scan the nodeMap for its domain type. Eg.: => NA|NA||NA, senc(k,msg),
	 * senc| 4.) Set the new type detials node's domain type
	 */
	public static List<TypeDetails> analyseVariableDeclaration(String methodSignature,
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap) {

		List<TypeDetails> updatedVarableNodeTypeList = new ArrayList<TypeDetails>();
		Node variableNode = null;
		String domainTypeOfVariableNode = null;

		HashMap<Node, Expression> allvariableDec = MethodAnalyser.allvariableDecExprMap.get(methodSignature);
		TypeDetails updatedVarableNodeType;
		// Analyze each variable declaration
		for (Entry<Node, Expression> variableDecMap : allvariableDec.entrySet()) {
			Node node = variableDecMap.getKey();
			VariableDeclarationExpr expr = (VariableDeclarationExpr) variableDecMap.getValue();
			// Eg. expr => String str3 = decrypt(str2, ssshhhhhhhhhhh!!!!)
			NodeList<VariableDeclarator> vdList = expr.getVariables();
			System.out.println("-------------------Variable Declaration ---------------------- " + vdList.size());
			////////////////////////////////////// VariableDeclaration(Find Declared Value Domain Type) type propagation
			////////////////////////////////////// ////////////////////////////////////////////////// 
			for (VariableDeclarator vd : vdList) {
				// Eg. vd => str3 = decrypt(str2, "ssshhhhhhhhhhh!!!!")
				String variable = vd.getNameAsString(); // Eg. variable => str3
				Type type = vd.getType(); // Eg. type => String
				List<Node> childrenOfVD = vd.getChildNodes();
				for (Node n : childrenOfVD) {
					Node refNode = null;
					// Eg. n => String, str3, decrypt(str2, "ssshhhhhhhhhhh!!!!")
					if (n.toString().equals(variable)) {// Save the variable node
						variableNode = n;
					}
					// Filter the type and variable name so the remaining is a value or an method
					// call expression
					if (!n.toString().equals(variable) && !n.toString().equals(type.asString())) {
						// Eg. declared value => decrypt(str2, "ssshhhhhhhhhhh!!!!")
						System.out.println(n);
						// Iterate through the nodeMap and find the type of this method call expression/
						// value
						for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
							LinkedHashMap<Node, Node> nodes = nmp.getKey();
							for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
								if (nAp.getKey() == n && nAp.getValue() == n.getParentNode().get()) {
									System.out.println("*********************************** 4");

									System.out.println("*********************************** variabledec  "
											+ variableNode.toString());
									System.out.println("*********************************** variabledec  "
											+ nmp.getValue().getDomianType());
									domainTypeOfVariableNode = nmp.getValue().getDomianType();
									/// *Do not set the value id yet here. It should be the existing value id of the
									/// variable node
									updatedVarableNodeType = new TypeDetails();
									updatedVarableNodeType.setNode(variableNode);
									updatedVarableNodeType.setPrimaryType(nmp.getValue().getPrimaryType());
									updatedVarableNodeType.setDomianType(domainTypeOfVariableNode);
									System.out.println(n);
									System.out.println(variableNode.toString());
									System.out.println(domainTypeOfVariableNode);
									updatedVarableNodeTypeList.add(updatedVarableNodeType);

								}
							}
						}
					} else if (n.toString().equals(variable)) {
						refNode = n;
					}
					////////////////////////// VariableDeclaration(Value --> Variable Name) type
					////////////////////////// propagation ////////////////////////////////////
					// if (domainTypeOfVariableNode!=null && refNode!=null) {
					// updatedVarableNodeType = new TypeDetails();
					// updatedVarableNodeType.setNode(refNode);
					// updatedVarableNodeType.setPrimaryType(type.asString());
					// updatedVarableNodeType.setDomianType(domainTypeOfVariableNode);
					// System.out.println(refNode.toString());
					// System.out.println(domainTypeOfVariableNode);
					// updatedVarableNodeTypeList.add(updatedVarableNodeType);
					// }
				}
				
				////////////
				
//
//				System.out.println("-------------------Variable Declaration ---------------------- " + vdList.size());
//				////////////////////////////////////// VariableDeclaration(Find Declared Value Domain Type) type propagation
//				////////////////////////////////////// ////////////////////////////////////////////////// 
//				for (VariableDeclarator vd : vdList) {
//					// Eg. vd => str3 = decrypt(str2, "ssshhhhhhhhhhh!!!!")
//					String variable = vd.getNameAsString(); // Eg. variable => str3
//					Type type = vd.getType(); // Eg. type => String
//					List<Node> childrenOfVD = vd.getChildNodes();
//					for (Node n : childrenOfVD) {
//						Node refNode = null;
//						// Eg. n => String, str3, decrypt(str2, "ssshhhhhhhhhhh!!!!")
//						if (n.toString().equals(variable)) {// Save the variable node
//							variableNode = n;
//						}
//						// Filter the type and variable name so the remaining is a value or an method
//						// call expression
//						if (!n.toString().equals(variable) && !n.toString().equals(type.asString())) {
//							// Eg. declared value => decrypt(str2, "ssshhhhhhhhhhh!!!!")
//							System.out.println(n);
//							// Iterate through the nodeMap and find the type of this method call expression/
//							// value
//							for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
//								LinkedHashMap<Node, Node> nodes = nmp.getKey();
//								for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
//									if (nAp.getKey() == n && nAp.getValue() == n.getParentNode().get()) {
//										System.out.println("*********************************** 4");
//
//										System.out.println("*********************************** variabledec  "
//												+ variableNode.toString());
//										System.out.println("*********************************** variabledec  "
//												+ nmp.getValue().getDomianType());
//										domainTypeOfVariableNode = nmp.getValue().getDomianType();
//										/// *Do not set the value id yet here. It should be the existing value id of the
//										/// variable node
//										updatedVarableNodeType = new TypeDetails();
//										updatedVarableNodeType.setNode(variableNode);
//										updatedVarableNodeType.setPrimaryType(nmp.getValue().getPrimaryType());
//										updatedVarableNodeType.setDomianType(domainTypeOfVariableNode);
//										System.out.println(n);
//										System.out.println(variableNode.toString());
//										System.out.println(domainTypeOfVariableNode);
//										updatedVarableNodeTypeList.add(updatedVarableNodeType);
//
//									}
//								}
//							}
//						} else if (n.toString().equals(variable)) {
//							refNode = n;
//						}
//					}
//				}
				///////////

			}

		}

		return updatedVarableNodeTypeList;

	}

}
