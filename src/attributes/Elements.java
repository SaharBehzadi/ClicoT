package attributes;
import java.util.Comparator;

import org.apache.commons.math3.distribution.NormalDistribution;

import tree.Node;

public class Elements extends Attribute {
	
	public double SpecificCost = 0.0;
	public double NonspecificCost = 0.0;
	public double Deviation=0.0;
	public int id;
	public int type;
	public String Description; 
	public boolean isSpecific;
	public NormalDistribution NumericalNonspecificDist;
	public NormalDistribution NumericalSpecificDist;
	public Node specificTree;
	
	
	
	 public static Comparator<Elements> COMPARE_BY_Deviation = new Comparator<Elements>() {
	        public int compare(Elements one, Elements other) {

	        	
	        	if(one.Deviation<other.Deviation)
	    			return 1;
	    		if(one.Deviation>other.Deviation)
	    			return -1;
	    		else
	    			return 0;
	        }
	    };
	
}
