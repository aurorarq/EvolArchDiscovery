package net.sf.jclec.sbse.cl2cmp.ranking.exp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.Column.NominalColumn;
import es.uco.kdis.datapro.dataset.Column.NumericalColumn;
import es.uco.kdis.datapro.dataset.Source.ExcelDataset;
import es.uco.kdis.datapro.exception.IllegalFormatSpecificationException;
import es.uco.kdis.datapro.exception.NotAddedValueException;

public class OrderIndividualsEvaluations {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		String dirname = "C:/Users/Aurora/Documents/KDIS/revistas/2013_Ranking/experimentación_inf_sci/pruebas-evaluaciones/res/borg/";
		int nExecutions = 30;//Integer.parseInt(args[1]);

		String resFile = dirname + "-ranking-inds.xlsx";
		String format = "fffffff";
		File dir = new File(dirname);
		File [] subdir = dir.listFiles();

		File [] executions;
		System.out.println(dir);
		int size = dir.listFiles().length*nExecutions;

		ArrayList<Double> sortedICD = new ArrayList<Double>(size);
		ArrayList<Double> sortedERP = new ArrayList<Double>(size);
		ArrayList<Double> sortedGCR = new ArrayList<Double>(size);

		Dataset dataset, res;
		NumericalColumn col;

		double rank1, rank2, rank3;

		// Create a dataset for the mean results of each algorithm
		res = new ExcelDataset();
		res.addColumn(new NominalColumn("Population Size"));	// The algorithm name
		
		File filenames [] = dir.listFiles()[0].listFiles();
		for(File f: filenames){
			String name = f.getName().substring(f.getName().indexOf("-i")+2, f.getName().indexOf("-e"));
			res.getColumn(0).addValue(name);
		}
		
		// For each directory (each algorithm)
		int n=1;
		for(File d: subdir){
			
			System.out.println(d.getName());
			executions = d.listFiles();
			res.addColumn(new NumericalColumn("Ranking"+d.getName()));
			
			sortedICD.clear();
			sortedERP.clear();
			sortedGCR.clear();
			
			for(File f: executions){
				System.out.println(f.getName());
				
				
				// Open dataset
				dataset = new ExcelDataset(f.getAbsolutePath());
				dataset.setMissingValue("#NUM!");
				try {
					((ExcelDataset)dataset).readDataset("nv", format);
				} catch (IndexOutOfBoundsException | IOException
						| NotAddedValueException
						| IllegalFormatSpecificationException e) {
					e.printStackTrace();
					System.exit(-1);
				}
				catch(InputMismatchException e){
					e.printStackTrace();
				}

				// Copy all the values
				col = (NumericalColumn) dataset.getColumn(3);
				for(int i=0; i<nExecutions; i++)
					sortedICD.add((Double)col.getElement(i));

				col = (NumericalColumn) dataset.getColumn(4);
				for(int i=0; i<nExecutions; i++)
					sortedERP.add((Double)col.getElement(i));

				col = (NumericalColumn) dataset.getColumn(5);
				for(int i=0; i<nExecutions; i++)
					sortedGCR.add((Double)col.getElement(i));
			}

			// Sort values for each metric
			Collections.sort(sortedICD);
			Collections.reverse(sortedICD);	// Reverse order
			Collections.sort(sortedERP);
			Collections.sort(sortedGCR);

			// Set the new ranking value for each individual in each algorithm execution

			// For each directory (each algorithm)
			for(File f: executions){
				// Open dataset
				dataset = new ExcelDataset(f.getAbsolutePath());
				dataset.setMissingValue("#NUM!");
				try {
					((ExcelDataset)dataset).readDataset("nv", format);
				} catch (IndexOutOfBoundsException | IOException
						| NotAddedValueException
						| IllegalFormatSpecificationException e) {
					e.printStackTrace();
					System.exit(-1);
				}
				catch(InputMismatchException e){
					e.printStackTrace();
				}

				// Add column
				dataset.addColumn(new NumericalColumn("UpdatedRanking"));
				for(int i=0; i<nExecutions; i++){

					// Get the new ranking value
					rank1 = getRanking(sortedICD, ((Double)dataset.getColumn(3).getElement(i)).doubleValue(), true);
					rank2 = getRanking(sortedERP, ((Double)dataset.getColumn(4).getElement(i)).doubleValue(), false);
					rank3 = getRanking(sortedGCR, ((Double)dataset.getColumn(5).getElement(i)).doubleValue(), false);
					dataset.getColumn(7).addValue(rank1+rank2+rank3);
				}

				// Finally, set the total ranking of the algorithm
				List<Object> values = dataset.getColumn(7).getValues();
				double total = 0.0;
				for(Object o: values){
					total += ((Double)o).doubleValue();
				}

				// Copy this information in the second dataset
				res.getColumn(n).addValue(total);
			}
			n++;
		}
		// Save res2 dataset
		try {
			((ExcelDataset)res).writeDataset(resFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int getRanking(ArrayList<Double> list, double value, boolean reverted){
		// Set rankings
		int ranking = 1;
		double rankingValue;
		boolean finish = false;
		int size = list.size();
		for(int i=0; !finish && i<size; i++){
			rankingValue = (double)list.get(i);
			if(reverted){
				if(value<rankingValue)
					ranking++;
				else
					finish=true;
			}
			else{
				if(value>rankingValue)
					ranking++;
				else
					finish=true;
			}
		}
		return ranking;
	}
}
