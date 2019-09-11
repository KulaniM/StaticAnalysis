package km.kulani.javadf;

import java.io.IOException;

import gr.gousiosg.javacg.stat.JCallGraph;
import jd.core.DecompilerException;
import km.kulani.javadf.util.ExecutePython;

public class JavaSourceGen {

	// Decompiled by using JD-Core library
	
	private static String generateJavaClassFromByteCode(String jarPath, String internalClassName) {
		String code = null;
		try {
			code = new jd.core.Decompiler().decompileClass(jarPath, internalClassName);
			System.out.println(code);
			System.out.println("Java code generation");
		} catch (DecompilerException e) {
			e.printStackTrace();
		}
		return code;
	}

	// Decompiled by Procyon v0.5.30
	private static String generateJavaClassFromByteCode(String classPath) {
		String code = null;

		try {
			Process p = Runtime.getRuntime().exec(new String[] { "java", "-jar",
					"lib/procyon-decompiler-0.5.30.jar", classPath });
			code = ExecutePython.printAppendedResponseofCmdProcess(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Java code generation using Procyon v0.5.30");
		return code;
	}
	
	public static String genJavaSourceFromClass(String classPath) {
		String javacode =  null;
		try {
			javacode = JavaSourceGen.generateJavaClassFromByteCode(JCallGraph.BASE_CLASS_FOLDER + classPath);
		} catch (Exception e) {
			System.out.println("Exception at JCallGraph calling JavaSourceGen generateJavaClassFromByteCode method !!!");
		}
		
		if(javacode == "Error") {
			try {				
				javacode = JavaSourceGen.generateJavaClassFromByteCode(JCallGraph.JAR_PATH, classPath);
			} catch (Exception e) {
				System.out.println("Exception at JCallGraph calling JavaSourceGen JDCORE  method !!!");
			}
		}
		return javacode;
	}	
}
