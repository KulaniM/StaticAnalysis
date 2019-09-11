package km.kulani.javadf.semantics;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bcel.generic.MethodGen;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import gr.gousiosg.javacg.stat.JCallGraph;
import gr.gousiosg.javacg.stat.MethodVisitor;
import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.util.NormalizeType;

/**
 * 
 * @author root The MethodSignature is required to give a unique identification
 *         for each method, which is used as the key when storing in HashMaps.
 *         Is used to easily find/access methods and its corresponding
 *         relations.
 *
 */

public class MethodSignature {

	private String methodSignature;

	private MethodSummary mdec;

	public MethodSignature() {
		super();
	}

	public MethodSignature(MethodSummary mcall) {
		this.mdec = mcall;
	}

	// When the MethodSummary instance is available, this method can be used to
	// generate the method signature
	public String genMethodSignature() {
		String methodSig = null;
		int i = 0;
		if (this.mdec != null) {
			String methodName = mdec.getMethodname();
			methodSig = methodName;
			methodSig = methodSig.concat("(");
			HashMap<String, TypeDetails> params = mdec.getInputParameters();
			for (Map.Entry<String, TypeDetails> entry : params.entrySet()) {
				if (i != 0) {
					methodSig = methodSig.concat(",");
				}
				TypeDetails value = entry.getValue();
				String type = NormalizeType.getNormalizedType(value.getPrimaryType());
				methodSig = methodSig.concat(type);
				i++;
			}
			methodSig = methodSig.concat(")");
			setMethodSignature(methodSig);
		}

		return getMethodSignature();
	}

	// If the node is a MethodCallExpr, generate the method signature
	public static String genMethodSignature(MethodCallExpr mcexpr, LinkedHashMap<String, String> variableMAp,
			LinkedHashMap<String, String> fildDeclarationsMap) {
		

		// track the found and not-found args and their types => for checkWith
		// methodListForEachClassInJAR in JCallGraph
		HashMap<Integer, String> allArgNodeNamesInOrder = new LinkedHashMap<>();// <arg index, arg name>
		HashMap<Integer, String> typeFound = new LinkedHashMap<>(); // <arg index, arg type>
		HashMap<Integer, String> typeNotFound = new LinkedHashMap<>();// <arg index, arg name>

		String methodSig = null;
		int i = 0;
		if (mcexpr != null) {
			NodeList<Expression> argList = mcexpr.getArguments();
			String name = mcexpr.getNameAsString();
			methodSig = name;
			methodSig = methodSig.concat("(");
			for (Expression exArg : argList) {
				System.out.println(".................." + exArg);
				allArgNodeNamesInOrder.put(i, exArg.toString());// add all args and index
			
				if (i != 0) {
					methodSig = methodSig.concat(",");
				}
				String primaryT = null;				
				// Check if the variable is a filed declaration and in the fildDeclarationsMap
				primaryT = checkTheArgsIsFromAFieldDeclaration(exArg, fildDeclarationsMap);
				if (primaryT != null) {
					methodSig = methodSig.concat(NormalizeType.getNormalizedType(primaryT));
				}

				if (primaryT == null) {
					try {
						primaryT = exArg.calculateResolvedType().describe().toString();
						methodSig = methodSig.concat(primaryT);
					} catch (Exception e) {
						typeNotFound.put(i, exArg.toString());// type not found
						System.out.println("NO EXPRESSION FOUND MethodSignature.class");
					}
				}
				// Check if the variable name node is in the variableTypeMap
				if (primaryT == null && variableMAp != null) {
					primaryT = variableMAp.get(exArg.toString().trim());
					if (primaryT != null) {
						methodSig = methodSig.concat(NormalizeType.getNormalizedType(primaryT));
					}
				}
				//TODO
				//e.g. (new X509EncodedKeySpec(array))
				if (primaryT == null) {
					if (TypeDetails.findExprType(exArg).equals("isObjectCreationExpr")) {
						ObjectCreationExpr oexpr = (ObjectCreationExpr) exArg;
						if (oexpr.getTypeAsString().equals("X509EncodedKeySpec")) {
							primaryT = "java.security.spec.KeySpec";
							methodSig = methodSig.concat(primaryT);
						}
					}
				}
				
				typeFound.put(i, primaryT);// type found
				i++;

			}
			methodSig = methodSig.concat(")");

			// Now if there are args with type not found ==> check in the
			// methodListForEachClassInJAR at JCallGraph
			if (typeNotFound.size() > 0) {
				// .......................
				// Get the
			}

			new MethodSignature().setMethodSignature(methodSig);
		}

		return methodSig;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
		
		//workaround
		if (methodSignature.equals("a(SparseArray,com.google.android.apps.chromecast.app.n.k)")) {
			this.methodSignature = "a(android.util.SparseArray,com.google.android.apps.chromecast.app.n.k)";
		}
	}

	/*
	 * // Check if the variable is a field declaration and in the
	 * fieldDeclarationsMap
	 */
	private static String checkTheArgsIsFromAFieldDeclaration(Expression exArg,
			LinkedHashMap<String, String> fildDeclarationsMap) {
		String primaryT = null;
		if (fildDeclarationsMap != null && exArg.toString().trim().contains("this")) {
			String a[] = exArg.toString().trim().split("\\.");
			// get the corresponding variable refer by this: it is the second value in the
			// array e.g. this.x
			String fieldVarialbe = a[1];
			if (a.length == 1) {
				// type is the cass name
				primaryT = JCallGraph.CURRENT_LOCAL_CLASS_PATH;
				System.out.println(
						"------------------------pirmary type from current class name------------------->>" + primaryT);
			} else if (a.length == 2) {
				// Get its type
				primaryT = fildDeclarationsMap.get(fieldVarialbe);
				System.out.println(
						"-----------------------pirmary type from class reference at fields------------------->> "
								+ primaryT);

			} else if (a.length >= 3) {
				// if the argument is like this.x.m() and the x is a previously analyzed class
				// check the m() method, which is a[2] is in the methodListForEachClassInJAR
				// defined at the JCallGraph class
				// type is the return type of the method call
				int len = a.length;
				// Find the last reference before method call
				String classRef = fildDeclarationsMap.get(fieldVarialbe);

				// here assume the methods does not have parameters
				String ref = classRef;
				for (int i = 2; i < len; i++) {
					ref = getReturnTypeOfMethodCallUsingFieldRef(a[i], ref);
				}
				primaryT = ref;

			}
		}
		return primaryT;
	}

	private static String getReturnTypeOfMethodCallUsingFieldRef(String methodName, String classRef) {
		String returnType = null;
		LinkedHashMap<String, List<MethodGen>> allMethodSignatureList = JCallGraph.methodListForEachClassInJAR;
		for (Entry<String, List<MethodGen>> entry : allMethodSignatureList.entrySet()) {
			String classNAme = entry.getKey();
			if (classNAme.equals(classRef)) {
				System.out.println(classNAme);
				List<MethodGen> methodList = entry.getValue();
				for (MethodGen mg : methodList) {
					if (getMethodSignatureGivenMethodGen(mg).equals(methodName)) {
						System.out.println(methodName);
						returnType = mg.getReturnType().toString();
						System.out.println(
								"------------------------pirmary type from chain class reference------------------->>"
										+ returnType);
					}
				}
			}
		}
		return returnType;
	}

	private static String getMethodSignatureGivenMethodGen(MethodGen mg) {
		String signature = null;
		signature = mg.getName() + "(" + MethodVisitor.argumentList(mg.getArgumentTypes()) + ")";
		return signature;
	}

}
