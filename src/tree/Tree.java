package tree;

import java.util.ArrayList;
import data.DataItem;

public class Tree {
	
	public static void setNodeProbabilites(Node _node, ArrayList<DataItem> data, int attributeindex){
		
		ArrayList<Node> children = _node.getChildren();
		
		for(int i=0;i<children.size();i++){
		Node node = children.get(i);
		if(!node.isLeaf()){
			
			setNodeProbabilites(node,data,attributeindex);	
			
			double childprobabilities = 0.0;
			for(int k=0; k<node.getChildren().size();k++){
			    
				
				Node child = (Node) node.getChildren().get(k);
				childprobabilities += child.getProbability();
			}
			
			node.setProbability(childprobabilities);
		
			}
		else {
			
		String desc = node.getDescription();
		int nodecount = 0;
		
		for(int j=0;j<data.size();j++){	
			
		String value = data.get(j).categoricalvalues[attributeindex];
	//	System.out.println(value);
		if(value.equalsIgnoreCase(desc)){ nodecount ++;}
			
				}
		double prob = (double)nodecount/data.size();
		node.setProbability(prob);
			}
		}//for i
			
	  }	
	
	public static void getDescendants(Node _node, ArrayList<Node> result){
		
		for(int i=0; i<_node.getChildren().size();i++){
			
			result.add(_node.getChildren().get(i));
			getDescendants((Node) _node.getChildren().get(i),result);
		}
		
	}
	
	public static double getProbabilityByDesc(Node _node,String _desc){
	
	double result =0.0;
		if(_node.getDescription().equalsIgnoreCase(_desc)){result = _node.getProbability();}	
		else{
			for(int i=0;i<_node.getChildren().size();i++){
			
			result = getProbabilityByDesc((Node) _node.getChildren().get(i),_desc);	
			if(result>0.0)break;	
			}
		}	
		return result;
		}
	
	
	public static Node getNodeByDesc(Node _node,String _desc){
		
	Node result = null;
	
		if(_node.getDescription().equalsIgnoreCase(_desc)){result = _node;}	
		else{
			for(int i=0;i<_node.getChildren().size();i++){
			
			result = getNodeByDesc((Node) _node.getChildren().get(i),_desc);	
			if(result!=null)break;	
			}
		}	
		return result;
	}
	
	
	public static Node getNodeByID(Node _node,int id){
		
		Node result = null;
		
			if(_node.id==id){result = _node;}	
			else{
				for(int i=0;i<_node.getChildren().size();i++){
				
				result = getNodeByID((Node) _node.getChildren().get(i),id);	
				if(result!=null)break;	
				}
			}	
			return result;
	}
	
	
	public static Node getFather(Node _tree,Node _node){
		
	Node result = null;	
	
	ArrayList<Node> children = _tree.getChildren();
	
	if(children.size()>0) {
	for(int i=0;i<_tree.getChildren().size();i++){
	 
		Node child = (Node) _tree.getChildren().get(i);
		
			if(child.getDescription()==_node.getDescription()){
			
				result = _tree;
				if(result!=null)break;
			}	
			
			else{
					if(result==null){
					result = getFather(child, _node);
					}
						
				}	
		}
	}
	
	return result;
	}
	
	
	public static ArrayList<Node> getSiblings(Node _tree, Node _node){
	
	Node father = getFather(_tree,_node);
	ArrayList<Node> children = (ArrayList<Node>) father.getChildren().clone();	
	for(int i=0;i<children.size();i++){
			if(_node.getDescription()==((Node) children.get(i)).getDescription()){
				
				children.remove(i);
				
			}
			
		}	
		
	return children;	
		
	}
	
	public static void getAncestors(Node _node, Node _tree, ArrayList<Node> result){
		
	Node father = getFather(_tree,_node);	
	if(father!=null && !father.isRoot()) {
		
		result.add(father);	
		getAncestors(father,_tree,result);
	}
			
	}
	

	
	
	public static void getLeafNodes(Node _node,ArrayList<Node> result){
	
	if(_node.isLeaf()){result.add(_node);}
	else{
		
		for(int i=0;i<_node.getChildren().size();i++){
			
			 getLeafNodes((Node) _node.getChildren().get(i),result);	
			
			}
		}	
	}
		
		
  public static Node cloneTree(Node _node) {
	Node newRoot = new Node(_node);
		
	cloneChildren(newRoot);
	  
	return newRoot;  
  }
	  
  public static void cloneChildren(Node _node){
  ArrayList<Node> newChildren = new ArrayList<Node>();	  
  
		for(int i=0;i<_node.getChildren().size();i++){
		
			Node child =  (Node) _node.getChildren().get(i);
			Node newChild = new Node(child);
			
			newChildren.add(newChild);
			cloneChildren(newChild);
			
		}  
	  _node.setChildren(newChildren);
  }
	
	public static ArrayList<Node> getMostFrequentNodes(Node _node){
		ArrayList<Node> result = new ArrayList<Node>();
		double max = 0;
		for(int i=0;i<_node.getChildren().size();i++){
		Node node = (Node) _node.getChildren().get(i);
		if(node.getProbability()>max){
			max=node.getProbability();
			result = new ArrayList<Node>(); 
			result.add(node);
			}
		else if (node.getProbability()==max){
			result.add(node);
			}
		
		}
		
		for (int i=0;i<result.size();i++) {
		
			result.get(i).setSpecific(true);	
		}
		return result;
	}
	
	
	public static void getMostDeviatingNodes(Node _specifictree, Node _backgroundtree, ArrayList<Node> result,double max){
		
		
		for(int i=0;i<_specifictree.getChildren().size();i++){	
			
			Node node = (Node) _specifictree.getChildren().get(i);
			Node back_node = (Node) _backgroundtree.getChildren().get(i);
			
			double dev =  node.getProbability() - back_node.getProbability();
		
			if(dev>max) {max = dev;result.clear();result.add(node);node.setSpecific(true);}
			else if (dev == max){result.add(node);node.setSpecific(true);
			}
		}
		
		for(int i=0;i<_specifictree.getChildren().size();i++){

		Node node = (Node) _specifictree.getChildren().get(i);
		Node back_node = (Node) _backgroundtree.getChildren().get(i);
			
			getMostDeviatingNodes(node,back_node,result,max);	
				
		}	
			
		
	}
	
	
	public static int getNumberOfDescendants(Node _node, int sum){
		
	int result = sum;
	
	result += _node.getChildren().size();
	
	for(int i=0; i<_node.getChildren().size();i++){
		
		result = getNumberOfDescendants((Node) _node.getChildren().get(i),result);
	}
	
	return result;

	}
	
	public static void sumTreeByLeafs(Node _node) {

		ArrayList<Node> children = _node.getChildren();

		for (int i = 0; i < children.size(); i++) {
			Node node = children.get(i);
			if (!node.isLeaf()) {
				sumTreeByLeafs(node);

				double childprobabilities = 0.0;
				for (int k = 0; k < node.getChildren().size(); k++) {

					Node child = (Node) node.getChildren().get(k);
					childprobabilities += child.getProbability();
				}

				node.setProbability(childprobabilities);

			}

		}

	}
	
	
}
