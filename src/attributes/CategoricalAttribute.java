package attributes;

import tree.Node;

public class CategoricalAttribute extends Attribute {
	

	private String description;
	public Node rootNode;
	public Node Back_Tree;
	private boolean isSpecific;
	public double Specific_Cost;
	public double NonSpecific_Cost;
	
	
	public CategoricalAttribute() {};
	
	public CategoricalAttribute(String _name, String _type, Node _node, String _desc, boolean _is_spec,Node _BackNode) {
		
	super(_name,_type);
	this.rootNode = _node;
	this.isSpecific = _is_spec;	
	this.description = _desc;
	this.Back_Tree= _BackNode;
	}
	
		
	public void setSpecific(boolean _in){
		
		this.isSpecific = _in;
			
	}
	
	public boolean isSpecific() {
		
		return this.isSpecific;	
			
	}
	
	public String getDescription(){
		
	return this.description;	
		
	}
	
	public void setDescription(String _in){
		
		this.description = _in;	
			
		}
	
	
	

	
}
