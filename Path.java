import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.jgrapht.*;
import org.jgrapht.graph.*;

public class Path{

    private ArrayList<CustomWeightedEdge> orderedEdgeList;

    private ArrayList<StringBuffer> bubbleSequences;

    //private HashSet<Integer> paths;
    private HashSet<Integer> readset;

    private static final int MIN_SUPPORT = 4;
    
    public HashSet<Integer> getReadSet(){
	return this.readset;
    }

    public int getReadSetSize(){
	return this.readset.size();
    }

    public ArrayList<StringBuffer> getBubbleSequences(){
	return this.bubbleSequences;
    }

    public void setReadSet(HashSet<Integer> rs){
	this.readset = rs;
    }

    public void subtractReadSet(HashSet<Integer> ors){
	this.readset.removeAll(ors);
    }

    public void printBubbleSequence(SimpleDirectedWeightedGraph<Node, CustomWeightedEdge> g){
	StringBuffer bf = new StringBuffer();
	for(int i=0;i<this.orderedEdgeList.size()-1;i++){
	    CustomWeightedEdge e = this.orderedEdgeList.get(i);
	    CustomWeightedEdge e2 = this.orderedEdgeList.get(i+1);
	    bf.append(g.getEdgeTarget(e).getBase() + "(" + g.getEdgeWeight(e)+":"+g.getEdgeWeight(e2)+")");
	}
	System.err.println("bubble:\t" + bf.toString());
    }

    public void initBubbleSequence(SimpleDirectedWeightedGraph<Node, CustomWeightedEdge> g){
	if(bubbleSequences.size() == 0){
	    StringBuffer bf = new StringBuffer();
	    for(int i=0;i<this.orderedEdgeList.size()-1;i++){
		CustomWeightedEdge e = this.orderedEdgeList.get(i);
		bf.append(g.getEdgeTarget(e).getBase());
	    }
	    System.err.println("bubble:\t" + bf.toString());
	    bubbleSequences.add(bf);
	}else{
	    System.err.println("Shouldn't be called here.");
	    System.exit(-1);
	}
    }
    
    public void addBubbleSequence(StringBuffer sb){
	this.bubbleSequences.add(sb);
    }

    public Path deepCopy(){
	Path p = new Path();
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    p.appendEdge(e);
	}
	for(StringBuffer sb : this.bubbleSequences){
	    p.addBubbleSequence(sb);
	}
	p.setReadSet(this.getReadSetDeepCopy());
	return p;
    }
    

    public boolean isSupportedPath(){
	if(readset.size() >= Path.MIN_SUPPORT){
	    return true;
	}
	return false;
    }

    public void computeReadSet(){
	System.err.println("Verifying:");
	this.printPath();
	HashSet<Integer> tmpset = new HashSet<Integer>();
	HashSet<Integer> unionUniqueSet = new HashSet<Integer>();
	//first check if size of intersection is nonzero.
 	for(int i=0; i<this.orderedEdgeList.size(); i++){
	    CustomWeightedEdge e = this.orderedEdgeList.get(i);
	    if(e.isUniqueEdge())
		unionUniqueSet.addAll(e.getReadHashSet());
	    
	    if(i == 0)
		tmpset.addAll(e.getReadHashSet());
	    else{
		tmpset.retainAll(e.getReadHashSet());//we take intersection
		if(tmpset.size() == 0)
		    break;
	    }
	}
	
	//intersection is nonzero, we will add supplemnentary evidences(uniqEdgeReads union)
	if(tmpset.size() >= Path.MIN_SUPPORT ){
	    System.err.print("InersectionSize\t" + tmpset.size()+ "\tUnionUniqSetSize\t" + unionUniqueSet.size());
	    /*
	    ArrayList<CustomWeightedEdge> nonUniqueEdges = new ArrayList<CustomWeightedEdge>();
	    for(CustomWeightedEdge e : this.orderedEdgeList){
		if(!e.isUniqueEdge()){
		    nonUniqueEdges.add(e);
		    HashSet<Integer> tmpset2 = new HashSet<Integer>();
		    tmpset2.addAll(e.getReadHashSet());
		    tmpset2.retainAll(unionUniqueSet);
		}
		}*/
	    tmpset.addAll(unionUniqueSet);
	    System.err.println("\tTotalSetSize\t" + tmpset.size());
	    for(CustomWeightedEdge e : this.orderedEdgeList){
		e.subtractSet(tmpset);
	    }
	}else{
	    System.err.print("InersectionSize\t" + tmpset.size()+ "\tUnionUniqSetSize\t" + unionUniqueSet.size());
	    tmpset = new HashSet<Integer>();
	    System.err.println("TotalSetSize\t" + tmpset.size() + "\t----> REMOVED");
	}
	this.readset = tmpset;
    }


    
    //intersection of reads acorss edges
    //also add reads belonging to unique edges
    //this works because the bubble is small.
    /*public void computeReadSet(){
	for(int i=0; i<this.orderedEdgeList.size(); i++){
	    CustomWeightedEdge e = this.orderedEdgeList.get(i);
	    if(i==0){
		this.readset = e.getReadHashSetDeepCopy();
	    }else{
		this.readset.retainAll(e.getReadHashSet());
		if(this.readSet.size() == 0){
		    break;
		}
	    }
	}
	
	if(this.readSet.size() > 0){
	    for(CustomWeightedEdge e : this.orderedEdgeList)
		e.substractSet(this.readset);
	}
    }
    */
    

    public ArrayList<CustomWeightedEdge> getOrderedEdgeList(){
	return this.orderedEdgeList;
    }

    public Path(){
	this.orderedEdgeList = new ArrayList<CustomWeightedEdge>();
	this.readset = new HashSet<Integer>();
	this.bubbleSequences = new ArrayList<StringBuffer>();
    }

    public void appendEdge(CustomWeightedEdge e){
	this.orderedEdgeList.add(e);
    }

    public Path(CustomWeightedEdge e){
	this();
	this.appendEdge(e);
    }

    public Node getLastVertex(SimpleDirectedWeightedGraph<Node, CustomWeightedEdge> g){
	if(this.orderedEdgeList == null || this.orderedEdgeList.size() == 0)
	    return null;
	else{
	    return g.getEdgeTarget(this.orderedEdgeList.get(this.orderedEdgeList.size() - 1));
	}
    }
    
    // M : M 
    // tp is used multiple times and op is used multiple times
    public Path mergePathManytoMany(Path other){
	Path np = this.mergePaths(other);
	np.getIntersection(other);
	return np;
    }

    // 1 : M
    //tp is used once but op is used multiple times
    public Path mergePath1toMany(Path other){
	Path np = this.mergePaths(other);
	return np;
    }

    // M : 1
    //tp is used multiple times but op is used once.
    public Path mergePathManyto1(Path other){
	Path np = this.mergePaths(other);
	np.setReadSet(other.getReadSetDeepCopy());
	return np;
    }

    // 1 : 1
    //1 to 1 connection tp and op are not split into other flows
    public Path mergePathsUnique(Path other){
	Path np = this.mergePaths(other);
	np.unionReadSets(other);
	return np;
    }
    
    private void unionReadSets(Path other){
	this.readset.addAll(other.getReadSet());
    }
    
    /* this simply merged edges */
    public Path mergePaths(Path other){
	Path p = this.deepCopy();
	p.getOrderedEdgeList().addAll(other.getOrderedEdgeList());
	p.getBubbleSequences().addAll(other.getBubbleSequences());
	return p;
    }

    /* phasing based on unique edges */
    //checks for phasing support information between two paths
    //Two paths are phased if intersection of reads passing through unique edges.
    //@returns true if intersecting sets are NOT empty
    //@returns false otherwise.
    public boolean isPhasedWithOLD(Path other){
	//this set
	HashSet<Integer> ts = this.getUnionOfUniqueEdgesReadSet();
	//other set
	HashSet<Integer> os = other.getUnionOfUniqueEdgesReadSet();
	System.out.println("TS:\t");
	Path.printHashSet(ts);
	System.out.println("OS:\t");
	Path.printHashSet(os);
	
	ts.retainAll(os);
	System.out.print("\t");
	Path.printHashSet(ts);
	if(ts.size() >= Path.MIN_SUPPORT)
	    return true;
	return false;
    }
    
    public void getIntersection(Path other){
	this.readset.retainAll(other.getReadSet());
    }

    private HashSet<Integer> getReadSetDeepCopy(){
	HashSet<Integer> tmp = new HashSet<Integer>();
	Iterator<Integer> itr = this.readset.iterator();
	while(itr.hasNext()){
	    tmp.add(itr.next());
	}
	return tmp;
    }

    /* phasing based on read set */
    public boolean isPhasedWith(Path other){
	HashSet<Integer> copyset = this.getReadSetDeepCopy();
	copyset.retainAll(other.getReadSet());
	if(copyset.size() >= Path.MIN_SUPPORT){
	    System.err.println("PHASED[intersectionSize:" + copyset.size() + "]");
	    return true;
	}else{
	    System.err.println("NOT PHASED[intersectionSize:" + copyset.size() + "]");
	    if(copyset.size() > 0){
		this.subtractReadSet(copyset);
		other.subtractReadSet(copyset);
	    }
	    return false;
	}
    }

    public String getNumUniqueEdges(){
	int count = 0;
	StringBuffer bf = new StringBuffer();
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    if(e.isUniqueEdge()){
		bf.append("{"+e.getEdgeId()+"}");
		count++;
	    }
	}
	bf.append("(" + count + ")");
	return bf.toString();
	//return count;
    }
    
    public void printInfo(){
	System.err.print("NumEdges:" + this.orderedEdgeList.size());
	System.err.print("\tNumUniqueEdges:" + this.getNumUniqueEdges() + "\n");
    }

    public void printPath(){
	System.err.print("NumEdges:" + this.orderedEdgeList.size() + "\t");
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    System.err.print("{"+e.getEdgeId()+"}");
	}
	System.err.println();
    }

    

    public static void printHashSet(HashSet<Integer> hs){
	Iterator<Integer> itr = hs.iterator();
	System.out.print("{");
	while(itr.hasNext())
	    System.out.print("," + itr.next().intValue());
	System.out.println("}");
    }

    //if there is no uniqueEdges, return null
    //else it returns union HashSet<Integer> of reads over all unique edges.
    //size 0 if there is reads covering unique edge
    public HashSet<Integer> getUnionOfUniqueEdgesReadSet(){
	System.err.println("UnionOfUniqueEdges");
	HashSet<Integer> s = new HashSet<Integer>();
	boolean atLeast1UniqueEdge = false;
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    System.err.print("|" + e.getNumActivePath() + "|");
	    if(e.isUniqueEdge()){
		System.err.println("U:"+ e.getEdgeId());
		atLeast1UniqueEdge  = true;
		s.addAll(e.getReadHashSet());
	    }else
		System.err.println("R:"+ e.getEdgeId());
	}
	if(!atLeast1UniqueEdge)
	    return null;
	return s;
    }
    

    public void initPathCounter(){
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    e.initNumActivePath(); //sets it to zero 
	    //e.includeEdge(); // increment numActivePath
	}
    }

    public void includePath(){
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    e.includeEdge();
	}
    }
    
    public void includePathNTimes(int n){
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    e.includeEdgeNTimes(n);
	}
    }

    public void excludePath(){
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    e.excludeEdge();
	}
    }

    public void printNumActivePaths(){
	for(CustomWeightedEdge e : this.orderedEdgeList){
	    System.err.print(e.getEdgeId() + "\t" + e.getNumActivePath() + "\t|\t");
	}
	System.err.println();
    }
}
