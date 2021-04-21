import java.io.IOException;
import java.util.ArrayList;

import attributes.AttributeStructure;
import clustering.Cluster;
import clustering.Clusterer;
import data.DataSet;
import filehandler.XMLFileHandler;
import misc.CsvWriter;
import misc.Utils;

public class Main {
 

	
	public static void main(String[] args) throws IOException {
		
		int ITERATIONS = 100;
	
	
		XMLFileHandler handler = new XMLFileHandler();
		
		DataSet data = 	handler.read("big_data/Auto MPG/Car_mpg_withoutLabel_hierarchy12.txt");
		
//		DataSet data = handler.read("Synthetic/Noise/Noise1_0.txt");
		
		AttributeStructure attributes = data.getAttributeStructure();
	
		double min = Double.POSITIVE_INFINITY;
		
		ArrayList<Cluster> clustering = new ArrayList<Cluster>();
		ArrayList<ArrayList<Cluster>> clusterhistory = new ArrayList<ArrayList<Cluster>>();
		
		int count = 0;
		
		long starttime = System.nanoTime();
        double averageRuntime = 0.0;
				
		for(int i=0;i<ITERATIONS;i++){
		count++;
		System.out.println(count);
			
		Clusterer clusterer = new Clusterer(attributes,data,"point");
		clusterer.startClustering();
		averageRuntime += clusterer.runtime;
			if(clusterer.getFinalCosts()<min){
					min = clusterer.getFinalCosts();
					clustering = clusterer.getClustering();
					clusterhistory = clusterer.getClusterHistory();
				}
		}
		

        averageRuntime = averageRuntime / (double) ITERATIONS;
        System.out.println("averageRuntime: " + averageRuntime);

		long estimatedtime = System.nanoTime() - starttime;
		System.out.println("TIME:" + estimatedtime/1000000);

		
		for(int i=0;i<clustering.size();i++){
			
		System.out.println("CLUSTER "+i+" :");
		System.out.println(clustering.get(i).getDataItems().size());
			
		}
	
		Utils.printImages(clusterhistory, data);
		Utils.printClusterCosts(clusterhistory);

		System.out.println("SMALLEST_COSTS:"+ min);
		System.out.println("CLUSTERS: " +clustering.size());
		System.out.println("DONE");
		
	
	
  }
		

	
	
}
