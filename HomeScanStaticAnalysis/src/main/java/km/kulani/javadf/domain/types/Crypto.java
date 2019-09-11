package km.kulani.javadf.domain.types;

import java.util.HashMap;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;

import km.kulani.javadf.TreeAnalysis;

public class Crypto extends DomainType {

	protected String instanceTypeString;
	protected String mode;
	protected Node referenceNode;
	private String key;

	public Crypto() {

	}

	public String getInstanceTypeString() {
		return instanceTypeString;
	}

	public void setInstanceTypeString(String instanceTypeString) {
		this.instanceTypeString = instanceTypeString;
		if (instanceTypeString.contains("AES")||instanceTypeString.contains("DES")) {
			setMode("s");
			setKey("k");
		} else if (instanceTypeString.contains("RSA")||instanceTypeString.contains("DSA")) {
			setMode("a");
			setKey("s/p");
		}
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Node getReferenceNode() {
		return referenceNode;
	}

	public void setReferenceNode(Node referenceNode) {
		this.referenceNode = referenceNode;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static HashMap<String, DomainType> updateCryptoReferenceNode(MethodCallExpr mexpr, Node currentNode,
			HashMap<String, DomainType> domainHashMap) {
		// E.g.: Cipher.getInstance("AES/ECB/PKCS5Padding")
		// Get the argument =>"AES/ECB/PKCS5Padding"
		String instanceTypeString = mexpr.getArgument(0).toString();
		// Find the reference node which stores the instance
		List<Node> siblings = TreeAnalysis.getSibilings(currentNode);
		for (Node sib : siblings) {
			if (domainHashMap.containsKey(sib.toString())) {
				Crypto crypto = (Crypto) domainHashMap.get(sib.toString());
				crypto.setInstanceTypeString(instanceTypeString);
			}
		}
		return domainHashMap;
	}

	public static HashMap<String, DomainType> updateCryptoTypeEncOrDec(Node currentNode,
			HashMap<String, DomainType> domainHashMap) {
		String domainType = null;
		List<Node> children = currentNode.getChildNodes();
		Crypto crypto = null;
		String k = null;
		for (Node child : children) {
			if (domainHashMap.containsKey(child.toString())) {
				crypto = (Crypto) domainHashMap.get(child.toString());
				k = child.toString();
			}
			if (child.toString().equals("1")) {
				domainType = "enc";
			}
			if (child.toString().equals("2")) {
				domainType = "dec";
			}
		}
		if (crypto != null && domainType.equals("enc")) {
			Encryption enc = null;
			if(crypto.getKey().equals("s/p")) {
				 enc = new Encryption(crypto, "pk");
			}else {
				 enc = new Encryption(crypto, crypto.getKey());				
			}
			domainHashMap.put(k, enc);

		} else if (crypto != null && domainType.equals("dec")) {
			Decryption dec = null;
			if(crypto.getKey().equals("s/p")) {
				dec = new Decryption(crypto, "sk");
			}else {
				dec = new Decryption(crypto, crypto.getKey());				
			}
			domainHashMap.put(k, dec);
		}

		return domainHashMap;
	}

}
