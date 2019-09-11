package km.kulani.javadf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;

import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.semantics.MethodSignature;
import km.kulani.javadf.util.NormalizeType;

public class ForwardSymbolAnalysis {

	public static HashMap<String, List<String>> analyzeMethod(Node node, LinkedHashMap<String, String> variableNameTypeMap,
			LinkedHashMap<String, String> fildDeclarationsMap, HashMap<String, List<String>> labelMap,
			NodeList<Parameter> outerMethodParams) {
		// Find the type using the expression of the node
		// key is param , value list is nodes with similar type

		// TODO
		// analyse return node: Done
		// add domain knowledge labels
		// handle setKey(mykey) type of calls
		for (Parameter parameter : outerMethodParams) {
			List<String> nodeString = new ArrayList<>();
			if (labelMap.get("input") != null && labelMap.get("input").size() > 0) {
				nodeString = labelMap.get("input");
			}
			if (!isInCheck(parameter.getNameAsString(), nodeString)) {
				nodeString.add(parameter.getNameAsString());
				labelMap = addLabelToMap("input", nodeString, labelMap);
			}
		}

		try {
			/////////////////////////// RETURN STMNT ANALYSIS //////////////////
			/////////////////////////////////////////////////////////////////////////////////
			if (node.getMetaModel().toString().equals("ReturnStmt")) {
				// get R.H.S
				List<Node> children = node.getChildNodes();
				for (Node child : children) {
					// if R.H.S is a method call expression
					HashMap<String, Node> rhs = expressionAnalysis(child, variableNameTypeMap, fildDeclarationsMap,
							labelMap, outerMethodParams);
					if (rhs != null && rhs.size() > 0) {
						///////////////////////
						List<String> nodeString = new ArrayList<>();
						nodeString = constructNodeString(nodeString, rhs, "return");
						////////////////////////////
						System.out.println("varaiable:  return   <<----- related node: " + nodeString);
						System.out.println("&&&&&&&&&&&&&&&&&&ReturnStmnt&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
						labelMap = addLabelToMap("retun", nodeString, labelMap);
					}
				}
			} else if (node.findFirst(Expression.class).isPresent()) {
				Expression expr = node.findFirst(Expression.class).get();
				Node n = expr.getParentNodeForChildren();
				if (n.equals(node)) {
					///////////////////////// METHODCALL EXPR //////////////////////////////////////
					////////////////////////////////////////////////////////////////////////////////
					if (TypeDetails.findExprType(expr).equals("isMethodCallExpr")) {
						// later update it when the method #methodParamTypeMap gets updated
						HashMap<String, Node> rhs = new HashMap<>();
						rhs = methodCallAnalysis(n, expr, variableNameTypeMap, fildDeclarationsMap, labelMap,
								outerMethodParams);
						Node firstChild = expr.getChildNodes().get(0);
						// should avoid static method calls' reference
						// add reference - first child is always the reference (if exist)
						boolean isAnArg = false;
						for (Parameter expression : outerMethodParams) {
							if (expression.getNameAsString().equals(firstChild.toString())) {// avoid method name node
								isAnArg = true;
							}
						}
						MethodCallExpr mexpr = (MethodCallExpr) expr;
						if (mexpr.getNameAsString().equals(firstChild.toString())
								|| (labelMap.containsKey(firstChild.toString()) || isAnArg)) {// avoid method name node
																								// x --> allow
							// use the firstchild(reference) as the key in labelMap
							if (rhs != null && rhs.size() > 0) {
								String key = firstChild.toString();
								///////////////////////
								List<String> nodeString = new ArrayList<>();
								nodeString = constructNodeString(nodeString, rhs, firstChild.toString());
								////////////////////////////
								// handle when the method name is the first argument --> key <= method signature
								if (mexpr.getNameAsString().equals(firstChild.toString())) {
									key = MethodSignature.genMethodSignature(mexpr, variableNameTypeMap,
											fildDeclarationsMap);
								}
								////////////////////////////
								System.out.println("varaiable: " + key + " <<----- related node: " + nodeString);
								System.out.println("&&&&&&&&&&&&&&&&&&MethodCallExpr&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
								labelMap = addLabelToMap(key, nodeString, labelMap);
							}
						}
						///////////////////////// VARIABLE DECL /////////////////////////////////////
						/////////////////////////////////////////////////////////////////////////////
					} else if (TypeDetails.findExprType(expr).equals("isVariableDeclarationExpr")) {
						// get R.H.S
						List<Node> children = node.getChildNodes();
						for (Node child : children) {

							Expression subexpr = child.findFirst(Expression.class).get();
							HashMap<String, Node> rhs = new HashMap<>();// need to find by following approach

							if (!TypeDetails.findExprType(subexpr).equals("isMethodCallExpr") && (NormalizeType
									.getNormalizedType(subexpr.calculateResolvedType().describe().toString())
									.equals("java.lang.String")
									|| NormalizeType
											.getNormalizedType(subexpr.calculateResolvedType().describe().toString())
											.equals("java.lang.Integer"))) {
								NodeList<VariableDeclarator> variables = ((VariableDeclarationExpr) expr)
										.getVariables();
								for (VariableDeclarator v : variables) {
									if (!v.getChildNodes().get(1).toString().equals(subexpr.toString())) {
										List<String> strList = new ArrayList<>();
										strList.add(subexpr.toString());
										labelMap.put(v.getChildNodes().get(1).toString(), strList);
									}
								}
								//////////////////////////////////////////////////////////////////////////
								// if R.H.S is an algebraic expression
								// express l.h.s using r.h.s
								// if R.H.S is a method call expression
							} else {
								rhs = expressionAnalysis(child, variableNameTypeMap, fildDeclarationsMap, labelMap,
										outerMethodParams);
							}
							if (rhs != null && rhs.size() > 0) {
								NodeList<VariableDeclarator> variables = ((VariableDeclarationExpr) expr)
										.getVariables();
								for (VariableDeclarator v : variables) {
									///////////////////////
									List<String> nodeString = new ArrayList<>();
									nodeString = constructNodeString(nodeString, rhs, v.getNameAsString());
									////////////////////////////
									System.out.println("varaiable: " + v.getNameAsString() + " <<----- related node: "
											+ nodeString);
									System.out.println("&&&&&&&&&&&&&&&&&VariableDecl&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
									labelMap = addLabelToMap(v.getChildNodes().get(1).toString(), nodeString, labelMap);
								}
							}
						}
						////////////////////////////// ASSIGNMENT //////////////////////////////////////
						//////////////////////////////////////////////////////////////////////////////
					} else if (TypeDetails.findExprType(expr).equals("isAssignExpr")) {
						// get R.H.S
						List<Node> children = node.getChildNodes();
						for (Node child : children) {
							HashMap<String, Node> rhs = expressionAnalysis(child, variableNameTypeMap,
									fildDeclarationsMap, labelMap, outerMethodParams);
							if (rhs != null && rhs.size() > 0) {
								Node target = ((AssignExpr) expr).getTarget();
								///////////////////////
								List<String> nodeString = new ArrayList<>();
								nodeString = constructNodeString(nodeString, rhs, target.toString());
								////////////////////////////
								System.out.println("varaiable: " + target + " <<----- related node: " + nodeString);
								System.out.println("&&&&&&&&&&&&&&&&&&AssignExpr&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
								labelMap = addLabelToMap(target.toString(), nodeString, labelMap);

							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("NO EXPRESSION FOUND " + "ForwardSymbolAnalysis.class");
		}
		return labelMap;
	}

	// there can be more than one variables in the R.H.S, therefore return a node
	// list
	private static HashMap<String, Node> methodCallAnalysis(Node node, Expression expr,
			LinkedHashMap<String, String> variableNameTypeMap, LinkedHashMap<String, String> fildDeclarationsMap,
			HashMap<String, List<String>> labelMap, NodeList<Parameter> outerMethodParams) {
		HashMap<String, Node> returnNodes = new LinkedHashMap<>();// LinkedHashMap to preserve insert order
		MethodCallExpr mexpr = (MethodCallExpr) expr;
		String signatures = MethodSignature.genMethodSignature(mexpr, variableNameTypeMap, fildDeclarationsMap);
		NodeList<Expression> args = mexpr.getArguments();
		System.out.println("============= ForwardSymbolAnalysis Method Signature ==================>" + signatures);

		// add all params
		for (Node argNode : args) {
			// if param is method call expression
			Expression argExpr = argNode.findFirst(Expression.class).get();
			if (TypeDetails.findExprType(argExpr).equals("isMethodCallExpr")) {
				returnNodes.putAll(methodCallAnalysis(argNode, argExpr, variableNameTypeMap, fildDeclarationsMap,
						labelMap, outerMethodParams));
			} else {
				returnNodes.put(argNode.toString(), argNode);
			}
		}

		// add reference - first child is always the reference (if exist)
		Node firstChild = mexpr.getChildNodes().get(0);
		boolean isAnArg = false;
		for (Parameter expression : outerMethodParams) {
			if (expression.getNameAsString().equals(firstChild.toString())) {// avoid method name node
				isAnArg = true;
			}
		}
		if (!firstChild.toString().equals(mexpr.getName().asString())
				&& (labelMap.containsKey(firstChild.toString()) || isAnArg)) {
			// avoid static method call's reference
			returnNodes.put(firstChild.toString(), firstChild);
		}
		// handle when first child is method name --> the method is inside the class
		if (firstChild.toString().equals(mexpr.getNameAsString())) {
			// add only the params
			// the method name will be the key in labelmap

			for (Expression expression : args) {
				boolean isAlreadyIn = false;
				for (Entry<String, Node> entryReturn : returnNodes.entrySet()) {
					if (entryReturn.getKey().equals(expression.toString())) {
						isAlreadyIn = true;
					}
				}
				if (!isAlreadyIn) {
					returnNodes.put(expression.toString(), expression);
				}
			}

		}
		// handle sb.toString() type method calls ==> no arguments
		if (args != null && args.size() == 0) {
			if (!firstChild.toString().equals(mexpr.getName().asString())
					&& (labelMap.containsKey(firstChild.toString()) || isAnArg)) {
				// avoid static method call's reference
				returnNodes.put(firstChild.toString(), firstChild);
			}
		}

		/////////////////////////////////////////////////////////////////////////////////
		/////////////////////////// ENCRYPTION OR DECRYPTION //
		////////////////////////////////////////////////////////////////////////////////
		if ((signatures.equals("getInstance(java.lang.String)")
				|| signatures.equals("getInstance(java.lang.String,java.lang.String)"))
				&& (mexpr.getScope().isPresent() && mexpr.getScope().get().toString().equals("Cipher"))) {
			// cipher.key
		} else if (signatures.equals("init(int,javax.crypto.spec.SecretKeySpec)")
				|| signatures.equals("init(int,PublicKey)")) {
			// cipher.key = args.get(1)
			// returnNodes.put("cipher.key", mexpr);// have to handle at all method call
			// analysis

		} else if (signatures.equals("doFinal(byte[])") || signatures.equals("doFinal()")) {
			// message = enc(command, secret)
			// about key
			// returnNodes.put("cipher.key", mexpr);
			// returnNodes.put("encrypt/decrypt",mexpr );
			// input param

			////////////////////////////////////////////////////////////////////////////////
			//////////////////////////// HASH FUNCTIONS
			////////////////////////////////////////////////////////////////////////////////
		} else if (signatures.equals("digest()") || signatures.equals("digest(byte[])")) {
			// first argument => variable name
			// return the first argument
			// returnNode = args.get(0);
			////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////// Utility - String processing
			////////////////////////////////////////////////////////////////////////////////
		} else if (signatures.equals("getBytes()") || signatures.equals("getBytes(java.lang.String)")) {
			// variable reference to method call => variable name
			// reference is the first child
			// return reference variable
			// returnNodes.add(node.getChildNodes().get(0));
		} else if (signatures.equals("copyOf(byte[],int)")) {
			// first argument => variable name
			// return the first argument
			// returnNode = args.get(0);
		}
		return returnNodes;
	}

	private static HashMap<String, Node> expressionAnalysis(Node child, LinkedHashMap<String, String> variableNameTypeMap,
			LinkedHashMap<String, String> fildDeclarationsMap, HashMap<String, List<String>> labelMap,
			NodeList<Parameter> outerMethodParams) {
		// if R.H.S is a method call expression
		Expression subexpr = child.findFirst(Expression.class).get();
		HashMap<String, Node> rhs = new HashMap<>(); // need to find by following approach
		if (TypeDetails.findExprType(subexpr).equals("isMethodCallExpr")) {
			rhs = methodCallAnalysis(child, subexpr, variableNameTypeMap, fildDeclarationsMap, labelMap,
					outerMethodParams);

		} else if (TypeDetails.findExprType(subexpr).equals("isObjectCreationExpr")) {
			ObjectCreationExpr oexpr = (ObjectCreationExpr) subexpr;
			NodeList<Expression> args = oexpr.getArguments();
			for (Expression expression : args) {
				if (TypeDetails.findExprType(expression).equals("isMethodCallExpr")) {
					rhs.putAll(methodCallAnalysis(expression, expression, variableNameTypeMap, fildDeclarationsMap,
							labelMap, outerMethodParams));
				} else {
					rhs.put(expression.toString(), expression);
				}
			}
			if (args != null && args.size() == 0) {
				rhs.put("noargs", null);
			}
		}
		return rhs;
	}

	private static HashMap<String, List<String>> addLabelToMap(String key, List<String> valList,
			HashMap<String, List<String>> labelMap) {
		if (!key.equals(valList)) {
			if (!labelMap.containsKey(key)) {
				labelMap.put(key, valList);
			} else {
				for (Map.Entry<String, List<String>> entry : labelMap.entrySet()) {
					String exiskey = entry.getKey();
					List<String> values = entry.getValue();
					boolean isAlreadyIn = false;
					if (exiskey.equals(key.toString())) {
						for (String val : values) {
							if (isInCheck(val, valList)) {
								isAlreadyIn = true;
							}
						}
						if (!isAlreadyIn) {
							values.addAll(valList);
							labelMap.put(exiskey, values);
						}
					}
				}
			}
		}
		return labelMap;
	}

	private static boolean isInCheck(String val, List<String> valList) {
		boolean isIn = false;
		for (String string : valList) {
			if (string.equals(val)) {
				isIn = true;
			}
		}
		return isIn;
	}

	private static List<String> constructNodeString(List<String> nodeString, HashMap<String, Node> rhs, String key) {
		if (rhs != null && rhs.size() > 0) {
			for (Map.Entry<String, Node> entry : rhs.entrySet()) {
				if (entry.getKey().equals("noargs")) {
					nodeString.add(key);
				} else if (!key.equals(entry.getKey()) && !isInCheck(entry.getKey(), nodeString)) {
					nodeString.add(entry.getKey());
				}
			}
		}
		return nodeString;
	}
}
