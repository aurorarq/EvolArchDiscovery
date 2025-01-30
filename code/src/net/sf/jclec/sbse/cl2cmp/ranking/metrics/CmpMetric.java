package net.sf.jclec.sbse.cl2cmp.ranking.metrics;

import java.util.Comparator;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Dataset;
import net.sf.jclec.IConfigure;
import net.sf.jclec.IIndividual;
import net.sf.jclec.JCLEC;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;

/**
 * An abstract metric for the evaluation of
 * a desirable design characteristic in the
 * 'Classes to Components' (CL2CMP) problem.
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (July 2013)
 * 	<p>
 * </ul>
 * */
public abstract class CmpMetric implements JCLEC, IConfigure{

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -6124004331146205302L;

	/** Value associated with the metric */
	protected double result;

	/** To be maximized or minimized? */
	protected boolean maximize;

	/** Dataset that contains the problem information (analysis model) */
	protected Dataset dataset;

	/** Comparator */
	protected MetricComparator comparator;

	/** Path to metrics package */
	static public String path = "net.sf.jclec.sbse.cl2cmp.ranking.metrics.";
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * Maximization flag must be refined by the subclass
	 * */
	public CmpMetric(){
		this.result = Double.NaN;
	}

	/**
	 * Parameterized constructor
	 * @param dataset: The dataset
	 * @param maximize: True for metric maximization, false for minimization
	 * */
	public CmpMetric(Dataset dataset, boolean maximize){
		super();
		this.dataset = dataset;
		this.result = Double.NaN;
		this.maximize = maximize;
	}

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------- Abstract methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Prepare the computation. Required characteristics
	 * are taken from the individual.
	 * @param ind: The individual
	 * */
	public abstract void prepare(RankingIndividual ind);

	/**
	 * Compute the metric. <code>prepare()</code> is
	 * executed before to prepare computation.
	 * @see prepare(IIndividual ind) 
	 * */
	public abstract void calculate(RankingIndividual ind);

	/**
	 * Set the value in the correspondent field
	 * of the individual
	 * @param ind: The individual
	 * */
	public abstract void setOnIndividual(RankingIndividual ind);

	/** 
	 * Get the metric from the correspondent 
	 * individual property.
	 * @param ind: The individual
	 * @return Metric value in the individual
	 * */
	public abstract double getFromIndividual(RankingIndividual ind);
	
	@Override
	public void configure(Configuration settings) {
		// Do nothing
	}
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Get the maximize flag
	 * @return True if metric must be maximized, false otherwise
	 * */
	public boolean isMaximize(){
		return this.maximize;
	}
	
	/**
	 * Set the maximize flag
	 * @param maximize: New flag value
	 * Note: flag is propagated to comparator inverse flag  
	 * */
	public void setMaximize(boolean maximize){
		this.maximize = maximize;
		this.comparator.setInverse(!this.maximize);
	}

	/**
	 * Set dataset
	 * @param dataset: The dataset
	 * */
	public void setDataset(Dataset dataset){
		this.dataset = dataset;
	}

	/**
	 * Return the result
	 * @return The result
	 * */
	public double getResult(){
		return this.result;
	}

	/**
	 * Get the comparator
	 * @return Metric comparator
	 * */
	public Comparator<IIndividual> getComparator(){
		return this.comparator;
	}
	
	/////////////////////////////////////////////////////////////////
	// -------------------------------------------- Protected classes
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Compare two individuals with
	 * the metric defined by the class.
	 * To be refined by the subclasses.
	 * */
	protected abstract class MetricComparator implements Comparator<IIndividual>{
	
		/** Inverse flag */
		protected boolean inverse;
		
		@Override
		public int compare(IIndividual arg0, IIndividual arg1) {
			// To be refined by the subclasses
			return 0;
		}
		
		/**
		 * Sets the invert flag
		 * @param inverse Invert flag
		 */
		public void setInverse(boolean inverse) {
			this.inverse = inverse;
		}
		
		/**
		 * Access to 'inverse' flag
		 * @return Inverse flag value
		 */
		public boolean isInverse() {
			return inverse;
		}
	}
}
