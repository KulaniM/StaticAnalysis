# SpecExtractraction
HomeScan Specification Extraction
This project is based on following existing projects on GitHub.
1) JavaParser/JavaSymbolSovler project on github: https://github.com/javaparser/javasymbolsolver
2) Java Call Graph project on github: https://github.com/gousiosg/java-callgraph

### JavaClassParser.java
The main class which initiates the method analysis. JavaClassParser.java parses the source code of a class file to the MethodAnalyser.java which extends the VoidVisitorAdapter.java of JavaSymbolSovler project.

		// Analyze methods inside the class
		cu.accept(new MethodAnalyser(), null); 

### MethodAnalyser.java
Following that, MethodAnalyser visits all the methods in the parsed class. During the visit the JavaSymbolSolver allows to access the method code as program nodes. Program nodes can be statements, expressions or unlabled leaf nodes.
MethodAnalyser.java of HomeScan further analyses the MethodCallExpr to find security sensitive method calls/functions in the program. For example, encryption, decryption, signature, hashing, and etc. HomeScan identifies such method calls by the method signature of them corresponding to the Java API implementations.
Example: Inferring encryption or decryption

if (signatures.equals("init(int,javax.crypto.spec.SecretKeySpec)")|| signatures.equals("init(int,PublicKey)")) {
	if (params.get(0).toString().equals("1")) {
		domainType = "enc";
	} else if (params.get(0).toString().equals("2")) {
		domainType = "dec";
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

	if (trackLocalNodeTypeMap.containsKey(children.get(0))) {
		if (trackLocalNodeTypeMap.get((children).get(0)).equals("enc")) {
			methodParamTypeMap.put(params.get(0), "plaintext");//one method param to do final
		}else if(trackLocalNodeTypeMap.get((children).get(0)).equals("dec")) {
			methodParamTypeMap.put(params.get(0), "ciphertext");//one method param to do final
		}
								    
	}
}

Consequently, MethodAnalyser.java assigns a domain type lable to each method call in interest.


### Type System
HomeScan defines several rules to infer types through static analysis.

#### Encryption and Decryption (Crypto Types)

Source Code for encrypting a string:

	public static String a(String message, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(1, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes("UTF-8")));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}
Given the source code HomeScan acheives following objectives:

1.) Identify cryptograhic operations.

2.) Identify whether the key is synchronous or asynchrounus.

3.) Identify encryption or decryption operation.

##### Rules:
HomeScan refers the Java API for cryptographic operations: https://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html

1.) If HomeScan finds method call expressions with "getInstance(java.lang.String)" as signature  and "Cipher" as the scope, it identifies that cryptographic operations are in the source code.  Hence, creates a new instance of Crypto Type Class and map it with the reference node of java Cipher object.

2.) Next, HomeScan analyses the value of first argument to the getInstance function. If the argument contatins "AES" or "DES" HomeScan updates the mode property of the Crypto Type instance as "s" representing synchornous key, or if contains "RSA" or "DSA" update it as "a" representing asynchronous key.

3.) Next, if HomeSCan finds "init(int,javax.crypto.spec.SecretKeySpec)" method signature, it further analyses the corresponding method call expression to find the value of the first argment to the init function. If the first argument is "1" HomeSCan creates a Encryption type instance (Subclass of Crypto Type Class) or else if it is "2" creates a Decrption type instance.

4.) Finally, if HomeScan finds subsequent method signature with "doFinal(byte[])", it updates the type of the this method call expression using the information in Crypto Type instance. Based on the sync/asnync and enc/dec the keys will be assigned. Hence, the ouput type will be one from the following;
senc(msg,k), sdec(msg,k), aenc(msg,pk) or adec(msg,sk)

### DomainTypePopulater.java
Next, HomeScan propagate the identified domain types within the program node tree of each method and between method trees. 

(1) HomeScan do the propagation within the method in three steps. First, HomeScan analyses program nodes which are VariableDeclarations and propagate the types assigned at RHS to the LHS of the expressoin. Second, similar to first, propate types from RHS to LHS at AssignmentExprs. Lastly, HomeScan propagates the types from the children to the return statement (parent). 

(2) HomeScan propagates the assigned domain type of each return statement to its method call sites.

In the JavaClassParser.java, we do the step (1) and (2) recursively untile the domain types are fully propated within all program nodes of all method trees.

### Current Issues:
(1) The domian type is not fully propogated if the corresponding value is indirectly related to the return statement. For example, the value is an array referrence, but the return statement is based on elements of the array.

(2) If a method returns nothing (void), then the identified domain types within that method does not propogate to the program nodes in other methods which uses them.
