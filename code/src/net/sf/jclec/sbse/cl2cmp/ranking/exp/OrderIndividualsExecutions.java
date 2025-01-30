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

public class OrderIndividualsExecutions {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String dirname = "C:/Users/Aurora/Documents/KDIS/revistas/2013_Ranking/experimentación_inf_sci/pruebas-mutadores/res/datapro/individuo";
		int nExecutions = 31;//Integer.parseInt(args[1]);

		String resFile = dirname + "-ranking-inds.xlsx";
		String format = "sfffffffff";
		File dir = new File(dirname);
		File [] executions = dir.listFiles();

		int size = dir.listFiles().length*nExecutions;

		ArrayList<Double> sortedICD = new ArrayList<Double>(size);
		ArrayList<Double> sortedERP = new ArrayList<Double>(size);
		ArrayList<Double> sortedGCR = new ArrayList<Double>(size);

		Dataset dataset, res;
		NumericalColumn col;

		double rank1, rank2, rank3;

		// Create a dataset for the mean results of each algorithm
		res = new ExcelDataset();
		res.addColumn(new NominalColumn("Algorithm"));	// The algorithm name
		res.addColumn(new NumericalColumn("Ranking"));

		// For each directory (each algorithm)
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
			col = (NumericalColumn) dataset.getColumn(7);
			for(int i=0; i<nExecutions; i++)
				sortedICD.add((Double)col.getElement(i));

			col = (NumericalColumn) dataset.getColumn(8);
			for(int i=0; i<nExecutions; i++)
				sortedERP.add((Double)col.getElement(i));

			col = (NumericalColumn) dataset.getColumn(9);
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
				rank1 = getRanking(sortedICD, ((Double)dataset.getColumn(7).getElement(i)).doubleValue(), true);
				rank2 = getRanking(sortedERP, ((Double)dataset.getColumn(8).getElement(i)).doubleValue(), false);
				rank3 = getRanking(sortedGCR, ((Double)dataset.getColumn(9).getElement(i)).doubleValue(), false);
				dataset.getColumn(10).addValue(rank1+rank2+rank3);
			}

			// Finally, set the total ranking of the algorithm
			List<Object> values = dataset.getColumn(10).getValues();
			double total = 0.0;
			for(Object o: values){
				total += ((Double)o).doubleValue();
			}

			// Copy this information in the second dataset
			res.getColumn(0).addValue(f.getName());
			res.getColumn(1).addValue(total);
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
