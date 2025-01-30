package net.sf.jclec.sbse.cl2cmp.ranking;

import net.sf.jclec.sbse.cl2cmp.Cl2CmpSpecies;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;
import net.sf.jclec.syntaxtree.SyntaxTreeIndividual;

/**
 * Species for 'Classes to Components' (Cl2Cmp) problem
 * formulated as a ranking-based mono-objective problem.
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
public class RankingSpecies extends Cl2CmpSpecies{

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 4440458652705570134L;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public RankingSpecies(){
		super();
	}

	//////////////////////////////////////////////////////////////////
	//------------------------------------------------- Public methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Create new individual.
	 * @param genotype: Individual genotype.
	 * @param id: Individual id
	 * @return A new individual with the given genotype.
	 * */
	public SyntaxTreeIndividual createIndividual(SyntaxTree genotype, int id) {
		return new RankingIndividual(genotype, id);
	}

	//////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////
	
	@Override
	public SyntaxTreeIndividual createIndividual(SyntaxTree genotype) {
		return new RankingIndividual(genotype);
	}
}