package net.sf.jclec.sbse.cl2cmp.ranking;

import org.apache.commons.lang.builder.EqualsBuilder;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.fitness.ISimpleFitness;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.sbse.cl2cmp.Cl2CmpIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.metrics.Metrics;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Individual for 'Classes to Components' (CL2CMP) problem.
 * It adds ranking properties to CmpIndividual.
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

public class RankingIndividual extends Cl2CmpIndividual {

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------------- Properties
	//////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 5923236293053621038L;

	/** 
	 * Metric Rankings (used for fitness).
	 * Each position stores the ranking of the
	 * metric at this position in the enumeration 
	 * <code>Metrics</code>. Ranking will be set
	 * only when the correspondent metric is used
	 * in the evaluator.
	 * */
	private double rankings [];

	///// Metrics values for rankings

	/** Average cohesion */
	private double icd = -1.0;

	/** Overall CV */
	private double cv = -1.0;

	/** Maximum difference in components size */
	private double difSize = -1.0;

	/** Total external weighted relations */
	private double erp = -1.0;

	/** Groups ratio */
	private double gcr = -1.0;

	/** Identifier needed for the correspondence between 
	 * parent and offsprings (initial order in the population) */
	private int id;

	//////////////////////////////////////////////////////////////////
	//--------------------------------------------------- Constructors
	//////////////////////////////////////////////////////////////////

	/**
	 * Constructor that sets individual genotype
	 * @param genotype: Individual genotype
	 * */
	public RankingIndividual(SyntaxTree genotype){
		super(genotype);			// Set genotype
		this.rankings = new double[Metrics.values().length];
	}
	
	/**
	 * Constructor that sets individual genotype
	 * @param genotype: Individual genotype
	 * @param id: The individual id (position in the population without order)
	 * */
	public RankingIndividual(SyntaxTree genotype, int id){
		super(genotype);			// Set genotype
		this.rankings = new double[Metrics.values().length];
		this.id = id;
	}

	/**
	 * Constructor that sets individual genotype and fitness
	 * @param genotype: Individual genotype
	 * @param fitness:  Individual fitness
	 * @param id: The individual id (position in the population without order)
	 */
	public RankingIndividual(SyntaxTree genotype, IFitness fitness, int id) {
		super(genotype,fitness);	// Set genotype and fitness
		this.rankings = new double[Metrics.values().length];
		this.id = id;
	}
	
	/**
	 * Constructor that sets individual genotype and fitness
	 * @param genotype: Individual genotype
	 * @param fitness:  Individual fitness
	 */
	public RankingIndividual(SyntaxTree genotype, IFitness fitness) {
		super(genotype,fitness);	// Set genotype and fitness
		this.rankings = new double[Metrics.values().length];
	}

	//////////////////////////////////////////////////////////////////
	//----------------------------------------------- Override methods
	//////////////////////////////////////////////////////////////////

	/** 
	 * Copy the individual
	 * @return A copy of the individual
	 * */
	@Override
	public IIndividual copy() {
		// Create new individual
		RankingIndividual ind = new RankingIndividual(this.genotype.copy(), this.id);

		// Set phenotype
		ind.setPhenotypefromGenotype();

		// Copy properties
		ind.setNumberOfComponents(this.getNumberOfComponents());
		ind.setNumberOfConnectors(this.getNumberOfConnectors());
		ind.setHasIsolatedComponents(this.hasIsolatedComponents());
		ind.setHasMutuallyDepComponents(this.hasMutuallyDepComponents());

		// Copy fitness and metrics
		if(this.fitness != null){
			ind.setFitness(this.getFitness());

			ind.icd = this.icd;
			ind.erp = this.erp;
			ind.cv = this.cv;
			ind.difSize = this.difSize;
			ind.gcr = this.gcr;

			// Copy rankings
			ind.setRankings(this.rankings);

			// Copy rest of properties
			ind.setNumberOfClasses(this.getNumberOfClasses());
			ind.setNumberOfGroups(this.getNumberOfGroups());
			ind.setClassesDistribution(this.getClassesDistribution());
			ind.setClassesToGroups(this.getClassesToGroups());

			// Other properties
			ind.setExternalConnections(this.getExternalConnections());
			ind.setNumberOfProvided(this.getNumberOfProvided());
			ind.setNumberOfRequired(this.getNumberOfRequired());

		}
		return ind;
	}

	@Override
	public String toString(){

		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		if(this.fitness != null && this.fitness instanceof ISimpleFitness)
				buffer.append("\nFitness: " + ((SimpleValueFitness)this.fitness).getValue());
	
		// Metrics
		if(this.icd!=-1)
			buffer.append("\nICD: " + this.icd);
		if(this.erp!=-1)
			buffer.append("\nERP: " + this.erp);
		if(this.cv!=-1)
			buffer.append("\nCV: " + this.cv);
		if(this.difSize!=-1)
			buffer.append("\nDifSize: " + this.difSize);
		if(this.gcr!=-1)
			buffer.append("\nGCR: " + this.gcr);
		
		return buffer.toString();
	}

	@Override
	public boolean equals(Object other){	
		if (other instanceof RankingIndividual) {
			RankingIndividual ind = (RankingIndividual) other;
			// Check general properties: type of solution and fitness
			EqualsBuilder eb = new EqualsBuilder();
			eb.append(genotype, ind.genotype);
			eb.append(fitness, ind.fitness);		
			return eb.isEquals();
		}
		else
			return false;
	}
	
	//////////////////////////////////////////////////////////////////
	//-------------------------------- Public getters/setters methods
	//////////////////////////////////////////////////////////////////

	/**
	 * Get the ranking for a metric
	 * @param index: The metric index
	 * @return The value of the ranking for this metric
	 * */
	public double getRanking(int index){
		return this.rankings[index];
	}

	/**
	 * Set rankings
	 * @param rankings: The metric rankings
	 * */
	public void setRankings(double [] rankings){
		this.rankings = rankings;
	}

	/**
	 * Set ranking for a metric
	 * @param index: The metric index
	 * @param ranking: The ranking value
	 * */
	public void setRanking(int index, double ranking){
		this.rankings[index] = ranking;
	}

	/**
	 * Get ICD
	 * @return ICD value
	 * */
	public double getICD() {
		return this.icd;
	}

	/**
	 * Set ICD
	 * @param icd New icd value
	 * */
	public void setICD(double icd) {
		this.icd = icd;
	}

	/**
	 * Get CV
	 * @return CV value
	 * */
	public double getCV() {
		return this.cv;
	}

	/**
	 * Set CV
	 * @param cv: New CV value
	 * */
	public void setCV(double cv) {
		this.cv = cv;
	}

	/**
	 * Get groups ratio
	 * @return groups ratio value
	 * */
	public double getGCR() {
		return this.gcr;
	}

	/**
	 * Set groups ratio
	 * @param groups ratio value
	 * */
	public void setGCR(double gcr) {
		this.gcr = gcr;
	}

	/**
	 * Get maximal size difference
	 * @return difSize value
	 * */
	public double getDifSize() {
		return this.difSize;
	}

	/**
	 * Set difSize
	 * @param difSize: New difSize value
	 * */
	public void setDifSize(double difSize) {
		this.difSize = difSize;
	}

	/**
	 * Get external relations value
	 * @return total weighted external relations
	 * */
	public double getERP() {
		return this.erp;
	}

	/**
	 * Set external relations value
	 * @param extRel: New difSize value
	 * */
	public void setERP(double erp) {
		this.erp = erp;
	}
	
	/**
	 * Get individual identifier
	 * @return ID
	 * */
	public int getId(){
		return this.id;
	}
	
	/**
	 * Set individual identifier
	 * @param id: The new ID
	 * */
	public void setId(int id){
		this.id = id;
	}
}