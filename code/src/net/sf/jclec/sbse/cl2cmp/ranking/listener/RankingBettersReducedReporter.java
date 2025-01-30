package net.sf.jclec.sbse.cl2cmp.ranking.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Column.ColumnAbstraction;
import es.uco.kdis.datapro.dataset.Column.NominalColumn;
import es.uco.kdis.datapro.dataset.Column.NumericalColumn;
import es.uco.kdis.datapro.dataset.Source.ExcelDataset;
import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.IIndividual;
import net.sf.jclec.algorithm.PopulationAlgorithm;
import net.sf.jclec.sbse.cl2cmp.Cl2CmpMutator;
import net.sf.jclec.sbse.cl2cmp.mut.AbstractCmpMutator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingAlgorithm;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingEvaluator;
import net.sf.jclec.sbse.cl2cmp.ranking.metrics.CmpMetric;

/**
 * Reporter for 'Classes to Components' (Cl2Cmp) problem.
 * It presents reduced results (only fitness metrics) of
 * betters individuals.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (September 2013)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 *  * */
public class RankingBettersReducedReporter extends RankingBettersReporter  {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	private static final long serialVersionUID = 8588270685575163848L;

	/** Attributes to be saved in the dataset */
	private String [] tags = {"#Betters", "Fitness", "#Components", "#Connectors", "Time(ms)"};

	private int actualExecution;
	
	private int numberOfExect;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////	

	/**
	 * Empty Constructor
	 * */
	public RankingBettersReducedReporter() {
		super();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public void configure(Configuration settings) {
		this.numberOfExect = settings.getInt("number-of-executions",1);
	}

	@Override
	public void algorithmStarted(AlgorithmEvent event) {
		// Create report title for this instance
		String datasetname = ((RankingEvaluator)((RankingAlgorithm)event.getAlgorithm()).getEvaluator()).getDatasetFileName();
		if(datasetname.contains("/"))
			datasetname=datasetname.substring(datasetname.lastIndexOf("/")+1);
		datasetname = datasetname.substring(0, datasetname.lastIndexOf("."));
		
		this.reportTitle = datasetname;
		this.reportTitle += "-s" + ((RankingAlgorithm)event.getAlgorithm()).getSelectorType();
		this.reportTitle += "-r" + ((RankingAlgorithm)event.getAlgorithm()).getReplacementType();
		this.reportTitle += "-i" + ((PopulationAlgorithm)event.getAlgorithm()).getPopulationSize();
		this.reportTitle += "-g" + ((PopulationAlgorithm)event.getAlgorithm()).getMaxOfGenerations();

		Cl2CmpMutator base = (Cl2CmpMutator)((RankingAlgorithm)event.getAlgorithm()).getMutator();
		List<AbstractCmpMutator> mut = base.getMutators();
		String probs = "-p";
		for(AbstractCmpMutator m: mut){
			probs += "-" +(int)(m.getWeight()*100);
		}
		this.reportTitle += probs;
		this.reportTitle += "-betters-final";
		
		this.reportFile = new File(this.reportTitle+".xlsx");
		if(!this.reportFile.exists()){
			if(this.reportFile.getParentFile()!=null)
				this.reportFile.getParentFile().mkdirs();
			this.isFirstExecution=true;
		}
		else
			this.isFirstExecution=false;

		// Metric names
		ArrayList<CmpMetric> metrics = ((RankingEvaluator)((RankingAlgorithm)((PopulationAlgorithm)event.getAlgorithm())).getEvaluator()).getMetrics();
		this.metricsTags = new String[metrics.size()];
		String name;
		for(int i=0; i<this.metricsTags.length; i++){
			name = metrics.get(i).getClass().getName();
			name = name.substring(name.lastIndexOf(".")+1);
			this.metricsTags[i] = name;
		}

		// First execution
		if(this.isFirstExecution){

			// Create dataset if it is the first execution
			this.resultsDataset = new ExcelDataset();
			this.resultsDataset.setName(this.reportTitle);

			// Add a column with the name of the metrics to be saved
			this.resultsDataset.addColumn(new NominalColumn("Ejecucion"));
			for(int i=0; i<tags.length; i++){
				this.resultsDataset.addColumn(new NumericalColumn(tags[i]));
			}

			// Add a column with the name of the fitness metrics to be saved
			for(int i=0; i<this.metricsTags.length; i++){
				this.resultsDataset.addColumn(new NumericalColumn(metricsTags[i]));
			}

			this.actualExecution = 1;
		}

		// Other executions, open the dataset and add new attributes
		else{

			this.resultsDataset = new ExcelDataset(this.reportTitle+".xlsx");
			try {
				String format = "s";	// The first column is nominal
				// Concatenate 'f' for numerical columns
				for(int i=0; i<tags.length; i++)
					format = format.concat("f");

				for(int i=0; i<this.metricsTags.length; i++)
					format = format.concat("f");

				this.resultsDataset.setNullValue("?");
				((ExcelDataset)this.resultsDataset).readDataset("nv", format);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.actualExecution = this.resultsDataset.getColumn(0).getSize() +1;
	}

	@Override
	public void iterationCompleted(AlgorithmEvent event) {
		//do nothing
	}

	@Override
	public void algorithmFinished(AlgorithmEvent event) {
		// Do last generation report
		doIterationReport((PopulationAlgorithm) event.getAlgorithm(), true);

		// Compute mean in column (for last execution)
		int size = this.resultsDataset.getColumn(0).getSize();
		if(numberOfExect>1 && size==numberOfExect){
			List<ColumnAbstraction> cols = this.resultsDataset.getColumns();
			int n = cols.size();
			NumericalColumn col;
			double avg, sd;
			cols.get(0).addValue("MEDIA");
			cols.get(0).addValue("SD");
			for(int i=1; i<n; i++){
				col = (NumericalColumn) cols.get(i);
				avg = col.mean();
				sd = col.standardDeviation();
				col.addValue(avg);
				col.addValue(sd);
			}
		}
		
		// Save dataset
		try {
			this.resultsDataset.setNumberOfDecimals(4);
			((ExcelDataset)this.resultsDataset).writeDataset(this.reportTitle+".xlsx");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void algorithmTerminated(AlgorithmEvent oEvent) {
		// Do nothing
	}

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------- Protected methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Do iteration report
	 * @param algorithm The algorithm
	 * @param force Force report
	 * */
	protected void doIterationReport(PopulationAlgorithm algorithm, boolean force) {
		
		// Get betters
		List<IIndividual> betters = selectIndividuals(algorithm);
		
		// Save the values in the dataset
		String aux = "Ejecucion " + this.actualExecution;
		this.resultsDataset.getColumn(0).addValue(aux);
		
		// Betters size
		this.resultsDataset.getColumn(1).addValue((double)betters.size());

		// Betters average fitness
		this.resultsDataset.getColumn(2).addValue(averageFitness(betters));
		
		// Average components and connectors
		double avg[] = averageCompConn(betters);
		this.resultsDataset.getColumn(3).addValue(avg[0]);
		this.resultsDataset.getColumn(4).addValue(avg[1]);

		this.resultsDataset.getColumn(5).addValue((double)((RankingAlgorithm)algorithm).getTime());
		
		// Value of each fitness metrics
		int col = 6;
		ArrayList<CmpMetric> metrics = ((RankingEvaluator)((RankingAlgorithm)algorithm).getEvaluator()).getMetrics();
		for(int i=0; i<metrics.size(); i++){
			this.resultsDataset.getColumn(col).addValue(metricAverage(metrics.get(i), betters));
			col++;
		}
	}
}
