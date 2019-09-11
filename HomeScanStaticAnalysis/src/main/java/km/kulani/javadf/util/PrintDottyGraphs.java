package km.kulani.javadf.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.javaparser.ast.Node;

import gr.gousiosg.javacg.stat.JCallGraph;
import km.kulani.javadf.domain.types.TypeDetails;

public class PrintDottyGraphs {
	private static final String BASE_OUTPUT_FOLDER = System.getProperty("user.dir") + "/output";
	private static int incrementer = 0;

	public PrintDottyGraphs() {
		super();
		new DeleteFileFolder(BASE_OUTPUT_FOLDER);
		
	}
	
	public static void generateAndPrintDottyForAllNodeMap(LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> secondupdatedAllNodeMap) {
		for (Entry<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> entry : secondupdatedAllNodeMap
				.entrySet()) {
		
			String methodSignature = entry.getKey();
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap = entry.getValue();
			System.out.println("\n Method:......." + methodSignature);
			//////////////////////////////////////
			///////// generate the dotty node tree for nodeMap
			PrintDottyGraphs.generateDottyGraphForMethod(nodeMap, methodSignature);
			////////////////////////////////////////
			/////////////////////////////////////////
		}
		PrintDottyGraphs.printDotty();
	}
	
	private static void printDotty() {
		ExecutePython.execPythonScript("./printDotty.py");// Path: ./java-callgraph-master/printDotty.py
	}
	
	
	private static void generateDottyGraphForMethod(LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap, String methodName) {
		String dot = "digraph \"" + methodName + "\" {\n label=\"" + methodName + "\"\n";
		dot += "{\n node [shape=box]} \n";

		for (Map.Entry<LinkedHashMap<Node, Node>, TypeDetails> nodemapEntry : nodeMap.entrySet()) {
			LinkedHashMap<Node, Node> nodes = nodemapEntry.getKey();
			TypeDetails typeDetails2 = nodemapEntry.getValue();

			String primaryType = typeDetails2.getPrimaryType();
			String domainType = typeDetails2.getDomianType();
			String index = typeDetails2.getValueID();
			dot = generateDottyCompatibleString(dot, nodes, primaryType,domainType, index, nodeMap);
		}
		dot += "}\n";

		writeDottyStringToFile(dot, methodName);// dottyGraphString
	}
	
	
	private static String generateDottyCompatibleString(String inputdottyGraphString, LinkedHashMap<Node, Node> nodes,
			String primaryType,String domainType, String index, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> nodeMap) {
		Node node = null;
		Node parent = null;
		String parentIndex = null;

		for (Map.Entry<Node, Node> nAp : nodes.entrySet()) {
			node = nAp.getKey();
			parent = nAp.getValue();
		}
		for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nodeM : nodeMap.entrySet()) {
			LinkedHashMap<Node, Node> nodeNparent = nodeM.getKey();
			for (Map.Entry<Node, Node> nAp2 : nodeNparent.entrySet()) {
				Node parent2 = nAp2.getKey();
				if (parent2.equals(parent)) {
					parentIndex = nodeM.getValue().getValueID();
				}
			}
		}
		if (domainType != "" && domainType != null && !domainType.equals("NA") && !domainType.equals("method")) {
			System.out.println(node);
			System.out.println("domain type: " + domainType);
		}
		//System.out.println("key: " + node + "   type: " + type);
		inputdottyGraphString += parentIndex + " -> " + index + "[label=\"" + node.toString().replace("\"", "")
				+ " Type is: " + primaryType + "\"" + "];\n";
		inputdottyGraphString += index + "[label=\"Node is: " + node.toString().replace("\"", "") + "\n Primary T is: "
				+ primaryType + "\n Domain T is: "
						+ domainType + "\"" + ",fontcolor=\"blue\"];\n  ";
		return inputdottyGraphString;
	}

	private static void writeDottyStringToFile(String dottyCompatibleString, String methodName) {
		try {
			Files.createDirectories(Paths.get(BASE_OUTPUT_FOLDER+"/"+JCallGraph.CURRNET_CLASS_NAME));
			String filePath = BASE_OUTPUT_FOLDER+"/"+JCallGraph.CURRNET_CLASS_NAME + "/" + methodName + "_" + (incrementer++) + ".txt";
			Files.deleteIfExists(Paths.get(filePath));
			Files.createFile(Paths.get(filePath));
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
			writer.append(dottyCompatibleString);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
