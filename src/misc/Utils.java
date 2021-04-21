package misc;

import static java.lang.Math.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

import attributes.CategoricalAttribute;
import attributes.NumericalAttribute;
import clustering.Cluster;
import data.DataItem;
import data.DataSet;
import tree.*;

public class Utils {

	public static double MIN_VAR = 1E-2;

	public static double calculateMean(double[] values) {

		double mean = 0;

		for (int j = 0; j < values.length; j++) {
			mean += values[j];
		}

		mean /= values.length;
		return mean;
	}

	public static double calculateVariance(double[] values, double mean) {

		double var = 0;

		for (int j = 0; j < values.length; j++) {

			var += Math.pow(values[j] - mean, 2);

		}

		var /= values.length;

		return var;
	}

	public static String trim(String str) {
		return str.trim().replaceAll("_", " ");
	}

	public static double log2(double x) {
		return log(x) / log(2);
	}

	public static double calculateT(double[] means, double[] variances, double[] number) {

		double result = (means[0] - means[1]) / (Math.sqrt((variances[0] / number[0]) + (variances[1] / number[1])));

		return result;

	}

	public static double calculateV(double[] variances, double[] number, double[] degF) {

		double result = (Math.pow((variances[0] / number[0]) + (variances[1] / number[1]), 2))
				/ ((Math.pow(variances[0], 2) / (Math.pow(number[0], 2) * degF[0]))
						+ (Math.pow(variances[1], 2) / (Math.pow(number[1], 2) * degF[1])));

		return result;
	}

	public static double calculateG(double[] means, double[] variances, double[] number) {

		double result = (means[0] - means[1]) / Math.sqrt(
				((number[0] - 1) * variances[0]) + ((number[1] - 1) * variances[1]) / (number[0] + number[1] - 2));
		result = Math.abs(result);
		return result;

	}

	public static TreeMap<Double, DataItem> putFirstEntries(int max, SortedMap<Double, DataItem> source) {
		int count = 0;
		TreeMap<Double, DataItem> target = new TreeMap<Double, DataItem>();
		for (Entry<Double, DataItem> entry : source.entrySet()) {
			if (count >= max)
				break;

			target.put(entry.getKey(), entry.getValue());
			count++;
		}
		return target;
	}

	public static void clearOutputDirectories(String[] in) {

		File csv_directory = new File(in[0]);
		File outputdiretory = new File(in[1]);
		File costdiretory = new File(in[2]);

		try {
			FileUtils.cleanDirectory(csv_directory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			FileUtils.cleanDirectory(outputdiretory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			FileUtils.cleanDirectory(costdiretory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static int getRandomInt(Random random, int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

//	public static void printImages(ArrayList<ArrayList<Cluster>> clusterhistory, DataSet data,
//			DataSet_OriginalDataset data_originalDataset, String plotskript, String rpath) throws IOException {

	
//	public static void printImages(ArrayList<ArrayList<Cluster>> clusterhistory, DataSet data, String plotskript, String rpath) throws IOException {
	public static void printImages(ArrayList<ArrayList<Cluster>> clusterhistory, DataSet data) throws IOException {
		for (int i = 0; i < clusterhistory.size(); i++) {

			ArrayList<Cluster> currentclustering = clusterhistory.get(i);
//			CsvWriter csvwriter = new CsvWriter(currentclustering, data, data_originalDataset);
			CsvWriter csvwriter = new CsvWriter(currentclustering, data);
//			if (i % 2 != 0)
//				csvwriter.writeToCsv(
//						"clusters_"
//								+ i + "split.csv","clusters_"
//										+ i + "_ClusterID_split.csv");
//			else
//				csvwriter.writeToCsv(
//						"clusters_"
//								+ i + ".csv","clusters_"
//										+ i + "_ClusterID_.csv");
			
			if(i==clusterhistory.size()-1)
			csvwriter.writeToCsv(
			"Final Result.csv","Final Result_ClusterID.csv");

//			Runtime.getRuntime().exec(rpath + plotskript);

		}

	}

	public static void printClusterCosts(ArrayList<ArrayList<Cluster>> clusterhistory) {

		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < clusterhistory.size(); i++) {
			if (i == clusterhistory.size() - 2) { // only append final step
				buff.append("++++++++++++++++++++++ \n");
				buff.append("STEP" + i + ": \n");
				buff.append("++++++++++++++++++++++ \n");

				ArrayList<Cluster> clustering = clusterhistory.get(i);

				for (int j = 0; j < clustering.size(); j++) {
					

					Cluster c = clustering.get(j);
					buff.append("********************** \n");
					buff.append("Cluster" + j + ": 	"+clustering.get(j).getDataItems().size());
					buff.append("	********************** \n");

					ArrayList<NumericalAttribute> numericalAttributes = c.getNumericalAttributes();
					ArrayList<CategoricalAttribute> categoricalAttributes = c.getCategoricalAttributes();
					

					for (int k = 0; k < numericalAttributes.size(); k++) {

						NumericalAttribute a = numericalAttributes.get(k);
						buff.append(a.getName() + " specifc:" + a.isSpecific() + "	\n");

					}

					ArrayList<DataItem> clusterData = clustering.get(j).getDataItems();
					
					for (int k = 0; k < categoricalAttributes.size(); k++) {
						
						Node Cluster_Tree = Tree.cloneTree(categoricalAttributes.get(k).rootNode);
						
						Tree.setNodeProbabilites(Cluster_Tree, clusterData, k);

						Node Root = categoricalAttributes.get(k).rootNode;
						Node Back_Tree= categoricalAttributes.get(k).Back_Tree;
						CategoricalAttribute a = categoricalAttributes.get(k);
						buff.append(a.getDescription() + " specifc:" + a.isSpecific() + "	\n");

						ArrayList<Node> Specific_Node = new ArrayList<Node>();

						Tree.getDescendants(Root, Specific_Node);
						
						for (int z = 0; z < Specific_Node.size(); z++) {
							
							double Deviation=Tree.getProbabilityByDesc(Cluster_Tree,Specific_Node.get(z).getDescription())
									-Tree.getProbabilityByDesc(Back_Tree, Specific_Node.get(z).getDescription());
							
							buff.append(z +"-	"+Specific_Node.get(z).getDescription() + ", C_Prob.:  "
									+ Tree.getProbabilityByDesc(Cluster_Tree,Specific_Node.get(z).getDescription())+", B_Prob.:  "
									+ Tree.getProbabilityByDesc(Back_Tree, Specific_Node.get(z).getDescription())+", S_Prob:  " 
									+ Tree.getProbabilityByDesc(Root, Specific_Node.get(z).getDescription())+								
									", Dev:  "+Deviation+ "	\n");
						}
						buff.append("	-----------------------------------------  \n");
						buff.append("Specific Attributes:\n");

						for (int z = 0; z < Specific_Node.size(); z++) {
							if (Specific_Node.get(z).isSpecific())
								buff.append(Specific_Node.get(z).getDescription() + "	Prob.:     " 
							+ Tree.getProbabilityByDesc(Cluster_Tree, Specific_Node.get(z).getDescription())+ "	\n");
						}

					}
				}
			}
		}

		writeStringToFile(
				"C:/Users/saharb63cs/Desktop/Sahar/Data minig/Clustering mixed type/David_Documents/R-workspace/clustercosts/costs.txt",
				buff);

	}

	public static void writeStringToFile(String filename, StringBuffer buff) {

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(filename);
			fileWriter.append(buff);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
