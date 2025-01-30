package net.sf.jclec.sbse.cl2cmp.ranking;

import java.util.Comparator;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.fitness.ValueFitnessComparator;

/**
 * Individuals comparator
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * <p>History
 * <ul>
 * <li>1.0: Creation (July 2013)
 * </ul>
 * */
public class IndividualsComparator implements Comparator<IIndividual> {

	private ValueFitnessComparator comparator = new ValueFitnessComparator();
	
	private boolean inverse = false;
	
	@Override
	public int compare(IIndividual ind1, IIndividual ind2) {
		IFitness fitness1 = ind1.getFitness();
		IFitness fitness2 = ind2.getFitness();
		// Set the same inverse flag
		comparator.setInverse(inverse);
		// Compare fitness
		return comparator.compare(fitness1, fitness2);
	}
	
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}
}
