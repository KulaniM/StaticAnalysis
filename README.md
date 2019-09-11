# HomeScan Static Analyzer
HomeScan uses this static analyzer to extract protocol information from available programs of a smart home system.
This project is based on following existing projects on GitHub.
1) JavaParser/JavaSymbolSovler project on github: https://github.com/javaparser/javasymbolsolver
2) Java Call Graph project on github: https://github.com/gousiosg/java-callgraph
3) Java Decompiler project on github: https://github.com/ststeiger/procyon

To use this static analyzer, download the project and set the following values at the 

#### JAR_PATH
This property requires to decompile the jar using JD-GUI.

JAR_PATH = <absoloute path to the jar>

#### initClassPath
The class that uses Security APIs. For example, we included a class that uses Java Cryptography API in the TestHomeScan project.

initClassPath = "simple2/Class.class";
	



		
	
