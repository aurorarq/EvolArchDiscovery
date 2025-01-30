package net.sf.jclec.sbse.cl2cmp.ranking.metrics;

import es.uco.kdis.datapro.dataset.Dataset;
import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;

/**
 * Groups/components ratio (GR) metric for the
 * 'Classes to Components' (CL2CMP) problem.
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (September 2013)
 * </ul>
 * 
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */

public class GCR extends CmpMetric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -3666008566826953846L;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public GCR(){
		super();
		this.maximize = false;
		this.comparator = new GCRComparator();
		this.comparator.setInverse(false);	// If maximize, inverse flag set to false
	}

	/**
	 * Parameterized constructor
	 * @param dataset The dataset
	 * */
	public GCR(Dataset dataset){
		super(dataset,false);
		this.comparator = new GCRComparator();
		this.comparator.setInverse(false);
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
		double numberOfComponents = (double)ind.getNumberOfComponents();
		double numberOfGroups = 0.0;

		// Mean and standard deviation of components size
		for(int i=0; i<numberOfComponents; i++){
			numberOfGroups += ind.getNumberOfGroups(i);
		}	
		this.result = numberOfGroups/numberOfComponents;
		
		// TODO
		/*double numOfComponents = (double)ind.getNumberOfComponents();
		double numOfGroups = 0.0;
		double numOfClasses = this.dataset.getColumns().size();
		// Total number of groups
		for(int i=0; i<numOfComponents; i++){
			numOfGroups += ind.getNumberOfGroups(i);
		}
		result = 1.0 - ((numOfGroups-numOfComponents)/(numOfClasses-numOfComponents));*/
	}

	@Override
	public void setOnIndividual(RankingIndividual ind) {
		ind.setGCR(this.result);
	}

	@Override
	public double getFromIndividual(RankingIndividual ind) {
		return ind.getGCR();
	}
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------ Private class
	/////////////////////////////////////////////////////////////////

	/**
	 * Compare two individuals using
	 * their groups ratio values
	 * */
	private class GCRComparator extends MetricComparator{

		@Override
		public int compare(IIndividual ind1, IIndividual ind2) {
			double value1, value2;  

			// Get cohesion values
			try {
				value1 = ((RankingIndividual)ind1).getGCR();
				value2 = ((RankingIndividual)ind2).getGCR();
			}
			catch(ClassCastException e) {
				throw new IllegalArgumentException
				("CmpIndividual expected as arguments");
			}

			// Compare values
			if (value1 > value2) {
	            return inverse ? -1 : 1;
	        }
	        else if(value1 < value1) {
	            return inverse ? 1 : -1;
	        }
	        else {
	            return 0;
	        }
		}
	}
}