import java.util.*;

public class Sequence{

    private ArrayList<Base> seq;
    private String alleleName; //allele name: ex) A:01:01:01:01
    private StringBuffer columnSequence; //columnsSequence is a string containing all the paddings to match the length of MSA
    private StringBuffer fullSequence;   //fullSequence is 5'-3' DNA string without any paddings. So it's either equal to or shorter than columnSequence.
    private int[] boundaries;//contains start position of each boundary, alternating between intron/exon
    private int[] segmentOffsets;
    
    public Sequence(){
	this.seq = new ArrayList<Base>();
	this.alleleName = null;
	this.columnSequence = null;
	this.fullSequence = null;
	this.boundaries = null;
	this.segmentOffsets = null;
    }

    public int[] getSegmentOffsets(){
	return this.segmentOffsets;
    }

    public int[] getBoundaries(){
	return this.boundaries;
    }

    //returns nth intron. here n is 0-based
    //   n(0-based)  intronNum   boundariesIndex
    //       0          1              0         
    //       1          2              2
    //       2          3              4
    public ArrayList<Base> getNthIntron(int n){
	int index = n * 2; // from n to boundariesIndex
	int sIndex = this.boundaries[index];
	int eIndex = -1;
	if(index == this.boundaries.length-1)//last intron
	    eIndex = this.seq.size()+1;
	else
	    eIndex = this.boundaries[index+1];
	return (ArrayList<Base>) this.seq.subList(sIndex, eIndex);
    }

    public int numBlocks(){
	return this.boundaries.length;
    }

    /* this processes nuc only allele --> only containing EXONS*/
    public void Sequence(String allele, String msfSequence, boolean abbrv, Sequence ref){
	this();
	this.alleleName = allele;
	String[] tokens = msfSequence.split("\\|");

	/* we set the number of blocks same as the reference sequence */
	this.boundaries = new int[ref.numBlocks()];
	this.segmentOffsets = new int[ref.numBlocks()];
	
	/* first we copy the first intron from the reference */
	this.base = this.base.addAll(ref.getNthIntron(0));
	this.boundaries[0] = ref.getBoundaries[0];
	this.segmentOffsets[0] = ref.getSegmentOffsets()[0];

	
	int offset = ref.getSegmentOffsets()[0];
	int curStartColPos = ref.getBoundaries[1];
	
	boolean isExon = true;
	int exonNum = 0;
	for(int i=0; i<tokens.length; i++){
	    exonNum++;
	    int updatedOffset = processBlock(tokens[0], isExon, exonNum, offset);
	    this.segmentOffsets[2*i+1] = updatedOffset - offset;
	    offset = updatedOffset;
	    this.boundaries[2*i+1] = curStartColPos;
	    
	    this.base = this.base.addAll(ref.getNthIntron(i+1));
	    this.boundaries[2*(i+1)] = ref.getBoundaries()[2*(i+1)];
	    this.segmentOffsets[2*(i+1)] = ref.getSegmentOffsets();
	    offset = offset + this.segmentOffsets[2*(i+1)];
	    curStartColPos = ref.getBoundaries()[2*(i+1)+1];
	}
	
    }


    //msfSequence still has intron/exon boundary symbol Embedded.
    // <INTRON1>|<EXON1>|<INTRON2>|<EXON2>|...
    // allele --> allelename
    // msfSequence --> msf sequence string without blanks
    // abbrv --> do we need to take care of abbreviation?
    // isNuc --> this is a nuc allele, if true
    public void Sequence(String allele, String msfSequence, boolean abbrv, boolean isNuc){
	this();
	this.alleleName = allele;
	String[] tokens = msfSequence.split("\\|");
	this.boundaries = new int[tokens.length];
	this.segmentOffsets = new int[tokens.length];
	int offset = 0;
	boolean isExon = false;
	int intronNum = 0;
	int exonNum = 0;
	int curStartColPos = 0;
	for(int i=0; i<tokens.length; i++){
	    curStartColPos++;
	    this.boundaries[i] = curStartColPos;
	    if(i%2 == 0){//intron
		isExon = false;
		intronNum++;
		int updatedOffset = processBlock(tokens[i], isExon, intronNum, offset);
		this.segmentOffsets[i] = updatedOffset - offset;
		offset = updatedOffset;
	    }else{
		isExon = true;
		exonNum++;
		int updatedOffset = processBlock(tokens[i], isExon, exonNum, offset);
		this.segmentOffsets[i] = updatedOffset - offset;
		offset = updatedOffset;
	    }
	    curStartColPos = this.seq.size();
	}
    }

    public Sequence(String exonOnlyMsfSequence, Sequence genSeq){
	this();
	String[] tokens = msfSequence.split("\\|");
	this.boundaries = new int[genSeq.getBoundaries.length];
	this.segmentOffsets = new int[this.boundaries.length];
	int offset = 0;
	int exonNum = 0;
	int curStartColPos = 0;
	//for each exon
	for(int i=0; i<tokens.length; i++){
	    genSeq.
	}
    }
    
    
    public int processBlock(String blockSeq, boolean isExon, int intronExonNum, int offset){
	int colPos = this.seq.size(); //1-based column Position
	int base2colOffset = offset;
	int basePos = colPos - base2colOffset;
	
	for(int i=0; i<blockSeq.length(); i++){
	    char curBase = blockSeq.charAt(i);
	    this.columnSequence.append(curBase);
	    colPos++;
	    if(Base.isBase(curBase)){
		basePos++;
		this.fullSequence.append(curBase);
	    }else if(Base.isGap(curBase))
		base2colOffset++;
	    else
		System.err.out.println("WHAT ELSE????\nBlockSeq:" + blockSeq + "\n@"  + (i+1) + ":" curBase);
		    
	    this.seq.add(new Base(blockSeq.charAt(i), basePos, colPos, base2colOffset, isExon, intronExonNum));
	}
	
	return base2colOffset;
    }
    
}
