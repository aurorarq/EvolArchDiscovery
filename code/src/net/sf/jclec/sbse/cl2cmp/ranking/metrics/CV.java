package net.sf.jclec.sbse.cl2cmp.ranking.metrics;

import es.uco.kdis.datapro.dataset.Dataset;
import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;

/**
 * Coefficient of Variation (CV) for the
 * 'Classes to Components' (CL2CMP) problem.
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (July 2013)
 * </ul>
 * */
public class CV extends CmpMetric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -8878549952871748114L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * Maximization flag set to false as default
	 * */
	public CV(){
		super();
		this.maximize = false;
		this.comparator = new CVComparator();
		this.comparator.setInverse(this.maximize);	// If maximize, inverse flag set to true
	}

	/**
	 * Parameterized constructor
	 * @param dataset The dataset
	 * @param maximize True for metric maximization, false for minimization
	 * */
	public CV(Dataset dataset, boolean maximize){
		super(dataset, maximize);
		this.comparator = new CVComparator();
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
		double numberOfClasses;
		double avg = 0.0, std = 0.0;

		// Mean and standard deviation of components size
		for(int i=0; i<numberOfComponents; i++){
			numberOfClasses = ind.getNumberOfClasses(i);
			avg += numberOfClasses;
			std += numberOfClasses*numberOfClasses;
		}
		
		avg /= numberOfComponents;
		std = (std/numberOfComponents) - avg*avg;
		std = Math.sqrt(std);
	
		this.result = std/avg;
	}

	@Override
	public void setOnIndividual(RankingIndividual ind) {
		// Set CV in the individual
		ind.setCV(this.result);
	}

	@Override
	public double getFromIndividual(RankingIndividual ind) {
		return ind.getCV();
	}

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------ Private class
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Compare two individuals using
	 * their CV values
	 * */
	private class CVComparator extends MetricComparator{

		@Override
		public int compare(IIndividual ind1, IIndividual ind2) {
			double value1, value2;  
			
			// Get CV values
			try {
				value1 = ((RankingIndividual)ind1).getCV();
				value2 = ((RankingIndividual)ind2).getCV();
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
