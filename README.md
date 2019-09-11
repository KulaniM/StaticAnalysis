# HomeScan Static Analyzer
HomeScan uses a static analyzer to extract protocol information from available programs of a smart home system.
This project is based on following existing projects on GitHub.
1) JavaParser/JavaSymbolSovler project on github: https://github.com/javaparser/javasymbolsolver
2) Java Call Graph project on github: https://github.com/gousiosg/java-callgraph

### JavaClassParser.java
The main class which initiates the method analysis. JavaClassParser.java parses the source code of a class file to the MethodAnalyser.java which extends the VoidVisitorAdapter.java of JavaSymbolSovler project.

		// Analyze methods inside the class
		cu.accept(new MethodAnalyser(), null); 

