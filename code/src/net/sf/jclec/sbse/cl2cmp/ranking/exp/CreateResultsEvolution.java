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

public class CreateResultsEvolution {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dirname = "C:/Users/Aurora/Documents/KDIS/revistas/2013_Ranking/experimentación_inf_sci/pruebas-evaluaciones/res/nekohtml/";
		int meanRow = 30;//Integer.parseInt(args[1]);
		int column = 5;
		String datasetName = dirname.substring(dirname.indexOf("/res/")+5,dirname.length()-1);
		System.out.println(datasetName);
		String resFile = dirname + datasetName + "-gcr" + ".xlsx";
		String format = "fffffff";
		File dir = new File(dirname);
		File [] subdir = dir.listFiles();

		File [] executions;
		Dataset dataset, res;

		// Create a dataset for the mean results of each algorithm
		res = new ExcelDataset();
		res.addColumn(new NominalColumn("Population Size"));	// The algorithm name
		
		File filenames [] = dir.listFiles()[0].listFiles();
		for(File f: filenames){
			String name = f.getName().substring(f.getName().indexOf("-i")+2, f.getName().indexOf("-e"));
			res.getColumn(0).addValue(name);
		}

		File columns [] = dir.listFiles();
		for(File c: columns){
			res.addColumn(new NumericalColumn(c.getName()));
		}
		// For each directory (each algorithm)
		int n=1;

		for(File d: subdir){
			System.out.println(d.getName());
			executions = d.listFiles();
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

				// Copy this information in the second dataset
				Object value = dataset.getColumn(column).getElement(meanRow);
				res.getColumn(n).addValue(value);
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
}
