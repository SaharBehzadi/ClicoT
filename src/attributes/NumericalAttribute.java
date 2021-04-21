package attributes;
import org.apache.commons.math3.distribution.NormalDistribution;

public class NumericalAttribute extends Attribute {
	
private double globalMean = 0.0;
private double globalVariance = 0.0;
private NormalDistribution dist;
private boolean isSpecific;
public int id;

public NumericalAttribute() {};

public NumericalAttribute(String _name, String _type, NormalDistribution _dist, boolean _is_spec,double globalmean,double globalvariance){

super(_name,_type);
this.dist = _dist;	
this.isSpecific = _is_spec;
this.globalMean = globalmean;
this.globalVariance = globalvariance;

}

	public void setGlobalMean(double _in) {
		this.globalMean = _in;
	}
	
		
	public double getGlobalMean () {
		return this.globalMean;
	}
	
	public NormalDistribution getNormalDistribution (){
		
		return this.dist;
	}
	
	public void setGlobalVariance(double _in) {
		this.globalVariance = _in;
	}
	
	public double getGlobalVariance() {
		return this.globalVariance;
	}
	
	public void setSpecific(boolean _in){
		
		this.isSpecific = _in;
			
	}
	
	public boolean isSpecific() {
		
		return this.isSpecific;	
			
	}

	
}
