package net.sf.jclec.sbse.cl2cmp.ranking.exp;

import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.Column.NominalColumn;
import es.uco.kdis.datapro.dataset.Column.NumericalColumn;
import es.uco.kdis.datapro.dataset.Source.ExcelDataset;
import es.uco.kdis.datapro.exception.IllegalFormatSpecificationException;
import es.uco.kdis.datapro.exception.NotAddedValueException;

public class CreateResultsDataset {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dirname = "C:/Users/Aurora/Documents/KDIS/revistas/2013_Ranking/experimentación_inf_sci/pruebas-mutadores/res/";
		String datasetName = "datapro";
		String resFile1 = dirname + "/" + datasetName + "/" + datasetName + "-best-individual.xlsx";
		String resFile2 = dirname + "/" + datasetName + "/" + datasetName + "-population.xlsx";
		
		String format = "sfffffffff"; // for individuals
		String format2 = "sfffffffffffffff"; // for population

		File dir = new File(dirname + "/" + datasetName + "/individuo");
		File [] executions = dir.listFiles();
		File dir2 = new File(dirname + "/" + datasetName + "/población");
		File [] executions2 = dir2.listFiles();
		int row = 30;	// average value of 30 executions
		Dataset dataset, res, res2;
		int j;
		
		// FIRST RESULTS: BEST INDIVIDUAL

		// Create a dataset for the mean results of each algorithm
		res = new ExcelDataset();
		res.addColumn(new NominalColumn("Algorithm"));	// The algorithm name
		res.addColumn(new NumericalColumn("#Components"));
		res.addColumn(new NumericalColumn("#Conectors"));
		res.addColumn(new NumericalColumn("Invalid"));
		res.addColumn(new NumericalColumn("ICD"));
		res.addColumn(new NumericalColumn("ERP"));
		res.addColumn(new NumericalColumn("GCR"));

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
			res.getColumn(0).addValue(f.getName().substring(f.getName().indexOf("p-"),f.getName().indexOf("-best")));
			res.getColumn(1).addValue(dataset.getColumn(2).getElement(row));
			res.getColumn(2).addValue(dataset.getColumn(3).getElement(row));
			res.getColumn(3).addValue(dataset.getColumn(4).getElement(row));
			res.getColumn(4).addValue(dataset.getColumn(7).getElement(row));
			res.getColumn(5).addValue(dataset.getColumn(8).getElement(row));
			res.getColumn(6).addValue(dataset.getColumn(9).getElement(row));
		}

		// Save res dataset
		try {
			((ExcelDataset)res).writeDataset(resFile1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// SECOND RESULTS: POPULATION
		res2 = new ExcelDataset();
		res2.addColumn(new NominalColumn("Algorithm"));	// The algorithm name
		res2.addColumn(new NumericalColumn("#Components"));
		res2.addColumn(new NumericalColumn("#Conectors"));
		res2.addColumn(new NumericalColumn("#Invalid"));
		res2.addColumn(new NumericalColumn("ICD"));
		res2.addColumn(new NumericalColumn("ERP"));
		res2.addColumn(new NumericalColumn("GCR"));

		res2.addColumn(new NumericalColumn("2 Comp"));
		res2.addColumn(new NumericalColumn("3 Comp"));
		res2.addColumn(new NumericalColumn("4 Comp"));
		res2.addColumn(new NumericalColumn("5 Comp"));
		res2.addColumn(new NumericalColumn("6 Comp"));
		res2.addColumn(new NumericalColumn("7 Comp"));
		res2.addColumn(new NumericalColumn("8 Comp"));

		// For each directory (each algorithm)
		for(File f: executions2){
			System.out.println(f.getName());
			// Open dataset
			dataset = new ExcelDataset(f.getAbsolutePath());
			try {
				((ExcelDataset)dataset).readDataset("nv", format2);
			} catch (IndexOutOfBoundsException | IOException
					| NotAddedValueException
					| IllegalFormatSpecificationException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			catch(InputMismatchException e){
				e.printStackTrace();
			}
			res2.getColumn(0).addValue(f.getName().substring(f.getName().indexOf("p-"),f.getName().indexOf("-population")));
			j=1;
			for(int i=3; i<=15; i++){
				res2.getColumn(j).addValue(dataset.getColumn(i).getElement(row));
				j++;
			}
		}

		// Save res dataset
		try {
			((ExcelDataset)res2).writeDataset(resFile2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
