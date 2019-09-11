package km.kulani.javadf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class SolveSymbols {

	public static void solveLabels(LinkedHashMap<String, HashMap<String, List<String>>> allNodeMap, int counter) {
		HashMap<String, HashMap<String, List<String>>> prevlabelMap = new HashMap<>();
		prevlabelMap = createCopy(prevlabelMap, allNodeMap);
		for (Entry<String, HashMap<String, List<String>>> entryprevlabelMap : prevlabelMap.entrySet()) {
			System.out.println("\n" + entryprevlabelMap.getKey() + " :  Initial ............................."
					+ entryprevlabelMap.getValue());
		}
		///////////////////////////////////////// BEGIN////////////////////////////////////////////////////////////
		for (Entry<String, HashMap<String, List<String>>> entryofAllNodeMap : allNodeMap.entrySet()) {
			System.out.println(
					"\n" + entryofAllNodeMap.getKey() + " :  Label Map ............................." + counter);
			String methodSign = entryofAllNodeMap.getKey();
			HashMap<String, List<String>> labelMap = entryofAllNodeMap.getValue();
			// Get inputs of particular method
			List<String> inputs = entryofAllNodeMap.getValue().get("input"); // input : [myKey]
			for (Entry<String, List<String>> currentVarKeyNLabels : labelMap.entrySet()) {
				System.out.println(currentVarKeyNLabels.getKey() + " : " + currentVarKeyNLabels.getValue());

			if (inputs != null) {
				for (String input : inputs) {
					if (isInCheck(input, currentVarKeyNLabels.getValue())) {// key : ["UTF-8", myKey, sha]
						// re-search the list where the key is used -==> replace the val with list
						String currentKey = currentVarKeyNLabels.getKey(); // key
						List<String> currentValList = currentVarKeyNLabels.getValue();// ["UTF-8", myKey, sha]
						for (Entry<String, List<String>> researchVarKeyNLabels : labelMap.entrySet()) {
							// if current key is in valueList ==> replace | e.g. sha : ["SHA-1", key] ==>
							// sha : ["SHA-1", "UTF-8", myKey, sha]
							if (isInCheck(currentKey, researchVarKeyNLabels.getValue())) {
								String preKey = researchVarKeyNLabels.getKey();
								List<String> preVAlList = researchVarKeyNLabels.getValue();
								// String toRemove = null;
								// for (String researchval : preVAlList) {
								// if (currentKey.equals(researchval)) {
								// toRemove = researchval;
								// }
								// }
								// preVAlList.remove(getListIndex(toRemove, preVAlList));
								// // add new val
								// preVAlList.addAll(currentValList);
								// labelMap.put(preKey, removeDuplicate(preVAlList));
								// // update all nodemap
								// allNodeMap.put(methodSign, labelMap);
								/////////////////////////// Replacing the above commented
								int c = 0;
								for (String researchval : preVAlList) {
									if (currentKey.equals(researchval)) {
										//instead remove move rest of the elements by one index
										//then add the new list
										// c = start point 
										preVAlList = moveListElementsByOneIndex(preVAlList,c);
										preVAlList.addAll(currentValList);
										labelMap.put(preKey, removeDuplicate(preVAlList));
										allNodeMap.put(methodSign, labelMap);
										
									}
									c += 1;
								}
								///////////////////////////
							}
						}
					}
				}
			}
			}
		}
		counter += 1;
		if (!prevlabelMap.equals(allNodeMap)) {
			solveLabels(allNodeMap, counter);
			///////////////////////////////////////// END //////////////////////////////////
		} else {
			///////////////////////////////////// AFTER CONVERGE////////////////////////////
			////////////////////////////////////// PROPGATE INPUTS//////////////////////////
			// solve the input parameters with actual inputs
			// find the method call location
			// use class variables/constants or method local variables/constants
			for (Entry<String, HashMap<String, List<String>>> entryofAllNodeMap : allNodeMap.entrySet()) {
				HashMap<String, List<String>> labelMap = entryofAllNodeMap.getValue();
				// If the current method signature is MAIN method////////////////////////////
				// Then propagate the constants to the rest of the variables which uses them.
				// LATER chekc whether it requires to do for all the methods !!! ???
				labelMap = propagateConstantsInMain(labelMap, entryofAllNodeMap.getKey());
				/////////////////////////////////////////////////////////////////////////////////////
				// find inner keys which are method signatures by checking equality with keys in
				///////////////////////////////////////////////////////////////////////////////////// allnodemap
				allNodeMap = propagateActualInputsToMethods(labelMap, allNodeMap);
			}
			for (Entry<String, HashMap<String, List<String>>> entryprevlabelMap : allNodeMap.entrySet()) {
				System.out.println("\n" + entryprevlabelMap.getKey() + " :  Finale ............................."
						+ entryprevlabelMap.getValue());
			}
		}
	}
	
	private static List<String> moveListElementsByOneIndex(List<String> inputList, int startIndex){
		List<String> newList = new ArrayList<>();
		int c = 0;
		for (String input : inputList) {
			if (c == startIndex) {
				//skip the element
			} else {
				newList.add(input);
				
			}
			c += 1;
		}
		

		return newList;		
	}

	private static List<String> removeDuplicate(List<String> valList) {
		List<String> newlist = new ArrayList<>();
		Set<String> stringStet = new HashSet<>();
		for (String string : valList) {
			stringStet.add(string);
		}
		for (String string : stringStet) {
			newlist.add(string);
		}
		return newlist;
	}

	private static int getListIndex(String val, List<String> valList) {
		int i = 0;
		for (String string : valList) {
			if (string.equals(val)) {
				return i;
			}
			i += 1;
		}
		return -1;
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

	private static String listToString(List<String> list) {
		String listString = "";
		int c = 0;
		listString = listString.concat("[");
		for (String s : list) {
			if (c != 0) {
				listString += ",";
			}
			listString += s;
		}
		listString = listString.concat("]");
		return listString;
	}

	private static HashMap<String, HashMap<String, List<String>>> createCopy(
			HashMap<String, HashMap<String, List<String>>> prevlabelMap,
			HashMap<String, HashMap<String, List<String>>> allNodeMap) {
		for (String m : allNodeMap.keySet()) {
			HashMap<String, List<String>> x = new HashMap<>();
			for (Entry<String, List<String>> labelmap : allNodeMap.get(m).entrySet()) {
				List<String> newL = new ArrayList<>();
				for (String label : labelmap.getValue()) {
					newL.add(label);
				}
				x.put(labelmap.getKey(), newL);
			}
			prevlabelMap.put(m, x);
		}
		return prevlabelMap;
	}

	private static HashMap<String, List<String>> propagateConstantsInMain(HashMap<String, List<String>> labelMap,
			String methodSign) {
		if (methodSign.equals("main(java.lang.String[])")) {
			for (Entry<String, List<String>> currentVarKeyNLabels : labelMap.entrySet()) {
				String mainMkey = currentVarKeyNLabels.getKey();
				// e.g. originalString
				List<String> mainMkeysVal = currentVarKeyNLabels.getValue();
				// e.g. =["secret_message_to_smart_device"]
				// check if mainMkey is used in the other key's values
				for (Entry<String, List<String>> checkVarKeyNLabels : labelMap.entrySet()) {
					List<String> check = checkVarKeyNLabels.getValue();
					int k = 0;
					for (String val : check) {
						if (val.equals(mainMkey)) {
							String preVal = check.get(k);
							preVal = preVal.concat("=");
							if (mainMkeysVal.size() == 1) {
								for (String newVal : mainMkeysVal) {
									check.set(k, preVal.concat(newVal));
								}
							} else if (mainMkeysVal.size() > 1) {
								int c = 0;
								for (String newVal : mainMkeysVal) {
									//////////////////////////////////////////
									// IF there is a key with the name of newVal
									// THEN replace with newVal before next step
									for (Entry<String, List<String>> varKeyNLabels : labelMap.entrySet()) {
										List<String> valList = varKeyNLabels.getValue();
										if (varKeyNLabels.getKey().equals(newVal)) {
											newVal = newVal.concat("=" + listToString(valList));
										}
									}
									//////////////////////////////////////////
									if (c == 0) {
										preVal = preVal.concat("[");
										preVal = preVal.concat(newVal);
									} else if (c != 0) {
										preVal = preVal.concat(",");
										preVal = preVal.concat(newVal);
									}
									c += 1;
								}
								preVal = preVal.concat("]");
								check.set(k, preVal);
							}
						}
						k += 1;
					}
				}
			}
		}
		return labelMap;
	}

	private static LinkedHashMap<String, HashMap<String, List<String>>> propagateActualInputsToMethods(
			HashMap<String, List<String>> labelMap, LinkedHashMap<String, HashMap<String, List<String>>> allNodeMap) {
		for (Entry<String, List<String>> currentVarKeyNLabels : labelMap.entrySet()) {
			String potentialMethodSig = currentVarKeyNLabels.getKey();
			List<String> actualInputArgs = currentVarKeyNLabels.getValue();
			// pass the actual input args to the method implementation
			for (Entry<String, HashMap<String, List<String>>> aallNodeMap : allNodeMap.entrySet()) {
				if (potentialMethodSig.equals(aallNodeMap.getKey())) {// method implementation
					// get defined arguments
					List<String> definedArgs = aallNodeMap.getValue().get("input");
					// assign actual args to defined args
					if (definedArgs!= null && definedArgs.size()>0) {
						System.out.println("definedArgs size: " + definedArgs.size());
						System.out.println("actualInputArgs size: " + actualInputArgs.size());
						if (definedArgs.size() == actualInputArgs.size()) { ////////////FIX LATER !!!!
							int i = 0;
							for (String acArgs : actualInputArgs) {
								String preDArg = definedArgs.get(i);
								String updatepreDArg = preDArg.concat("=" + acArgs);
								definedArgs.set(i, updatepreDArg);
								// System.out.println("key: "+aallNodeMap.getKey()+ "value:
								// "+aallNodeMap.getValue());
								// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
								for (Entry<String, List<String>> suballNodeMap : aallNodeMap.getValue().entrySet()) {
									List<String> valLsit = suballNodeMap.getValue();
									int j = 0;
									for (String val : valLsit) {
										if (val.equals(preDArg)) {// input arg present
											// set actual argument
											valLsit.set(j, updatepreDArg); // allnodemap auto updated
											// System.out.println("key: "+aallNodeMap.getKey()+ "value:
											// "+aallNodeMap.getValue());
											// System.out.println("!!!!!!!!!$$$$$$$$$$$$$$!!!!!!!!!!!!!");
										}
										j += 1;
									}
								}
								i += 1;
							}
						}
					}
				}
			}
		}
		return allNodeMap;
	}
}
