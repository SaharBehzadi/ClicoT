package misc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import attributes.CategoricalAttribute;
import attributes.NumericalAttribute;
import clustering.Cluster;
import data.DataItem;
import data.DataSet;

public class CsvWriter {

	private String fileheader;
	private ArrayList<Cluster> clustering;
	private DataSet data;

//	public CsvWriter(ArrayList<Cluster> _clustering, DataSet _data, DataSet_OriginalDataset data_originalDataset) {

	public CsvWriter(ArrayList<Cluster> _clustering, DataSet _data) {

		this.clustering = _clustering;
		this.data = _data;
//		this.data_originalDataset=data_originalDataset;

		ArrayList<NumericalAttribute> numAts = data.getAttributeStructure().getNumericalAttributes();
		ArrayList<CategoricalAttribute> catAts = data.getAttributeStructure().getCategoricalAttributes();

		StringBuffer buff = new StringBuffer();
		StringBuffer buff_ClusterID = new StringBuffer();

		for (int i = 0; i < numAts.size(); i++) {

			buff.append(numAts.get(i).getName() + ",");

		}

		for (int i = 0; i < catAts.size(); i++) {

			buff.append(catAts.get(i).getDescription() + ",");

		}

		buff.append("clusterid \n");
		fileheader = buff.toString();
	}

	public void writeToCsv(String filename, String ClusterID_filename) {

		FileWriter fileWriter = null;
		FileWriter fileWriter_ClusterID = null;

		try {
			fileWriter = new FileWriter(filename);
			fileWriter_ClusterID = new FileWriter(ClusterID_filename);
			fileWriter.append(fileheader);

			for (int i = 0; i < this.clustering.size(); i++) {
//				int ClusterID=0;
//				for(int j=0;j<this.data_originalDataset.size();j++)
//					if(this.data_originalDataset.get(j).id==clustering.get(i).getDataItem(0).id){
//						ClusterID= (int) this.data_originalDataset.get(j).numericalvalues[2];
//						break;
//					}
				
				for (int j = 0; j < clustering.get(i).getDataItems().size(); j++)
//					clustering.get(i).getDataItem(j).ClusterID=ClusterID;
					clustering.get(i).getDataItem(j).ClusterID=i+1;
			}

			for (int i = 0; i < this.data.size(); i++) {

				StringBuffer buff = new StringBuffer();
				StringBuffer buff_clusterID = new StringBuffer();
				DataItem d = data.get(i);

				for (int u = 0; u < d.numericalvalues.length; u++)
					buff.append(d.numericalvalues[u] + ",");
				for (int v = 0; v < d.categoricalvalues.length; v++)
					buff.append(d.categoricalvalues[v] + ",");
						
				buff.append(d.ClusterID + " \n");
				buff_clusterID.append(d.ClusterID + " \n");
				fileWriter.append(buff.toString());
				fileWriter_ClusterID.append(buff_clusterID.toString());

			} // for i

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
				fileWriter_ClusterID.flush();
				fileWriter_ClusterID.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
