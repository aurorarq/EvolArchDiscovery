package net.sf.jclec.sbse.cl2cmp.ranking.metrics;

import es.uco.kdis.datapro.dataset.Dataset;
import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;

/**
 * Metric that counts the maximal difference
 * between component sizes for the
 * 'Classes to Components' (CL2CMP) problem.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (July 2013)
 * </ul>
 * 
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class MaxDifSize extends CmpMetric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -5568892570450541133L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * Maximization flag set to false as default
	 * */
	public MaxDifSize(){
		super();
		this.maximize = false;
		this.comparator = new MaxDifSizeComparator();
		this.comparator.setInverse(this.maximize);	// If maximize, inverse flag set to true
	}

	/**
	 * Parameterized constructor
	 * @param maximize True for metric maximization, false for minimization
	 * */
	public MaxDifSize(Dataset dataset, boolean maximize){
		super(dataset, maximize);
		this.comparator = new MaxDifSizeComparator();
		this.comparator.setInverse(this.maximize);	// If maximize, inverse flag set to false
	}

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	public void prepare(RankingIndividual ind) {
		// Do nothing, needed metrics 
		// are properties of the individual
	}

	@Override
	public void calculate(RankingIndividual ind) {
		int numberOfComponents = ind.getNumberOfComponents();
		int maxSize = Integer.MIN_VALUE, minSize = Integer.MAX_VALUE;
		int numberOfClasses;
		// Get min and max size
		for(int i=0; i<numberOfComponents; i++){
			numberOfClasses = ind.getNumberOfClasses(i);
			if(numberOfClasses>maxSize)
				maxSize = numberOfClasses;
			if(numberOfClasses<minSize)
				minSize = numberOfClasses;
		}
		this.result = (double)maxSize-minSize;
	}

	@Override
	public void setOnIndividual(RankingIndividual ind) {
		ind.setDifSize(this.result);
	}
	
	@Override
	public double getFromIndividual(RankingIndividual ind) {
		return ind.getDifSize();
	}

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------ Private class
	/////////////////////////////////////////////////////////////////

	/**
	 * Compare two individuals using
	 * their size difference values
	 * */
	private class MaxDifSizeComparator extends MetricComparator{

		@Override
		public int compare(IIndividual ind1, IIndividual ind2) {
			double value1, value2;  

			// Get MaxDifSize values
			try {
				value1 = ((RankingIndividual)ind1).getDifSize();
				value2 = ((RankingIndividual)ind2).getDifSize();
			}
			catch(ClassCastException e) {
				throw new IllegalArgumentException
				("CmpIndividual expected as arguments");
			}

			// Compare values
			if (value1 > value2) {
				return inverse ? -1 : 1;
			}
			else if(value1 < value2) {
				return inverse ? 1 : -1;
			}
			else {
				return 0;
			}
		}
	}
}
