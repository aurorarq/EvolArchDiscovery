package net.sf.jclec.sbse.cl2cmp.ranking.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.IAlgorithmListener;
import net.sf.jclec.IConfigure;
import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.algorithm.PopulationAlgorithm;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.cl2cmp.Cl2CmpMutator;
import net.sf.jclec.sbse.cl2cmp.mut.AbstractCmpMutator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingAlgorithm;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingEvaluator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.metrics.CmpMetric;
import net.sf.jclec.util.IndividualStatistics;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.Column.NumericalColumn;
import es.uco.kdis.datapro.dataset.Source.ExcelDataset;

/**
 * Reporter for 'Classes to Components' (Cl2Cmp) problem.
 * It stores the convergence of each metric and fitness of the best individual.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (October 2013)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class RankingPartialBestReporter implements IAlgorithmListener, IConfigure {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** ID */
	private static final long serialVersionUID = 1629223267445006607L;

	/** Name of the report*/
	private String reportTitle;

	/** Report frequency */
	private int reportFrequency;

	/** Report file */
	private File reportFile;

	/** Report dataset */
	private Dataset dataset;

	/** The report already exits (for multiple executions) */
	private boolean isFirstExecution = false;

	/** Number of executions */
	private int nExecutions;

	/** Dataset format */
	private String format = "fffffff";

	/** Column names */
	private String [] colnames = {"Fitness", "#Components", "#Connectors", "ICD", "ERP", "GCR", "Time"};

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////	

	/**
	 * Empty Constructor
	 * */
	public RankingPartialBestReporter() {
		super();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public void configure(Configuration settings) {
		// Set report title (default "untitled")
		//this.reportTitle = settings.getString("report-title", "untitled");
		// Set report frequency (default 10 generations)
		this.reportFrequency = settings.getInt("report-frequency", 1);
		this.nExecutions = settings.getInt("number-of-executions", 1);
	}

	@Override
	public void algorithmStarted(AlgorithmEvent event) {

		// Create report title for this instance
		createName((PopulationAlgorithm)event.getAlgorithm());
		this.reportFile = new File(this.reportTitle);

		if(!this.reportFile.exists()){
			if(this.reportFile.getParentFile()!=null)
				this.reportFile.getParentFile().mkdirs();
			this.isFirstExecution=true;
		}
		else
			this.isFirstExecution=false;
		doIterationReport((PopulationAlgorithm) event.getAlgorithm(), true);
	}

	@Override
	public void iterationCompleted(AlgorithmEvent event) {
		doIterationReport((PopulationAlgorithm) event.getAlgorithm(), false);
	}

	@Override
	public void algorithmFinished(AlgorithmEvent event) {
		// Do last generation report
		doIterationReport((PopulationAlgorithm) event.getAlgorithm(), true);
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
		
		// Fitness comparator
		Comparator<IFitness> comparator = algorithm.getEvaluator().getComparator();
		// Population individuals
		List<IIndividual> inhabitants = algorithm.getInhabitants();
		// Actual generation
		int generation = algorithm.getGeneration();

		// Check if this is correct generation
		if (!force && generation%this.reportFrequency != 0) {
			return;
		}	

		createName(algorithm);
		
		// Open the correspondent dataset
		if(this.isFirstExecution){
			this.dataset = new ExcelDataset(this.reportTitle);
			for(int i=0; i<colnames.length; i++){
				this.dataset.addColumn(new NumericalColumn(colnames[i]));
			}
		}

		else{
			this.dataset = new ExcelDataset(this.reportTitle);
			try {
				((ExcelDataset)dataset).readDataset("nv", format);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Best individual
		IIndividual best = IndividualStatistics.bestIndividual(inhabitants, comparator);

		// Save the values in the dataset
		this.dataset.getColumn(0).addValue(((SimpleValueFitness)best.getFitness()).getValue());
		this.dataset.getColumn(1).addValue((double)((RankingIndividual)best).getNumberOfComponents());
		this.dataset.getColumn(2).addValue((double)((RankingIndividual)best).getNumberOfConnectors());

		ArrayList<CmpMetric> metrics = ((RankingEvaluator)((RankingAlgorithm)algorithm).getEvaluator()).getMetrics();
		this.dataset.getColumn(3).addValue(metrics.get(0).getFromIndividual((RankingIndividual)best));
		this.dataset.getColumn(4).addValue(metrics.get(1).getFromIndividual((RankingIndividual)best));
		this.dataset.getColumn(5).addValue(metrics.get(2).getFromIndividual((RankingIndividual)best));
		
		this.dataset.getColumn(6).addValue((double)((RankingAlgorithm)algorithm).getTime());

		// Save datasets
		try {

			// Get means
			int size = this.dataset.getColumn(0).getSize();
			double mean,sd;
			if(nExecutions>1 && size==nExecutions){
				for(int i=0; i<colnames.length; i++){	
					mean = ((NumericalColumn)this.dataset.getColumn(i)).mean();
					sd = ((NumericalColumn)this.dataset.getColumn(i)).standardDeviation();
					this.dataset.getColumn(i).addValue(mean);
					if(Double.isInfinite(sd))
						this.dataset.getColumn(i).addValue(0.0);
					else
						this.dataset.getColumn(i).addValue(sd);
				}
			}
			
			// Save dataset
			((ExcelDataset)this.dataset).writeDataset(this.reportTitle);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void createName(PopulationAlgorithm algorithm){
		// Create report title for this instance
		String datasetname = ((RankingEvaluator)((RankingAlgorithm)algorithm).getEvaluator()).getDatasetFileName();
		if(datasetname.contains("/"))
			datasetname=datasetname.substring(datasetname.lastIndexOf("/")+1);
		this.reportTitle = datasetname.substring(0, datasetname.lastIndexOf("."));

		this.reportTitle += "-s" + ((RankingAlgorithm)algorithm).getSelectorType();
		this.reportTitle += "-r" + ((RankingAlgorithm)algorithm).getReplacementType();
		this.reportTitle += "-i" + ((PopulationAlgorithm)algorithm).getPopulationSize();
		this.reportTitle += "-g" + ((PopulationAlgorithm)algorithm).getGeneration();

		Cl2CmpMutator base = (Cl2CmpMutator)((RankingAlgorithm)algorithm).getMutator();
		List<AbstractCmpMutator> mut = base.getMutators();
		String probs = "-p";
		for(AbstractCmpMutator m: mut){
			probs += "-" +(int)(m.getWeight()*100);
		}
		this.reportTitle += probs + ".xlsx";
	}
}