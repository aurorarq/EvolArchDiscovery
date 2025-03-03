package net.sf.jclec.sbse.cl2cmp.ranking.listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.Column.NominalColumn;
import es.uco.kdis.datapro.dataset.Column.NumericalColumn;
import es.uco.kdis.datapro.dataset.Source.ExcelDataset;
import es.uco.kdis.datapro.datatypes.NullValue;

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

/**
 * Reporter for 'Classes to Components' (Cl2Cmp) problem.
 * It presents extended results for the population.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (July 2013)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class RankingPopulationReporter implements IAlgorithmListener, IConfigure  {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	private static final long serialVersionUID = 8588270685575163848L;

	/** Name of the report*/
	protected String reportTitle;

	/** Report frequency */
	private int reportFrequency;

	/** Report file */
	protected File reportFile;

	/** Report dataset */
	protected Dataset resultsDataset;

	/** The report already exits (for multiple executions) */
	protected boolean isFirstExecution = false;

	/** Attributes to be saved in the dataset (general information) */
	private String [] tags = {"Best Fitness", "Worst Fitness", "Median Fitness", "Avg Fitness", 
			"Var Fitness", "Avg #Components", "Avg #Connectors", "#New Offsprings", "#Invalids"};

	/** Attributes to be saved in the dataset (metrics used in the algorithm) */
	protected String [] metricsTags;

	/** Actual column */
	protected int actualColumn;

	/** Dataset for size distribution */
	private Dataset sizeDataset;

	/** Minimum number of components */
	protected int min;

	/** Maximum number of components */
	protected int max;

	/** Actual column in SizeDataset */
	private int actualSizeColumn;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////	

	/**
	 * Empty Constructor
	 * */
	public RankingPopulationReporter() {
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
		this.reportFrequency = settings.getInt("report-frequency", 10); 
	}

	@Override
	public void algorithmStarted(AlgorithmEvent event) {
		// Create report title for this instance
		
		String datasetname = ((RankingEvaluator)((RankingAlgorithm)event.getAlgorithm()).getEvaluator()).getDatasetFileName();
		if(datasetname.contains("/"))
			datasetname=datasetname.substring(datasetname.lastIndexOf("/")+1);
		this.reportTitle = datasetname.substring(0, datasetname.lastIndexOf("."));
		
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
		
		this.reportFile = new File(this.reportTitle+"-population"+".xlsx");
		if(!this.reportFile.exists()){
			if(this.reportFile.getParentFile()!=null)
				this.reportFile.getParentFile().mkdirs();
			this.isFirstExecution=true;
		}
		else
			this.isFirstExecution=false;

		// Create columns (one per each generation report, plus one for the generation 0)
		int numOfCols = ((PopulationAlgorithm)event.getAlgorithm()).getMaxOfGenerations()/this.reportFrequency + 1;

		this.actualColumn = 1;
		this.actualSizeColumn = 1;

		this.min = ((RankingAlgorithm)((PopulationAlgorithm)event.getAlgorithm())).getMinNumberOfComponents();
		this.max = ((RankingAlgorithm)((PopulationAlgorithm)event.getAlgorithm())).getMaxNumberOfComponents();


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

			// Create datasets if it is the first execution
			this.resultsDataset = new ExcelDataset();
			this.resultsDataset.setName(this.reportTitle);

			// Add a column with the name of the general metrics to be saved
			this.resultsDataset.addColumn(new NominalColumn("Metrics"));
			for(int i=0; i<this.tags.length; i++)
				this.resultsDataset.getColumn(0).addValue(this.tags[i]);

			// Get the name of the fitness metrics used
			// Add a column with the name of the fitness metrics to be saved
			for(int i=0; i<this.metricsTags.length; i++){
				this.resultsDataset.getColumn(0).addValue(this.metricsTags[i]);
			}

			// Add numerical columns (one for each generation that will be reported)
			for(int i=0; i<numOfCols; i++)
				this.resultsDataset.addColumn(new NumericalColumn("Gen-"+(i*this.reportFrequency)), i+1);

			this.sizeDataset = new ExcelDataset();
			this.sizeDataset.setName(this.reportTitle+"-components-freq");

			this.sizeDataset.addColumn(new NumericalColumn("#Components"));
			for(int i=this.min; i<=this.max; i++)
				this.sizeDataset.getColumn(0).addValue((double)i);

			// Add numerical columns
			for(int i=1, j=0; i<=numOfCols; j=i, i++){
				this.sizeDataset.addColumn(new NumericalColumn("BSET Gen-"+((i-1)*this.reportFrequency)), i+j);
				this.sizeDataset.addColumn(new NumericalColumn("CSET Gen-"+((i-1)*this.reportFrequency)), i+j+1);
			}
		}

		// Other executions, open the dataset and add new attributes
		else{
			this.resultsDataset = new ExcelDataset(this.reportTitle+"-population.xlsx");
			try {
				String format = "s";	// The first column is nominal
				// Concatenate 'f' for numerical columns
				for(int i=0; i<numOfCols; i++)
					format = format.concat("f");
				this.resultsDataset.setNullValue("?");
				((ExcelDataset)this.resultsDataset).readDataset("nv", format);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Append new values in the metric's column for the new experiment
			for(int i=0; i<this.tags.length; i++)
				this.resultsDataset.getColumn(0).addValue(this.tags[i]);

			for(int i=0; i<this.metricsTags.length; i++)
				this.resultsDataset.getColumn(0).addValue(this.metricsTags[i]);

			this.sizeDataset = new ExcelDataset(this.reportTitle+"-components-freq.xlsx");

			try {
				String format = "f";	// The first column is nominal
				// Concatenate 'f' for numerical columns
				for(int i=0; i<numOfCols*2; i++)
					format = format.concat("f");
				this.sizeDataset.setNullValue("?");
				((ExcelDataset)this.sizeDataset).readDataset("nv", format);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Append new values in the metric's column for the new experiment
			for(int i=this.min; i<=this.max; i++)
				this.sizeDataset.getColumn(0).addValue((double)i);
		}

		// Do an iteration report
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

		// Save dataset
		try {
			((ExcelDataset)this.resultsDataset).writeDataset(this.reportTitle+"-population.xlsx");
			((ExcelDataset)this.sizeDataset).writeDataset(this.reportTitle+"-components-freq.xlsx");
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

		// Do population report

		// Statistics: only with valids individuals
		List<IIndividual> validInds = new ArrayList<IIndividual>();
		for(IIndividual ind: inhabitants)
			if(!((RankingIndividual)ind).isInvalid())
				validInds.add(ind);
		IIndividual best, worst, median;
		double bestFitness, worstFitness, medianFitness;
		double [] avgvar;

		if(validInds.size()>0){
			best = IndividualStatistics.bestIndividual(validInds, comparator);
			worst = IndividualStatistics.worstIndividual(validInds, comparator);
			median = IndividualStatistics.medianIndividual(validInds, comparator);

			bestFitness = ((SimpleValueFitness)best.getFitness()).getValue();
			worstFitness = ((SimpleValueFitness)worst.getFitness()).getValue();
			medianFitness = ((SimpleValueFitness)median.getFitness()).getValue();

			// Average fitness and fitness variance
			avgvar = IndividualStatistics.averageFitnessAndFitnessVariance(validInds);
		}
		else{
			// Set invalid individual in all cases
			best = worst = median = inhabitants.get(0);
			bestFitness = worstFitness = medianFitness = -1.0;
			avgvar = new double[2];
			avgvar[0] = avgvar[1] = -1.0;
		}

		// Save the values in the dataset
		this.resultsDataset.getColumn(this.actualColumn).addValue(bestFitness);
		this.resultsDataset.getColumn(this.actualColumn).addValue(worstFitness);
		this.resultsDataset.getColumn(this.actualColumn).addValue(medianFitness);
		this.resultsDataset.getColumn(this.actualColumn).addValue(avgvar[0]);
		this.resultsDataset.getColumn(this.actualColumn).addValue(avgvar[1]);


		// Verbose: average metrics in population (only with valid individuals)
		double aux = averageComponents(validInds);
		this.resultsDataset.getColumn(this.actualColumn).addValue(aux);

		aux = averageConnectors(validInds);
		this.resultsDataset.getColumn(this.actualColumn).addValue(aux);

		this.resultsDataset.getColumn(this.actualColumn).addValue((double)((RankingAlgorithm)algorithm).getNumberOfOffsprings());
		this.resultsDataset.getColumn(this.actualColumn).addValue((double)((RankingAlgorithm)algorithm).getNumberOfInvalids());

		// Average value of each fitness metrics
		ArrayList<CmpMetric> metrics = ((RankingEvaluator)((RankingAlgorithm)algorithm).getEvaluator()).getMetrics();
		CmpMetric m;
		double [] values = new double[validInds.size()];

		for(int i=0; i<this.metricsTags.length; i++){
			try {
				m = metrics.get(i).getClass().newInstance();
				for(int j=0; j<validInds.size(); j++){
					values[j] = m.getFromIndividual((RankingIndividual)validInds.get(j));
				}
			} catch (Exception e) {
				System.err.println("Illegal metric instantiation");
				e.printStackTrace();
				break;
			}

			aux = mean(values);
			this.resultsDataset.getColumn(this.actualColumn).addValue(aux);
		}

		this.actualColumn++;

		// Save values in the size dataset
		int [] compFreq = ((RankingAlgorithm)algorithm).getComponentsFrecuency();
		for(int i=0; i<compFreq.length; i++){
			this.sizeDataset.getColumn(this.actualSizeColumn).addValue((double)compFreq[i]);
		}
		if(generation > 0){
			int [] mutCompFreq = ((RankingAlgorithm)algorithm).getComponentsMutFrecuency();
			for(int i=0; i<compFreq.length; i++){
				this.sizeDataset.getColumn(this.actualSizeColumn+1).addValue((double)mutCompFreq[i]);
			}
		}
		else{
			for(int i=0; i<compFreq.length; i++){
				this.sizeDataset.getColumn(this.actualSizeColumn+1).addValue(NullValue.getNullValue());
			}
		}
		this.actualSizeColumn +=2;
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Return the mean of components in the population
	 * @param inhabitants The list of individuals
	 * @return The mean value
	 * */
	protected double averageComponents(List<IIndividual> inhabitants){
		double avg = 0.0;
		int size = inhabitants.size();
		if(size>0){
			for(int i=0; i<size; i++)
				avg += ((RankingIndividual)(inhabitants.get(i))).getNumberOfComponents();
			avg /= size;
		}
		return avg;
	}

	/**
	 * Return the mean of connectors in the population
	 * @param inhabitants The list of individuals
	 * @return The mean value
	 * */
	protected double averageConnectors(List<IIndividual> inhabitants){
		double avg = 0.0;
		int size = inhabitants.size();
		if(size>0){
			for(int i=0; i<size; i++)
				avg += ((RankingIndividual)(inhabitants.get(i))).getNumberOfConnectors();
			avg /= size;
		}
		return avg;
	}

	/**
	 * Return the mean of the elements in the array
	 * @param values The array with the values
	 * @return The mean value
	 * */
	protected double mean(double [] values){
		double avg = 0.0;
		int size = values.length;
		if(size>0){

			for(int i=0; i<size; i++){
				avg += values[i];
			}
			avg /= size;
		}
		return avg;
	}
}