import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.jgrapht.*;
import org.jgrapht.graph.*;


//single path through a superbubble.
//each superbubble can have multiple AllelePath
public class AllelePath{

    private Path bubblePath;//this is the original bubblePath before adding interBubble paths;
    
    private ArrayList<CustomWeightedEdge> orderedEdgeList;
    
    //this keeps track of indices where disconnect happens due to exonic boundary.
    private ArrayList<Integer> fractureEndIndex;

    //private ArrayList<Integer> mergedOpIndicies;

    private double probability;

    private double weightedIntersectionSum;
    
    private int mergedNums;

    private String sequence;

    private String sequenceName;

    public AllelePath(){
	this.bubblePath = null;
	this.orderedEdgeList = new ArrayList<CustomWeightedEdge>();
	this.fractureEndIndex = new ArrayList<Integer>();
	//this.mergedOpIndicies = new ArrayList<Integer>();
	this.weightedIntersectionSum = 0.0d;
	this.mergedNums = 0;
	this.probability = 0.0d;
	this.sequence = null;
	this.sequenceName = null;
    }

    public AllelePath(double p, double wis, int mn, Path bp){//ArrayList<Integer> moi, Path bp){
	this();
	this.bubblePath = bp;
	this.weightedIntersectionSum = wis;
	this.mergedNums = mn;
	this.probability = p;
	//this.mergedOpIndicies = moi;
    }

    public Path getBubblePath(){
	return this.bubblePath;
    }

    public double[] getJointProbability(AllelePath other){
	return this.bubblePath.getJointProbability(other.getBubblePath());
    }

    private void printFractureEndIndex(){
	System.err.print("FractureEndIndex:[");
	for(Integer i: this.fractureEndIndex)
	    System.err.print(" " + i.intValue() +"  ");
	System.err.println("]");
    }
    
    public void setFractureEndIndex(){
	this.fractureEndIndex.add(new Integer(this.orderedEdgeList.size()));
    }

    public void appendEdge(CustomWeightedEdge e){
	this.orderedEdgeList.add(e);
    }
    
    public void appendAllEdges(Path other){
	this.orderedEdgeList.addAll(other.getOrderedEdgeList());
    }

    public void appendAllEdges(ArrayList<CustomWeightedEdge> anotherOrderedEdgeList){
	this.orderedEdgeList.addAll(anotherOrderedEdgeList);
    }

    public void printPath(SimpleDirectedWeightedGraph<Node, CustomWeightedEdge> g, int superBubbleNum, int n){
	this.printFractureEndIndex();
	System.out.println("IntersectionScore:\t" + this.weightedIntersectionSum + "\t" + this.probability);
	System.out.println(this.toFasta().toString());
	//System.out.println(">candidate_" + superBubbleNum + "-" + n + "\n" + this.sequence);//this.toString(g, superBubbleNum, n));
    }

    public StringBuffer toFasta(){
	return new StringBuffer(">" + this.sequenceName + "\n" + this.sequence + "\n");
    }

    public String getSequence(){
	return this.sequence;
    }
    
    public double getProbability(){
	return this.probability;
    }
    
    public double getIntersectionSum(){
	return this.weightedIntersectionSum;
    }
    
    public int getMergedNums(){
	return this.mergedNums;
    }
    
    public void setSequenceString(SimpleDirectedWeightedGraph<Node, CustomWeightedEdge> g, int superBubbleNum, int n){
	
	StringBuffer bf = new StringBuffer();
	CustomWeightedEdge pre = null;
	int disconnectCount = 0;
	int curfi = 0;
	for(int i=0; i<this.orderedEdgeList.size(); i++){
	    CustomWeightedEdge cur = this.orderedEdgeList.get(i);
	    //cur should never be null
	    if(cur == null){
		System.err.print("TMP");
		System.err.println("HUH??????:\t" + i + "\tSize:\t" + this.orderedEdgeList.size() );
	    }
	    char curChar;
	    //if it's not the first node and preEdge and curEdge are not connected
	    //    OR
	    //first node
	    //we need to add the beginning node of the edge
	    /*
	    if( (pre != null && !g.getEdgeTarget(pre).equals(g.getEdgeSource(cur)))
		|| pre == null ){
		if(pre != null){
		    
		    disconnectCount++;
		    //bf.append("[DISCONNECT @ " + i + "]");
		    //bf.append("*");
		}
		//bf.append("[DISCONNECT @ " + i + "]");
		//bf.append("*");
		
		curChar = g.getEdgeSource(cur).getBase();
		if(curChar != '.')
		bf.append(curChar);
		}*/
	    if( (this.fractureEndIndex.size() > curfi)
		&& (i == this.fractureEndIndex.get(curfi).intValue()) ){
		curChar = g.getEdgeSource(cur).getBase();
		if(curChar != '.')
		    bf.append(curChar);
		curfi++;
	    }
	    
	    curChar = g.getEdgeTarget(cur).getBase();
	    if(curChar != '.')
		bf.append(curChar);
	    pre = cur;
	}
	//String finalStr = bf.toString();
	this.sequence = bf.toString();
	this.sequenceName = "candidate_" + superBubbleNum + "-" + n;
	//System.err.println(">candidate_" + superBubbleNum + "-" + n + "\n" + finalStr);
	//return finalStr;
    }
}
