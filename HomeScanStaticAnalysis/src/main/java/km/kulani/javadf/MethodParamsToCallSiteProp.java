package km.kulani.javadf;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.util.NormalizeType;

public class MethodParamsToCallSiteProp {

	public static void updateCallSiteOfMethodParamTypes(
			LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> allNodeMapp) {
		Set allKeys = allNodeMapp.keySet();
		for (Object object : allKeys) {
			System.out.println(object);
			HashMap<String, MethodCallExpr> methodExprsInThisMethod = MethodAnalyser.allmethodCallExprMap.get(object);
			if (methodExprsInThisMethod != null) {
				for (Map.Entry<String, MethodCallExpr> e : methodExprsInThisMethod.entrySet()) {
					// If the methodExpr is with implementation and already analyzed
					LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> analyzedMethodExpr = allNodeMapp
							.get(e.getKey());// check the existence of method implementation analysis
					NodeList<Parameter> paramNodes = MethodAnalyser.allMethodDeclMap.get(e.getKey()); // param nodes
																										// of the
					try {
						// <analyzedMethodExpr>
						if (analyzedMethodExpr != null) {
							if (analyzedMethodExpr.size() > 0) {
								System.out.println(e.getKey() + ":");
								MethodCallExpr mexpr = e.getValue();
								NodeList<Expression> params = mexpr.getArguments();

								for (int i = 0; i < params.size(); i++) {
									Node targetNode = params.get(i);
									String primaryType = params.get(i).calculateResolvedType().describe().toString();
									String domainType = "NA";

									for (Map.Entry<LinkedHashMap<Node, Node>, TypeDetails> aMethodExpr : analyzedMethodExpr
											.entrySet()) {
										LinkedHashMap<Node, Node> key = aMethodExpr.getKey();
										Node child = null;
										Node parent = null;
										Parameter pnode = paramNodes.get(i);
										// System.out.println("param "+ pnode.getChildNodes().get(1));
										child = pnode.getChildNodes().get(1);
										parent = pnode.getParentNodeForChildren();
										for (Map.Entry<Node, Node> keynode : key.entrySet()) {
											if (keynode.getKey().toString().equals(child.toString())
													&& keynode.getValue().toString().equals(parent.toString())) {
												domainType = aMethodExpr.getValue().getDomianType();
												/////// find value and set value to the correct nodMap
												LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMapToUpdate = allNodeMapp
														.get(object);
												for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nmp : nodeMapToUpdate
														.entrySet()) {
													LinkedHashMap<Node, Node> nodes = nmp.getKey();
													for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {

														if (nAp.getKey().toString().equals(targetNode.toString())
																&& NormalizeType
																		.getNormalizedType(
																				nmp.getValue().getPrimaryType())
																		.equals(NormalizeType
																				.getNormalizedType(primaryType))) {
															System.out.println(
																	"*********************************** 5 MethodParamsToCallSiteProp:");
															System.out.println("taget node: " + targetNode + "type: "
																	+ domainType);

															TypeDetails typeOfNodeToUpdate = nmp.getValue();

															if (!domainType.equals("NA")) { // avoid if NA at update
																String preType = nmp.getValue().getDomianType();
																String postType = domainType;
																if (!postType.equals(preType) && // avoid if updateequal
																									// old
																		!preType.contains(postType)) {// avoid if old
																										// contain
																										// update

																	if (!preType.equals("NA")) {
																		typeOfNodeToUpdate.setDomianType(
																				nmp.getValue().getDomianType() + " ->  "
																						+ postType);
																	} else {
																		typeOfNodeToUpdate.setDomianType(domainType);
																	}
																	nodeMapToUpdate.replace(nmp.getKey(),
																			typeOfNodeToUpdate);
																	allNodeMapp.replace(object.toString(),
																			nodeMapToUpdate);
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					} catch (Exception ex) {
						System.out.println("Exception at MethodParamsToCallSiteProp..");
					}
				}
			}
		}
	}

}
