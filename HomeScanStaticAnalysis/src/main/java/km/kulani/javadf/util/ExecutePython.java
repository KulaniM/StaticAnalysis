package km.kulani.javadf.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecutePython {

	public static String execPythonScript(String fileName) {
		String s = null;
		try {
			Process p = Runtime.getRuntime().exec("python " + fileName);
			s = printResponseofCmdProcess(p);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static String execPythonScript(String fileName, String argument1, String argument2) {
		String s = null;
		try {
			Process p = Runtime.getRuntime().exec(new String[] { "python", fileName, argument1, argument2 });
			s = printResponseofCmdProcess(p);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static String printResponseofCmdProcess(Process p) {
		String output = null;
		try {
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// read the output from the command
			while ((output = stdInput.readLine()) != null) {
				return output;
			}

			// read any errors from the attempted command
			while ((output = stdError.readLine()) != null) {
				return output;
			}
			// System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	public static String printAppendedResponseofCmdProcess(Process p) {
		String output = "";
		try {
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// read the output from the command
			String line;
			int i =0;
			while ((line = stdInput.readLine()) != null) {				
				i++;
				if (i>3) {					
					output += line + System.getProperty("line.separator");
				}

			}
			if (output != "") {
				return output;
			}
			// read any errors from the attempted command
			while ((output = stdError.readLine()) != null) {
				output = "Error";
				return output;
			}
			// System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

}
