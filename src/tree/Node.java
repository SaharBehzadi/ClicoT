package tree;

import java.util.ArrayList;

public class Node {
	
	private ArrayList<Node> children = new ArrayList<Node>();
	private boolean isRoot = false;
	private boolean isLeaf = false;
	private double probability;
	private String description;
	private int pathlength;
	private boolean isSpecific;
	public int id;
	
	public Node() {};
	
	public Node(String desc,int _pathlength, int id){
		
		this.description = desc;
		this.pathlength = _pathlength;
		this.id=id;
			
	}

  public Node(Node node) {
	   this.children = node.children;
	   this.isRoot = node.isRoot;
	   this.isLeaf = node.isLeaf;
	   this.probability = node.probability;
	   this.description = node.description;
	   this.pathlength = node.pathlength;
	   this.id= node.id;
	   this.isSpecific=node.isSpecific;
	  }
	
	public void addChild(Node newchild){
		
	children.add(newchild);	
		
	}
	
	public void setLeaf(boolean _in){
	
	this.isLeaf = _in;
		
	}
	
	public boolean isLeaf() { 
		
	return this.isLeaf;	
	}
	
	public boolean isRoot() {
		
	return this.isRoot;	
		
	}
	
	public void setSpecific(boolean _in){
		
		this.isSpecific = _in;
			
	}
	
	public boolean isSpecific() {
		
		return this.isSpecific;	
			
	}
	
	public void setProbability(double _in){
		
	this.probability = _in;
		
	}
	
	public void setRoot(boolean _in){
		
	this.isRoot = _in;	
		
	}
	
	public void setChildren(ArrayList<Node> _in){
		
	this.children = _in;	
		
	}
	
	public ArrayList<Node> getChildren (){
		
	return this.children;	
		
	}
	
	public String getDescription(){
		
		return this.description;
	}
	
	public double getProbability(){
		
		return this.probability;
	}
	
	public int getPathlength(){
	
		return this.pathlength;
		
	}
	
	
	
}
