package km.kulani.javadf.semantics;

import java.util.HashMap;
import java.util.Map;

import km.kulani.javadf.domain.types.TypeDetails;
import km.kulani.javadf.util.NormalizeType;

/*
 * This class manages the set of variables - remove redundant
 * 
 * 
 */
public class ValueTypeDirectory {

	private static ValueTypeDirectory myObj;
	private HashMap<Integer, TypeDetails> valueTypeDetailsMap = new HashMap<Integer, TypeDetails>();
	private int newkey = 0;

	private ValueTypeDirectory() {
		super();
	}

	/**
	 * Create a static method to get instance.
	 */
	public static ValueTypeDirectory getInstance() {
		if (myObj == null) {
			myObj = new ValueTypeDirectory();
		}
		return myObj;
	}
 /// ############# return the normalized type of the input node
	public String generarateValueTypeDirectory(TypeDetails inputTypeDetials) {
		boolean isInMap = false;
		for (Map.Entry<Integer, TypeDetails> entry : this.valueTypeDetailsMap.entrySet()) {
			TypeDetails value = entry.getValue();

			if (value.getNode().equals(inputTypeDetials.getNode()) && NormalizeType.getNormalizedType(
					value.getPrimaryType()).equals(NormalizeType.getNormalizedType(inputTypeDetials.getPrimaryType()))) {
				isInMap = true;
			}
		}
		if (!isInMap) {
			this.valueTypeDetailsMap.put(newkey++, inputTypeDetials);
			
		}
		return NormalizeType.getNormalizedType(inputTypeDetials.getPrimaryType());
	}



	public HashMap<Integer, TypeDetails> getValueTypeDetailsMap() {
		return valueTypeDetailsMap;
	}

}
