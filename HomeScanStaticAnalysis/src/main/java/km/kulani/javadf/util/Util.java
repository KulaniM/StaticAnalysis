package km.kulani.javadf.util;

public class Util {

	//com.google.android.apps.chromecast.app.n.k
	//convert to  "/home/HomeScanSpecExtraction/ChromecastCP/com/google/android/apps/chromecast/app/n/k.class"
	public static String getClassPath(String classWithPackageName) {
		String classPath ="";
		classWithPackageName = classWithPackageName.replace(".", "/");
		classPath = classPath.concat(classWithPackageName);
		classPath = classPath.concat(".class");		
		return classPath;
	}
	
	public static String getClassName(String classWithPackageName) {
		String[] array = classWithPackageName.split("\\.");
		return array[array.length-1];
	}
}
