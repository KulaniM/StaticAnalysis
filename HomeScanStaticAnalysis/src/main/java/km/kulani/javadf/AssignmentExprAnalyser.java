package km.kulani.javadf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;

import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.util.NormalizeType;

/*
 * The objective is to assign the domain type of the LHS of the expression to the variable on the RHS.
 */
public class AssignmentExprAnalyser {
	
	public static LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updateAssignmentVariableNameType(String methodSignature,
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap) {

		List<TypeDetails> updateVaraibleNodeList = analyseAssignmentExpressions(methodSignature, nodeMap);
		for (TypeDetails updateVaraibleNode : updateVaraibleNodeList) {
			Node n = updateVaraibleNode.getNode();
			// update the allNodeMap
			for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
				LinkedHashMap<Node, Node> nodes = nmp.getKey();
				for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
					// Type primary type of both the nodes has to be normalized

					if (nAp.getKey().toString().equals(n.toString())
							&& NormalizeType.getNormalizedType(nmp.getValue().getPrimaryType())
									.equals(NormalizeType.getNormalizedType(updateVaraibleNode.getPrimaryType()))) {
						System.out.println("*********************************** 5");
						System.out.println( " Node: "+ n + "type: "+updateVaraibleNode.getDomianType());
						if (!updateVaraibleNode.getDomianType().equals("NA")) { // avoid if NA at update							
							System.out.println( " Type pre: "+ nmp.getValue().getDomianType());
							System.out.println( " Tyep post: "+ updateVaraibleNode.getDomianType());
							String preType = nmp.getValue().getDomianType();
							String postType = updateVaraibleNode.getDomianType();
							if (!postType.equals(preType) && //avoid if update equal old
									!preType.contains(postType)){//avoid if old contain update
								updateVaraibleNode.setValueID(nmp.getValue().getValueID());
								if (!preType.equals("NA")) {
									updateVaraibleNode.setDomianType(nmp.getValue().getDomianType() +" ->  " +postType);									
								} else {
									updateVaraibleNode.setDomianType(updateVaraibleNode.getDomianType());	
								}
								nodeMap.replace(nmp.getKey(), updateVaraibleNode);	
								/////////////////////////////////////////////////////// update methodcal expr nodes (Used at CrossExpressionAnalysis.java)
								//////////////////////////////////////////////////////////////////////////////////////
								HashMap<String, String> methodCallExprType = MethodAnalyser.allMethodCallExprTypeMap.get(methodSignature);// type of method expression
								Expression expr = n.findFirst(Expression.class).get();
								if (TypeDetails.findExprType(expr).equals("isMethodCallExpr")) {
									methodCallExprType.put(n.toString(), updateVaraibleNode.getDomianType());
								}
								//////////////////////////////////////////////////////////////////////////////////////
								//////////////////////////////////////////////////////////////////////////////////////
							}
						}
					}
				}
			}
		}
		return nodeMap;
	}

	public static List<TypeDetails> analyseAssignmentExpressions(String methodSignature,
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap) {

		List<TypeDetails> updatedVarableNodeTypeList = new ArrayList<TypeDetails>();
		Node variableNode = null;
		String domainTypeOfVariableNode = null;		

		HashMap<String, Node> allassignments = MethodAnalyser.allassignmentExprMap.get(methodSignature);
		TypeDetails updatedVarableNodeType;
		for (Entry<String, Node> assignmentMap : allassignments.entrySet()) {
			String node = assignmentMap.getKey();
			System.out.println("----------------------- Assignment Expression -------------------------" + node);
			//////////////////////////////////////Assignment(Value --> Target) type propagation //////////////////////////////////////////////////
			//E.g.: Assignment expression => key = sha.digest(key)
			AssignExpr asexpr = (AssignExpr) assignmentMap.getValue();
			//E.g.: getValue => sha.digest(key)
			Expression assignedValue = asexpr.getValue();
			//E.g.: getTarget => key
			variableNode = asexpr.getTarget();
			for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
				LinkedHashMap<Node, Node> nodes = nmp.getKey();
				for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
					//Get the domain type of the assigned value
					if (nAp.getKey() == assignedValue && nAp.getValue() == assignedValue.getParentNode().get()) {
						System.out.println("*********************************** 4");
						domainTypeOfVariableNode = nmp.getValue().getDomianType();
						/// *Do not set the value id yet here. It should be the existing value id of the
						/// variable node
						updatedVarableNodeType = new TypeDetails();
						updatedVarableNodeType.setNode(variableNode);
						updatedVarableNodeType.setPrimaryType(nmp.getValue().getPrimaryType());
						updatedVarableNodeType.setDomianType(domainTypeOfVariableNode);
						System.out.println(variableNode.toString());
						System.out.println(domainTypeOfVariableNode);
						updatedVarableNodeTypeList.add(updatedVarableNodeType);

					}
				}
			}
			////////////////////////////////////// Assignment(Target --> Value) type propagation //////////////////////////////////////////////////
			//E.g.: Assignment expression => key = myKey.getBytes("UTF-8");
			//AssignExpr asexpr = (AssignExpr) assignmentMap.getValue();
			//E.g.: getValue => myKey.getBytes("UTF-8");
			Node targetNode = asexpr.getValue();
			//E.g.: getTarget => key
			Node sourceNode = asexpr.getTarget(); // type known
			for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMap.entrySet()) {
				LinkedHashMap<Node, Node> nodes = nmp.getKey();
				for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
					//Get the domain type of the assigned value
					if (nAp.getKey() == sourceNode && nAp.getValue() == sourceNode.getParentNode().get()) {
						System.out.println("*********************************** 45");
						domainTypeOfVariableNode = nmp.getValue().getDomianType();
						/// *Do not set the value id yet here. It should be the existing value id of the
						/// variable node
						updatedVarableNodeType = new TypeDetails();
						updatedVarableNodeType.setNode(targetNode);
						updatedVarableNodeType.setPrimaryType(nmp.getValue().getPrimaryType());
						updatedVarableNodeType.setDomianType(domainTypeOfVariableNode);
						System.out.println(targetNode.toString());
						System.out.println(domainTypeOfVariableNode);
						updatedVarableNodeTypeList.add(updatedVarableNodeType);

					}
				}
			}
		}
		return updatedVarableNodeTypeList;
	}
}
