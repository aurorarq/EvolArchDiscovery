package net.sf.jclec.sbse.cl2cmp.ranking;

import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.cl2cmp.Cl2CmpMutator;
import net.sf.jclec.sbse.cl2cmp.Cl2CmpIndividual;

/**
 * Extension of Cl2CmpMutator which considers
 * the identification of individuals
 * 
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * 
 * @version 1.0
 * History:
 * <ul>
 * 	<li>1.0: Creation (December 2013)
 * </ul>
 * */

public class RankingMutator extends Cl2CmpMutator{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 4935661550697665517L;

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	protected void mutateNext() {

		// Individual to be mutated
		IIndividual ind = this.parentsBuffer.get(this.parentsCounter);
		IIndividual mutInd = null;
		int numOfAttempts = 0;
		int randomIndex=-1;				// The random mutator index
		double randomValue;			
		boolean isNewValidIndividual = false;

		// Try to generate a valid individual. If the mutation is performed 10 times
		// without exit, return the parent
		int numMutators = this.roulette.length;

		// Check if the roulette is not empty (at least one mutator is applicable) 
		if(this.roulette[numMutators-1] != 0.0){
			do{
				randomIndex = -1;
				randomValue = this.randgen.raw();
				// Selects mutator using the roulette 
				for (int i=0; i<this.roulette.length && randomIndex==-1; i++) {
					if (randomValue <= this.roulette[i]) {
						randomIndex = i;
					}
				}
				mutInd = this.mutators.get(randomIndex).mutateIndividual((Cl2CmpIndividual)ind, this.randgen);
				((RankingIndividual)mutInd).setId(((RankingIndividual)ind).getId());

				isNewValidIndividual = !((Cl2CmpIndividual)mutInd).isInvalid();
				numOfAttempts++;
			}while(numOfAttempts<=this.maxOfAttempts && !isNewValidIndividual);

			// Valid individual, add to sons buffer
			if(isNewValidIndividual){
				this.sonsBuffer.add(mutInd);
				this.newOffsprings++;
			}

			// Invalids offsprings are allowed with a certain probability
			else{
				randomValue = this.randgen.raw();
				// Add invalid offspring
				if(randomValue <= this.probInvalids){
					this.sonsBuffer.add(mutInd);
					this.newOffsprings++;
				}
				// Add parent
				else{
					this.sonsBuffer.add(ind);
				}
			}
		}
		else // Non applicable mutator: Return parent
			this.sonsBuffer.add(ind);
	}
}