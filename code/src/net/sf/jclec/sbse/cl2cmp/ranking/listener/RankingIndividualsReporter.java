package net.sf.jclec.sbse.cl2cmp.ranking.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;

import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.IAlgorithmListener;
import net.sf.jclec.IConfigure;
import net.sf.jclec.IIndividual;
import net.sf.jclec.algorithm.PopulationAlgorithm;
import net.sf.jclec.sbse.cl2cmp.Cl2CmpMutator;
import net.sf.jclec.sbse.cl2cmp.mut.AbstractCmpMutator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingAlgorithm;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingEvaluator;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.metrics.CmpMetric;

/**
 * Reporter for 'Classes to Components' (Cl2Cmp) problem.
 * It stores the complete population.
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (September 2013)
 * </ul>
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class RankingIndividualsReporter implements IAlgorithmListener, IConfigure{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 8911256421294510964L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////	

	public RankingIndividualsReporter(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	public void configure(Configuration settings) {
		// Do nothing
	}

	@Override
	public void algorithmStarted(AlgorithmEvent event) {
		// Do nothing
	}

	@Override
	public void iterationCompleted(AlgorithmEvent event) {
		// Do nothing
	}

	@Override
	public void algorithmFinished(AlgorithmEvent event) {
		
		File reportFile;
		FileWriter reportFileWriter;
		
		
		String datasetname = ((RankingEvaluator)((RankingAlgorithm)event.getAlgorithm()).getEvaluator()).getDatasetFileName();
		if(datasetname.contains("/"))
			datasetname=datasetname.substring(datasetname.lastIndexOf("/")+1);
		String reportTitle = datasetname.substring(0, datasetname.lastIndexOf("."));
		
		reportTitle += "-s" + ((RankingAlgorithm)event.getAlgorithm()).getSelectorType();
		reportTitle += "-r" + ((RankingAlgorithm)event.getAlgorithm()).getReplacementType();
		reportTitle += "-i" + ((PopulationAlgorithm)event.getAlgorithm()).getPopulationSize();
		reportTitle += "-g" + ((PopulationAlgorithm)event.getAlgorithm()).getMaxOfGenerations();
		
		
		Cl2CmpMutator base = (Cl2CmpMutator)((RankingAlgorithm)event.getAlgorithm()).getMutator();
		List<AbstractCmpMutator> mut = base.getMutators();
		String probs = "-p";
		for(AbstractCmpMutator m: mut){
			probs += "-" +(int)m.getWeight()*100;
		}
		reportTitle += probs;
		reportFile = new File(reportTitle + "-finalInds.txt");
		
		if(!reportFile.exists()){
			if(reportFile.getParentFile()!=null)
				reportFile.getParentFile().mkdirs();
		}
		
		try {
			reportFileWriter = new FileWriter(reportFile, true);
			List<IIndividual> inhabitants = ((PopulationAlgorithm)event.getAlgorithm()).getInhabitants();
			ArrayList<CmpMetric> metrics = ((RankingEvaluator)((RankingAlgorithm)((PopulationAlgorithm)event.getAlgorithm())).getEvaluator()).getMetrics();
			StringBuffer buffer = new StringBuffer();
			
			for (IIndividual ind : inhabitants) {
				if(!((RankingIndividual)ind).isInvalid()){
					for(CmpMetric m: metrics){
						buffer.append(" " + m.getFromIndividual((RankingIndividual)ind));
					}
					buffer.append("\n");
				}
			}
			reportFileWriter.flush();
			reportFileWriter.write(buffer+"-------------------------\n");
			reportFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void algorithmTerminated(AlgorithmEvent e) {
		// Do nothing
	}
}