# pip install graphviz
from graphviz import Source
import os
import sys 

#/home/Automation/dotty-0.9.0-RC1/bin
sys.path.append("/usr/bin/dot")

directorypath = './output/'#./java-callgraph-master/output


dirList = next(os.walk(directorypath))[1]
print dirList
for d in dirList:
    print d;
    fileList = os.listdir(directorypath + d)
    for f in fileList:
        abpath = directorypath + d + '/'+ f
        text_from_file = str()
        with open (abpath) as file:
            text_from_file = file.read()
        src = Source(text_from_file)
        src.render(abpath+'.gv',view=False)
