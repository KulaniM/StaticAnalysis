package km.kulani.javadf.util;

public class NormalizeType {
	
	public static String getNormalizedType(String type) {
        //System.out.println("Type getNormalizedType " + type);
		if (type != null) {
			if (type.trim().equals("String") | type.trim().equals("java.lang.String")) {
				type = "java.lang.String";
			} 
			else if (type.trim().equals("String[]") | type.trim().equals("java.lang.String[]")) {
				type = "java.lang.String[]";
			} 
			else if (type.trim().equals("Integer") | type.trim().equals("java.lang.Integer")) {
				type = "java.lang.Integer";
			}
		}
		return type;
	}

}
