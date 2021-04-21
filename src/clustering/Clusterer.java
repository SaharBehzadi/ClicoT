package clustering;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToIntFunction;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import javax.rmi.CORBA.Util;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import attributes.Attribute;
import attributes.AttributeStructure;
import attributes.CategoricalAttribute;
import attributes.NumericalAttribute;
import attributes.Elements;
import data.DataItem;
import data.DataSet;
import misc.Utils;
import tree.Node;
import tree.Tree;

public class Clusterer {

	private DataSet data;
	private Node temp_Specific_Tree;
	private AttributeStructure attributes;
	private String splitmode;
	private ArrayList<Cluster> clustering = new ArrayList<Cluster>();
	private double finalcosts = 0;
	private double oldcosts;
	private boolean firststart = true;
	private boolean clusteringchanged = true;
	private ArrayList<ArrayList<Cluster>> clusterhistory;
	int counter = 0;
    public double runtime;

	public Clusterer(AttributeStructure attributes, DataSet data, String splitmode) {

		this.attributes = attributes;
		this.data = data;
		this.splitmode = splitmode;
		runtime=0.0;

	}

	public void startClustering() {
		
        long startTime = System.currentTimeMillis();
        
		ArrayList<CategoricalAttribute> categoricalAts = attributes.getCategoricalAttributes();
		for(int i=0;i<categoricalAts.size();i++)
			categoricalAts.get(i).Back_Tree= Tree.cloneTree(categoricalAts.get(i).rootNode);

		clusterhistory = new ArrayList<ArrayList<Cluster>>();

		ArrayList<DataItem> data_items = new ArrayList<DataItem>();

		Cluster startCluster = new Cluster();

		for (int i = 0; i < data.size(); i++) {
			data.get(i).setCluster(startCluster);
			data_items.add(data.get(i));
		}

		startCluster.setData_items(data_items);

		clustering.add(startCluster);

		for (int i = 0; i < clustering.size(); i++) {
			clustering.get(i).clearAttributes();
		}

		for (int i = 0; i < clustering.size(); i++) {
			setCostFunctions(clustering.get(i));
		}

		update(this.clustering);
        
		long endTime = System.currentTimeMillis();
		runtime = (double) (endTime - startTime);
	}

	public void update(ArrayList<Cluster> clustering) {

		ArrayList<Cluster> savecluster = cloneClustering(clustering);
		clusterhistory.add(savecluster);

		if (clustering.size() == 1 && firststart) {
			oldcosts = Double.POSITIVE_INFINITY;
			firststart = false;
		}

		ArrayList<Cluster> oldclustering = cloneClustering(clustering);

		Cluster clusterToSplit = getMostExpensiveCluster(clustering);
		int length_of_split_cluster = clusterToSplit.getDataItems().size();

		// ---------------- initialization
		
		if (length_of_split_cluster > 1) {

			if (splitmode == "point") {
				int oldidx = clustering.indexOf(clusterToSplit);
				ArrayList<Cluster> two_clusters = pointSplitCluster(clusterToSplit);
				clustering.add(oldidx, two_clusters.get(0));
				clustering.add(two_clusters.get(1));

			}

			if (splitmode == "random") {
				ArrayList<Cluster> two_clusters = randomSplitCluster(clusterToSplit);
				for (int i = 0; i < two_clusters.size(); i++) {
					clustering.add(two_clusters.get(i));
				}
			}

			if (splitmode == "extended") {
				int oldidx = clustering.indexOf(clusterToSplit);
				ArrayList<Cluster> two_clusters = this.pointSplitClusterExtended(clusterToSplit);
				clustering.add(oldidx, two_clusters.get(0));
				clustering.add(two_clusters.get(1));
			}

		}

		ArrayList<Cluster> splitcluster = cloneClustering(clustering);
		clusterhistory.add(splitcluster);

		counter++;

		int whilecounter = 0;

		while (clusteringchanged) {

			reAssignDataItems();
			setAllCostFunctions();

			whilecounter++;
			if (whilecounter == 10)
				break;

		}
		clusteringchanged = true;

		double costs = getClusteringCosts(clustering);

		if (costs < oldcosts) {

			oldcosts = costs;
			update(clustering);

		}

		else {

			this.clustering = oldclustering;
			this.finalcosts = oldcosts;
		}

	}

	public void setCostFunctions_Initialization(Cluster cluster) {

		ArrayList<NumericalAttribute> numericalAts = attributes.getNumericalAttributes();
		ArrayList<CategoricalAttribute> categoricalAts = attributes.getCategoricalAttributes();
		ArrayList<DataItem> clusterData = cluster.getDataItems();

		double totalnumcosts = 0;
		double totalcatcosts = 0;
		double totalparamcosts = 0;

		for (int i = 0; i < numericalAts.size(); i++) {

			cluster.addNumericalAttribute(numericalAts.get(i));

			if (numericalAts.get(i).consider()) {

				double global_mean = numericalAts.get(i).getGlobalMean();
				double global_variance = numericalAts.get(i).getGlobalVariance();
				double cluster_mean = 0;
				double cluster_variance = 0;

				double[] values = new double[clusterData.size()];

				for (int j = 0; j < clusterData.size(); j++) {

					values[j] = clusterData.get(j).numericalvalues[i];

				}

				cluster_mean = Utils.calculateMean(values);
				cluster_variance = Utils.calculateVariance(values, cluster_mean);

//				for initialization of a new cluster(when we have only one point for each cluster) we consider the global variance as the cluster variance
				if (cluster_variance < global_variance / 1000) {
					cluster_variance = global_variance / clusterData.size();
				}

				NormalDistribution specificDist = new NormalDistribution(cluster_mean, Math.sqrt(cluster_variance));
				NormalDistribution unspecificDist = new NormalDistribution(global_mean, Math.sqrt(global_variance));

				double specific_costs = 0;
				double unspecific_costs = 0;

				for (int j = 0; j < cluster.getDataItems().size(); j++) {

					double val = values[j];
					double pdfvalue_unspec = unspecificDist.density(val);
					double pdfvalue_spec = specificDist.density(val);

					if (pdfvalue_unspec == 0)
						pdfvalue_unspec = 10E-7;
					if (pdfvalue_spec == 0)
						pdfvalue_spec = 10E-7;

					unspecific_costs += (Utils.log2(pdfvalue_unspec)) * -1;
					specific_costs += (Utils.log2(pdfvalue_spec)) * -1;

				}

				double paramcosts = Utils.log2(clusterData.size());

				specific_costs += paramcosts;

				if (specific_costs <= unspecific_costs) {

					cluster.setNumericalAttribute(i, new NumericalAttribute(numericalAts.get(i).getName(),
							numericalAts.get(i).getType(), specificDist, false, global_mean, global_variance));

					totalnumcosts += specific_costs;
					totalparamcosts += paramcosts;
				} else {
					cluster.setNumericalAttribute(i, new NumericalAttribute(numericalAts.get(i).getName(),
							numericalAts.get(i).getType(), unspecificDist, false, global_mean, global_variance));

					totalnumcosts += unspecific_costs;
				}

			}

		} // for i numerical Attr.

		for (int i = 0; i < categoricalAts.size(); i++) {

			if (categoricalAts.get(i).consider()) {

				String[] cat_values = new String[clusterData.size()];

				for (int j = 0; j < clusterData.size(); j++) {
					cat_values[j] = clusterData.get(j).categoricalvalues[i];
				}

				Node backGroundTree = categoricalAts.get(i).rootNode;

				Node specificTree = Tree.cloneTree(backGroundTree);
				Tree.setNodeProbabilites(specificTree, clusterData, i); // set
																		// relative
																		// probs
																		// according
																		// to
																		// cluster

				// ArrayList mostFreqNodes =
				// Tree.getMostFrequentNodes(specificTree);

				ArrayList<Node> mostFreqNodes = new ArrayList<Node>();
				Tree.getMostDeviatingNodes(specificTree, backGroundTree, mostFreqNodes, 0);

				Iterator<Node> it = mostFreqNodes.iterator();
				while (it.hasNext()) {
					Node n = it.next();
					Node father = Tree.getFather(specificTree, n);
					if (mostFreqNodes.contains(father)) {
						it.remove();
					}
				}

				double mostfreqbacksum = 0.0;

				for (int j = 0; j < mostFreqNodes.size(); j++) {

					Node node = (Node) mostFreqNodes.get(j);
					(node).setSpecific(true);
					String desc = node.getDescription();
					double prob = Tree.getProbabilityByDesc(backGroundTree, desc);
					mostfreqbacksum += prob;
				}

				double rest_1 = 0;
				double rest_2 = mostfreqbacksum;
				double ratio = 0.0;

				if (mostfreqbacksum == 1.0)
					ratio = 1.0;
				else {

					for (int j = 0; j < mostFreqNodes.size(); j++) {

						Node freqNode = (Node) mostFreqNodes.get(j);
						rest_1 += freqNode.getProbability();
					}

					if (rest_1 > 1.0)
						rest_1 = 1.0; // case for round error
					rest_1 = 1 - rest_1;
					rest_2 = 1 - rest_2;
					ratio = rest_1 / rest_2;
				}

				setSpecTreeProbs(specificTree, backGroundTree, mostFreqNodes, ratio);
				// New_setSpecTreeProbs(specificTree, backGroundTree,
				// mostFreqNodes, ratio);

				ArrayList<Node> leafs = new ArrayList<Node>();
				ArrayList<Node> backleafs = new ArrayList<Node>();
				Tree.getLeafNodes(specificTree, leafs);
				Tree.getLeafNodes(backGroundTree, backleafs);

				double non_zero_leafs = 0.0;
				double zero_leafs = 0.0;
				double correction_value = 0.01;
				double zero_prob_back_probs = 0.0;

				for (int j = 0; j < leafs.size(); j++) {

					Node leaf = leafs.get(j);
					double prob = leaf.getProbability();
					if (prob != 0.0)
						non_zero_leafs++;

					else {
						zero_leafs++;
						String desc = leaf.getDescription();
						zero_prob_back_probs += Tree.getProbabilityByDesc(backGroundTree, desc);
					}

				}

				if (zero_leafs > 0) {

					for (int j = 0; j < leafs.size(); j++) {

						Node leaf = leafs.get(j);
						double prob = leaf.getProbability();
						if (prob != 0.0) {
							double oldprob = leaf.getProbability();

							// double newprob =
							// oldprob-correction_value/non_zero_leafs;
							double newprob = oldprob - oldprob * correction_value;
							leaf.setProbability(newprob);

						}

						else {
							String desc = leaf.getDescription();
							double newprob = (correction_value / zero_prob_back_probs)
									* Tree.getProbabilityByDesc(backGroundTree, desc);
							leaf.setProbability(newprob);

						}

					}

					Tree.sumTreeByLeafs(specificTree);

				}

				double specific_costs = 0;
				double unspecific_costs = 0;

				for (int j = 0; j < cluster.getDataItems().size(); j++) {

					String val = cat_values[j];
					double background_prob = Tree.getProbabilityByDesc(backGroundTree, val);
					double specific_prob = Tree.getProbabilityByDesc(specificTree, val);

					// inconsitency check, if data correct not needed
					if (specific_prob == 0) {
						specific_prob += 0.00001;
					}

					unspecific_costs += (Utils.log2(background_prob) * -1);
					specific_costs += (Utils.log2(specific_prob) * -1);

				}

				int num_of_nodes = 1;
				num_of_nodes = Tree.getNumberOfDescendants(specificTree, num_of_nodes);
				double paramcosts = (double) num_of_nodes * 0.5 * Utils.log2(clusterData.size());
				specific_costs += paramcosts;

				if (specific_costs < unspecific_costs) {

					cluster.addCategoricalAttribute(
							new CategoricalAttribute(categoricalAts.get(i).getName(), categoricalAts.get(i).getType(),
									specificTree, categoricalAts.get(i).getDescription(), false, categoricalAts.get(i).Back_Tree));
					totalparamcosts += paramcosts;
					totalcatcosts += specific_costs;
				} else {

					cluster.addCategoricalAttribute(
							new CategoricalAttribute(categoricalAts.get(i).getName(), categoricalAts.get(i).getType(),
									backGroundTree, categoricalAts.get(i).getDescription(), false, categoricalAts.get(i).Back_Tree));

					totalcatcosts += unspecific_costs;
				}

			}

		} // for i

		double id_costs = Utils.log2((double) data.size() / (double) clusterData.size());
		id_costs *= (double) clusterData.size();

		cluster.setIdCosts(id_costs);
		cluster.setNumCosts(totalnumcosts);
		cluster.setCatCosts(totalcatcosts);
		cluster.setParamCosts(totalparamcosts);
		double totalcosts = totalnumcosts + totalcatcosts + totalparamcosts + id_costs;
		cluster.setTotalCosts(totalcosts);

	}

	public Cluster getMostExpensiveCluster(ArrayList<Cluster> _clustering) {

		Cluster result = new Cluster();
		double max = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < _clustering.size(); i++) {

			double costs = _clustering.get(i).getTotalCosts();
			if (costs > max) {
				max = costs;
				result = _clustering.get(i);
			}

		}
		return result;
	}

	public ArrayList<Cluster> pointSplitCluster(Cluster clusterToSplit) {

		ArrayList<Cluster> result = new ArrayList<Cluster>();
		int length_of_split_cluster = clusterToSplit.getDataItems().size();

		clustering.remove(clusterToSplit);

		Random random = new Random();
		int rand_idx = Utils.getRandomInt(random, 0, length_of_split_cluster - 1);
		int rand_idx2 = Utils.getRandomInt(random, 0, length_of_split_cluster - 1);
		while (rand_idx == rand_idx2)
			rand_idx2 = Utils.getRandomInt(random, 0, length_of_split_cluster - 1);

		Cluster newcluster1 = new Cluster();
		Cluster newcluster2 = new Cluster();

		DataItem a = clusterToSplit.getDataItems().get(rand_idx);
		DataItem b = clusterToSplit.getDataItems().get(rand_idx2);
		ArrayList<DataItem> data_items_a = new ArrayList<DataItem>();
		ArrayList<DataItem> data_items_b = new ArrayList<DataItem>();
		data_items_a.add(a);
		data_items_b.add(b);
		a.setCluster(newcluster1);
		b.setCluster(newcluster2);
		newcluster1.setData_items(data_items_a);
		newcluster2.setData_items(data_items_b);

		setCostFunctions_Initialization(newcluster1);
		setCostFunctions_Initialization(newcluster2);

		result.add(newcluster1);
		result.add(newcluster2);

		return result;

	}

	public ArrayList<Cluster> randomSplitCluster(Cluster cluster) {

		ArrayList<Cluster> result = new ArrayList<Cluster>();
		Cluster cluster1 = new Cluster();
		Cluster cluster2 = new Cluster();
		ArrayList<DataItem> data_items1 = new ArrayList<DataItem>();
		ArrayList<DataItem> data_items2 = new ArrayList<DataItem>();

		ArrayList<DataItem> old_items = new ArrayList<DataItem>(cluster.getDataItems());
		Collections.shuffle(old_items);

		for (int i = 0; i < old_items.size(); i++) {
			if ((i % 2) == 0) {
				data_items1.add(old_items.get(i));
				old_items.get(i).setCluster(cluster1);
			} else {
				data_items2.add(old_items.get(i));
				old_items.get(i).setCluster(cluster2);
			}
		}

		cluster1.setData_items(data_items1);
		cluster2.setData_items(data_items2);
		setCostFunctions(cluster1);
		setCostFunctions(cluster2);
		result.add(cluster1);
		result.add(cluster2);

		clustering.remove(cluster);

		return result;
	}

	public ArrayList<Cluster> pointSplitClusterExtended(Cluster clusterToSplit) {

		ArrayList<Cluster> result = new ArrayList<Cluster>();
		int length_of_split_cluster = clusterToSplit.getDataItems().size();

		clustering.remove(clusterToSplit);

		Random random = new Random();
		int rand_idx = Utils.getRandomInt(random, 0, length_of_split_cluster - 1);
		int rand_idx2 = Utils.getRandomInt(random, 0, length_of_split_cluster - 1);
		while (rand_idx == rand_idx2)
			rand_idx2 = Utils.getRandomInt(random, 0, length_of_split_cluster - 1);

		Cluster newcluster1 = new Cluster();
		Cluster newcluster2 = new Cluster();

		DataItem a = clusterToSplit.getDataItems().get(rand_idx);

		ArrayList<DataItem> sortedpoints_a = sortPointsByDistance(a, clusterToSplit.getDataItems());

		DataItem b = this.getMostDistantPoint(sortedpoints_a);

		ArrayList<DataItem> sortedpoints_b = sortPointsByDistance(b, clusterToSplit.getDataItems());

		ArrayList<DataItem> data_items_a = new ArrayList<DataItem>();
		ArrayList<DataItem> data_items_b = new ArrayList<DataItem>();

		int[] deviders = { 2, 4, 8, 10, 20, 30, length_of_split_cluster };
		int idx = Utils.getRandomInt(random, 0, 6);

		int devider = deviders[idx];
		int environmentsize = length_of_split_cluster / devider;
		if (environmentsize < 1)
			environmentsize = 1;

		ArrayList<DataItem> list1 = getNearestPoints(sortedpoints_a, environmentsize);
		ArrayList<DataItem> list2 = getNearestPoints(sortedpoints_b, environmentsize);

		for (int i = 0; i < list1.size(); i++) {
			DataItem item = list1.get(i);
			if (list2.contains(item)) {
				list2.remove(item);
			}
		}

		for (int i = 0; i < list1.size(); i++) {
			data_items_a.add(list1.get(i));
			list1.get(i).setCluster(newcluster1);
		}

		for (int i = 0; i < list2.size(); i++) {
			data_items_b.add(list2.get(i));
			list2.get(i).setCluster(newcluster2);

		}

		// data_items_a.add(a);
		// data_items_b.add(b);

		newcluster1.setData_items(data_items_a);
		newcluster2.setData_items(data_items_b);

		setCostFunctions(newcluster1);
		setCostFunctions(newcluster2);
		result.add(newcluster1);
		result.add(newcluster2);

		return result;
	}

	public void setCostFunctions(Cluster cluster) {

		ArrayList<NumericalAttribute> numericalAts = attributes.getNumericalAttributes();
		ArrayList<CategoricalAttribute> categoricalAts = attributes.getCategoricalAttributes();
		ArrayList<DataItem> clusterData = cluster.getDataItems();
		ArrayList<Node> Cat_Attr_SpecificTree = new ArrayList<Node>();
		ArrayList<Node> Cluster_SpecificTree = new ArrayList<Node>();
		int [] Is_Specific_Cat= new int[categoricalAts.size()];

		if (clusterData.size() == 0)
			return;

		double totalnumcosts = 0;
		double totalcatcosts = 0;
		double totalparamcosts = 0;

		// ---------------------------- New Approach_Greedy

		ArrayList<Elements> OrderedDeviation = new ArrayList<Elements>();
		int Element_id = 0;

		// ----------------------Categorical Attributes _ Set_deviation----------------

		for (int i = 0; i < categoricalAts.size(); i++) {

			String[] cat_values = new String[clusterData.size()];

			for (int j = 0; j < clusterData.size(); j++) {
				cat_values[j] = clusterData.get(j).categoricalvalues[i];
			}

			Node backGroundTree = categoricalAts.get(i).rootNode;

			// set Specific Tree for the current categorical attribute based on
			// background tree and data points in this cluster

//			set cluster relative tree
			Node SpecificTree_object = Tree.cloneTree(backGroundTree);
			Tree.setNodeProbabilites(SpecificTree_object, clusterData, i);

//			save specific tree for each cat attr.
			Node tempAttr = new Node(Tree.cloneTree(SpecificTree_object));
			Cat_Attr_SpecificTree.add(Tree.cloneTree(tempAttr));
			
// 			save cluster relative tree to avoid constructing again
			Node tempCluster = new Node();
			tempCluster = Tree.cloneTree(SpecificTree_object);
			Cluster_SpecificTree.add(Tree.cloneTree(tempCluster));

			int NumOFNodes = 1;
			NumOFNodes = Tree.getNumberOfDescendants(backGroundTree, NumOFNodes);

			for (int j = Element_id; j < NumOFNodes + Element_id; j++) {

				Node specificTree = new Node();
				specificTree = Tree.cloneTree(Cluster_SpecificTree.get(i));

				ArrayList<Node> mostFreqNodes = new ArrayList<Node>();
				mostFreqNodes.add(Tree.getNodeByID(specificTree, j));

//				mark this element as specific in cluster relative tree or current specific tree
				for (int k = 0; k < mostFreqNodes.size(); k++) 
					Tree.getNodeByDesc(specificTree, mostFreqNodes.get(k).getDescription()).setSpecific(true);

//				set Specific tree's probabilities
				temp_Specific_Tree = new Node();
				temp_Specific_Tree = Tree.cloneTree(specificTree);
				New_setSpecTreeProbs(backGroundTree, specificTree);
				specificTree = new Node();
				specificTree = Tree.cloneTree(temp_Specific_Tree);

				double specific_costs = 0;
				double unspecific_costs = 0;

//				compute specific cost based on specific Tree and unspecific cost based on background tree 
				for (int k = 0; k < cluster.getDataItems().size(); k++) {

					String val = cat_values[k];
					double background_prob = Tree.getProbabilityByDesc(backGroundTree, val);
					double specific_prob = Tree.getProbabilityByDesc(specificTree, val);

					// inconsistency check, if data correct not needed
					if (specific_prob == 0) {
						specific_prob += 0.00001;
					}
					double cost_back_node = Utils.log2(background_prob) * -1;
					double cost_specific_node = Utils.log2(specific_prob) * -1;
					
					if(Double.isInfinite(cost_back_node))
						System.out.println("Infinite");

					if(Double.isInfinite(cost_specific_node))
						System.out.println("Infinite");
					
					unspecific_costs += cost_back_node;
					specific_costs += cost_specific_node;


				}

//				mark the element as non-specific since we will decide later 
				for (int k = 0; k < mostFreqNodes.size(); k++)
					Tree.getNodeByDesc(specificTree, mostFreqNodes.get(k).getDescription()).setSpecific(false);

				double paramcosts = 0.5 * Utils.log2(clusterData.size());

				specific_costs += paramcosts;

				categoricalAts.get(i).NonSpecific_Cost = unspecific_costs;
				Elements new_Element = new Elements();
				new_Element.SpecificCost = specific_costs;
				new_Element.NonspecificCost = unspecific_costs;
				new_Element.Deviation = unspecific_costs - specific_costs;
				new_Element.Description = mostFreqNodes.get(0).getDescription();
				new_Element.id = j;
				new_Element.type = 0;
				new_Element.isSpecific = false;
				new_Element.specificTree = new Node();
				new_Element.specificTree = Tree.cloneTree(specificTree);
				OrderedDeviation.add(new_Element);

			} // for j, all variables for a categorical attributes

			Element_id += NumOFNodes;

		} // for all categorical attributes

		// ----------------------Numerical Attributes_Set_deviation------------------------

		for (int i = 0; i < numericalAts.size(); i++) {

			cluster.addNumericalAttribute(numericalAts.get(i));

			numericalAts.get(i).id = Element_id;
			double global_mean = numericalAts.get(i).getGlobalMean();
			double global_variance = numericalAts.get(i).getGlobalVariance();
			double cluster_mean = 0;
			double cluster_variance = 0;

			double[] values = new double[clusterData.size()];

			for (int j = 0; j < clusterData.size(); j++) 
				values[j] = clusterData.get(j).numericalvalues[i];

			cluster_mean = Utils.calculateMean(values);
			cluster_variance = Utils.calculateVariance(values, cluster_mean);

			if (cluster_variance < global_variance / 1000) 
				cluster_variance = global_variance / clusterData.size();

int m=0;
			if(cluster_variance==0)
				m=0;
			NormalDistribution specificDist = new NormalDistribution(cluster_mean, Math.sqrt(cluster_variance));
			NormalDistribution unspecificDist = new NormalDistribution(global_mean, Math.sqrt(global_variance));

			double specific_costs = 0;
			double unspecific_costs = 0;

			for (int j = 0; j < cluster.getDataItems().size(); j++) {

				double val = values[j];
				double pdfvalue_unspec = unspecificDist.density(val);
				double pdfvalue_spec = specificDist.density(val);

				if (pdfvalue_unspec == 0)
					pdfvalue_unspec = 10E-7;
				if (pdfvalue_spec == 0)
					pdfvalue_spec = 10E-7;

				unspecific_costs += (Utils.log2(pdfvalue_unspec)) * -1;
				specific_costs += (Utils.log2(pdfvalue_spec)) * -1;

			}

			double paramcosts = Utils.log2(clusterData.size());

			Elements new_Element = new Elements();
			new_Element.SpecificCost = specific_costs + paramcosts;
			new_Element.NonspecificCost = unspecific_costs;
			new_Element.Deviation = Math.sqrt(unspecific_costs - specific_costs - paramcosts);
			new_Element.id = Element_id++;
			new_Element.type = 1;
			new_Element.Description = numericalAts.get(i).getName();
			new_Element.isSpecific = false;
			new_Element.NumericalNonspecificDist = unspecificDist;
			new_Element.NumericalSpecificDist = specificDist;

			OrderedDeviation.add(new_Element);
		}

		// ---------------------------- Select Specific Element

		boolean It_pays_off = true;
		int NumOfSpecificAttributes = 0;

		double NumOfElements = OrderedDeviation.size();

		while (It_pays_off) {

			// Sort numerical attributes based on SpecificDataCost – DataCost

			OrderedDeviation.sort(Elements.COMPARE_BY_Deviation);

			if (OrderedDeviation.get(0).Deviation <= 0) {
				It_pays_off = false;
				break;
			}

			NumOfSpecificAttributes++;
			double SpecifiCIDCost = 0;

			if (NumOfElements == NumOfSpecificAttributes)
				SpecifiCIDCost = 0;

			else
				SpecifiCIDCost = NumOfSpecificAttributes * Math.log(NumOfElements / NumOfSpecificAttributes)
						+ (NumOfElements - NumOfSpecificAttributes)
								* Math.log(NumOfElements / (NumOfElements - NumOfSpecificAttributes));

			// for categorical Attributes

			if (OrderedDeviation.get(0).type == 0) {

//				if(OrderedDeviation.get(0).id==3 || OrderedDeviation.get(0).id==4 )
//					System.out.println("green");
				

				// find corresponding node to specificID and set as specific

				
				Tree.getNodeByID(OrderedDeviation.get(0).specificTree, OrderedDeviation.get(0).id).setSpecific(true);
				
				int Specific_Cat_Attr_ID = 0;

				for (int i = 0; i < categoricalAts.size(); i++) {

//					find specific categorical attr. and set as specific
					Node Specific_Categorical_Attr = Tree.getNodeByID(Cat_Attr_SpecificTree.get(i),
							OrderedDeviation.get(0).id);

					if (Specific_Categorical_Attr != null){
						Specific_Cat_Attr_ID = i;
						Cat_Attr_SpecificTree.set(i, Tree.cloneTree(OrderedDeviation.get(0).specificTree));
						Is_Specific_Cat[Specific_Cat_Attr_ID]=1;
//						categoricalAts.get(i).setSpecific(true);
						break;
					}
				}

				String[] cat_values = new String[clusterData.size()];

				for (int j = 0; j < clusterData.size(); j++) {
					cat_values[j] = clusterData.get(j).categoricalvalues[Specific_Cat_Attr_ID];
				}

				Node Root = categoricalAts.get(Specific_Cat_Attr_ID).rootNode;

				int NumOFNodes = 1;
				NumOFNodes = Tree.getNumberOfDescendants(Root, NumOFNodes);

				ArrayList<Node> LastSpecificNodes = new ArrayList<Node>();

				// find all specific Nodes from Categorical Specific Tree

				for (int j = Root.id; j < Root.id + NumOFNodes; j++) {

					Node Find_Spe_Cat_Attr = Tree.getNodeByID(Cat_Attr_SpecificTree.get(Specific_Cat_Attr_ID), j);

					if (Find_Spe_Cat_Attr.isSpecific())
						LastSpecificNodes.add(Find_Spe_Cat_Attr);

				}

				// for all nodes except mostFreqNodes

				for (int j = Root.id; j < Root.id + NumOFNodes; j++) {

					ArrayList<Node> mostFreqNodes = new ArrayList<Node>();

//					set current specific tree with the last specific tree
					Node specificTree = new Node();
					specificTree = Tree.cloneTree(OrderedDeviation.get(0).specificTree);

					for (int k = 0; k < LastSpecificNodes.size(); k++) {
						Tree.getNodeByID(specificTree, LastSpecificNodes.get(k).id).setSpecific(true);
						mostFreqNodes.add(Tree.getNodeByID(specificTree, LastSpecificNodes.get(k).id));
					}

					if (Tree.getNodeByID(Cat_Attr_SpecificTree.get(Specific_Cat_Attr_ID), j).isSpecific()) {
						Tree.getNodeByID(specificTree, j).setSpecific(true);
						continue;
					}

					mostFreqNodes.add(Tree.getNodeByID(specificTree, j));
					Tree.getNodeByID(specificTree, j).setSpecific(true);

					temp_Specific_Tree = new Node();
					temp_Specific_Tree = Tree.cloneTree(specificTree);

					// considering original back ground tree as current background tree
					New_setSpecTreeProbs(Root, specificTree);

					specificTree = new Node();
					specificTree = Tree.cloneTree(temp_Specific_Tree);

					double specific_costs = 0;
					double unspecific_costs = 0;

					for (int k = 0; k < cluster.getDataItems().size(); k++) {

						String val = cat_values[k];

						// previous specific Tree comparing to current specific tree
						double background_prob = Tree.getProbabilityByDesc(Cat_Attr_SpecificTree.get(Specific_Cat_Attr_ID), val);

						double specific_prob = Tree.getProbabilityByDesc(specificTree, val);

						// inconsistency check, if data correct not needed
						if (specific_prob == 0) {
							specific_prob += 0.00001;
						}

						double a = (Utils.log2(background_prob) * -1);
						double b = (Utils.log2(specific_prob) * -1);

						unspecific_costs += (Utils.log2(background_prob) * -1);
						specific_costs += (Utils.log2(specific_prob) * -1);

					}

					double paramcosts = 0.5 * Utils.log2(clusterData.size());

					specific_costs += paramcosts;

					int orderedID = 0;
					for (int z = 0; z < OrderedDeviation.size(); z++)
						if (OrderedDeviation.get(z).id == j) {
							orderedID = z;
							break;
						}

					Tree.getNodeByID(specificTree, j).setSpecific(false);

					OrderedDeviation.get(orderedID).Deviation = unspecific_costs - specific_costs;
					OrderedDeviation.get(orderedID).SpecificCost = specific_costs;
					OrderedDeviation.get(orderedID).NonspecificCost = unspecific_costs;
					OrderedDeviation.get(orderedID).specificTree = Tree.cloneTree(specificTree);
				}


				// set categorical specific cost for each categorical attribute
				categoricalAts.get(Specific_Cat_Attr_ID).Specific_Cost = OrderedDeviation.get(0).SpecificCost;
				OrderedDeviation.remove(0);
			}

			// for Numerical Attributes

			else {

				NumericalAttribute Specific_Numerical_Attr = new NumericalAttribute();

				int pecific_Numerical_Attr_ID = 0;

				for (int i = 0; i < numericalAts.size(); i++)
					if (numericalAts.get(i).id == OrderedDeviation.get(0).id) {
						Specific_Numerical_Attr = numericalAts.get(i);
						Specific_Numerical_Attr.setSpecific(true);
						pecific_Numerical_Attr_ID = i;
						break;
					}

				cluster.setNumericalAttribute(pecific_Numerical_Attr_ID,
						new NumericalAttribute(Specific_Numerical_Attr.getName(), Specific_Numerical_Attr.getType(),
								OrderedDeviation.get(0).NumericalSpecificDist, true,
								Specific_Numerical_Attr.getGlobalMean(), Specific_Numerical_Attr.getGlobalVariance()));

				totalnumcosts += OrderedDeviation.get(0).SpecificCost + SpecifiCIDCost;
				OrderedDeviation.remove(0);
			}

		} // end while

		for (int i = 0; i < OrderedDeviation.size(); i++) {

			if(OrderedDeviation.get(i).type == 1){

				NumericalAttribute NonSpecific_Numerical_Attr = new NumericalAttribute();
				int NonSpecific_Numerical_Attr_id = 0;
				for (int j = 0; j < numericalAts.size(); j++)
					if (numericalAts.get(j).id == OrderedDeviation.get(i).id) {
						NonSpecific_Numerical_Attr = numericalAts.get(j);
						NonSpecific_Numerical_Attr_id = j;
						break;
					}

				totalnumcosts += OrderedDeviation.get(i).NonspecificCost;

				cluster.setNumericalAttribute(NonSpecific_Numerical_Attr_id,
						new NumericalAttribute(NonSpecific_Numerical_Attr.getName(),
								NonSpecific_Numerical_Attr.getType(), OrderedDeviation.get(i).NumericalNonspecificDist,
								false, NonSpecific_Numerical_Attr.getGlobalMean(),
								NonSpecific_Numerical_Attr.getGlobalVariance()));
			}

		}

		// add Non-Specific Categorical Attributes

		for (int i = 0; i < categoricalAts.size(); i++) {
			
//			if (categoricalAts.get(i).isSpecific()) {
			if(Is_Specific_Cat[i]==1){
				cluster.addCategoricalAttribute(
						new CategoricalAttribute(categoricalAts.get(i).getName(), categoricalAts.get(i).getType(),
								Cat_Attr_SpecificTree.get(i), categoricalAts.get(i).getDescription(), true, categoricalAts.get(i).Back_Tree));
				totalcatcosts += categoricalAts.get(i).Specific_Cost;
			}

			else {
				cluster.addCategoricalAttribute(
						new CategoricalAttribute(categoricalAts.get(i).getName(), categoricalAts.get(i).getType(),
								categoricalAts.get(i).rootNode, categoricalAts.get(i).getDescription(), false, categoricalAts.get(i).Back_Tree));
				totalcatcosts += categoricalAts.get(i).NonSpecific_Cost;
			}
		}

		// cluster ID cost

		double id_costs = Utils.log2((double) data.size() / (double) clusterData.size());
		id_costs *= (double) clusterData.size();

		cluster.setIdCosts(id_costs);
		cluster.setNumCosts(totalnumcosts);
		cluster.setCatCosts(totalcatcosts);
		cluster.setParamCosts(totalparamcosts);
		cluster.setTotalCosts(totalnumcosts + totalcatcosts + id_costs);

	}

	public void reAssignDataItems() {

		clusteringchanged = false;

		for (int i = 0; i < data.size(); i++) {

			DataItem item = data.get(i);
			double min = Double.POSITIVE_INFINITY;
			Cluster newcluster = null;

			for (int k = 0; k < clustering.size(); k++) {

				Cluster cluster = clustering.get(k);

				double numcosts = getNumCostsForOneItem(cluster, item);
				double catcosts = getCatCostsForOneItem(cluster, item);

				double newcosts = catcosts + numcosts;

				if (newcosts < min) {

					min = newcosts;
					newcluster = cluster;

				}

			} // for k

			if (!newcluster.getDataItems().contains(item)) {
				item.getCluster().getDataItems().remove(item);
				item.setCluster(newcluster);
				newcluster.getDataItems().add(item);
				clusteringchanged = true;
			}

		}

		// remove cluster with length 0;

		Iterator<Cluster> it = clustering.iterator();
		while (it.hasNext()) {
			Cluster c = it.next();
			if (c.getDataItems().size() == 0) {
				it.remove();
			}
		}

	}// reassign

	public double getNumCostsForOneItem(Cluster cluster, DataItem dataitem) {

		double numerical_costs = 0;

		ArrayList<NumericalAttribute> numericalAts = cluster.getNumericalAttributes();

		for (int j = 0; j < numericalAts.size(); j++) {

			if (numericalAts.get(j).consider()) {

				NormalDistribution dist = numericalAts.get(j).getNormalDistribution();

				double pdfvalue = dist.density(dataitem.numericalvalues[j]);
				if (pdfvalue == 0.0) {

					pdfvalue = 10E-7;

				}

				double cost = Utils.log2(pdfvalue) * -1;
				numerical_costs += cost;

			}

		} // for i

		return numerical_costs;
	}

	public double getCatCostsForOneItem(Cluster cluster, DataItem dataitem) {

		double categorical_costs = 0;

		ArrayList<CategoricalAttribute> categoricalAts = cluster.getCategoricalAttributes();

		for (int j = 0; j < categoricalAts.size(); j++) {

			if (categoricalAts.get(j).consider()) {

				Node tree = categoricalAts.get(j).rootNode;
				double prob = 0;
				String desc = dataitem.categoricalvalues[j];
				if (desc == "") {

					System.out.println("STOP");

				}
				prob = Tree.getProbabilityByDesc(tree, desc);
				Node searchnode = Tree.getNodeByDesc(tree, desc);
				if (searchnode != null) { // case for unknown values

					Node father = Tree.getFather(tree, searchnode);

					if (prob == 0.0)
						prob = father.getProbability();

					categorical_costs += Utils.log2(prob) * -1;

				}

			}

		}

		return categorical_costs;
	}

	public void setSpecTreeProbs(Node _specificTree, Node _backgroundTree, ArrayList<Node> mfn, double _ratio) {

		for (int i = 0; i < _specificTree.getChildren().size(); i++) {

			Node node = (Node) _specificTree.getChildren().get(i);
			Node back_node = (Node) _backgroundTree.getChildren().get(i);

			for (int j = 0; j < mfn.size(); j++) {

				Node mfn_node = (Node) mfn.get(j);

				if (node.getDescription().equalsIgnoreCase(mfn_node.getDescription())) {
					continue;
				}

				else {
					if (!node.isLeaf()) {

						setSpecTreeProbs(node, back_node, mfn, _ratio);

						double childprobabilities = 0.0;

						for (int k = 0; k < node.getChildren().size(); k++) {

							Node child = (Node) node.getChildren().get(k);

							childprobabilities += child.getProbability();
						}

						node.setProbability(childprobabilities);
					}

					else
						node.setProbability(back_node.getProbability() * _ratio);

				}
			}
		}
	}

	public double[] New_setSpecTreeProbs(Node _backgroundTree, Node V) {

		double[] SSP_SUP = new double[2];

		Node node = (Node) Tree.getNodeByDesc(temp_Specific_Tree, V.getDescription());
		Node back_node = (Node) Tree.getNodeByDesc(_backgroundTree, V.getDescription());

		if (V.isLeaf() && V.isSpecific()) {
			SSP_SUP[0] = node.getProbability();
			SSP_SUP[1] = 0.0;
			return SSP_SUP;
		}

		if (V.isLeaf() && !V.isSpecific()) {
			SSP_SUP[0] = 0.0;
			SSP_SUP[1] = back_node.getProbability();
			return SSP_SUP;
		}

		if (!V.isLeaf() && (V.isSpecific() || V.isRoot())) {

			for (int i = 0; i < V.getChildren().size(); i++) {
				double[] temp = New_setSpecTreeProbs(_backgroundTree, V.getChildren().get(i));
				SSP_SUP[0] += temp[0];
				SSP_SUP[1] += temp[1];
			}
			double factor = 0;

			if (SSP_SUP[1] == 0)
				factor = 1;
			else
				factor = (node.getProbability() - SSP_SUP[0]) / SSP_SUP[1];

			for (int i = 0; i < V.getChildren().size(); i++)
				PropagateDown(_backgroundTree, V.getChildren().get(i), factor);

			SSP_SUP[0] = Tree.getNodeByDesc(temp_Specific_Tree, V.getDescription()).getProbability();
			SSP_SUP[1] = 0.0;
			return SSP_SUP;
		}

		for (int i = 0; i < V.getChildren().size(); i++) {
			double[] temp = New_setSpecTreeProbs(_backgroundTree, V.getChildren().get(i));
			SSP_SUP[0] += temp[0];
			SSP_SUP[1] += temp[1];
		}
		return SSP_SUP;

	}

	public void PropagateDown(Node _backgroundTree, Node V, double factor) {

		Node node = (Node) Tree.getNodeByDesc(temp_Specific_Tree, V.getDescription());
		Node back_node = (Node) Tree.getNodeByDesc(_backgroundTree, V.getDescription());

		if (V.isSpecific())
			return;

		if (V.isLeaf()) {
			double temp = back_node.getProbability() * factor;
			Tree.getNodeByDesc(temp_Specific_Tree, V.getDescription()).setProbability(temp);
			return;
		}

		double sum = 0;
		for (int i = 0; i < V.getChildren().size(); i++) {
			PropagateDown(_backgroundTree, V.getChildren().get(i), factor);
			sum += Tree.getNodeByDesc(temp_Specific_Tree, V.getChildren().get(i).getDescription()).getProbability();
		}
		Tree.getNodeByDesc(temp_Specific_Tree, V.getDescription()).setProbability(sum);
	}

	public ArrayList<Cluster> cloneClustering(ArrayList<Cluster> _clustering) {

		ArrayList<Cluster> result = new ArrayList<Cluster>();

		for (int i = 0; i < _clustering.size(); i++) {

			Cluster newcluster = new Cluster(_clustering.get(i));
			result.add(newcluster);
		}

		return result;
	}

	public double getClusteringCosts(ArrayList<Cluster> _clustering) {
		double result = 0;

		for (int i = 0; i < _clustering.size(); i++) {
			result += _clustering.get(i).getTotalCosts();
		}
		return result;
	}

	public ArrayList<Cluster> getClustering() {

		return this.clustering;

	}

	public double getFinalCosts() {

		return this.finalcosts;

	}

	public ArrayList<ArrayList<Cluster>> getClusterHistory() {

		return this.clusterhistory;

	}

	public void setAllCostFunctions() {

		for (int i = 0; i < clustering.size(); i++) {
			clustering.get(i).clearAttributes();
		}

		for (int i = 0; i < clustering.size(); i++) {

			Cluster c = clustering.get(i);
			setCostFunctions(c);
		}

	}

	public DataItem getMostDistantPoint(ArrayList<DataItem> sortedpoints) {

		DataItem result = sortedpoints.get(sortedpoints.size() - 1);

		return result;

	}

	public ArrayList<DataItem> getNearestPoints(ArrayList<DataItem> sortedpoints, int max) {

		ArrayList<DataItem> result = new ArrayList<DataItem>();

		for (int i = 0; i < max; i++) {
			result.add(sortedpoints.get(i));
		}

		return result;

	}

	public ArrayList<DataItem> sortPointsByDistance(DataItem point, ArrayList<DataItem> data) {

		TreeMap<Double, DataItem> treeresult = new TreeMap<Double, DataItem>();
		ArrayList<DataItem> result = new ArrayList<DataItem>();
		Cluster cluster = new Cluster();
		ArrayList<DataItem> data_items = new ArrayList<DataItem>();
		data_items.add(point);
		cluster.setData_items(data_items);
		setCostFunctions(cluster);

		for (int i = 0; i < data.size(); i++) {

			DataItem current = data.get(i);
			EuclideanDistance distmeasure = new EuclideanDistance();
			Double dist = distmeasure.compute(current.numericalvalues, point.numericalvalues);

			treeresult.put(dist, current);

		}

		for (Map.Entry<Double, DataItem> entry : treeresult.entrySet()) {

			DataItem value = entry.getValue();

			if (!value.equals(point)) {

				result.add(value);
			}
		}

		return result;

	}

	public boolean checkClustering(ArrayList<Cluster> _clustering) {
		boolean result = false;
		int sum = 0;
		for (int i = 0; i < _clustering.size(); i++) {
			sum += ((Cluster) _clustering.get(i)).getDataItems().size();

		}

		if (sum == data.size())
			result = true;
		return result;
	}

}// class
