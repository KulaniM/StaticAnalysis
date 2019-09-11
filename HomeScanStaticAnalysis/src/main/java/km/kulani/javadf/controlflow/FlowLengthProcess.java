package km.kulani.javadf.controlflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.github.javaparser.ast.Node;

import km.kulani.javadf.domain.types.TypeDetails;

/**
 * 
 * @author root In the JavaClasParser this class is used to find the minimal
 *         number of recursions required to efficiently propagate the domain
 *         types throughout all the methods in the selected class;
 *
 */

public class FlowLengthProcess {

	/*
	 * MAX ITERATIONS REQUIRED IS: = 3-1 = 2 e.g.: [setKey(java.lang.String)->
	 * encrypt(java.lang.String,java.lang.String)-> main(java.lang.String)]
	 */
	public static int findTheMaxFlowLength(List<String> methodListWithDomainIdentified) {
		List<List<String>> legthsList = findTheLongestFlow(methodListWithDomainIdentified);
		int currentMax = 0;
		for (List<String> x : legthsList) {
			currentMax = Math.max(currentMax, x.size());
		}

		return currentMax - 1;
	}

	/*
	 * output: [[decrypt(java.lang.String,java.lang.String),
	 * main(java.lang.String)], [setKey(java.lang.String),
	 * encrypt(java.lang.String,java.lang.String), main(java.lang.String)],
	 * [setKey(java.lang.String), decrypt(java.lang.String,java.lang.String),
	 * main(java.lang.String)], [alteredEncrypt(java.lang.String),
	 * main(java.lang.String)], [encrypt(java.lang.String,java.lang.String),
	 * main(java.lang.String)]]
	 */
	private static List<List<String>> findTheLongestFlow(List<String> methodListWithDomainIdentified) {
		HashMap<String, List<FlowEntry>> flowMap = generateBackwardFlowForAllNodeMap(methodListWithDomainIdentified);
		System.out.println(flowMap);
		List<List<String>> methodLists = new ArrayList<List<String>>();
		System.out.println("............................................");
		for (Entry<String, List<FlowEntry>> entry : flowMap.entrySet()) {
			String methodSignature = entry.getKey();
			System.out.println(methodSignature + " =========> analyse");
			List<FlowEntry> backwFlow = entry.getValue();
			if (backwFlow != null) {
				if (backwFlow.size() > 0) {
					for (FlowEntry fe : backwFlow) {
						List<String> msList = new ArrayList<>();
						String ms = fe.getMethodSignature();
						msList.add(methodSignature);
						msList.add(ms);
						List<String> msList2 = exploreFlow(flowMap, ms, msList);
						if (msList2.size() > 0) {
							methodLists.add(msList2);
						}
					}
				}
			}
			System.out.println(methodLists);
		}
		return methodLists;
	}

	// Helping method to recursively explore the flow of a given method signature
	private static List<String> exploreFlow(HashMap<String, List<FlowEntry>> flowMap, String methodSignature,
			List<String> msList) {
		List<FlowEntry> feList = flowMap.get(methodSignature);
		System.out.println("....... " + feList);
		if (feList != null) {
			if (feList.size() > 0) {
				for (FlowEntry fe : feList) {
					String ms = fe.getMethodSignature();
					msList.add(ms);
					msList = exploreFlow(flowMap, ms, msList);
				}
			}
		}
		return msList;
	}

	// Generate the backward flow for each method in allNodeMap, as a list of flow
	// entries
	public static HashMap<String, List<FlowEntry>> generateBackwardFlowForAllNodeMap(
			List<String> methodListWithDomainIdentified) {
		
		HashMap<String, List<FlowEntry>> flowMap = new HashMap<>();		
		for(String methodSignature: methodListWithDomainIdentified) {
			System.out.println(methodSignature);
			List<FlowEntry> flow = generateFlowsForNodeMap(methodSignature, true, false);
			flowMap.put(methodSignature, flow);
		}
		
		
		
		return flowMap;
	}

	// Can be used to generate the list of flow entries of either forward or
	// backward flows
	private static List<FlowEntry> generateFlowsForNodeMap(String methodSignature, boolean isbackward,
			boolean isforward) {
		if (isforward) {
			// print forward call graph for each method in the class
			List<FlowEntry> fflow = FlowProcess.generateForwardMethodCallGraph(methodSignature);
			if (fflow != null) {
				for (FlowEntry f : fflow) {
					System.out.println("forward: " + f.getMethodSignature() + "/" + f.getClassName());
				}
			}
			return fflow;
		}
		if (isbackward) {
			// print the backward call graph for each method in the class
			List<FlowEntry> bflow = FlowProcess.generateBackwardMethodCallGraph( methodSignature);
			if (bflow != null) {
				for (FlowEntry b : bflow) {
					System.out.println("backward: " + b.getMethodSignature() + "/" + b.getClassName());
				}
			}
			return bflow;
		}
		return null;
	}

}
