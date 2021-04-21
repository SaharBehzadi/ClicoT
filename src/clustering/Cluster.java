package clustering;
import java.util.ArrayList;

import attributes.CategoricalAttribute;
import attributes.NumericalAttribute;
import data.DataItem;

public class Cluster {
	

	private ArrayList<DataItem> data_items;
	private ArrayList<NumericalAttribute> numericalAttributes= new ArrayList<NumericalAttribute>();
	private ArrayList<CategoricalAttribute> categoricalAttributes= new ArrayList<CategoricalAttribute>();
	private double numcosts;
	private double catcosts;
	private double idcosts;
	private double paramcosts;
	private double totalcosts;

	public Cluster () {}
	
	public Cluster (ArrayList<DataItem> data) {
		
		this.data_items = data;
	}
	
	public Cluster(Cluster _in){
	this.data_items = new ArrayList<DataItem>(_in.getDataItems());
	this.numericalAttributes = _in.numericalAttributes;
	this.categoricalAttributes = _in.categoricalAttributes;
	this.numcosts = _in.getNumCosts();
	this.catcosts = _in.getCatCosts();
	this.idcosts =  _in.getIdCost();
	this.paramcosts = _in.getParamCosts();
	this.totalcosts = _in.getTotalCosts();
		
	}
	
	
	
	public void setData_items(ArrayList<DataItem> in){
		
		this.data_items = in;
	}
	
	public ArrayList<DataItem> getDataItems(){
		
	return this.data_items;
	
	}
	
	public DataItem getDataItem (int idx) {
	
	return data_items.get(idx);
		
	}
	
	public void setNumericalAttribute(int id,NumericalAttribute attr){
		this.numericalAttributes.set(id,attr);
	}
	
	public void addNumericalAttribute(NumericalAttribute attr){
		this.numericalAttributes.add(attr);
	}
	
	public ArrayList<NumericalAttribute> getNumericalAttributes(){
		
		return this.numericalAttributes;
	}
	
	public ArrayList<NumericalAttribute> getSpecificNumericalAttributes(){
	
		ArrayList<NumericalAttribute> result = new ArrayList<NumericalAttribute>();	
		
		for(int i=0;i<numericalAttributes.size();i++){
		
			if(numericalAttributes.get(i).isSpecific()) result.add(numericalAttributes.get(i));	
		}
		
		return result;
	}
	
	
	public ArrayList<CategoricalAttribute> getSpecificCategoricalAttributes(){
		
		ArrayList<CategoricalAttribute> result = new ArrayList<CategoricalAttribute>();	
		
		for(int i=0;i<categoricalAttributes.size();i++){
		
			if(categoricalAttributes.get(i).isSpecific()) result.add(categoricalAttributes.get(i));	
		}
		
		return result;
	}
	
	public ArrayList<CategoricalAttribute> getCategoricalAttributes(){
		
		return this.categoricalAttributes;
		
	}
	
	
	public void addCategoricalAttribute(CategoricalAttribute attr){
		this.categoricalAttributes.add(attr);
	}
	
	
	public void clearAttributes(){
		
		this.numericalAttributes =  new ArrayList<NumericalAttribute>();
		this.categoricalAttributes = new ArrayList<CategoricalAttribute>();
		
	}
	
	public void setNumCosts(double _in){	
		this.numcosts = _in;		
	}
	
	public double getNumCosts(){
		return this.numcosts;	
	}
	
	public void setCatCosts(double _in){	
		this.catcosts = _in;		
	}
		
	public double getCatCosts(){
		return this.catcosts;		
	}
		
	public void setIdCosts(double _in){
		this.idcosts = _in;		
	}
		
	public double getIdCost(){
		return this.idcosts;	
	}
		
	public void setParamCosts(double _in){
		this.paramcosts = _in;		
	}
			
	public double getParamCosts(){
		return this.paramcosts;		
	}
	
	public void setTotalCosts(double _in){
		this.totalcosts = _in;		
	}
			
	public double getTotalCosts(){
		return this.totalcosts;		
	}
	
}
