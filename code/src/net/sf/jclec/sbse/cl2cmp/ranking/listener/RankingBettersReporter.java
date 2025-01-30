package net.sf.jclec.sbse.cl2cmp.ranking.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.Column.NominalColumn;
import es.uco.kdis.datapro.dataset.Column.NumericalColumn;
import es.uco.kdis.datapro.dataset.Source.ExcelDataset;

import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.IAlgorithmListener;
import net.sf.jclec.IConfigure;
import net.sf.jclec.IIndividual;
import net.sf.jclec.algorithm.PopulationAlgorithm;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.cl2cmp.Cl2CmpMutator;
import net.sf.jclec.sbse.cl2cmp.mut.AbstractCmpMutator;
import net.sf.jclec.sbse.cl2cmp.ranking.IndividualsComparator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingAlgorithm;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingEvaluator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.metrics.CmpMetric;

/**
 * Reporter for 'Classes to Components' (Cl2Cmp) problem.
 * It presents extended results for best valid individuals (one
 * of each size).
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (September 2013)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class RankingBettersReporter implements IAlgorithmListener, IConfigure  {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	private static final long serialVersionUID = 8588270685575163848L;

	/** Name of the report*/
	protected String reportTitle;

	/** Report frequency */
	protected int reportFrequency;

	/** Report file */
	protected File reportFile;

	/** Report file writer */
	protected FileWriter reportFileWriter;

	/** Report dataset */
	protected Dataset resultsDataset;

	/** The report already exits (for multiple executions) */
	protected boolean isFirstExecution = false;

	/** Attributes to be saved in the dataset */
	private String [] tags = {"#Betters", "Fitness", "#Components", "#Connectors"};

	/** Attributes to be saved in the dataset (metrics used in the algorithm) */
	protected String [] metricsTags;

	/** Actual column */
	protected int actualColumn;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////	

	/**
	 * Empty Constructor
	 * */
	public RankingBettersReporter() {
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
		
		this.reportFile = new File(this.reportTitle+"-bestInds.txt");
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

			// Add a column with the name of the metrics to be saved
			this.resultsDataset.addColumn(new NominalColumn("Metrics"));
			for(int i=0; i<this.tags.length; i++)
				this.resultsDataset.getColumn(0).addValue(this.tags[i]);

			// Add a column with the name of the fitness metrics to be saved
			for(int i=0; i<this.metricsTags.length; i++){
				this.resultsDataset.getColumn(0).addValue(this.metricsTags[i]);
			}

			// Add numerical columns (one for each generation that will be reported)
			for(int i=0; i<numOfCols; i++)
				this.resultsDataset.addColumn(new NumericalColumn("Gen-"+(i*this.reportFrequency)), i+1);
		}

		// Other executions, open the dataset and add new attributes
		else{
			this.resultsDataset = new ExcelDataset(this.reportTitle+"-betters.xlsx");
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
			// Metrics tags
			for(int i=0; i<this.metricsTags.length; i++)
				this.resultsDataset.getColumn(0).addValue(this.metricsTags[i]);
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

		// Save betters
		saveIndividuals((PopulationAlgorithm) event.getAlgorithm());

		// Close report file if necessary
		if (this.reportFile != null) {
			try {
				this.reportFileWriter.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Save dataset
		try {
			((ExcelDataset)this.resultsDataset).writeDataset(this.reportTitle+"-bettters.xlsx");
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
		
		// Actual generation
		int generation = algorithm.getGeneration();

		// Check if this is correct generation
		if (!force && generation%this.reportFrequency != 0) {
			return;
		}

		// Select betters individuals
		List<IIndividual> betters = selectIndividuals(algorithm);

		// Betters size
		this.resultsDataset.getColumn(this.actualColumn).addValue((double)betters.size());

		// Betters average fitness
		this.resultsDataset.getColumn(this.actualColumn).addValue(averageFitness(betters));
		
		// Average components and connectors
		double avg[] = averageCompConn(betters);
		this.resultsDataset.getColumn(this.actualColumn).addValue(avg[0]);
		this.resultsDataset.getColumn(this.actualColumn).addValue(avg[1]);

		// Value of each fitness metrics
		ArrayList<CmpMetric> metrics = ((RankingEvaluator)((RankingAlgorithm)algorithm).getEvaluator()).getMetrics();
		for(int i=0; i<metrics.size(); i++){
			this.resultsDataset.getColumn(this.actualColumn).addValue(metricAverage(metrics.get(i), betters));
		}
		this.actualColumn++;
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------ Private methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get the average number of components and connectors
	 * in the betters set
	 * @param betters The betters set
	 * @return average number of components and connectors

	 * */
	protected double[] averageCompConn(List<IIndividual> betters) {
		double avg [] = new double[]{0.0,0.0};
		int size = betters.size();
		RankingIndividual ind;
		int n = 0;
		for(int i=0; i<size; i++){
			ind = ((RankingIndividual)betters.get(i));
			if(!ind.isInvalid()){
				avg[0] += ind.getNumberOfComponents();
				avg[1] += ind.getNumberOfConnectors();
				n++;
			}
		}
		avg[0]/=n;
		avg[1]/=n;
		return avg;
	}
	
	/**
	 * Get the average fitness
	 * in the betters set
	 * @param betters The betters set
	 * @return average fitness
	 * */
	protected double averageFitness(List<IIndividual> betters) {
		double avg = 0.0;
		int size = betters.size();
		RankingIndividual ind;
		int n = 0;
		for(int i=0; i<size; i++){
			ind = ((RankingIndividual)betters.get(i));
			if(!ind.isInvalid()){
				avg += ((SimpleValueFitness)ind.getFitness()).getValue();
				n++;
			}
		}
		avg/=n;
		return avg;
	}

	/**
	 * Get the average value of a specified metric
	 * in the betters set
	 * @param metric The metric to be evaluated
	 * @param betters The betters set
	 * @return average value of metric in individuals
	 * */
	protected double metricAverage(CmpMetric metric, List<IIndividual> betters){
		double avg = 0.0;
		int size = betters.size();
		RankingIndividual ind;
		int n = 0;
		for(int i=0; i<size; i++){
			ind = ((RankingIndividual)betters.get(i));
			if(!ind.isInvalid()){
				avg += metric.getFromIndividual(ind); 
				n++;
			}
		}
		avg/=n;
		return avg;
	}

	/**
	 * Save best individuals
	 * @param algorithm The algorithm
	 * */
	protected List<IIndividual> selectIndividuals(PopulationAlgorithm algorithm) {
		IndividualsComparator comparator = new IndividualsComparator();
		comparator.setInverse(false);

		// Population individuals
		List<IIndividual> inhabitants = algorithm.getInhabitants();
		int min = ((RankingAlgorithm)algorithm).getMinNumberOfComponents();
		int max = ((RankingAlgorithm)algorithm).getMaxNumberOfComponents();
		int size = inhabitants.size();
		Collections.sort(inhabitants,comparator);

		// Select best individuals: one of each size
		List<IIndividual> betters = new ArrayList<IIndividual>();

		BitSet bitset = new BitSet((max-min+1));
		int numOfComponents;
		for(int i=0; i<size; i++){
			IIndividual ind = inhabitants.get(i);
			numOfComponents = ((RankingIndividual)ind).getNumberOfComponents();
			if(!bitset.get(numOfComponents-min) && !((RankingIndividual)ind).isInvalid()){
				betters.add(ind.copy()); // copy individual
				bitset.set(numOfComponents-min); // set the size found
			}
		}
		
		return betters;
	}
	
	/**
	 * Save betters in file
	 * @param algorithm The algorithm
	 * */
	private void saveIndividuals(PopulationAlgorithm algorithm){
		List<IIndividual> best = selectIndividuals(algorithm);
		StringBuffer sb = new StringBuffer("\nBEST INDIVIDUALS\n--\n");
		for(int i=0; i<best.size(); i++){
			sb.append(((RankingIndividual)best.get(i)).toString() + "\n--\n");
		}
		if(algorithm.getGeneration()>=algorithm.getMaxOfGenerations()){	
			sb.append("\n--------------------------------------------------\n\n");
		}
		// Write string to the report file (if necessary) 
		try {
			this.reportFileWriter = new FileWriter(this.reportTitle + "-betters.txt", true);
			this.reportFileWriter.write(sb.toString());
			this.reportFileWriter.flush();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}