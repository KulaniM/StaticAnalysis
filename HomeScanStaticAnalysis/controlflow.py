############################################################################################
########### depends on the output of this project https://github.com/gousiosg/java-callgraph
import sys
import json

callgraph = []
callgraphList = []

flowgraph = {}
forward = {}
backward = {}

init_caller_method =sys.argv[1] #str('a()')
init_caller_class = sys.argv[2]#str('hha')        
init_callee_method = sys.argv[1]#str('getActivity()')
init_callee_class = sys.argv[2]#str('com.google.android.chimera.Fragment')


##
##  M:class1:<method1>(arg_types) (typeofcall)class2:<method2>(arg_types)
##  The line means that method1 of class1 called method2 of class2. The type of call can have one of the following values

with open('output2/callgraph.txt') as f:
    content = f.read()
    callgraph = content.strip().split(', ')
    callgraph[0] = callgraph[0].replace('[', '')
    callgraph[-1] = callgraph[-1].replace(']', '')
    #print(len(callgraph))
    #print(callgraph[-1])
    callgraph = list(set(callgraph))
    for call in callgraph:
        callgraphD = {}
        temp1 = call.strip().split(' ')
        temp2 = temp1[0].strip().split(':')
        if temp2[0] is "M":
            temp3 = temp1[1].strip().split(':')
            callgraphD['caller_method'] = temp2[2]
            callgraphD['caller_class'] = temp2[1]
            callgraphD['callee_method'] = temp3[1]
            temp4 = temp3[0].strip().split(')')
            callgraphD['callee_class'] =  temp4[1]     
            callgraphD['callee_invoke-type'] =  temp4[0].replace('(', '')   
            callgraphList.append(callgraphD)

##### Print all the methods called by the given_method of given_class### FORWARD FLOW
def forwardflow(caller_method,caller_class):
    j = 0
    temp5 = []
    fward = []
    for calldir in callgraphList:
        if str(calldir.get("caller_method"))==caller_method and str(calldir.get("caller_class"))==caller_class:
            j = j + 1
            temp5.append(calldir)
            fward.append(calldir.get('callee_method')+'/'+calldir.get('callee_class'))
    if len(fward):
        forward.update({caller_method+'/'+caller_class: fward})
    return temp5

##### Print all the methods wich call within the given_method of given_class ### BACKWARD FLOW
def backwardflow(callee_method,callee_class):
    j = 0
    temp6 = []
    bward = []
    for calldir in callgraphList:
        if str(calldir.get("callee_method"))==callee_method and str(calldir.get("callee_class"))==callee_class:
            j = j + 1
            temp6.append(calldir)
            bward.append(calldir.get('caller_method')+'/'+calldir.get('caller_class'))
    if len(bward):
        backward.update({callee_method+'/'+callee_class: bward})
    return temp6

##### forward flow call graph
i = 0 
def forwardcallgraph(init_caller_method, init_caller_class): 
    global i
    i = i+1
    fleveli = forwardflow(init_caller_method,init_caller_class)
    for callee in fleveli:
        nextlevel = forwardcallgraph(callee.get('callee_method'), callee.get('callee_class'))
    flowgraph.update({"forward":forward})        
    
##### backward flow call graph
k = 0 
def backwardcallgraph(init_callee_method, init_callee_class): 
    global k
    k = k+1
    fleveli = backwardflow(init_callee_method,init_callee_class)
    for caller in fleveli:
        nextlevel = backwardcallgraph(caller.get('caller_method'), caller.get('caller_class'))        
    flowgraph.update({"backward":backward})    

###### call generate forward flow graph
#print('///////////////////////////////////////////////////////////////////////////')
#print('////////////////////////// FORWARD FLOW GRAPH /////////////////////////////')
#print('///////////////////////////////////////////////////////////////////////////')
forwardcallgraph(init_caller_method, init_caller_class)
#print(forward)

###### call generate forward flow graph
#print('///////////////////////////////////////////////////////////////////////////')
#print('////////////////////////// BACKWARD FLOW GRAPH ////////////////////////////')
#print('///////////////////////////////////////////////////////////////////////////')
backwardcallgraph(init_callee_method, init_callee_class)
#print(backward)
print(flowgraph)
