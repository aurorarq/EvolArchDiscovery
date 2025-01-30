package net.sf.jclec.sbse.cl2cmp.ranking.exp;

import java.io.IOException;
import java.util.List;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.Column.ColumnAbstraction;
import es.uco.kdis.datapro.dataset.Column.NominalColumn;
import es.uco.kdis.datapro.dataset.Column.NumericalColumn;
import es.uco.kdis.datapro.dataset.Source.ExcelDataset;

/**
 * This class takes result reporters and
 * creates a new one as a summary. For
 * experimentation purposes.
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class CompactResults {

	private static Dataset res1;
	private static Dataset res2;
	private static String format1 = "sffffffff";			//Best-reporter
	private static String format2 = "sfffffffffffffff";	// Population-reporter

	private static String dir = "C:/Users/Aurora/Documents/KDIS/revistas/2013_IEEE_TEC/experimentación/mutadores/res/borg";
	private static String pref = "borg-s3-r3-i100-g100-", suf1="-best-final.xlsx";
	private static String suf2="-population-final.xlsx";

	private static Dataset data1, data2;

	public static void main(String[] args) {

		String name = "";

		createDatasets();

		for(int i=10; i<=60; i+=10){
			for(int j=10; j<=60; j+=10){
				for(int k=10; k<=60; k+=10){
					for(int l=10; l<=60; l++){
						int m = 100-i-j-k-l;
						if(m>=10 && ((double)m)%10==0){
							name = "p-" + i + "-" + j + "-" + k + "-" + l + "-" + m;
							//if(!name.equalsIgnoreCase("p-30-10-30-20-10")){
									System.out.println(name);
									readDatasets(name);
									copyResults(name);
							//}
						}
					}
				}
			}
		}

		// Save dataset
		saveDatasets();
		
	}

	public static void createDatasets(){
		// Create compact results
		res1 = new ExcelDataset();
		res1.addColumn(new NominalColumn());
		for(int i=1; i<format1.length(); i++)
			res1.addColumn(new NumericalColumn());

		res2 = new ExcelDataset();
		res2.addColumn(new NominalColumn());
		for(int i=1; i<format2.length(); i++)
			res2.addColumn(new NumericalColumn());
	}

	public static void readDatasets(String name){
		// Read results
		data1 = new ExcelDataset(dir+"/"+pref+name+suf1);
		data2 = new ExcelDataset(dir+"/"+pref+name+suf2);
		try {
			((ExcelDataset)data1).readDataset("nv", format1);
			((ExcelDataset)data2).readDataset("nv", format2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copyResults(String name){
		// Copy average values
		List<ColumnAbstraction> cols1 = data1.getColumns();
		List<ColumnAbstraction> cols2 = data2.getColumns();
		int size = cols1.get(0).getSize();

		res1.getColumn(0).addValue(name);
		for(int i=1; i<cols1.size(); i++){
			res1.getColumn(i).addValue(cols1.get(i).getElement(size-2));
		}

		res2.getColumn(0).addValue(name);
		for(int i=1; i<cols2.size(); i++){
			res2.getColumn(i).addValue(cols2.get(i).getElement(size-2));
		}

		// Set column names
		res1.getColumn(0).setName("MUT WEIGHTS");
		for(int i=1; i<cols1.size(); i++){
			res1.getColumn(i).setName(cols1.get(i).getName());
		}

		res2.getColumn(0).setName("MUT WEIGHTS");
		for(int i=1; i<cols2.size(); i++){
			res2.getColumn(i).setName(cols2.get(i).getName());
		}
	}
	
	public static void saveDatasets(){
		try {
			((ExcelDataset)res1).writeDataset(dir+"/"+pref+suf1.substring(1));
			((ExcelDataset)res2).writeDataset(dir+"/"+pref+suf2.substring(1));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
