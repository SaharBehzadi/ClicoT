package attributes;
import java.util.ArrayList;

public class AttributeStructure extends ArrayList <Attribute> {
	
private int num_of_numericals;	
private int num_of_categoricals;		


public void setNumOfNumericals(int num) {
 this.num_of_numericals = num;	
}


public void setNumOfCategoricals(int num) {
 this.num_of_categoricals = num;
}

public int getNumOfNumericals(){
 return this.num_of_numericals;		
}


public int getNumOfCategoricals(){
 return this.num_of_categoricals;		
}


public ArrayList<NumericalAttribute> getNumericalAttributes(){

ArrayList<NumericalAttribute> numericalAttributes = new ArrayList<NumericalAttribute>();	
	
	for(int i=0; i<this.size();i++){
		
		Attribute at = this.get(i);
		
		if (at instanceof NumericalAttribute ) {	
			
			numericalAttributes.add(((NumericalAttribute)at));	
		}
		
	}	
		
return 	numericalAttributes;
}

public ArrayList<CategoricalAttribute> getCategoricalAttributes(){

ArrayList<CategoricalAttribute> categoricalAttributes = new ArrayList<CategoricalAttribute>();	
	
	for(int i=0; i<this.size();i++){
		
		Attribute at = this.get(i);
		
		if (at instanceof CategoricalAttribute ) {	
			
			categoricalAttributes.add(((CategoricalAttribute)at));	
		}
		
	}	
		
return 	categoricalAttributes;
}

}//class
