/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gr.gousiosg.javacg.stat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.MethodGen;

import km.kulani.javadf.JavaClassParser;
import km.kulani.javadf.JavaSourceGen;
import km.kulani.javadf.util.DeleteFileFolder;

/**
 * Constructs a callgraph out of a JAR archive. Can combine multiple archives
 * into a single call graph.
 * 
 * @author Georgios Gousios <gousiosg@gmail.com>
 * 
 */
public class JCallGraph {
	public static List<String> printList = new ArrayList<String>();

	//////////////////////////////////////////// SETTINGS...//////////////////////////////////////
	//////////////////////////////////////////// /////////////////////////////////////////////////
	//////////////// Philips Hue
	// jarPath ="/home/Automation/Philips_Hue/huelocalsdk.jar";
	// classPath ="com/philips/lighting/hue/sdk/upnp/PHUpnpManager.class";
	/////////////////////////////////////////////
	/////////////// Chromecast
	 public static final String JAR_PATH =
	 "/home/HomeScanSpecExtraction/ChromecastCP/classes-dex2jar.jar";
	 public static final String BASE_CLASS_FOLDER =
	 "/home/HomeScanSpecExtraction/ChromecastCP/";
	 public static String initClassPath =
	 "com/google/android/apps/chromecast/app/n/k.class";
	 public static String initMethodName = "a(java.lang.String,java.lang.String)";
	 public static String CURRENT_LOCAL_CLASS_PATH =
	 "com.google.android.apps.chromecast.app.n.k";
	 public static String CURRNET_CLASS_NAME = "k";
	///////////////////////////////////////////
//	////////////////// TestProject
//	public static final String JAR_PATH = "/home/HomeScanSpecExtraction/TestHomeScan/target/testcode-1.jar";
//	public static final String BASE_CLASS_FOLDER = "home/HomeScanSpecExtraction/TestHomeScan/target/classes/";
//	public static String initClassPath = "simple2/Class.class";
//	public static String initMethodName = "encrypt(java.lang.String,java.lang.String)";
//	public static String CURRENT_LOCAL_CLASS_PATH = "simple2.Class";
//	public static String CURRNET_CLASS_NAME = "Class";
	///////////////////////////////////// ..END..SETTINGS..////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////

	private static final String BASE_OUTPUT_FOLDER = System.getProperty("user.dir") + "/output2";

	// Track all the method signatures of each class in the input jar
	public static LinkedHashMap<String, List<MethodGen>> methodListForEachClassInJAR = new LinkedHashMap<>();// <classname, method
																									// data list>

	public JCallGraph() {
		super();
		new DeleteFileFolder(BASE_OUTPUT_FOLDER);
	}

	public static void main(String[] args) {
		ClassParser cp;
		try {
			String arg = JAR_PATH;

			File f = new File(arg);

			@SuppressWarnings("resource")
			JarFile jar = new JarFile(f);

			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.isDirectory())
					continue;

				if (!entry.getName().endsWith(".class"))
					continue;

				cp = new ClassParser(arg, entry.getName());
				ClassVisitor visitor = new ClassVisitor(cp.parse());
				visitor.start();
			}

			Files.createDirectories(Paths.get(BASE_OUTPUT_FOLDER));
			String filePath = BASE_OUTPUT_FOLDER + "/callgraph.txt";
			Files.deleteIfExists(Paths.get(filePath));
			Files.createFile(Paths.get(filePath));
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
			writer.append(printList.toString());
			writer.close();

			// Generate the java code from class file and call analyzer
			String javaCode = JavaSourceGen.genJavaSourceFromClass(initClassPath);
			JavaClassParser.analyseJavaMethod(javaCode, initMethodName);

		} catch (IOException e) {
			System.err.println("Error while processing jar: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
