package km.kulani.javadf.controlflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import gr.gousiosg.javacg.stat.JCallGraph;
import km.kulani.javadf.util.ExecutePython;

/**
 * 
 * @author root The actual class which executes the "controlflow.py" and
 *         generates the flows. The output of the python script is an JSON
 *         Object which stores the flow for each method signature in a
 *         JSONArrary
 * 
 *         The public methods generateForwardMethodCallGraph(nodeMap,
 *         methodSignature) and generateBackwardMethodCallGraph(nodeMap,
 *         methodSignature) can be used to generate the flows as a
 *         List<FlowEntry>
 *
 */

public class FlowProcess {

	private static FlowStructure jsonStringToJSON(String jsonString) {

		JSONObject obj = new JSONObject(jsonString);
		System.out.println(obj.length());

		JSONObject f = (JSONObject) obj.get("forward");
		// System.out.println(f + "\n");

		JSONObject b = (JSONObject) obj.get("backward");		
		//System.out.println(b);

		FlowStructure flow = jsontoFlowStructure(f, b);

		return flow;
	}

	// Convert the JSON output from the python script to the FlowEntries and then
	// FlowStructures
	private static FlowStructure jsontoFlowStructure(JSONObject forward, JSONObject backward) {
		FlowStructure flow = new FlowStructure();
		HashMap<String, List<FlowEntry>> forwardflow = new HashMap<>();
		HashMap<String, List<FlowEntry>> backwardflow = new HashMap<>();

		// forward flow
		Iterator<?> forwardkeys = forward.keys();
		while (forwardkeys.hasNext()) {
			String fkey = (String) forwardkeys.next();
			List<FlowEntry> valueDetails = jsonValueToFlowEntryArray(forward.get(fkey).toString());
			forwardflow.put(fkey, valueDetails);
		}

		// backward flow
		Iterator<?> backwardkeys = backward.keys();
		while (backwardkeys.hasNext()) {
			String bkey = (String) backwardkeys.next();
			List<FlowEntry> valueDetails = jsonValueToFlowEntryArray(backward.get(bkey).toString());
			backwardflow.put(bkey, valueDetails);
		}

		flow.setForwardflow(forwardflow);
		flow.setBackwardflow(backwardflow);

		return flow;
	}

	/*
	 * e.g.: {"encrypt(java.lang.String,java.lang.String)/simple2.Class":[
	 * "toString()/java.lang.Exception",
	 * "println(java.lang.String)/java.io.PrintStream",
	 * "doFinal(byte[])/javax.crypto.Cipher","<init>()/java.lang.StringBuilder",
	 * "encodeToString(byte[])/java.util.Base64$Encoder",
	 * "getEncoder()/java.util.Base64","setKey(java.lang.String)/simple2.Class",
	 * "append(java.lang.String)/java.lang.StringBuilder",
	 * "toString()/java.lang.StringBuilder",
	 * "getBytes(java.lang.String)/java.lang.String",
	 * "getInstance(java.lang.String)/javax.crypto.Cipher",
	 * "init(int,java.security.Key)/javax.crypto.Cipher"],
	 * 
	 * "setKey(java.lang.String)/simple2.Class":[
	 * "copyOf(byte[],int)/java.util.Arrays",
	 * "<init>(byte[],java.lang.String)/javax.crypto.spec.SecretKeySpec",
	 * "getBytes(java.lang.String)/java.lang.String",
	 * "getInstance(java.lang.String)/java.security.MessageDigest",
	 * "digest(byte[])/java.security.MessageDigest",
	 * "printStackTrace()/java.security.NoSuchAlgorithmException",
	 * "printStackTrace()/java.io.UnsupportedEncodingException"]}
	 */
	private static List<FlowEntry> jsonValueToFlowEntryArray(String value) {
		List<FlowEntry> flowEntryList = new ArrayList<FlowEntry>();
		value = value.replace("[", "");
		value = value.replace("]", "");
		List<String> stringList = new ArrayList<String>(Arrays.asList(value.trim().split("\",\"")));

		for (int i = 0; i < stringList.size(); i++) {
			String v = stringList.get(i);
			FlowEntry entry = new FlowEntry();
			String[] parts = v.split("/");
			entry.setMethodSignature(parts[0].replace("\"", ""));
			entry.setClassName(parts[1].replace("\"", ""));
			flowEntryList.add(entry);
		}

		return flowEntryList;
	}

	// Given the method signature of the root, this method generates the forward
	// call graph.
	public static List<FlowEntry> generateForwardMethodCallGraph(String methodSignature) {

		String jsonString = ExecutePython.execPythonScript("./controlflow.py", methodSignature,
				JCallGraph.CURRENT_LOCAL_CLASS_PATH);
		//System.out.println(jsonString);
		FlowStructure flowStruct = FlowProcess.jsonStringToJSON(jsonString);
		List<FlowEntry> fflow = flowStruct.getForwardflow().get(methodSignature + "/" + JCallGraph.CURRENT_LOCAL_CLASS_PATH);

		return fflow;
	}

	// Given the method signature of the root, this method generates the backward
	// call graph.
	public static List<FlowEntry> generateBackwardMethodCallGraph(String methodSignature) {

		String jsonString = ExecutePython.execPythonScript("./controlflow.py", methodSignature,
				JCallGraph.CURRENT_LOCAL_CLASS_PATH);
		System.out.println("testing !!!");
		System.out.println(jsonString);
		FlowStructure flowStruct = FlowProcess.jsonStringToJSON(jsonString);
		List<FlowEntry> bflow = flowStruct.getBackwardflow().get(methodSignature + "/" + JCallGraph.CURRENT_LOCAL_CLASS_PATH);

		return bflow;
	}

}
