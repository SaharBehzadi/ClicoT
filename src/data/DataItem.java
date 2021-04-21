package data;

import clustering.Cluster;

public class DataItem {
	
public int id;
public double[] numericalvalues;
public String[] categoricalvalues;
private Cluster cluster;
public int ClusterID;

public DataItem(int id) {this.id = id;}


	public DataItem(double[] numericalvalues,String[]categoricalvalues) {
		
	this.numericalvalues = numericalvalues;
		this.categoricalvalues = categoricalvalues;
		
		
	}
	
	public void setNumericalValues(double[] numericalvalues){
		this.numericalvalues = numericalvalues;	
		
		
	}
	
	public void setCategoricalValues(String[]categoricalvalues){
		this.categoricalvalues = categoricalvalues;
		
		
	}
	
	public double[] getNumericalValues(){
	
		return this.numericalvalues;
		
	}
	
	public int getId (){
		
		return this.id;
		
	}
	
	public Cluster getCluster (){
		
		return this.cluster;
		
	}
	
	public void setCluster(Cluster _in){
		
		this.cluster = _in;
		
	}
	
}
