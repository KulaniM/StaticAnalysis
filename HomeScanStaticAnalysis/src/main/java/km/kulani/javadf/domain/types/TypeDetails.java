package km.kulani.javadf.domain.types;

import java.util.HashSet;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;

/*
 * This class provides an object to store the type details of a variable
 * 
 * 
 */
public class TypeDetails {

	private String valueID;
	private String primaryType;
	private String domianType;
	private Node node;
	private String nodeType;
	private Expression expression;

	public TypeDetails() {
		super();
	}

	public TypeDetails(TypeDetails typeDetails) {
		this.valueID = typeDetails.getValueID();
		this.primaryType = typeDetails.getPrimaryType();
		this.domianType = typeDetails.getDomianType();
		this.node = typeDetails.getNode();
		try {
			Expression expr = node.findFirst(Expression.class).get();
			Node nodeOfExpression = expr.getParentNodeForChildren();
			if (nodeOfExpression.equals(node)) {
				this.nodeType = findExprType(expr);
				this.expression = expr;
			}
		} catch (Exception e) {
			System.out.println("NO EXPRESSION FOUND " + "TypeDetails.class");
		}
	}

	public String getValueID() {
		return valueID;
	}

	public void setValueID(String valueID) {
		this.valueID = valueID;
	}

	public String getPrimaryType() {
		return primaryType;
	}

	public void setPrimaryType(String primaryType) {
		this.primaryType = primaryType;
	}

	public String getDomianType() {
		return domianType;
	}

	public void setDomianType(String domianType) {
		String ndomainType = "";
		String[] v = domianType.split(" -> ");		
		Set<String> hash_Set = new HashSet<String>(); 
		for (String value : v) {			
			hash_Set.add(value.trim());
		}
		for (String value : hash_Set) {
			if(!ndomainType.equals("")) {
				ndomainType = ndomainType + " -> " + value;
			}else {
				ndomainType = value;
			}
		}
		this.domianType = ndomainType;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public static String findExprType(Expression expr) {

		if (expr.isAnnotationExpr()) {
			return "isAnnotationExpr";
		} else if (expr.isArrayAccessExpr()) {
			return "isArrayAccessExpr";
		} else if (expr.isArrayCreationExpr()) {
			return "isArrayCreationExpr";
		} else if (expr.isArrayInitializerExpr()) {
			return "isArrayInitializerExpr";
		} else if (expr.isAssignExpr()) {
			return "isAssignExpr";
		} else if (expr.isVariableDeclarationExpr()) {
			return "isVariableDeclarationExpr";
		} else if (expr.isBinaryExpr()) {
			return "isBinaryExpr";
		} else if (expr.isObjectCreationExpr()) {
			return "isObjectCreationExpr";		
		} else if (expr.isBooleanLiteralExpr()) {
			return "isBooleanLiteralExpr";
		} else if (expr.isCastExpr()) {
			return "isCastExpr";
		} else if (expr.isCharLiteralExpr()) {
			return "isCharLiteralExpr";
		} else if (expr.isClassExpr()) {
			return "isClassExpr";
		} else if (expr.isConditionalExpr()) {
			return "isConditionalExpr";
		} else if (expr.isDoubleLiteralExpr()) {
			return "isDoubleLiteralExpr";
		} else if (expr.isEnclosedExpr()) {
			return "isEnclosedExpr";
		} else if (expr.isFieldAccessExpr()) {
			return "isFieldAccessExpr";
		} else if (expr.isIntegerLiteralExpr()) {
			return "isIntegerLiteralExpr";
		} else if (expr.isLambdaExpr()) {
			return "isLambdaExpr";
		} else if (expr.isLiteralExpr()) {
			return "isLiteralExpr";
		} else if (expr.isMarkerAnnotationExpr()) {
			return "isMarkerAnnotationExpr";
		} else if (expr.isMethodCallExpr()) {
			return "isMethodCallExpr";
		} else if (expr.isMethodReferenceExpr()) {
			return "isMethodReferenceExpr";
		} else if (expr.isNameExpr()) {
			return "isNameExpr";
		} else if (expr.isNormalAnnotationExpr()) {
			return "isNormalAnnotationExpr";
		} else if (expr.isNullLiteralExpr()) {
			return "isNullLiteralExpr";
		}
		return null;
	}

}
