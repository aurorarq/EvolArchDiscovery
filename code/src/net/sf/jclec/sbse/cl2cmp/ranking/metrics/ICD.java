package net.sf.jclec.sbse.cl2cmp.ranking.metrics;

import java.util.ArrayList;

import es.uco.kdis.datapro.dataset.Dataset;
import es.uco.kdis.datapro.dataset.Column.MultivalueColumn;
import net.sf.jclec.IIndividual;
import net.sf.jclec.sbse.cl2cmp.ranking.RankingIndividual;
import net.sf.jclec.syntaxtree.SyntaxTree;

/**
 * Intra-Modular Coupling Density (ICD) Metric
 * inspired by "Optimization Model of COTS Selection Based 
 * on Cohesion and Coupling for Modular Software Systems 
 * under Multiple Applications Environment" (2012)
 * 
 * <p>History:
 * <ul>
 * 	<li>1.0: Creation (September 2013)
 * </ul>
 * 
 * @author Aurora Ramirez Quesada
 * @author Jose Raul Romero
 * @author Sebastian Ventura
 * @version 1.0
 * 
 * */
public class ICD extends CmpMetric {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Serial ID */
	private static final long serialVersionUID = -276132426718089381L;

	/** Internal relations for each component */
	private double c_in [];
	
	/** External relations for each component */
	private double c_out [];

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 * */
	public ICD(){
		super();
		this.maximize = true;
		this.comparator = new ICDComparator();
		this.comparator.setInverse(true);
	}

	/**
	 * Parameterized constructor
	 * @param dataset The dataset
	 * */
	public ICD(Dataset dataset){
		super(dataset,true);
		this.comparator = new ICDComparator();
		this.comparator.setInverse(true);
	}

	/////////////////////////////////////////////////////////////////
	//---------------------------------------------- Override methods
	/////////////////////////////////////////////////////////////////

	@Override
	public void prepare(RankingIndividual ind) {
		// Get genotype
		SyntaxTree genotype = ind.getGenotype();

		int numberOfComponents = ind.getNumberOfComponents();

		int j, otherClassIndex, actualCmp=-1;
		boolean isClass = false, isConnector = false;
		MultivalueColumn column, otherColumn;
		String symbol;

		// Initialize
		this.c_in = new double [numberOfComponents];
		this.c_out = new double [numberOfComponents];

		for(int i=0; i<numberOfComponents; i++){
			this.c_in[i] = this.c_out[i] = 0;
		}

		// Compute c_in for each component in the individual
		for(int i=1; !isConnector; i++){

			symbol = genotype.getNode(i).getSymbol();

			// Non terminal node
			if(genotype.getNode(i).arity()!=0){
				// The symbol classes indicates the beginning of a 
				// group of classes in a component
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
			else if(isClass){

				// Get the dataset information about the class
				column = (MultivalueColumn) this.dataset.getColumnByName(symbol);

				// Check the relations with the rest of class in the component
				j=i+1;	
				while(genotype.getNode(j).arity()==0){
					otherColumn = (MultivalueColumn)this.dataset.getColumnByName(genotype.getNode(j).getSymbol());
					otherClassIndex = this.dataset.getIndexOfColumn(otherColumn);

					ArrayList<Object> relations = (ArrayList<Object>) column.getMultiElement(otherClassIndex);

					// Not an invalid value
					if(relations.size()>1){
						this.c_in[actualCmp] += ((double)relations.size()/2);
					}
					j++;
				}
			}
		}// end of tree route
		
		// Compute c_out
		for(int i=0; i<numberOfComponents; i++){
			this.c_out[i] = ind.getNumberOfProvided(i) + ind.getNumberOfRequired(i);
		}
	}

	@Override
	public void calculate(RankingIndividual ind) {
		//this.result = 0.0;
		double sum_icd = 0.0;
		// 1) Sum
		/*for(int i=0; i<this.c_in.length; i++){
			if(this.c_in[i]!=0 || this.c_out[i]!=0)
				sum_icd += this.c_in[i]/(this.c_in[i]+this.c_out[i]);
		}*/
		
		// 1) Sum
		this.result = sum_icd;
		
		// 2) Mean
		//int size = this.c_in.length;
		//this.result = sum_icd/size;
		
		// 3) Mean value with n_max
		//int maxSize = 8;
		//this.result = sum_icd/maxSize;
		
		// 4) Maximize min ICD
		/*double min = Double.MAX_VALUE;
		double div;
		boolean finish = false;
		for(int i=0; !finish && i<this.c_in.length; i++){
			if(this.c_in[i]!=0 || this.c_out[i]!=0){
				div = this.c_in[i]/(this.c_in[i]+this.c_out[i]);
				if(div<min)
					min=div;
			}
			else{
				min = 0.0;
				finish=true;
			}
		}
		this.result = min;*/
		
		// 5) Weighted ICD
		double icd_i, classesRatio;
		double nComponents = c_in.length;
		double nClasses = 0.0;
		this.result = 0.0;
		// total number of classes
		for(int i=0; i<nComponents; i++){
			nClasses += ind.getNumberOfClasses(i);
		}
		
		for(int i=0; i<this.c_in.length; i++){
			if(this.c_in[i]!=0 || this.c_out[i]!=0){
				icd_i = this.c_in[i]/(this.c_in[i]+this.c_out[i]);
				classesRatio = (nClasses - ind.getNumberOfClasses(i))/nClasses;
				this.result += icd_i*classesRatio;
			}
		}
		this.result = this.result/nComponents;
	}

	@Override
	public void setOnIndividual(RankingIndividual ind) {
		ind.setICD(this.result);
	}

	@Override
	public double getFromIndividual(RankingIndividual ind) {
		return ind.getICD();
	}

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------ Private class
	/////////////////////////////////////////////////////////////////
	
	// Returns a negative integer, zero, or a positive integer as the first argument 
	// is less than, equal to, or greater than the second
	
	/**
	 * Compare two individuals using
	 * their ICD values
	 * */
	protected class ICDComparator extends MetricComparator{

		@Override
		public int compare(IIndividual ind1, IIndividual ind2) {
			double value1, value2;  

			// Get cohesion values
			try {
				value1 = ((RankingIndividual)ind1).getICD();
				value2 = ((RankingIndividual)ind2).getICD();
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