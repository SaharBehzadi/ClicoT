package data;
import java.util.ArrayList;

import attributes.AttributeStructure;
import attributes.CategoricalAttribute;
import attributes.NumericalAttribute;
import misc.Utils;
import tree.Node;
import tree.Tree;

@SuppressWarnings("serial")
public class DataSet extends ArrayList<DataItem> {
		
	
	public int addvalues = 0;
	private AttributeStructure attributes = null; 
	
	public DataSet(){};
	
	public DataSet(String data,AttributeStructure _attributes, String format){
	this.attributes = _attributes;

          String lines [] = data.split("\n");
          
          for(int i=0;i<lines.length;i++){
        	  
    	  double[] numericalvalues = new double[attributes.getNumOfNumericals()];
    	  String[] categoricalvalues = new String[attributes.getNumOfCategoricals()];
        	  
          if(lines[i].length()==0) continue;	
          String parts [] = lines[i].split("\t");
//          String parts [] = lines[i].split(",");	
          DataItem item = new DataItem(i);
          
          int numcount = 0;
          int catcount = 0;
      	   for(int j=0;j<parts.length;j++){ 	// -1 because of class labels at end of line
	      	   if(attributes.get(j).getType().equals("Numerical")) {
	      		      if(!parts[j].equals("?")){
	      		      Double x1 = new Double(parts[j]);	
	      		      numericalvalues[numcount] = x1;
	      		      }
	      		      else numericalvalues[numcount] = 0;
	      		      numcount ++;
	      	   }
	      	   
	      	   else {
	      		     String s1 = new String(parts[j]);
	      		     categoricalvalues[catcount] = s1;
	      		     
//		        	 System.out.println("value : " + parts[j]);
		        	 
		        	 catcount ++;
	      	   }
      	   
      	   
      	   } //for j
      	 
      	  item.setNumericalValues(numericalvalues);
      	  item.setCategoricalValues(categoricalvalues);
      	  this.add(item); 
          }//for i
          
        
          
          setGolbalMeans();
          setGlobalVariances();
          setBackgroundTreeProbabilities();
   
		
	}//DataSet
	
	public void setGolbalMeans(){
	
	ArrayList<NumericalAttribute> numericalAts = attributes.getNumericalAttributes();	
		
	for(int i=0; i<numericalAts.size();i++){	
	    
		NumericalAttribute at = numericalAts.get(i);
		double [] values = new double[this.size()]; 
	 
		for(int j=0;j<this.size();j++){
				
				values[j] = this.get(j).numericalvalues[i];
				   
				}
			
			 double golbalmean = Utils.calculateMean(values);
			 at.setGlobalMean(golbalmean);
		
	}	
 }
	
  public void setGlobalVariances(){
	  
		ArrayList<NumericalAttribute> numericalAts = attributes.getNumericalAttributes();	  
		
		for(int i=0; i<numericalAts.size();i++){	
			
		NumericalAttribute at = numericalAts.get(i);	
		double [] values = new double[this.size()]; 
		
		
		for(int j=0;j<this.size();j++){
		values[j] = this.get(j).numericalvalues[i];	
			   
			}
		double globalvar = Utils.calculateVariance(values, ((NumericalAttribute)at).getGlobalMean());
		at.setGlobalVariance(globalvar);
			
			
		}	  
  }
 
 public void setBackgroundTreeProbabilities(){
	 
	ArrayList<CategoricalAttribute> categoricalAts = attributes.getCategoricalAttributes();
	
		for (int i=0; i<categoricalAts.size();i++) {
			
		CategoricalAttribute at = categoricalAts.get(i);		
	
		Node rootNode = at.rootNode;
		rootNode.setProbability(1.0);
		 
		Tree.setNodeProbabilites(rootNode,this,i);	
		
	 }
 }


	
 public AttributeStructure getAttributeStructure(){
	 
	return this.attributes;
	 
 }


}//class
