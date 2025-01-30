package net.sf.jclec.sbse.cl2cmp.ranking;

import net.sf.jclec.sbse.cl2cmp.Cl2CmpTreeCreator;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Extension of Cl2CmpTreeCreator which
 * considers the identification of individuals
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

public class RankingTreeCreator extends Cl2CmpTreeCreator{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 6353659225345194589L;

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	@Override
	protected void createNext() {
		SyntaxTree genotype = new SyntaxTree();
		// Generate the random number of components
		int numberOfComponents = this.randgen.choose(this.schema.getMinNumOfComp(), this.schema.getMaxNumOfComp()+1);
		genotype = this.schema.createSyntaxTree(numberOfComponents, this.randgen);
		this.createdBuffer.add(((RankingSpecies)this.species).createIndividual(genotype, this.createdCounter));
	}

}
