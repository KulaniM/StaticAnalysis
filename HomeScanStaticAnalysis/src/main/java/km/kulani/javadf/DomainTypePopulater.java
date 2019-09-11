package km.kulani.javadf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;

import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.semantics.MethodSignature;

/**
 * 
 * @author root Populate the domain type of method call expressions (e.g.:
 *         encryption, hash, and etc) to the variables and then return
 *         statements Next, Use the updated allNodeMap and propagate the return
 *         type to the method call sites.
 */
public class DomainTypePopulater {

	public static LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> propagateDomainTypesFromMethodCallToVariablesThenToReturnNodes(
			LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> allNodeMapp) {
		for (Entry<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> entry : allNodeMapp.entrySet()) {
			String methodSignature = entry.getKey();
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap = entry.getValue();
			System.out.println("\n Method:......." + methodSignature);

			//////////////////// PROPAGATE FROM NAME EXPR TO EXPRE
			//////////////////// ///////////////////////AssignExpr,VariableDeclarationExpr,MethodCallExpr,ObjectCreationExpr
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updatedCrossExpressioneNodeMap = CrossExpressionAnalysis
					.updateCrossExpressionNodeTypes(methodSignature, nodeMap);
			allNodeMapp.replace(methodSignature, updatedCrossExpressioneNodeMap);


			printAllNodeMap(allNodeMapp.get(methodSignature), methodSignature);

			//////////////////// ANALSYSE ASSIGNMENT EXPRESSIONS//////////////////////////
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updatedAssignmentVariableNodeMap = AssignmentExprAnalyser
					.updateAssignmentVariableNameType(methodSignature, nodeMap);
			allNodeMapp.replace(methodSignature, updatedAssignmentVariableNodeMap);
			/////////////////////////////////////////////////////

			//////////////////// ANALSYSE VARIABLEDECLARATION
			//////////////////// EXPRESSIONS//////////////////////////
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updatedVariableNodeMap = VariableDeclarationAnalyser
					.updateVariableNameType(methodSignature, nodeMap);
			allNodeMapp.replace(methodSignature, updatedVariableNodeMap);
			/////////////////////////////////////////////////////

			/////////////////////// PROPAGATE FROM METHOD PARAMS TO CALL SITE
			MethodParamsToCallSiteProp.updateCallSiteOfMethodParamTypes(allNodeMapp);
			///////////////////////////////////////////////////////////////////////

			//////////////// ANALYSE THE RETURN STATEMENT & PROPAGATE DOMAIN TYPE FROM ITS
			// CHILDREN////////////////////////
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updatedReturnNodeMap = ReturnStmtAnalyser
					.updateReturnStatementType(methodSignature, nodeMap);
			allNodeMapp.replace(methodSignature, updatedReturnNodeMap);
			///////////////////////////////////////////
			
			
			//////////////////// PROPAGATE FROM NAME EXPR TO EXPRE.............RUN 2............. (to fully propagate)
			//////////////////// ///////////////////////AssignExpr,VariableDeclarationExpr,MethodCallExpr,ObjectCreationExpr
//			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> updatedCrossExpressioneNodeMap2 = CrossExpressionAnalysis
//					.updateCrossExpressionNodeTypes(methodSignature, nodeMap);
//			allNodeMapp.replace(methodSignature, updatedCrossExpressioneNodeMap2);

		}
		return allNodeMapp;

	}

	/*
	 * Use the updated allNodeMap and propagate the return type to the method call
	 * location.
	 */
	public static LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> propagteReturnTypeToMethodCallSites(
			LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> updatedAllNodeMap) {
		for (Entry<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> entry : updatedAllNodeMap
				.entrySet()) {
			String methodSignature = entry.getKey();
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap = entry.getValue();
			System.out.println("\n Method:......." + methodSignature);

			// Access each node in the node map
			Node node = null;
			TypeDetails initialTypeDetails = null;

			for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nodemap : nodeMap.entrySet()) {
				LinkedHashMap<Node, Node> nodes = nodemap.getKey();
				for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
					node = nAp.getKey();
					initialTypeDetails = nodemap.getValue();
				}
				// Create an instance of type details having node type (based on its type of
				// expression) assigned
				TypeDetails typeDetails = new TypeDetails(nodemap.getValue());

				if (typeDetails.getNodeType() != null) {
					// If the node type is method call expression, generate the method signature
					if (typeDetails.getNodeType().equals("isMethodCallExpr")) {
						String signatures = MethodSignature
								.genMethodSignature((MethodCallExpr) typeDetails.getExpression(), null, null);
						System.out.println(signatures);

						// Check if the method signature is in the allNodeMap: implemented method list
						Set<String> methodSet = MethodAnalyser.allNodeMap.keySet();
						for (String mSig : methodSet) {
							if (signatures.equals(mSig)) {
								System.out.println(
										"YESSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS.........................");
								// Get the type details of the actual return type of the implemented method
								List<TypeDetails> returnValueList = MethodAnalyser.returnStmtMap.get(signatures);
								if (returnValueList != null) {
									//// use a list of method summary instead
									System.out.println(returnValueList.size());
									for (TypeDetails returnValue : returnValueList) {
										System.out.println(returnValue.getNode());
										System.out.println(returnValue.getDomianType());
										if (!returnValue.getDomianType().equals("NA") && !initialTypeDetails
												.getDomianType().equals(returnValue.getDomianType())) {
											// Set the domain type to the type details of the corresponding node in the
											// nodeMap
											initialTypeDetails.setDomianType(returnValue.getDomianType());
											nodeMap.replace(nodes, initialTypeDetails);
											updatedAllNodeMap.put(methodSignature, nodeMap);
											// Update the returnStmtMap
											List<TypeDetails> newValueList = new ArrayList<>();
											newValueList.add(initialTypeDetails);
											MethodAnalyser.returnStmtMap.put(signatures, newValueList);

											// AGAIN REQUIRES TO PROPAGATE THAT TO THE THE VARIABLE REPRESENTING THE
											// RETURN
											// VALUE
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return updatedAllNodeMap;
	}
	
	public static void printAllNodeMap(LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap, String methodName) {
		System.out.println("///////////////////////////////////////////////////////////////////////////");
		System.out.println("Method Signature: .........."+ methodName);
		for (Map.Entry<LinkedHashMap<Node, Node>, TypeDetails> nodemapEntry : nodeMap.entrySet()) {
			LinkedHashMap<Node, Node> nodes = nodemapEntry.getKey();

			TypeDetails typeDetails2 = nodemapEntry.getValue();
			String primaryType = typeDetails2.getPrimaryType();
			String domainType = typeDetails2.getDomianType();
		
			if (domainType != "NA") {
				for (Map.Entry<Node, Node> nodesEntry : nodes.entrySet()) {
					System.out.println("Node: "+ nodesEntry.getKey()+ " =>> domain type: " + domainType);
				}
			}
			
		}
		System.out.println("///////////////////////////////////////////////////////////////////////////");
	}

}
