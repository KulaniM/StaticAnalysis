package km.kulani.javadf.controlflow;

/**
 * 
 * @author root
 * FlowEntry is a single method. The object stores the information of the method i.e., method signature and its class name
 *
 */

public class FlowEntry {
	
	private String methodSignature;
	private String className;
	
	public String getMethodSignature() {
		return methodSignature;
	}
	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
}
