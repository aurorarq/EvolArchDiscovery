package net.sf.jclec.sbse.cl2cmp.ranking.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.IAlgorithmListener;
import net.sf.jclec.IConfigure;
import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.algorithm.PopulationAlgorithm;
import net.sf.jclec.sbse.cl2cmp.Cl2CmpMutator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingAlgorithm;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;
import net.sf.jclec.sbse.cl2cmp.mut.AbstractCmpMutator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingEvaluator;
import net.sf.jclec.util.IndividualStatistics;

/**
 * Listener for reporting the best individual in a file
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class RankingBestFileReporter implements IAlgorithmListener, IConfigure  {

	/** ID */
	private static final long serialVersionUID = -9098807968785282152L;

	/** Name of the report*/
	private String reportTitle;

	/** Report file */
	private File reportFile;

	/** Report file writer */
	private FileWriter reportFileWriter;

	/** Report frequency */
	private int reportFrequency;

	@Override
	public void configure(Configuration settings) {
		this.reportFrequency = settings.getInt("report-frequency", 1);
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
		this.reportTitle += "-g" + ((PopulationAlgorithm)event.getAlgorithm()).getGeneration();

		Cl2CmpMutator base = (Cl2CmpMutator)((RankingAlgorithm)event.getAlgorithm()).getMutator();
		List<AbstractCmpMutator> mut = base.getMutators();
		String probs = "-p";
		for(AbstractCmpMutator m: mut){
			probs += "-" +(int)(m.getWeight()*100);
		}
		this.reportTitle += probs;
		this.reportFile = new File(this.reportTitle);
		if(!this.reportFile.exists()){
			if(this.reportFile.getParentFile()!=null)
				this.reportFile.getParentFile().mkdirs();
		}
		
		// TODO Remove only for example
		createName((PopulationAlgorithm)event.getAlgorithm());
		bestIndividual((PopulationAlgorithm)event.getAlgorithm());
	}

	@Override
	public void iterationCompleted(AlgorithmEvent event) {

		// Check if this is correct generation
		int generation = ((PopulationAlgorithm)event.getAlgorithm()).getGeneration();
		
		if (generation%this.reportFrequency != 0) {
			return;
		}

		createName((PopulationAlgorithm)event.getAlgorithm());
		bestIndividual((PopulationAlgorithm)event.getAlgorithm());
	}

	@Override
	public void algorithmFinished(AlgorithmEvent event) {
		createName((PopulationAlgorithm)event.getAlgorithm());
		bestIndividual((PopulationAlgorithm)event.getAlgorithm());
	}

	@Override
	public void algorithmTerminated(AlgorithmEvent e) {
	}

	/**
	 * Save best individual in file
	 * @param algorithm The algorithm
	 * */
	private void bestIndividual(PopulationAlgorithm algorithm) {

		// Fitness comparator
		Comparator<IFitness> comparator = algorithm.getEvaluator().getComparator();
		// Population individuals
		List<IIndividual> inhabitants = algorithm.getInhabitants();

		// Do population report
		StringBuffer sb = new StringBuffer("\n-----\n\n");

		// Best individual
		IIndividual best = IndividualStatistics.bestIndividual(inhabitants, comparator);

		// Last report, add best individual
		sb.append(((RankingIndividual)best).toString() + "\n");

		// Write string to the report file (if necessary) 
		try {
			this.reportFile = new File(this.reportTitle);
			this.reportFileWriter = new FileWriter(this.reportFile, true);
			this.reportFileWriter.write(sb.toString());
			this.reportFileWriter.flush();
		} 
		catch (IOException e) {
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
		this.reportTitle += probs + "-best.txt";
	}

}
