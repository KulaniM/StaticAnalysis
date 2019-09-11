package km.kulani.javadf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import gr.gousiosg.javacg.stat.JCallGraph;
import km.kulani.javadf.controlflow.FlowEntry;
import km.kulani.javadf.controlflow.FlowLengthProcess;
import km.kulani.javadf.controlflow.FlowProcess;
import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.util.PrintDottyGraphs;
import km.kulani.javadf.util.Util;

/**
 * 
 * @author root This is the main class the method analysis is done 1)Create a
 *         type solver 2)Prepare the class source code 3)Analyze each method in
 *         the class 4)Access the allNodeMap generated while the initial
 *         analysis 5)Find the minimum no of iterations required for an
 *         efficient propagation of the domain types 6)Populate the domain types
 *         in all nodes in all method trees 7)generate the dotty graphs for each
 *         method
 *
 */
public class JavaClassParser {

	public static HashMap<String, HashMap<String, List<TypeDetails>>> globalReturnStmtMap = new HashMap<>();
	// <classname,<methodsignature,return type list>>

	public static void analyseJavaMethod(String classCode, String methodSign) {
		// Set up a minimal type solver that only looks at the classes used to run this
		// sample.
		CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
		combinedTypeSolver.add(new ReflectionTypeSolver());

		// Configure JavaParser to use type resolution
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
		JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);

		// Parse the source code of the class needs to be analyzed
		CompilationUnit cu = JavaParser.parse(classCode);

		////////////////////////// .INFER..SECURITY..SENSITIVE.FUNCTIONS.//////////////////////////////
		cu.accept(new MethodAnalyser(), null);
		///////////////////////////////////////////////////////////////////////////////////////////////
		///////////////// solve labels

		SolveSymbols.solveLabels(MethodAnalyser.alllabelMap, 0);

		System.out.println("############## Once the initial method analysis is completed ###############");

		///////////////////////////////////////////////////////////////////////////////////////////////
		// ..RECURSIVELY..UPDATE..THE..ALLNODEMAP..UNTILE..THE..DOMAIN..TYPES..ARE..FULLY..PROPAGATED
		///////////////////////////////////////////////////////////////////////////////////////////////
		LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> currentAllNodeMap = MethodAnalyser.allNodeMap;
		LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> newAllNodeMap = new LinkedHashMap<>();

		///////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////// ..FIND..THE..MIN..NUMBER..OF..ITERATIONS..REQUIRED..//////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////
		int maxFlowLenghth = 0;
		maxFlowLenghth = FlowLengthProcess.findTheMaxFlowLength(MethodAnalyser.methodListWithDomainTypesIdentified);
		System.out.println("MAX_FLOW_LENTH = " + maxFlowLenghth);
		////////////////////////// .ITERATION.NUMBER.FOUND..////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////

		////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////// .START.THE.PROPAGATING..///////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////
		LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> savedMap= new LinkedHashMap<>();
		int i = 1;

		do {

			if (newAllNodeMap.size() > 0) {
				currentAllNodeMap = newAllNodeMap;
			}
			if (newAllNodeMap.size() > 0) {
				savedMap = createCopy(newAllNodeMap);
			}

			System.out.println(i + "th/nd/rd Iteration.==========================================================");

			// For each method node tree propagate the type to the parent nodes of the tree
			LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> updatedAllNodeMap = DomainTypePopulater
					.propagateDomainTypesFromMethodCallToVariablesThenToReturnNodes(currentAllNodeMap);

			// Fore each method, propagate the return types to the method call sites
			LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> secondupdatedAllNodeMap = DomainTypePopulater
					.propagteReturnTypeToMethodCallSites(updatedAllNodeMap);

			newAllNodeMap = secondupdatedAllNodeMap;
			i++;
			
		} while (checkConvergence(savedMap,newAllNodeMap));//(i == maxFlowLenghth);//(i <= 4);(i <= 3);//
		///////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////// .END.OF.PROPAGATING..//////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////

		System.out.println("############## Once the domain type populations is completed ###############");

		///////////////////////////////// ..GENERATE.THE.DOTTY.GRAPH../////////////////////////////////////
		PrintDottyGraphs.generateAndPrintDottyForAllNodeMap(newAllNodeMap);
		///////////////////////////////////////////////////////////////////////////////////////////////////

		///////////////////////////////////////////////////////////////////////////////////////////////////
		//// ..RECIRSIVELY.ANALYZE.THE.NEXT.CALLER.METHOD.OF.THE.CURRENT.METHOD..//////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////
		List<FlowEntry> x = FlowProcess.generateBackwardMethodCallGraph(methodSign);
		String className;
		String methodName;
		if (x != null) {

			for (FlowEntry f : x) {
				// Add the current class with its return stmt map to the global map
				globalReturnStmtMap.put(JCallGraph.CURRENT_LOCAL_CLASS_PATH, MethodAnalyser.returnStmtMap);

				className = f.getClassName();// next class
				methodName = f.getMethodSignature();// next method in that class
				System.out.println("class = " + className + " method = " + methodName);
				System.out.println(
						"=======================....Next Method to be Analysed is:..===============================  "
								+ methodName);
				System.out.println(
						"=======================.............STARTING NEXT :........===============================  ");
				JCallGraph.CURRENT_LOCAL_CLASS_PATH = className;
				JCallGraph.CURRNET_CLASS_NAME = Util.getClassName(className);

				// Generate the java code from class file and call analyzer
				String classPath = Util.getClassPath(className);
				String javaCode = JavaSourceGen.genJavaSourceFromClass(classPath);
				JavaClassParser.analyseJavaMethod(javaCode, methodName);
			}
		} else {
			System.out.println(
					"=========================== .............FINISHED.........=================================");
			System.out.println(
					"===========================.....Last Method Analysed is:..=================================  "
							+ methodSign);
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////// ......END........./////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////
	}

	private static boolean checkConvergence(
			LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> previous,
			LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> current) {
		
		Set<String> keys = previous.keySet();
		for (String methodSig : keys) {
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> pnodeMap = previous.get(methodSig);
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> cnodeMap = current.get(methodSig);
			
			Set<LinkedHashMap<Node, Node>> nodeKeys = pnodeMap.keySet();
			for (LinkedHashMap<Node, Node> nodeKey : nodeKeys) {
				TypeDetails pType = pnodeMap.get(nodeKey);
				TypeDetails cType = cnodeMap.get(nodeKey);
				
				for ( Node key : nodeKey.keySet()) {
					System.out.println(key.toString());
				}
				if (pType!= null && cType != null && !(pType.getDomianType().equals(cType.getDomianType()))) {
					return false;
				}
			}
		}
		return true;
	}
	
	private static LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> createCopy(
			LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> allNodeMap) {
		LinkedHashMap<String, LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails>> newNodeMap = new LinkedHashMap<>();
		for (String m : allNodeMap.keySet()) {
			LinkedHashMap<LinkedHashMap<Node, Node>, TypeDetails> newnodeDetailsMap = new LinkedHashMap<>();
			for (Entry<LinkedHashMap<Node, Node>, TypeDetails> nodesDetails : allNodeMap.get(m).entrySet()) {
				LinkedHashMap<Node, Node> newnodeMap = new LinkedHashMap<>();
				TypeDetails typeD = new TypeDetails();
				typeD.setDomianType(nodesDetails.getValue().getDomianType());
				for (Node node : nodesDetails.getKey().keySet()) {
					newnodeMap.put(node, nodesDetails.getKey().get(node));
				}				
				newnodeDetailsMap.put(newnodeMap, typeD);
			}
			newNodeMap.put(m, newnodeDetailsMap);
		}
		return newNodeMap;
	}

}
