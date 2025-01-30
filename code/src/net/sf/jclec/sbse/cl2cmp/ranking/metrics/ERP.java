package net.sf.jclec.sbse.cl2cmp.ranking.metrics;

import java.util.ArrayList;
//import java.util.List;

import org.apache.commons.configuration.Configuration;

import es.uco.kdis.datapro.dataset.Dataset;
//simport es.uco.kdis.datapro.dataset.Column.ColumnAbstraction;
import es.uco.kdis.datapro.dataset.Column.MultivalueColumn;
import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * External Relations Penalty (ERP) Metric
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (September 2013)
 * </ul>
 * @author Aurora Ramirez
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * */
public class ERP extends CmpMetric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = 7160235706012434482L;

	/** Number of external connections for each component */
	private int componentNumberExternalConnections [];

	/** Max relation weight between pairs of components */
	private double componentMaxWeightedExternalConnections [][];

	/** Total relations weights between pairs of components */
	private double componentSumWeightedExternalConnections [][];

	/** UML relation weights */
	private double umlWeights [];

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * Maximization flag set to false as default
	 * */
	public ERP(){
		super();
		this.maximize = false;
		this.comparator = new ERPComparator();
		this.comparator.setInverse(false);
	}

	/**
	 * Parameterized constructor
	 * @param dataset: The dataset
	 * @param maximize: True for metric maximization, false for minimization
	 * @param umlWeights: UML relations weights
	 * */
	public ERP(Dataset dataset){
		super(dataset, false);
		this.comparator = new ERPComparator();
		this.comparator.setInverse(false);
	}

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////
	@Override
	public void prepare(RankingIndividual ind) {
		// Get genotype
		SyntaxTree genotype = ind.getGenotype();
		int numberOfComponents = ind.getNumberOfComponents();

		int j, actualIndex, classIndex, actualCmp=-1, otherCmp=-1;
		boolean isClass = false, isOtherClass = false, isConnector = false;
		MultivalueColumn column;
		int nav_ij, nav_ji, relationType;
		String symbol;

		// Initialize
		this.componentNumberExternalConnections = new int[numberOfComponents];
		this.componentMaxWeightedExternalConnections = new double [numberOfComponents][numberOfComponents];
		this.componentSumWeightedExternalConnections = new double [numberOfComponents][numberOfComponents];

		for(int i=0; i<numberOfComponents; i++){
			this.componentNumberExternalConnections[i] = 0;
			for(j=0; j<numberOfComponents; j++){
				this.componentMaxWeightedExternalConnections[i][j] = 0.0;
				this.componentSumWeightedExternalConnections[i][j] = 0.0;
			}
		}

		// Compute needed metrics for each component
		for(int i=1; !isConnector; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){
				// The symbol classes indicates the beginning of a new component
				if(symbol.equalsIgnoreCase("classes")){
					isClass=true;
					actualCmp++;
				}
				else if(symbol.equalsIgnoreCase("required-interfaces")){
					isClass=false;
				}
				else if(symbol.equalsIgnoreCase("connectors")){
					isConnector=true;
				}
			}

			// Terminal node
			else{
				// If the terminal is a class
				if(isClass){

					// Get the dataset information about the class
					column = (MultivalueColumn) this.dataset.getColumnByName(symbol);
					actualIndex = this.dataset.getIndexOfColumn(column);

					// Check the relations with classes belonging to other components
					otherCmp=actualCmp;			// Start in the actual component
					j=i+1;

					while(!(genotype.getNode(j).getSymbol().equalsIgnoreCase("connectors"))){

						// Search a class in the other component
						if((genotype.getNode(j-1).getSymbol().equalsIgnoreCase("classes"))){
							isOtherClass=true;
							otherCmp++;
						}

						if(isOtherClass){
							// Get relations between classes
							classIndex = this.dataset.getIndexOfColumn(this.dataset.getColumnByName(genotype.getNode(j).getSymbol()));
							ArrayList<Object> relations1 = (ArrayList<Object>) column.getMultiElement(classIndex);

							// Not an invalid value
							if(relations1.size()>1){

								ArrayList<Object> relations2 = (ArrayList<Object>)((MultivalueColumn)this.dataset.getColumn(classIndex)).getMultiElement(actualIndex);

								// Check type and navigation of each relation
								for(int k=1; k<relations1.size(); k+=2){

									relationType = Integer.parseInt((String)relations1.get(k-1));
									nav_ij = Integer.parseInt((String)relations1.get(k));
									nav_ji = Integer.parseInt((String)relations2.get(k));

									// Not a candidate interface, because its a bidirectional relation
									if(nav_ij==nav_ji){
										this.componentNumberExternalConnections[actualCmp]++;
										this.componentNumberExternalConnections[otherCmp]++;

										// Check the type of relation
										double sumTerm = 0.0;
										switch(relationType){
										case 1: sumTerm = this.umlWeights[0]; break; // Association
										case 2: sumTerm = this.umlWeights[1]; break; // Dependence
										case 3: sumTerm = this.umlWeights[2]; break; // Aggregation
										case 4: sumTerm = this.umlWeights[2]; break; // Composition	
										case 5: sumTerm = this.umlWeights[3]; break; // Generalization
										}

										// Update total sum
										this.componentSumWeightedExternalConnections[actualCmp][otherCmp] += sumTerm;
										this.componentSumWeightedExternalConnections[otherCmp][actualCmp] += sumTerm;

										// Update max value if necessary
										if(this.componentMaxWeightedExternalConnections[actualCmp][otherCmp] < sumTerm){
											this.componentMaxWeightedExternalConnections[actualCmp][otherCmp] = sumTerm;
											this.componentMaxWeightedExternalConnections[otherCmp][actualCmp] = sumTerm;
										}
									}
								}
							}
							// End of classes in the other component
							if(genotype.getNode(j+1).arity()!=0){
								isOtherClass=false;
							}
						}
						j++;
					}
				}
			}
		}// end of tree route
	}

	@Override
	public void calculate(RankingIndividual ind) {

		double numberOfComponents = ind.getNumberOfComponents();
		this.result = 0.0;

		// Total weighted external relations
		for(int i=0; i<numberOfComponents; i++)
			for(int j=i; j<numberOfComponents; j++)
				this.result += this.componentSumWeightedExternalConnections[i][j];
		// TODO
		/*double maximum=maximum();
		result = 1.0 - (result/maximum);
		*/
	}

	@Override
	public void setOnIndividual(RankingIndividual ind) {
		// Set result in the individual
		ind.setERP(this.result);
		// Verbose
		ind.setExternalConnections(this.componentNumberExternalConnections);
	}

	@Override
	public double getFromIndividual(RankingIndividual ind) {
		return ind.getERP();
	}

	@Override
	public void configure(Configuration settings) {
		// Configure weights for UML relations
		this.umlWeights = new double[4];
		this.umlWeights[0] = settings.getDouble("uml-relation-weights.assoc-weight", 1.0);
		this.umlWeights[1] = settings.getDouble("uml-relation-weights.depen-weight", 1.0);
		this.umlWeights[2] = settings.getDouble("uml-relation-weights.compos-weight", 1.0);
		this.umlWeights[3] = settings.getDouble("uml-relation-weights.gener-weight", 1.0);
	}
	
	
	/////////////////
	/*private double maximum(){
		List<Object> values, values2;
		List<ColumnAbstraction> cols = dataset.getColumns();
		int totalRel = 0;
		int size = cols.size(), size2;
		MultivalueColumn col;
		int type, nav_ij, nav_ji;
		for(int i=0; i<size; i++){
			col = (MultivalueColumn) cols.get(i);
			size2 = col.getSize();
			for(int j=i; j<size2; j++){
				values = col.getMultiElement(j);
				values2 = ((MultivalueColumn)this.dataset.getColumn(j)).getMultiElement(i);
				if(values.size()>1){//Not an invalid value

					for(int k=0; k<values.size(); k+=2){
						// Get the type of relationship
						type = Integer.parseInt((String)values.get(k));
						nav_ij = Integer.parseInt((String)values.get(k+1));
						nav_ji = Integer.parseInt((String)values2.get(k+1));

						// Not a candidate interface, because its a bidirectional relation
						if(nav_ij==nav_ji){
							// Add the correspondent weight to the total sum
							switch(type){
							case 1: totalRel += this.umlWeights[0]; break; // Association
							case 3: totalRel += this.umlWeights[1]; break; // Aggregation
							case 4: totalRel += this.umlWeights[2]; break; // Composition	
							case 5: totalRel += this.umlWeights[3]; break; // Generalization
							}
						}
					}
				}
			}
		}
		// Invert the total sum of weighted relationships
		return totalRel;
	}
	*/
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------ Private class
	/////////////////////////////////////////////////////////////////

	/**
	 * Compare two individuals using
	 * their number of external relations values
	 * */
	private class ERPComparator extends MetricComparator{

		@Override
		public int compare(IIndividual ind1, IIndividual ind2) {
			double value1, value2;  

			// Get cohesion values
			try {
				value1 = ((RankingIndividual)ind1).getERP();
				value2 = ((RankingIndividual)ind2).getERP();
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
