package km.kulani.javadf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import gr.gousiosg.javacg.stat.JCallGraph;
import km.kulani.javadf.domain.types.Crypto;
import km.kulani.javadf.domain.types.DomainType;
import km.kulani.javadf.domain.types.Hash;
import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.semantics.MethodSignature;
import km.kulani.javadf.semantics.MethodSummary;
import km.kulani.javadf.semantics.ValueTypeDirectory;
import km.kulani.javadf.util.NormalizeType;

/**
 * 
 * @author root A class which is inherited from the
 *         com.github.javaparser.ast.visitor.VoidVisitorAdapter. This class is
 *         called by the JavaClassParser, which gives the source code of a
 *         class. The visit method automatically analyzes each method in this
 *         class. On top of the base analysis provided by the java parser,
 *         HomeScan infers domain specific types available in the program, by
 *         analyzing the method call expressions.
 */

public class MethodAnalyser extends VoidVisitorAdapter<Void> {

	private static int number = 0;
	private static LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap;
	private static LinkedHashMap<LinkedHashMap<Node, Node>, String> annotatednodeMap;
	public static LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> allNodeMap = new LinkedHashMap<>();
	public static LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, String>> allannotatedNodeMap = new LinkedHashMap<>();// all annotated nodes //methodsig, node, annotation

	public static HashMap<String, List<TypeDetails>> returnStmtMap;// <method signature, return type
																	// details nodes>
	public static LinkedHashMap<String, HashMap<String, Node>> allassignmentExprMap = new LinkedHashMap<>();
	public static LinkedHashMap<String, HashMap<Node, Expression>> allvariableDecExprMap = new LinkedHashMap<>();
	public static LinkedHashMap<String, HashMap<Node, Expression>> allselectedExprNodeMap = new LinkedHashMap<>();
	public static LinkedHashMap<String, HashMap<String, String>> allnameExprTypeMap = new LinkedHashMap<>();//to store types of method params
	public static LinkedHashMap<String, HashMap<String, String>> allMethodCallExprTypeMap = new LinkedHashMap<>();//to store types of method call expressions
	public static LinkedHashMap<String, HashMap<String, MethodCallExpr>> allmethodCallExprMap = new LinkedHashMap<>();
	public static LinkedHashMap<String, HashMap<String, List<String>>> alllabelMap = new LinkedHashMap<>();// all labels
	public static HashMap<String, NodeList<Parameter>> allMethodDeclMap = new HashMap<>();// signature and param nodes
	public static List<String> methodListWithDomainTypesIdentified;
	HashMap<Node, Expression> variableDecExprMap;
	HashMap<String, Node> assignmentExprMap;
	LinkedHashMap<String, String> variableNameTypeMap = new LinkedHashMap<>();
	HashMap<Node, String> methodParamTypeMap = new HashMap<>();//local <paramNode, type>
	HashMap<Node, String> trackLocalNodeTypeMap = new HashMap<>();//local <node, type>
	HashMap<Node, Expression> selectedExprNodeMap = new HashMap<>();
	HashMap<String, String> nameExprTypeMap = new HashMap<>(); // to store types of method params
	HashMap<String, MethodCallExpr> methodCallExprMap = new HashMap<>(); //<methodsignature, methodcallexpr>
	HashMap<String, String> methodCallExprTypeMap = new HashMap<>(); //<node string, type> to store types of method params
	public static ValueTypeDirectory directory;
	private MethodSummary methodsum;
	private String currentMethodSignature;
	private List<TypeDetails> returnNodes;
	private LinkedHashMap<String, String> fildDeclarationsMap = new LinkedHashMap<>();// <varialbeNode, Type>
	HashMap<String, List<String>> labelMap; //track labels

	// Stores references of API calls and corresponding domain type object
	private HashMap<String, DomainType> domainHashMap;

	public MethodAnalyser() {
		super();
		directory = ValueTypeDirectory.getInstance();
		returnStmtMap = new HashMap<>();
		methodListWithDomainTypesIdentified = new ArrayList<String>();
	}

	// Track filed declarations: global variables and their types
	@Override
	public void visit(FieldDeclaration n, Void arg) {
		NodeList<VariableDeclarator> fieldVariables = n.getVariables();
		for (VariableDeclarator vd : fieldVariables) {
			String globalVarName = vd.getNameAsString();
			String type = vd.getTypeAsString();
			fildDeclarationsMap.put(globalVarName, type);
		}
		super.visit(n, arg);
	}

	@Override
	public void visit(MethodDeclaration n, Void arg) {
		System.out.println(
				"===================================== START METHOD ANALYSIS =============================================================");
		returnNodes = new CopyOnWriteArrayList<>();
		domainHashMap = new HashMap<String, DomainType>();

		List<Node> childList = n.getChildNodes();
		String methdodName = n.getName().toString();
		Node rootNode = n.getParentNodeForChildren();
		nodeMap = new LinkedHashMap<>();
		annotatednodeMap = new LinkedHashMap<>();
		assignmentExprMap = new HashMap<>();
		variableDecExprMap = new HashMap<>();
		selectedExprNodeMap = new HashMap<>();// reset for each method
		nameExprTypeMap = new HashMap<>();
		methodCallExprTypeMap = new HashMap<>();
		methodParamTypeMap = new HashMap<>();//local use
		trackLocalNodeTypeMap = new HashMap<>();//local use
		methodCallExprMap = new HashMap<>();
		labelMap = new HashMap<>(); //track labels

		// Create a summary of the Method
		this.methodsum = new MethodSummary();
		this.methodsum.setMethodname(methdodName);
		this.methodsum.setMethodNode(rootNode);
		this.methodsum.setClassName(JCallGraph.CURRNET_CLASS_NAME);
		NodeList<Parameter> params = n.getParameters();
		LinkedHashMap<String, TypeDetails> inputparams = new LinkedHashMap<>();
		for (Parameter p : params) {
			TypeDetails paramDetails = new TypeDetails();
			paramDetails.setPrimaryType(p.getTypeAsString());
			inputparams.put(p.toString(), paramDetails);
			// add variables representing params and their type to the map
			variableNameTypeMap.put(p.getNameAsString(), p.getTypeAsString());
		}
		this.methodsum.setInputParameters(inputparams);
		TypeDetails returnStmtDeails = new TypeDetails();
		returnStmtDeails.setPrimaryType(n.getTypeAsString());
		this.methodsum.setReturnStmt(returnStmtDeails);

		// generate method signature
		String signature = new MethodSignature(methodsum).genMethodSignature();
		System.out.println(signature);
		currentMethodSignature = signature;

		// set the primary type of the return node of the current method
		String primaryType = NormalizeType.getNormalizedType(returnStmtDeails.getPrimaryType());
		TypeDetails typeDetails = new TypeDetails();
		typeDetails.setValueID(Integer.toString(number));
		typeDetails.setPrimaryType(primaryType);
		typeDetails.setNode(rootNode);
		typeDetails.setDomianType("method");
		// add the root node to the nodeMap
		number = number + 1;
		LinkedHashMap<Node, Node> nodeAparent = new LinkedHashMap<>();
		nodeAparent.put(rootNode, rootNode.getParentNodeForChildren());
		nodeMap.put(nodeAparent, typeDetails);
		// initialize label map with params
		if (params != null & labelMap.size() == 0 ) {
			for (Parameter parameter : params) {
				labelMap.put(parameter.getNameAsString(), new ArrayList<String>());				
			}

		}

		// Dotty String beginning and root node
		nodeAndTypeTreeGeneration(childList, 0, labelMap, params);
		super.visit(n, arg);
		System.out.println(
				"===================================== END METHOD ANALYSIS =============================================================");

		// add the visited method to the list
		allNodeMap.put(signature, nodeMap);
		allannotatedNodeMap.put(signature, annotatednodeMap);//annotaed nodes
		returnStmtMap.put(currentMethodSignature, returnNodes);
		allassignmentExprMap.put(signature, assignmentExprMap);
		allvariableDecExprMap.put(signature, variableDecExprMap);//Used at analyseVarialbeDeclaration() in VariableDeclarationAnalyser class
		allselectedExprNodeMap.put(signature, selectedExprNodeMap); //allselectedExprNodeMap and allnameExprTypeMap to be used together
		allnameExprTypeMap.put(signature, nameExprTypeMap);
		allMethodCallExprTypeMap.put(signature, methodCallExprTypeMap);
		allmethodCallExprMap.put(signature, methodCallExprMap);
		allMethodDeclMap.put(signature, params);
		alllabelMap.put(signature,labelMap);// label management
	}

	// Updates the nodeMap including type details for each program node.
	public void nodeAndTypeTreeGeneration(List<Node> childNodeList, int stage, HashMap<String, List<String>> labelMap,NodeList<Parameter> outerMethodParams) {
		String primaryType = "NotSpecified";
		String domainType = "NA";
		String annotation = "NA"; 
		//if variabledecl/assignmentexpr and r.h.s is varialbe/constant => r.h.s  assign to => l.h.s
		//if varialbedecl/assignmentexpr and r.h.s has an operator => (r.h.s,r.h.s) assign to => l.h.s
		//if varialbedecl/assignmentexpr and r.h.s is a methodcallexpr => the reference of r.h.s => l.h.s
		//if varialbedecl/assignmentexpr and r.h.s is a methodcallexpr => the argument of r.h.s => l.h.s
		//if varialbedecl/assignmentexpr and r.h.s is a methodcallexpr => the (several arguments) of r.h.s => l.h.s
		//if varialbedecl/assignmentexpr and r.h.s is a methodcallexpr => the multiple method calls ??? of r.h.s => l.h.s
		
		
		for (int i = 0; i < childNodeList.size(); i++) {
			Node node = childNodeList.get(i);
			System.out.println("==> " + node);
			
			if (methodParamTypeMap.containsKey(node)) {				
				domainType = methodParamTypeMap.get(node);
			}
			//generate symbol map
			// IF params available
			labelMap = ForwardSymbolAnalysis.analyzeMethod(node, variableNameTypeMap, fildDeclarationsMap, labelMap, outerMethodParams);
			

			//}
			// Find the type using the expression of the node
			try {
				if (node.findFirst(Expression.class).isPresent()) {
					Expression expr = node.findFirst(Expression.class).get();

					Node n = expr.getParentNodeForChildren();
					if (n.equals(node)) {
						// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
						// @@@@@@@@@@@@@@@@@@@@@@@@@@@INFER DOMAIN TYPES@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
						if (TypeDetails.findExprType(expr).equals("isNameExpr")) {
							String name = ((NameExpr) expr).getName().asString();
							if (methodParamTypeMap.containsKey(n)) {
								nameExprTypeMap.put(name, methodParamTypeMap.get(n));// only if nameExpr is a param
								domainType = methodParamTypeMap.get(n);
							}
						}
						// To be used for AssignementExpr Analysis
						if (TypeDetails.findExprType(expr).equals("isVariableDeclarationExpr")) {
							selectedExprNodeMap.put(n, expr);
							variableDecExprMap.put(n, expr);
							// set the type of each variable name node
							NodeList<VariableDeclarator> variables = ((VariableDeclarationExpr) expr).getVariables();
							for (VariableDeclarator v : variables) {
								//if a parameter type is identified before e.g. at the object creation expression
								//List<String> predefined_param_types = Arrays.asList("secret_key");
								variableNameTypeMap.put(v.getNameAsString(), v.getType().asString());								
								
							}
							VariableDeclarationExpr vexpr = (VariableDeclarationExpr) expr;
							// Find the variable declarations of Cipher API class
							if (vexpr.getElementType().toString().equals("Cipher")) {
								Crypto cr = new Crypto();
								domainHashMap.put(vexpr.getVariable(0).getName().asString(), cr);
							}
							if (vexpr.getElementType().toString().equals("MessageDigest")) {
								Hash hf = new Hash();
								domainHashMap.put(vexpr.getVariable(0).getName().asString(), hf);
							}
						} else if (TypeDetails.findExprType(expr).equals("isAssignExpr")) {
							assignmentExprMap.put(n.toString(), n);
							selectedExprNodeMap.put(n, expr);
						} else if (TypeDetails.findExprType(expr).equals("isMethodCallExpr")) {
							MethodCallExpr mexpr = (MethodCallExpr) expr;
							selectedExprNodeMap.put(n, expr);
							String signatures = MethodSignature.genMethodSignature(mexpr, variableNameTypeMap,
									fildDeclarationsMap);
							NodeList<Expression> params = mexpr.getArguments();
							System.out.println("============= Method Signature ==================>" + signatures);
							methodCallExprMap.put(signatures, mexpr);
							//////////////////////////////////////////////////////////////////////////////
							/////////////////// PROPOGATE EXISTING INFERED DOMAIN TYPES
							//////////////////////////////////////////////////////////////////////////////
							// Propagate Return types
							Expression classRef = null;
							try {
								classRef = mexpr.getScope().get();
								if (classRef != null) {
									System.out.println(
											"============= Class Ref ==================>" + classRef.toString());
									HashMap<String, List<TypeDetails>> methodList = JavaClassParser.globalReturnStmtMap
											.get(classRef.toString());
									for (Entry<String, List<TypeDetails>> e : methodList.entrySet()) {
										if (e.getKey().equals(signatures)) {
											for (TypeDetails dt : e.getValue()) {
												if (dt.getDomianType() != "NA") {
													domainType = dt.getDomianType();
													System.out.println(domainType);
												}
											}
										}
									}
								}
							} catch (Exception e1) {
								System.out.println("NO SCOPE IN EXPRESSION " + "MethodAnalyser.class");
							}
							System.out.println("domain type: " + domainType);
							// propagate parameter types to call site							
							/////////////////////////////////////////////////////////////////////////////////
							/////////////////////////// ENCRYPTION OR DECRYPTION //
							////////////////////////////////////////////////////////////////////////////////
							if ((signatures.equals("getInstance(java.lang.String)")
									|| signatures.equals("getInstance(java.lang.String,java.lang.String)"))
									&& (mexpr.getScope().isPresent()
											&& mexpr.getScope().get().toString().equals("Cipher"))) {
								
								domainType = "sync/async";
								System.out.println(params.get(0).toString());
								domainHashMap = Crypto.updateCryptoReferenceNode(mexpr, node, domainHashMap);
								if (params.get(0).toString().toUpperCase().equals("\"AES/ECB/PKCS5PADDING\"")) {
									domainType = "symmetric";
								} else if (params.get(0).toString().toUpperCase().equals("\"RSA/NONE/PKCS1PADDING\"")) {
									domainType = "asymmetric";
								}


							} else if (signatures.equals("generatePublic(java.security.spec.KeySpec)") ) {
								domainType = "publicKey";
							} else if (signatures.equals("init(int,javax.crypto.spec.SecretKeySpec)")
									|| signatures.equals("init(int,PublicKey)")) {
								if (params.get(0).toString().equals("1")) {
									domainType = "encryption";
								} else if (params.get(0).toString().equals("2")) {
									domainType = "decryption";
								}
								if (signatures.equals("init(int,PublicKey)")) {
									NodeList<Expression> x = mexpr.getArguments();
									methodParamTypeMap.put(x.get(1), "public_key");
								}
								if (signatures.equals("init(int,javax.crypto.spec.SecretKeySpec)")) {
									NodeList<Expression> x = mexpr.getArguments();
									methodParamTypeMap.put(x.get(1), "symmetric_key");
								}
							

								List<Node> children = node.getChildNodes();// cipher, init, 1/2, secretKey
								trackLocalNodeTypeMap.put(children.get(0), domainType);
								domainHashMap = Crypto.updateCryptoTypeEncOrDec(node, domainHashMap);

							} else if (signatures.equals("doFinal(byte[])") || signatures.equals("doFinal()")) {
								List<Node> children = node.getChildNodes();
								for (Node child : children) {
									if (domainHashMap.containsKey(child.toString())) {
										domainType = ((Crypto) domainHashMap.get(child.toString())).getType();
									}
								}
								//set param as plaintext/ciphertext
								// check if reference cipher node is set
								if (trackLocalNodeTypeMap.containsKey(children.get(0))) {
									if (trackLocalNodeTypeMap.get((children).get(0)).equals("encryption")) {
										methodParamTypeMap.put(params.get(0), "plaintext");//one method param to do final
										domainType = "ciphertext";
									}else if(trackLocalNodeTypeMap.get((children).get(0)).equals("decryption")) {
										methodParamTypeMap.put(params.get(0), "ciphertext");//one method param to do final
										domainType = "plaintext";
										annotation = "encryption("+params.get(0);	//encryption(strToEncrypt,cipher.key)										
									}								    
								}
								
								/////////////////////////////////////////////////////////////////////////////////
								//////////////////////////// HASH FUNCTIONS
								/////////////////////////////////////////////////////////////////////////////////
							} else if (signatures.equals("getInstance(java.lang.String)")
									&& (mexpr.getScope().isPresent()
											&& mexpr.getScope().get().toString().equals("MessageDigest"))) {
								domainType = "hash";
								domainHashMap = Hash.updateHashReferenceNode(mexpr, node, domainHashMap);
							} else if (signatures.equals("digest()") || signatures.equals("digest(byte[])")) {
								domainType = "hash";
								List<Node> children = node.getChildNodes();
								for (Node child : children) {
									if (domainHashMap.containsKey(child.toString())) {
										domainType = ((Hash) domainHashMap.get(child.toString())).getType();
									}
								}
								////////////////////////////////////////////////////////////////////////////////
								//////////////////////////////// SIGNATURE
								////////////////////////////////////////////////////////////////////////////////
							} else if (signatures.equals("initSign(java.security.PrivateKey)")
									| signatures.equals("sign()")) {
								domainType = "sign";
								////////////////////////////////////////////////////////////////////////////////
								///////////////////////////////////// OTHER
								////////////////////////////////////////////////////////////////////////////////
							} else if (signatures.equals("send(java.net.DatagramPacket)")) {
								domainType = "Multicast UDP message";
							}
							
							if (!domainType.equals("NA")) {
								methodCallExprTypeMap.put(node.toString(), domainType);
							}
 
						} else if (TypeDetails.findExprType(expr).equals("isObjectCreationExpr")) {
							ObjectCreationExpr oexpr = (ObjectCreationExpr) expr;
							selectedExprNodeMap.put(n, expr);
							if (oexpr.getTypeAsString().equals("DatagramPacket")) {
								NodeList<Expression> x = oexpr.getArguments();
							}
							
							if (oexpr.getTypeAsString().equals("X509EncodedKeySpec")) {
								NodeList<Expression> x = oexpr.getArguments();
//								methodParamTypeMap.put(x.get(0), "key");
								domainType = "encodedKey";
							}
							if (oexpr.getTypeAsString().equals("SecretKeySpec")) {
								NodeList<Expression> x = oexpr.getArguments();
								methodParamTypeMap.put(x.get(0), "secret_key(KEY)");
								for (Expression xi : x) {
									System.out.println("foundddddddddddddddddddddddd" + xi);									
								}
							}
						}
						// @@@@@@@@@@@@@@@@@@@@@@@@@@@END INFER DOMAIN TYPES@@@@@@@@@@@@@@@@@@@@@@@@@@@@
						// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

						primaryType = expr.calculateResolvedType().describe().toString();
						System.out.println(expr.getClass().getSimpleName());

					}
				}
			} catch (Exception e) {
				System.out.println("NO EXPRESSION FOUND " + "MethodAnalyser.class");
			}

			// Set the type of variable name nodes
			if (primaryType == "NotSpecified" && variableNameTypeMap.containsKey(node.toString())) {
				primaryType = variableNameTypeMap.get(node.toString());
			}

			// Track the return statements in the program
			if (node.getMetaModel().toString().equals("ReturnStmt")) {
				TypeDetails returnT = new TypeDetails();
				returnT.setNode(node);
				returnT.setDomianType(domainType);
				this.methodsum.setReturnStmt(returnT);
				returnNodes.add(returnT);
			}
			// track the methods with domain types identified
			if (domainType != "" && domainType != null && !domainType.equals("NA") && !domainType.equals("method")) {
				if (!methodListWithDomainTypesIdentified.contains(currentMethodSignature.toString())) {
					methodListWithDomainTypesIdentified.add(currentMethodSignature);
				}
			}
			// Normalize the type
			TypeDetails typeDetails = new TypeDetails();
			typeDetails.setValueID(Integer.toString(number));
			typeDetails.setPrimaryType(primaryType);
			typeDetails.setNode(node);
			typeDetails.setDomianType(domainType);

			// String normalizedType = directory.generarateValueTypeDirectory(typeDetails);
			System.out.println("Directory size :                                                        "
					+ directory.getValueTypeDetailsMap().size());

			// Update the nodeMap
			number = number + 1;
			LinkedHashMap<Node, Node> nodeAparent = new LinkedHashMap<>();
			nodeAparent.put(node, node.getParentNode().get());
			nodeMap.put(nodeAparent, typeDetails);
			annotatednodeMap.put(nodeAparent, annotation);
			List<Node> innerchildNodeList = node.getChildNodes();
			if (innerchildNodeList.size() > 0) {
				nodeAndTypeTreeGeneration(innerchildNodeList, stage++, labelMap, outerMethodParams);
			}
			domainType = "NA";// reset
		}
	}

}
